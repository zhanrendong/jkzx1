package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.document.dao.dbo.EmailConfigInfo;

import java.util.UUID;

public interface EmailSettingRepo extends JpaRepository<EmailConfigInfo, UUID> {

    @Query("select e from EmailConfigInfo e")
    EmailConfigInfo findCurrentConfigInfo();
}
