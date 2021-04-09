package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.AccountTradeTask;

import java.util.UUID;

public interface AccountTradeTaskRepo extends JpaRepository<AccountTradeTask, UUID> {

    Boolean existsByLcmUUID(String lcmUUID);

}
