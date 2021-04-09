package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessConfigDbo;

import java.util.Collection;
import java.util.Optional;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface ProcessConfigRepo extends JpaRepository<ProcessConfigDbo, String> {

    /**
     * get valid process config by process id
     * @param processId -> process id
     * @return -> collection of process config
     */
    @Query(value = "from ProcessConfigDbo g where g.processId = ?1 and g.revoked = false ")
    Collection<ProcessConfigDbo> findValidProcessConfigDbosByProcessId(String processId);

    @Query(value = "select p.status from ProcessConfigDbo p where p.processId = ?1 and p.revoked = false and p.configName = ?2")
    Boolean findStatusByProcessIdAndConfigName(String processId, String configName);
}
