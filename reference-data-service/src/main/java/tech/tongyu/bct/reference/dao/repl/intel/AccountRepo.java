package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.Account;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepo extends JpaRepository<Account, UUID> {

    Long countByLegalName(String legalName);

    List<Account> findAllByLegalNameIn(List<String> legalNames);

    List<Account> findAllByAccountIdIn(List<String> accountIds);

    Optional<Account> findByAccountId(String accountId);

    List<Account> findAllByOrderByAccountIdAsc(Pageable p);

    List<Account> findByLegalNameIn(List<String> legalNames);

    List<Account> deleteByAccountId(String AccountId);

    List<Account> findByLegalName(String legalName);

    void deleteByLegalName(String legalName);

}
