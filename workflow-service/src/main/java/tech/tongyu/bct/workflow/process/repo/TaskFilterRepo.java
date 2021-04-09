package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.TaskFilterDbo;

import java.util.Collection;

/**
 * used for persistence of task related filters
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface TaskFilterRepo extends JpaRepository<TaskFilterDbo, String> {

    /**
     * list all valid task - filter relation with given list of tasks' id
     * @param taskIdList list of tasks' id
     * @return list of filters' id
     */
    @Query("from TaskFilterDbo tf where tf.revoked = false and tf.taskNodeId in (?1)")
    Collection<TaskFilterDbo> listValidTaskFilterByTaskId(Collection<String> taskIdList);

    /**
     * deletion of taskFilter where task node id in param collections
     * @param taskIdList list of task nodes' id
     */
    @Query("update TaskFilterDbo tf set tf.revoked = true where tf.revoked = false and tf.taskNodeId in (?1)")
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    void deleteAllByTaskIdIn(Collection<String> taskIdList);

}
