package tech.tongyu.bct.workflow.process.service.impl;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.dto.HistoricProcessInstanceDTO;
import tech.tongyu.bct.workflow.dto.HistoricProcessInstanceData;
import tech.tongyu.bct.workflow.dto.TaskHistoryDTO;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.manager.HistoricProcessInstanceManager;
import tech.tongyu.bct.workflow.process.manager.HistoricTaskManger;
import tech.tongyu.bct.workflow.process.manager.ProcessManager;
import tech.tongyu.bct.workflow.process.service.HistoricProcessInstanceService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static tech.tongyu.bct.workflow.process.service.impl.ProcessInstanceServiceImpl.getTaskHistory;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
@Component
public class HistoricProcessInstanceServiceImpl implements HistoricProcessInstanceService {

    private HistoricProcessInstanceManager historicProcessInstanceManager;
    private HistoricTaskManger historicTaskManger;
    private ProcessManager processManager;

    @Autowired
    public HistoricProcessInstanceServiceImpl(
            HistoricProcessInstanceManager historicProcessInstanceManager,
            HistoricTaskManger historicTaskManger,
            ProcessManager processManager) {
        this.historicProcessInstanceManager = historicProcessInstanceManager;
        this.historicTaskManger = historicTaskManger;
        this.processManager = processManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllHistoricProcessInstance() {
        historicProcessInstanceManager.clearAllHistoricProcessInstance();
    }

    @Override
    public Collection<HistoricProcessInstanceDTO> listHistoricProcessInstanceByUser(UserDTO userDTO) {
        return historicProcessInstanceManager.listHistoricProcessInstanceByUser(userDTO);
    }

    @Override
    public HistoricProcessInstanceData getHistoricProcessInstanceDataByProcessInstanceId(String processInstanceId) {
        HistoricProcessInstanceData historicProcessInstanceData = new HistoricProcessInstanceData();

        HistoricProcessInstance historicProcessInstance = historicProcessInstanceManager.getHistoricByProcessInstanceId(processInstanceId);
        CommonProcessData processData = CommonProcessData.fromActStorage(historicProcessInstance.getProcessVariables());
        historicProcessInstanceData.setProcessData(processData);
        historicProcessInstanceData.setHistoricProcessInstanceDTO(historicProcessInstanceManager.getProcessInstanceHistoryDTOByHistoricProcessInstance(historicProcessInstance));

        List<HistoricTaskInstance> historicTasks = historicTaskManger.listHistoricTaskInstanceByProcessInstanceId(processInstanceId);

        Map<String, CommonProcessData> historicVariableInstanceMap = historicTaskManger.getHistoricCommonProcessDataByProcessInstanceId(processInstanceId);
        List<TaskHistoryDTO> historyTaskDTOS = getTaskHistory(historicTasks,historicVariableInstanceMap);

        historicProcessInstanceData.setTaskHistoryDTOList(historyTaskDTOS);

        return historicProcessInstanceData;
    }
}
