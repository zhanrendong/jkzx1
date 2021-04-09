package tech.tongyu.bct.reference.dao.repl.intel;
import	java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.BankAccount;

import java.util.List;
import java.util.UUID;

public interface BankAccountRepo extends JpaRepository<BankAccount, UUID> {

    Boolean existsByLegalNameAndBankAccount(String legalName, String bankAccount);

    List<BankAccount> findAllByBankAccountContaining(String similarBankAccount);

    List<BankAccount> findAllByBankAccountNameContaining(String similarAccountName);

    Optional<BankAccount> findByLegalNameAndBankAccount(String legalName, String bankAccount);
}
