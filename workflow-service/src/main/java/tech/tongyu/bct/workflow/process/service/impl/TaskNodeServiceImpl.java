package tech.tongyu.bct.workflow.process.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.dto.ProcessDTO;
import tech.tongyu.bct.workflow.dto.TaskApproveGroupDTO;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.manager.TaskNodeManager;
import tech.tongyu.bct.workflow.process.service.TaskNodeService;

import java.util.*;

@Service
public class TaskNodeServiceImpl implements TaskNodeService {

    private TaskNodeManager taskNodeManager;


    @Autowired
    public TaskNodeServiceImpl(
            TaskNodeManager taskNodeManager) {
        this.taskNodeManager = taskNodeManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTaskAndApproveGroup(String processName, Collection<Map<String, Object>> taskList) {
        taskNodeManager.modifyTaskApproveGroupBatch(TaskApproveGroupDTO.ofList(taskList));
    }
}
