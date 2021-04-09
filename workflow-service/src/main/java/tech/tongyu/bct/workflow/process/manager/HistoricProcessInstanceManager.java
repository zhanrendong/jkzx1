package tech.tongyu.bct.workflow.process.manager;

import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.HistoricProcessInstanceDTO;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.process.manager.act.ActHistoricProcessInstanceManger;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HistoricProcessInstanceManager {

    private static Logger logger = LoggerFactory.getLogger(HistoricProcessInstanceManager.class);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private HistoryService historyService;
    private ActHistoricProcessInstanceManger actHistoricProcessInstanceManger;
    private AuthenticationService authenticationService;

    @Autowired
    public HistoricProcessInstanceManager(HistoryService historyService
            , ActHistoricProcessInstanceManger actHistoricProcessInstanceManger
            , AuthenticationService authenticationService){
        this.historyService = historyService;
        this.actHistoricProcessInstanceManger = actHistoricProcessInstanceManger;
        this.authenticationService = authenticationService;
    }

    public List<HistoricProcessInstanceDTO> listHistoricProcessInstanceByUser(UserDTO userDTO){
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery()
                .involvedUser(userDTO.getUserName())
                .includeProcessVariables()
                .finished()
                .list();
        return list.stream().map(history -> {
            CommonProcessData elderProcessData = CommonProcessData.fromActStorage(history.getProcessVariables());
            HistoricProcessInstanceDTO historyDTO = new HistoricProcessInstanceDTO(
                    userDTO
                    , formatter.format(history.getStartTime())
                    , formatter.format(history.getEndTime())
                    , history.getProcessDefinitionName()
                    , history.getId()
                    , elderProcessData.getProcessSequenceNum()
                    , elderProcessData.getSubject()
            );
            String deleteReason = history.getDeleteReason();
            if (deleteReason != null && !deleteReason.isEmpty())
                historyDTO.setDeleteReason(deleteReason);
            return historyDTO;
        }).collect(Collectors.toList());
    }

    public HistoricProcessInstance getHistoricByProcessInstanceId(String processInstanceId){
        return actHistoricProcessInstanceManger.getHistoricByProcessInstanceId(processInstanceId);
    }

    @Transactional
    public void clearAllHistoricProcessInstance(){
        for(HistoricProcessInstance historicProcessInstance: historyService.createHistoricProcessInstanceQuery().list()){
            logger.info("> 清除历史流程实例: {}", historicProcessInstance.getId());
            historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
        }

        for(HistoricTaskInstance historicTaskInstance: historyService.createHistoricTaskInstanceQuery().list()){
            logger.info("> 清除历史任务实例: {}", historicTaskInstance.getId());
            historyService.deleteHistoricTaskInstance(historicTaskInstance.getId());
        }
    }

    public HistoricProcessInstanceDTO getProcessInstanceHistoryDTOByHistoricProcessInstance(HistoricProcessInstance historicProcessInstance){
        CommonProcessData commonProcessData = CommonProcessData.fromActStorage(historicProcessInstance.getProcessVariables());

        HistoricProcessInstanceDTO historicProcessInstanceDTO = new HistoricProcessInstanceDTO(
                authenticationService.authenticateByUsername(historicProcessInstance.getStartUserId())
                , formatter.format(historicProcessInstance.getStartTime())
                , formatter.format(historicProcessInstance.getEndTime())
                , historicProcessInstance.getProcessDefinitionName()
                , historicProcessInstance.getId()
                , commonProcessData.getProcessSequenceNum()
                , commonProcessData.getSubject()
        );
        return historicProcessInstanceDTO;
    }

}
