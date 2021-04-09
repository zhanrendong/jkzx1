package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.TaskInstanceDTO;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;

import java.util.Collection;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
public interface TaskService {

    /**
     * claim one task and then complete it.
     * @param userDTO the one who will claim the task and complete it
     * @param taskId task's id
     * @param processData business data & control data
     * @return id of the related process instance
     */
    String claimAndCompleteTask(UserDTO userDTO, String taskId, CommonProcessData processData);

    /**
     *list task of one specified user
     * @param userDTO who want to know his/her tasks
     * @return collection of task instance
     */
    Collection<TaskInstanceDTO> listTasksByUser(UserDTO userDTO);
}
