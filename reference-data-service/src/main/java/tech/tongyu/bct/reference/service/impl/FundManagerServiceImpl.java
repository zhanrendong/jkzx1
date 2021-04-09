package tech.tongyu.bct.reference.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.client.dto.*;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.client.service.FundManagerService;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.reference.dao.dbo.FundEventRecord;
import tech.tongyu.bct.reference.dao.repl.intel.FundEventRecordRepo;
import tech.tongyu.bct.reference.service.BankAccountService;
import tech.tongyu.bct.reference.service.PartyService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FundManagerServiceImpl implements FundManagerService {

    private PartyService partyService;

    private AccountService accountService;

    private BankAccountService bankAccountService;

    private FundEventRecordRepo fundEventRecordRepo;

    @Autowired
    public FundManagerServiceImpl(PartyService partyService,
                                  AccountService accountService,
                                  BankAccountService bankAccountService,
                                  FundEventRecordRepo fundEventRecordRepo) {
        this.partyService = partyService;
        this.accountService = accountService;
        this.bankAccountService = bankAccountService;
        this.fundEventRecordRepo = fundEventRecordRepo;
    }

    @Override
    @Transactional
    public FundEventRecordDTO createFundTransRecord(FundEventRecordDTO fundEventRecordDto) {
        String clientId = fundEventRecordDto.getClientId();
        if (StringUtils.isBlank(clientId)){
            throw new CustomException("请输入交易对手clientId");
        }
        String bankAccount = fundEventRecordDto.getBankAccount();
        if (StringUtils.isBlank(bankAccount)){
            throw new CustomException("请输入交易对手银行账号bankAccount");
        }
        if (!partyService.isPartyExistsByLegalName(clientId)){
            throw new CustomException(String.format("找不到交易对手:[%s]的数据记录", clientId));
        }
        if (!bankAccountService.isBankAccountExistsByLegalNameAndAccount(clientId, bankAccount)){
            throw new CustomException(String.format("找不到交易对手:[%s],银行账户:[%s]的数据记录", clientId, bankAccount));
        }

        Long serialNumber = fundEventRecordRepo.countByClientIdAndPaymentDate(clientId, fundEventRecordDto.getPaymentDate());
        fundEventRecordDto.setSerialNumber(serialNumber.intValue());
        fundEventRecordDto.setProcessStatus(ProcessStatusEnum.UN_PROCESSED);
        FundEventRecord fundEventRecord = fundEventRecordRepo.save(transToDBO(fundEventRecordDto));

        //TODO http://jira.tongyu.tech:8080/browse/OTMS-1943 台帐部分如何与财务相关联，讨论
        AccountDTO account = accountService.getAccountByLegalName(clientId);
        String paymentAmount = fundEventRecord.getPaymentAmount().toPlainString();
        PaymentDirectionEnum paymentDirection = fundEventRecord.getPaymentDirection();
        AccountDirectionEnum accountDirection = fundEventRecord.getAccountDirection();
        if (AccountDirectionEnum.PARTY.equals(accountDirection)){
            switch (paymentDirection){
                case IN:
                    accountService.deposit(account.getAccountId(), paymentAmount, "财务入金划转自动操作台帐");
                    break;
                case OUT:
                    accountService.withdraw(account.getAccountId(), paymentAmount, "财务出金划转自动操作台帐");
                    break;
                default:
                    throw new CustomException("不支持当前类型操作");
            }
            fundEventRecord.setProcessStatus(ProcessStatusEnum.PROCESSED);
        }
        if (AccountDirectionEnum.COUNTER_PARTY.equals(accountDirection)){
            AccountOpRecordDTO accountOpRecordDto = new AccountOpRecordDTO();
            accountOpRecordDto.initDefaultValue();

            accountOpRecordDto.setAccountId(account.getAccountId());
            accountOpRecordDto.setLegalName(clientId);
            switch (paymentDirection){
                case IN:
                    accountOpRecordDto.setEvent(AccountEvent.DEPOSIT.toString());
                    accountOpRecordDto.setCounterPartyFundChange(new BigDecimal(paymentAmount));
                    break;
                case OUT:
                    accountOpRecordDto.setEvent(AccountEvent.WITHDRAW.toString());
                    accountOpRecordDto.setCounterPartyFundChange(new BigDecimal(paymentAmount).negate());
                    break;
                default:
                    throw new CustomException("不支持当前类型操作");
            }
            accountService.saveAccountOpRecord(accountOpRecordDto);
            fundEventRecord.setProcessStatus(ProcessStatusEnum.PROCESSED);
        }

        return transToDTO(fundEventRecordRepo.save(fundEventRecord));
    }

    @Override
    public FundEventRecordDTO updateFundTransRecord(FundEventRecordDTO fundEventRecordDto) {
        String uuid = fundEventRecordDto.getUuid();
        if (StringUtils.isBlank(uuid)){
            throw new CustomException("请输入待修改记录唯一标识uuid");
        }
        Optional<FundEventRecord> recordOptional = fundEventRecordRepo.findById(UUID.fromString(uuid));
        if (!recordOptional.isPresent()){
            throw new CustomException(String.format("查询唯一标识:[%s]的资金划转记录不存在", uuid));
        }
        ProcessStatusEnum processStatus = recordOptional.get().getProcessStatus();
        if (ProcessStatusEnum.PROCESSED.equals(processStatus)){
            throw new CustomException(String.format("查询唯一标识:[%s]的记录状态已经被处理,不能进行修改操作", uuid));
        }
        FundEventRecord fundEventRecord = fundEventRecordRepo.save(transToDBO(fundEventRecordDto));

        return transToDTO(fundEventRecord);
    }

    @Override
    public void deleteFundTransRecord(String uuid) {
        if (StringUtils.isBlank(uuid)){
            throw new CustomException("请输入待删除记录唯一标识uuid");
        }
        if (!fundEventRecordRepo.existsById(UUID.fromString(uuid))){
            throw new CustomException(String.format("查询唯一标识:[%s]的资金划转记录不存在", uuid));
        }
        fundEventRecordRepo.deleteById(UUID.fromString(uuid));
    }

    @Override
    public List<FundEventRecordDTO> findAllFundTransRecord() {
        return fundEventRecordRepo.findAll()
                .stream()
                .map(this::transToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FundEventRecordDTO> findByClientId(String clientId) {
        if (StringUtils.isBlank(clientId)){
            throw new CustomException("请输入交易对手clientId");
        }
        return fundEventRecordRepo.findAllByClientIdOrderByUpdatedAtDesc(clientId)
                .stream()
                .map(this::transToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FundEventRecordDTO> search(FundEventRecordDTO recordDto, LocalDate startDate, LocalDate endDate) {
        FundEventRecord record = new FundEventRecord();
        BeanUtils.copyProperties(recordDto, record);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        List<FundEventRecord> eventRecords = fundEventRecordRepo.findAll(Example.of(record, exampleMatcher));
        // 支付日期范围不为空时,搜索范围内记录
        if (Objects.nonNull(startDate) && Objects.nonNull(endDate)){
            eventRecords = eventRecords.stream()
                    .filter(r -> isDateBetween(r.getPaymentDate(), startDate, endDate))
                    .collect(Collectors.toList());
        }
        return eventRecords.stream()
                .map(this::transToDTO)
                .collect(Collectors.toList());
    }

    private Boolean isDateBetween(LocalDate date, LocalDate startDate, LocalDate endDate){
        return (date.isAfter(startDate) && date.isBefore(endDate))
                || date.equals(startDate) || date.equals(endDate);
    }

    private FundEventRecordDTO transToDTO(FundEventRecord fundEventRecord){
        UUID uuid = fundEventRecord.getUuid();
        FundEventRecordDTO fundEventRecordDto = new FundEventRecordDTO();
        BeanUtils.copyProperties(fundEventRecord, fundEventRecordDto);
        fundEventRecordDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());

        return fundEventRecordDto;
    }
    
    private FundEventRecord transToDBO(FundEventRecordDTO fundEventRecordDto){
        String uuid = fundEventRecordDto.getUuid();
        FundEventRecord fundEventRecord = new FundEventRecord();
        BeanUtils.copyProperties(fundEventRecordDto, fundEventRecord);
        fundEventRecord.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));

        return fundEventRecord;
    }

}
