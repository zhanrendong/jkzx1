package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessDbo;

import java.util.Collection;
import java.util.Optional;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface ProcessRepo extends JpaRepository<ProcessDbo, String> {

    /**
     * get valid process by process name
     * @param processName -> process name
     * @return -> process
     */
    @Query(value = "from ProcessDbo p where p.revoked = false and p.processName = ?1")
    Optional<ProcessDbo> findValidProcessDboByProcessName(String processName);

    /**
     * get all valid process
     * @return collection of process
     */
    @Query(value = "from ProcessDbo p where p.revoked = false")
    Collection<ProcessDbo> findAllValidProcess();

    /**
     * count valid process by process name
     * @param processName -> process name
     * @return -> count of process with given name
     */
    @Query(value = "select count(p) from ProcessDbo p where p.revoked = false and p.processName = ?1")
    Integer countValidProcessByProcessName(String processName);

    /**
     * modify process status by given process id
     * @param processName -> id of process
     * @param status -> true or false, to show whether the process is enabled
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update ProcessDbo u set u.status = ?2 where u.processName = ?1 and u.revoked = false")
    void modifyProcessDboStatusByProcessName(String processName, Boolean status);

    /**
     * find all process in process id
     * @param processId -> process id
     * @return -> all process by process id
     */
    @Query(value = "from ProcessDbo p where p.revoked = false and p.id in ?1")
    Collection<ProcessDbo> findAllValidProcessByProcessId(Collection<String> processId);
}
