package tech.tongyu.bct.reference.dao.repl.intel;



import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.AccountOpRecord;

import java.util.List;
import java.util.UUID;


public interface AccountOpRepo extends JpaRepository<AccountOpRecord, UUID> {

    Long countByAccountId(String accountId);

    List<AccountOpRecord> findByAccountIdInOrderByCreatedAtDesc(List<String> accountIds);

}

