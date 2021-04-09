package tech.tongyu.bct.reference.service;

import tech.tongyu.bct.reference.dto.BankAccountDTO;

import java.util.List;

public interface BankAccountService {

    String SCHEMA = "referenceDataService";

    void deleteBankAccount(String uuid);

    BankAccountDTO createBankAccount(BankAccountDTO bankAccountDto);

    BankAccountDTO updateBankAccount(BankAccountDTO bankAccountDto);

    List<String> listBySimilarBankAccount(String similarBankAccount);

    List<String> listBySimilarAccountName(String similarAccountName);

    List<BankAccountDTO> searchBankAccounts(BankAccountDTO bankAccountDto);

    Boolean isBankAccountExistsByLegalNameAndAccount(String legalName, String bankAccount);

    BankAccountDTO findBankAccountByLegalNameAndBankAccount(String legalName, String bankAccount);
}
