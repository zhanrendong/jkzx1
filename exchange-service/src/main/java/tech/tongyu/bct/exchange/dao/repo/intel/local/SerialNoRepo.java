package tech.tongyu.bct.exchange.dao.repo.intel.local;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.exchange.dao.dbo.local.SerialNo;
import tech.tongyu.bct.exchange.dao.dbo.local.SerialNo;

import javax.persistence.PersistenceUnit;
import java.util.UUID;

// TODO (http://jira.tongyu.tech/browse/OTMS-1749): use JPA to implement below repository
@PersistenceUnit(name = "oracle-primary")
public interface SerialNoRepo extends JpaRepository<SerialNo, UUID> {
}
