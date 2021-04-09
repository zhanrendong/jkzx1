package tech.tongyu.bct.workflow.process.service.impl;

import org.activiti.engine.repository.Deployment;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.workflow.dto.ModifiedTaskDTO;
import tech.tongyu.bct.workflow.dto.ProcessPersistenceDTO;
import tech.tongyu.bct.workflow.dto.TaskNode;
import tech.tongyu.bct.workflow.dto.TaskNodeDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.manager.ProcessInstanceManager;
import tech.tongyu.bct.workflow.process.manager.ProcessManager;
import tech.tongyu.bct.workflow.process.manager.TaskNodeManager;
import tech.tongyu.bct.workflow.process.manager.act.ActProcessManager;
import tech.tongyu.bct.workflow.process.manager.self.ProcessPersistenceManager;
import tech.tongyu.bct.workflow.process.service.ProcessService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ProcessServiceImpl implements ProcessService, DevLoadedTest {

    private static Logger logger = LoggerFactory.getLogger(ProcessServiceImpl.class);

    private ProcessManager processManager;
    private ProcessInstanceManager processInstanceManager;
    private TaskNodeManager taskNodeManager;
    private ActProcessManager actProcessManager;
    private ProcessPersistenceManager processPersistenceManager;

    @Autowired
    public ProcessServiceImpl(
            ProcessInstanceManager processInstanceManager,
            ProcessManager processManager,
            TaskNodeManager taskNodeManager,
            ActProcessManager actProcessManager,
            ProcessPersistenceManager processPersistenceManager){
        this.processManager = processManager;
        this.taskNodeManager = taskNodeManager;
        this.processInstanceManager = processInstanceManager;
        this.actProcessManager = actProcessManager;
        this.processPersistenceManager = processPersistenceManager;
    }

    @Override
    public Process getProcessByProcessName(String processName) {
        return processManager.getProcessByProcessName(processName);
    }

    @Override
    public ProcessPersistenceDTO getProcessPersistenceDTOByProcessName(String processName) {
        return processPersistenceManager.getProcessPersistenceDTOByProcessName(processName);
    }

    @Override
    public Collection<Process> listAllProcess() {
        return processManager.listAllProcess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllProcess() {
        logger.warn("\t=> now clear all process instance & processes...");
        processManager.clearAllProcess();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyProcessStatus(String processName, Boolean status) {
        logger.info("\t=> change process status... process: {}, enabled: {}", processName, status);
        processManager.modifyProcessStatus(processName, status);
    }

    /**
     * 0. check whether there are old bpmn model used by one or more ongoing process instance
     * 1. delete task nodes persisted in database
     * 2. create new task nodes replacing old ones with params
     * 3.1. delete old process definition
     * 3.2. generate a new process definition and deploy it.
     * @param processName process's name
     * @param modifiedTaskDTOCollection collection of task node
     * @return process
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Process modifyProcessTaskNode(String processName, Collection<ModifiedTaskDTO> modifiedTaskDTOCollection) {
        Process process = processManager.getProcessByProcessName(processName);
        if(processInstanceManager.hasOngoingProcessInstance(processName)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PROCESS_USED, processName);
        }
        taskNodeManager.deleteTaskNodeByProcessId(process.getProcessId());
        taskNodeManager.createTaskNode(process.getProcessId(), modifiedTaskDTOCollection.stream().map(modifiedTaskDTO -> (TaskNode) modifiedTaskDTO).collect(Collectors.toSet()));

        Deployment deployment = actProcessManager.createBpmnModelDeployment(processManager.getProcessByProcessName(processName));
        InputStream bpmnFile = actProcessManager.getBpmnFile(processName, deployment);
        try {
            FileUtils.copyInputStreamToFile(bpmnFile,new File("deployments/"+processName+".bpmn"));
        } catch (IOException e) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PROCESS_BPMN_CREATE);
        }

        return processManager.getProcessByProcessName(processName);
    }
}
