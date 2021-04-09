package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessFilterDbo;

import java.util.Collection;

/**
 * used for persistence related utility for the relation between process and filters
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface ProcessFilterRepo extends JpaRepository<ProcessFilterDbo, String> {

    /**
     * list all filters' id with given process's id
     * @param processId process's id
     * @return collection of filters' id
     */
    @Query("from ProcessFilterDbo pf where pf.revoked = false and pf.processId = ?1")
    Collection<ProcessFilterDbo> listValidProcessFilterByProcessId(String processId);
}
