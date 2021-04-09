package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.workflow.process.repo.entities.RequestDbo;

import java.util.Optional;

/**
 * @author yongbin
 */
public interface RequestRepo extends JpaRepository<RequestDbo, String> {

    /**
     * get valid request with given processId
     * @param processId -> id of process
     * @return -> request which will be requested after the completion of process instance
     */
    @Query(value = "from RequestDbo r where r.revoked = false and r.processId = ?1")
    Optional<RequestDbo> findValidRequestDboByProcessId(String processId);
}
