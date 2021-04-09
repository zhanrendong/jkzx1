package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.ModifiedTaskDTO;
import tech.tongyu.bct.workflow.dto.ProcessPersistenceDTO;
import tech.tongyu.bct.workflow.process.Process;
import java.util.Collection;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
public interface ProcessService {

    /**
     * get process by process name. throws exception if not found
     * @param processName the name of process definition
     * @return process
     */
    Process getProcessByProcessName(String processName);

    /**
     * get process persistence dto by process name. throws exception if not found
     * @param processName the name of process definition
     * @return process persistence dto
     */
    ProcessPersistenceDTO getProcessPersistenceDTOByProcessName(String processName);

    /**
     * list all processes
     * @return list of processes
     */
    Collection<Process> listAllProcess();

    /**
     * clear all process instance, and then clear all processes
     */
    void clearAllProcess();

    /**
     * change process's status, make it enabled or not.
     * @param processName the name of the process definition
     * @param status true or false, enabled when true
     */
    void modifyProcessStatus(String processName, Boolean status);

    /**
     * modify process's task node
     * @param processName process's name
     * @param modifiedTaskDTOCollection collection of task node
     * @return process definition
     */
    Process modifyProcessTaskNode(String processName, Collection<ModifiedTaskDTO> modifiedTaskDTOCollection);
}
