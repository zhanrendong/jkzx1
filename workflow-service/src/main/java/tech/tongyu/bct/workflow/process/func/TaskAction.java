package tech.tongyu.bct.workflow.process.func;

import tech.tongyu.bct.workflow.dto.process.ProcessData;

/**
 * task action
 * action bound to a task, when the task is completed, the action will be executed.
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface TaskAction extends Action {

    /**
     * get action's name
     * @param taskName task's name
     * @param processData the data of process
     * @return action's name
     */
    String getActionName(String taskName, ProcessData processData);
}
