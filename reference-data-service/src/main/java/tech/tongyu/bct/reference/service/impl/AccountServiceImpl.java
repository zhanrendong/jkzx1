package tech.tongyu.bct.reference.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tech.tongyu.bct.client.dto.AccountDTO;
import tech.tongyu.bct.client.dto.AccountEvent;
import tech.tongyu.bct.client.dto.AccountOpRecordDTO;
import tech.tongyu.bct.client.dto.AccountOpRecordStatus;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.TimeUtils;
import tech.tongyu.bct.reference.dao.dbo.Account;
import tech.tongyu.bct.reference.dao.dbo.AccountOpRecord;
import tech.tongyu.bct.reference.dao.repl.intel.AccountOpRepo;
import tech.tongyu.bct.reference.dao.repl.intel.AccountRepo;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.PartyService;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountOpRepo accountOpRepo;
    private AccountRepo accountRepo;

    @Autowired
    private PartyService partyService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    public AccountServiceImpl(AccountOpRepo accountOpRepo,
                              AccountRepo accountRepo) {
        this.accountOpRepo = accountOpRepo;
        this.accountRepo = accountRepo;

    }

    @Override
    public List<AccountDTO> findAccountsByLegalNames(List<String> legalNames) {
        if (CollectionUtils.isEmpty(legalNames)){
            throw new CustomException("请输入交易对手legalName");
        }
        return accountRepo.findAllByLegalNameIn(legalNames)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 通过accountId查找指定的帐号信息
     *
     * @param accountIds
     * @return
     */
    @Override
    public List<AccountDTO> findAccountsByAccountIds(List<String> accountIds) {
        if (CollectionUtils.isEmpty(accountIds)){
            return new ArrayList<>();
        }

        return accountRepo.findAllByAccountIdIn(accountIds)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDTO getAccountByLegalName(String legalName) {
        if(StringUtils.isBlank(legalName)){
            throw new CustomException("请输入交易对手legalName");
        }
        return accountRepo.findByLegalName(legalName)
                .stream()
                .findFirst()
                .map(this::convertToDTO)
                .orElseThrow(() -> new CustomException(String.format("找不到交易对手:[%s]的台帐记录", legalName)));
    }

    @Override
    @Transactional
    public AccountDTO saveAccount(String legalName) {
        Account account = new Account();
        account.initDefaltValue();
        account.setLegalName(legalName);
        account.setAccountId(generateAccountId(legalName));
        return convertToDTO(accountRepo.save(account));
    }

    @Override
    @Transactional
    public AccountOpRecordDTO saveAccountOpRecord(AccountOpRecordDTO recordDto) {
        String accountId = recordDto.getAccountId();
        return AccountLocker.getInstance().lockAndExecute(() -> {
            AccountOpRecord record = new AccountOpRecord();
            BeanUtils.copyProperties(recordDto, record);
            record.setEvent(AccountEvent.valueOf(recordDto.getEvent()));
            record.setAccountOpId(generateAccountOpId(accountId));
            record.setStatus(AccountOpRecordStatus.NORMAL);
            Account account = accountRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new CustomException(String.format("[%s]账户信息不存在", accountId)));
            if(!account.getNormalStatus()){
                throw new CustomException(String.format("[%s]账户信息异常,不能进行资金操作", accountId));
            }
            BigDecimal creditBalanceChange = recordDto.getCreditBalanceChange();
            record.setCreditUsedChange(creditBalanceChange.negate());
            updateAccountByOpRecord(account, record);

            AccountOpRecord save = accountOpRepo.save(record);
            return convertToDTO(save);
        },accountId);
    }

    @Override
    @Transactional
    public AccountOpRecordDTO deposit(String accountId, String amount, String information) {
        //accountLocker
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Account account = accountRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new CustomException(String.format("账户信息不存在:%s.", accountId)));
            AccountOpRecord record = account.deposit(new BigDecimal(amount), information);
            record.setAccountOpId(generateAccountOpId(accountId));
            record.setLegalName(account.getLegalName());
            accountOpRepo.save(record);
            accountRepo.save(account);
            return convertToDTO(record);
        }, accountId);
    }

    @Override
    @Transactional
    public AccountOpRecordDTO withdraw(String accountId, String amount, String information) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Account account = accountRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new CustomException(String.format("账户信息不存在:%s.", accountId)));
            AccountOpRecord record = account.tryWithdraw(new BigDecimal(amount), information);
            record.setAccountOpId(generateAccountOpId(accountId));
            record.setLegalName(account.getLegalName());
            accountOpRepo.save(record);
            accountRepo.save(account);
            return convertToDTO(record);
        }, accountId);
    }

    @Override
    @Transactional
    public AccountOpRecordDTO updateCredit(String accountId, BigDecimal credit, BigDecimal counterPartyCredit, String information) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Account account = accountRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new CustomException(String.format("账户信息不存在,%s", accountId)));
            BigDecimal creditUsed = account.getCreditUsed();
            if (credit.compareTo(creditUsed) < 0){
                throw new CustomException(String.format("账户[%s]调整授信金额[%s]不足已用授信[%s]",
                        accountId, credit.toPlainString() ,creditUsed.toPlainString()));
            }
            BigDecimal counterPartyCreditUsed = account.getCounterPartyCreditUsed();
            if (counterPartyCredit.compareTo(counterPartyCreditUsed) < 0){
                throw new CustomException(String.format("账户[%s]调整我方授信金额[%s]不足我方已用授信[%s]",
                        accountId, counterPartyCredit.toPlainString(), counterPartyCreditUsed.toPlainString()));
            }
            AccountOpRecord record = account.updateCredit(credit, counterPartyCredit, information);
            record.setAccountOpId(generateAccountOpId(accountId));
            record.setLegalName(account.getLegalName());
            accountOpRepo.save(record);
            accountRepo.save(account);
            return convertToDTO(record);
        },accountId);
    }

    @Override
    @Transactional
    public AccountOpRecordDTO changePremium(String accountId, String tradeId, BigDecimal premium, String information) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Account account = accountRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new CustomException(String.format("账户信息不存在:%s", accountId)));
            entityManager.detach(account);
            AccountOpRecord record = account.premiumChange(premium, information);
            record.setAccountOpId(generateAccountOpId(accountId));
            record.setEvent(AccountEvent.CHANGE_PREMIUM);
            record.setLegalName(account.getLegalName());
            record.setTradeId(tradeId);
            return convertToDTO(record);
        }, accountId);
    }

    @Override
    public AccountOpRecordDTO settleTrade(String accountId, String tradeId, BigDecimal amount, BigDecimal premium,
                                          AccountEvent event, String information) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Account account = accountRepo.findByAccountId(accountId)
                    .orElseThrow(() -> new CustomException(String.format("账户信息不存在:%s", accountId)));
            entityManager.detach(account);
            AccountOpRecord record = account.settleTrade(amount, premium, event, information);
            record.setAccountOpId(generateAccountOpId(accountId));
            record.setLegalName(account.getLegalName());
            record.setTradeId(tradeId);
            return convertToDTO(record);
        }, accountId);
    }

    @Override
    public AccountOpRecordDTO tradeCashFlow(String accountId, String tradeId, String cashFlow, String marginFlow) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Optional<Account> accountOptional = accountRepo.findByAccountId(accountId);
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                AccountOpRecord record = account.tradeCashFlowEvent(tradeId, new BigDecimal(cashFlow), new BigDecimal(marginFlow));
                if (account.getMargin().signum() < 0) {
                    throw new CustomException("操作失败,释放保证金金额大于冻结保证金金额！");
                }
                record.setAccountOpId(generateAccountOpId(accountId));
                record.setLegalName(account.getLegalName());
                accountOpRepo.save(record);
                accountRepo.save(account);
                return convertToDTO(record);
            }
            return convertToDTO(AccountOpRecord.InvalidRecord(
                    String.format("账户信息不存在: %s.", accountId), accountId));
        }, accountId);
    }

    @Override
    public AccountOpRecordDTO changeCredit(String accountId, String amount, String information) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Optional<Account> accountOptional = accountRepo.findByAccountId(accountId);
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                BigDecimal newCredit = account.getCredit().add(new BigDecimal(amount));
                AccountOpRecord record = account.changeCredit(newCredit, information);
                record.setAccountOpId(generateAccountOpId(accountId));
                record.setLegalName(account.getLegalName());
                accountOpRepo.save(record);
                accountRepo.save(account);
                return convertToDTO(record);
            }
            return convertToDTO(AccountOpRecord.InvalidRecord(
                    String.format("账户信息不存在: %s.", accountId), accountId));
        }, accountId);
    }

    @Override
    public AccountOpRecordDTO startTrade(String accountId, String tradeId, String gainOrCost, String marginToBePaid) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Optional<Account> accountOptional = accountRepo.findByAccountId(accountId);
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                AccountOpRecord record = account.tryInitializeTrade(
                        tradeId, new BigDecimal(gainOrCost), new BigDecimal(marginToBePaid));
                record.setAccountOpId(generateAccountOpId(accountId));
                record.setLegalName(account.getLegalName());
                accountOpRepo.save(record);
                accountRepo.save(account);
                return convertToDTO(record);
            }
            return convertToDTO(AccountOpRecord.InvalidRecord(
                    String.format("账户信息不存在: %s.", accountId), accountId));
        }, accountId);
    }

    @Override
    public AccountOpRecordDTO terminateTrade(String accountId, String tradeId, String startGainOrCost,
                                             String endGainOrCost, String marginRelease) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Optional<Account> accountOptional = accountRepo.findByAccountId(accountId);
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                AccountOpRecord record = account.terminateTrade(tradeId, new BigDecimal(startGainOrCost),
                        new BigDecimal(endGainOrCost), new BigDecimal(marginRelease));
                record.setAccountOpId(generateAccountOpId(accountId));
                record.setLegalName(account.getLegalName());
                accountOpRepo.save(record);
                accountRepo.save(account);
                return convertToDTO(record);
            }
            return convertToDTO(AccountOpRecord.InvalidRecord(
                    String.format("账户信息不存在 %s.", accountId), accountId));
        }, accountId);
    }

    @Override
    public AccountOpRecordDTO changeMargin(String accountId, String minimalMargin, String initialMargin, String callMargin) {
        return AccountLocker.getInstance().lockAndExecute(() -> {
            Optional<Account> accountOptional = accountRepo.findByAccountId(accountId);
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();
                AccountOpRecord record = account.marginChange(
                        new BigDecimal(minimalMargin), new BigDecimal(initialMargin), new BigDecimal(callMargin));
                record.setAccountOpId(generateAccountOpId(accountId));
                record.setLegalName(account.getLegalName());
                accountOpRepo.save(record);
                accountRepo.save(account);
                return convertToDTO(record);
            }
            return convertToDTO(AccountOpRecord.InvalidRecord(
                    String.format("账户信息不存在 %s.", accountId), accountId));
        }, accountId);
    }

    @Override
    public List<AccountDTO> listAccounts() {
        List<Account> accounts = accountRepo.findAll(new Sort(Sort.Direction.DESC, "updatedAt"));
        return accounts.stream()
                .map(account -> {
                    PartyDTO party = partyService.getByLegalName(account.getLegalName());
                    AccountDTO accountDto = convertToDTO(account);
                    accountDto.setSalesName(party.getSalesName());
                    accountDto.setMasterAgreementId(party.getMasterAgreementId());
                    return accountDto;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteByAccountId(String accountId) {
        if (StringUtils.isBlank(accountId)){
            throw new CustomException("请输入待删除账户编号accountId");
        }
        Account account = accountRepo.findByAccountId(accountId)
                .orElseThrow(() -> new CustomException(String.format("账户编号[%s],查询账户信息不存在", accountId)));
        accountRepo.deleteByAccountId(accountId);
        partyService.deleteByLegalName(account.getLegalName());
    }

    @Override
    @Transactional
    public void deleteByLegalName(String legalName) {
        if (StringUtils.isBlank(legalName)){
            throw new CustomException("请输入待删除账户交易对手legalName");
        }
        accountRepo.deleteByLegalName(legalName);
    }

    @Override
    public List<AccountDTO> searchByParty(PartyDTO party) {
        return partyService.listParty(party)
                .stream()
                .map(partyDto -> {
                    AccountDTO account = getAccountByLegalName(partyDto.getLegalName());
                    account.setMasterAgreementId(partyDto.getMasterAgreementId());
                    account.setSalesName(partyDto.getSalesName());
                    return account;
                }).collect(Collectors.toList());
    }

    @Override
    public List<AccountDTO> accountSearch(AccountDTO accountDto) {
        Account account = new Account();
        BeanUtils.copyProperties(accountDto, account);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return accountRepo.findAll(Example.of(account, exampleMatcher))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountOpRecordDTO> listAccountOpRecords(List<String> accountIds) {
        List<AccountOpRecord> opRecords = accountOpRepo.findByAccountIdInOrderByCreatedAtDesc(accountIds);
        return opRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountOpRecordDTO> accountOpRecordSearch(AccountOpRecordDTO recordDto) {
        AccountOpRecord record = new AccountOpRecord();
        BeanUtils.copyProperties(recordDto, record);
        String event = recordDto.getEvent();

        record.setEvent(StringUtils.isBlank(event) ? null : AccountEvent.valueOf(event));
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return accountOpRepo.findAll(Example.of(record, exampleMatcher))
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 通过交易对手信息生成账户标识
     * @param legalName
     * @return
     */
    private String generateAccountId(String legalName){
        synchronized (AccountServiceImpl.class) {
            Long count = accountRepo.countByLegalName(legalName);
            return legalName + count;
        }
    }

    /**
     * 通过账户标识生成账户操作编号
     * @param accountId
     * @return
     */
    private String generateAccountOpId(String accountId){
        return AccountLocker.getInstance().lockAndExecute(() ->{
            Long count = accountOpRepo.countByAccountId(accountId);
            return accountId + count;
        }, accountId);
    }

    /**
     * 根据账户操作更新账户信息
     * @param account
     * @param record
     */
    private void updateAccountByOpRecord(Account account, AccountOpRecord record){
        BigDecimal cash = account.getCash().add(record.getCashChange());
        if (cash.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出可用资金变化额度,可用资金:%s", account.getCash().toPlainString()));
        }
        account.setCash(cash);
        record.setCash(cash);
        BigDecimal debt = account.getDebt().add(record.getDebtChange());
        if (debt.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出负债变化额度,负债:%s", account.getDebt().toPlainString()));
        }
        account.setDebt(debt);
        record.setDebt(debt);
        BigDecimal creditUsed = account.getCreditUsed().add(record.getCreditUsedChange());
        if (creditUsed.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出已用授信变化额度,已用授信:%s", account.getCreditUsed().toPlainString()));
        }
        BigDecimal credit = account.getCredit();
        if (creditUsed.compareTo(credit) > 0){
            throw new CustomException(String.format("已用授信超出授信总额[%s]", credit.toPlainString()));
        }
        account.setCreditUsed(creditUsed);
        record.setCreditUsed(creditUsed);
        BigDecimal margin = account.getMargin().add(record.getMarginChange());
        if (margin.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出保证金变化额度,保证金[%s]", margin.toPlainString()));
        }
        account.setMargin(margin);
        record.setMargin(margin);
        BigDecimal premium = account.getPremium().add(record.getPremiumChange());
        account.setPremium(premium);
        record.setPremium(premium);
        BigDecimal realizedPnl = cash.add(margin).add(premium).subtract(debt).subtract(creditUsed)
                .subtract(account.getNetDeposit());
        BigDecimal realizedPnlChange = realizedPnl.subtract(account.getRealizedPnL());
        record.setRealizedPnLChange(realizedPnlChange);
        account.setRealizedPnL(realizedPnl);
        record.setRealizedPnL(realizedPnl);

        // 我方资金变动
        BigDecimal counterPartyFund = account.getCounterPartyFund().add(record.getCounterPartyFundChange());
        if (counterPartyFund.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出我方可用资金变化额度,我方可用资金[%s]", counterPartyFund.toPlainString()));
        }
        account.setCounterPartyFund(counterPartyFund);
        record.setCounterPartyFund(counterPartyFund);
        BigDecimal counterPartyCredit = account.getCounterPartyCredit().add(record.getCounterPartyCreditChange());
        if (counterPartyCredit.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出我方授信变化额度,我方授信额度[%s]", counterPartyCredit.toPlainString()));
        }
        account.setCounterPartyCredit(counterPartyCredit);
        record.setCounterPartyCredit(counterPartyCredit);
        BigDecimal counterPartyCreditBalance = account.getCounterPartyCreditBalance().add(record.getCounterPartyCreditBalanceChange());
        if (counterPartyCreditBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出我方授信余额变化额度,我方授信余额[%s]", counterPartyCreditBalance.toPlainString()));
        }
        account.setCounterPartyCreditBalance(counterPartyCreditBalance);
        BigDecimal counterPartyCreditUsed = account.getCounterPartyCreditUsed();
        if (counterPartyCreditUsed.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出我方已用授信变化额度,我方已用授信[%s]", counterPartyCreditUsed.toPlainString()));
        }
        if (counterPartyCreditUsed.compareTo(counterPartyCredit) > 0){
            throw new CustomException(String.format("变更后,我方已用授信[%s]不足我方授信总额[%s]",
                    counterPartyCreditBalance.toPlainString(), counterPartyCredit.toPlainString()));
        }
        record.setCounterPartyCreditBalance(counterPartyCreditBalance);
        BigDecimal counterPartyMargin = account.getCounterPartyMargin().add(record.getCounterPartyMarginChange());
        if (counterPartyMargin.compareTo(BigDecimal.ZERO) < 0){
            throw new CustomException(String.format("超出我方保证金变化额度,我方保证金额度[%s]", counterPartyMargin.toPlainString()));
        }
        account.setCounterPartyMargin(counterPartyMargin);
        record.setCounterPartyMargin(counterPartyMargin);

        accountRepo.save(account);
    }

    /**
     * 账户属性copy到DTO
     * @param account
     * @return
     */
    private AccountDTO convertToDTO(Account account){
        AccountDTO accountDto = new AccountDTO();
        BeanUtils.copyProperties(account, accountDto);
        accountDto.setCreatedAt(Objects.isNull(account.getCreatedAt()) ? null :
                TimeUtils.instantTransToLocalDateTime(account.getCreatedAt()));
        accountDto.setUpdatedAt(Objects.isNull(account.getUpdatedAt()) ? null :
                TimeUtils.instantTransToLocalDateTime(account.getUpdatedAt()));
        accountDto.setCreditBalance(account.getCredit().subtract(account.getCreditUsed()));
        return accountDto;
    }

    /**
     * 账户操作记录属性copy到DTO
     * @param record
     * @return
     */
    private AccountOpRecordDTO convertToDTO(AccountOpRecord record) {
        AccountOpRecordDTO recordDto = new AccountOpRecordDTO();
        recordDto.initDefaultValue();
        BeanUtils.copyProperties(record, recordDto);
        recordDto.setEvent(record.getEvent().toString());
        recordDto.setStatus(record.getStatus().toString());
        recordDto.setCreatedAt(Objects.isNull(record.getCreatedAt()) ? null :
                TimeUtils.instantTransToLocalDateTime(record.getCreatedAt()));
        recordDto.setUpdatedAt(Objects.isNull(record.getUpdatedAt()) ? null :
                TimeUtils.instantTransToLocalDateTime(record.getUpdatedAt()));
        recordDto.setCreditBalanceChange(record.getCreditChange().subtract(record.getCreditUsedChange()));
        return recordDto;
    }

}
