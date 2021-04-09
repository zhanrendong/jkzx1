package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.ProcessInstanceDTO;
import tech.tongyu.bct.workflow.dto.ProcessInstanceData;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceUserPerspectiveEnum;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface ProcessInstanceService {

    /**
     * used to clear all process instance in activiti
     * it will not clear any one historic process instance
     */
    void clearAllProcessInstance();

    /**
     * create a new process instance with given definition
     * @param userDTO the one who want to create a new process instance
     * @param process process(include process definition)
     * @param processData -> business data & control data
     * @return process instance, to show the process instance has been created successfully.
     */
    ProcessInstanceDTO startProcess(
            UserDTO userDTO
            , Process process
            , CommonProcessData processData);

    /**
     * terminate an ongoing process instance, the terminated process instance is actually being suspended.
     * this api should be deprecated before long.
     * @param processInstanceId -> process instance id
     */
    void terminateProcessInstance(String processInstanceId);

    /**
     * list all ongoing process instance
     * @param userDTO -> the one who requests.
     * @param processInstanceUserPerspectiveEnum started_by_me or executed_by_me
     * @return -> process instance
     */
    List<ProcessInstanceDTO> listOngoingProcessInstance(
            UserDTO userDTO
            , ProcessInstanceUserPerspectiveEnum processInstanceUserPerspectiveEnum);

    /**
     * list historic process instance & process instance altogether
     * @param userDTO -> the one who made request
     * @param processInstanceUserPerspectiveEnum started_by_me or executed_by_me
     * @param processInstanceStatus divide process instance by process instance status
     * @return list of process instance
     */
    Collection<ProcessInstanceDTO> listProcessInstanceByUserPerspective(
            UserDTO userDTO
            , ProcessInstanceUserPerspectiveEnum processInstanceUserPerspectiveEnum
            , Collection<ProcessInstanceStatusEnum> processInstanceStatus);

    /**
     * get the process data(business data & control data) & process instance data with the given process instance id
     * @param processInstanceId -> process instance id
     * @param userDTO -> the one who requests.
     * @return process instance data
     */
    ProcessInstanceData getProcessDataByProcessInstanceId(String processInstanceId, UserDTO userDTO);

    /**
     * get process instance diagram
     * @param processInstanceId -> process instance id
     * @return -> image with bytes
     */
    byte[] getProcessInstanceImageBytes(String processInstanceId);

    /**
     * list all ongoing process instance by process name
     * @param processName 流程定义名称
     * @return list of process instance
     */
    Collection<ProcessInstanceDTO> listOngoingProcessInstanceByProcessName(String processName);


}
