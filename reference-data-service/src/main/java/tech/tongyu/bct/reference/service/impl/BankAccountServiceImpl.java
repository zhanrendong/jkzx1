package tech.tongyu.bct.reference.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.reference.dao.dbo.BankAccount;
import tech.tongyu.bct.reference.dao.repl.intel.BankAccountRepo;
import tech.tongyu.bct.reference.dao.repl.intel.PartyRepo;
import tech.tongyu.bct.reference.dto.BankAccountDTO;
import tech.tongyu.bct.reference.service.BankAccountService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankAccountServiceImpl implements BankAccountService {

    private PartyRepo partyRepo;

    private BankAccountRepo bankAccountRepo;

    @Autowired
    public BankAccountServiceImpl(PartyRepo partyRepo,
                                  BankAccountRepo bankAccountRepo) {
        this.partyRepo = partyRepo;
        this.bankAccountRepo = bankAccountRepo;
    }

    @Override
    public BankAccountDTO createBankAccount(BankAccountDTO bankAccountDto) {
        String legalName = bankAccountDto.getLegalName();
        if (StringUtils.isBlank(legalName)){
            throw new CustomException("请输入交易对手legalName");
        }
        String bankAccount = bankAccountDto.getBankAccount();
        if (StringUtils.isBlank(bankAccountDto.getBankAccount())){
            throw new CustomException("请输入银行账户bankAccount");
        }
        if (isBankAccountExistsByLegalNameAndAccount(legalName, bankAccount)){
            throw new CustomException(String.format("已经存在相同交易对手[%s]下相同银行账户[%s]", legalName, bankAccount));
        }
        if (!partyRepo.existsByLegalName(legalName)){
            throw new CustomException(String.format("找不到交易对手为[%s]的数据记录", legalName));
        }
        return transToBankAccountDto(bankAccountRepo.save(transToBankAccount(bankAccountDto)));
    }

    @Override
    public BankAccountDTO updateBankAccount(BankAccountDTO bankAccountDto) {
        String uuid = bankAccountDto.getUuid();
        if (StringUtils.isBlank(uuid)){
            throw new CustomException("请输入交易对手账户唯一标识uuid");
        }
        String legalName = bankAccountDto.getLegalName();
        if (StringUtils.isBlank(legalName)){
            throw new CustomException("请输入交易对手legalName");
        }
        String bankAccount = bankAccountDto.getBankAccount();
        if (StringUtils.isBlank(bankAccountDto.getBankAccount())){
            throw new CustomException("请输入银行账户bankAccount");
        }
        if (!bankAccountRepo.existsById(UUID.fromString(uuid))){
            throw new CustomException(String.format("找不到UUID为[%s]的数据记录", uuid));
        }

        if (isBankAccountExistsByLegalNameAndAccountAndUuid(legalName, bankAccount, uuid)){
            throw new CustomException(String.format("已经存在相同交易对手[%s]下相同银行账户[%s]", legalName, bankAccount));
        }
        if (!partyRepo.existsByLegalName(legalName)){
            throw new CustomException(String.format("找不到交易对手为[%s]的数据记录", legalName));
        }
        return transToBankAccountDto(bankAccountRepo.save(transToBankAccount(bankAccountDto)));
    }

    @Override
    public void deleteBankAccount(String uuid) {
        UUID uuidDel = UUID.fromString(uuid);
        if (!bankAccountRepo.existsById(uuidDel)){
            throw new CustomException(String.format("找不到UUID为[%s]的数据记录", uuid));
        }
        bankAccountRepo.deleteById(uuidDel);
    }

    @Override
    public List<String> listBySimilarBankAccount(String similarBankAccount) {
        return bankAccountRepo.findAllByBankAccountContaining(similarBankAccount)
                .stream()
                .map(BankAccount::getBankAccount)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listBySimilarAccountName(String similarAccountName) {
        return bankAccountRepo.findAllByBankAccountNameContaining(similarAccountName)
                .stream()
                .map(BankAccount::getBankAccountName)
                .collect(Collectors.toList());
    }

    @Override
    public List<BankAccountDTO> searchBankAccounts(BankAccountDTO bankAccountDto) {
        BankAccount bankAccount = transToBankAccount(bankAccountDto);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return bankAccountRepo.findAll(Example.of(bankAccount, exampleMatcher))
                .stream()
                .map(this::transToBankAccountDto)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isBankAccountExistsByLegalNameAndAccount(String legalName, String bankAccount) {
        if (StringUtils.isBlank(legalName)){
            throw new CustomException("请输入交易对手legalName");
        }
        if (StringUtils.isBlank(bankAccount)){
            throw new CustomException("请输入交易对手银行账户bankAccount");
        }

        return bankAccountRepo.existsByLegalNameAndBankAccount(legalName, bankAccount);
    }

    private Boolean isBankAccountExistsByLegalNameAndAccountAndUuid(String legalName, String bankAccount, String uuid) {
        Optional<BankAccount> account = bankAccountRepo.findByLegalNameAndBankAccount(legalName, bankAccount);

        if (account.isPresent()){
            return !StringUtils.equals(account.get().getUuid().toString(), uuid);
        } else {
            return account.isPresent();
        }
    }

    @Override
    public BankAccountDTO findBankAccountByLegalNameAndBankAccount(String legalName, String bankAccount) {
        if (StringUtils.isBlank(bankAccount)){
            throw new CustomException("请输入交易对手银行账户bankAccount");
        }
        if (StringUtils.isBlank(legalName)){
            throw new CustomException("请输入交易对手legalName");
        }
        BankAccount account = bankAccountRepo.findByLegalNameAndBankAccount(legalName, bankAccount)
                .orElseThrow(() -> new CustomException("未找到交易对手银行账户bankAccount"));
        return transToBankAccountDto(account);
    }

    private BankAccount transToBankAccount(BankAccountDTO bankAccountDto){
        String uuid = bankAccountDto.getUuid();
        BankAccount bankAccount = new BankAccount();
        BeanUtils.copyProperties(bankAccountDto, bankAccount);
        bankAccount.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));

        return bankAccount;
    }


    private BankAccountDTO transToBankAccountDto(BankAccount bankAccount){
        UUID uuid = bankAccount.getUuid();
        BankAccountDTO bankAccountDto = new BankAccountDTO();
        BeanUtils.copyProperties(bankAccount, bankAccountDto);
        bankAccountDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());

        return bankAccountDto;
    }

}
