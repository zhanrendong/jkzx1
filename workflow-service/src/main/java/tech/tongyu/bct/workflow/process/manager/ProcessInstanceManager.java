package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.ProcessInstanceDTO;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.ProcessConstants;
import tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum;
import tech.tongyu.bct.workflow.process.generator.ProcessInstanceSubjectGenerator;
import tech.tongyu.bct.workflow.process.generator.ProcessNumGenerator;
import tech.tongyu.bct.workflow.process.manager.act.ActHistoricProcessInstanceManger;
import tech.tongyu.bct.workflow.process.manager.act.ActProcessInstanceManager;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;
import static tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum.PROCESS_ABANDON;
import static tech.tongyu.bct.workflow.process.enums.ProcessInstanceStatusEnum.PROCESS_UNFINISHED;

@Component
public class ProcessInstanceManager {

    private static Logger logger = LoggerFactory.getLogger(ProcessInstanceManager.class);
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private RuntimeService runtimeService;
    private AuthenticationService authenticationService;
    private ActProcessInstanceManager actProcessInstanceManager;
    private ActHistoricProcessInstanceManger actHistoricProcessInstanceManger;
    private ProcessNumGenerator processNumGenerator;
    private ProcessInstanceSubjectGenerator processInstanceSubjectGenerator;

    @Autowired
    public ProcessInstanceManager(
            RuntimeService runtimeService,
            ActProcessInstanceManager actProcessInstanceManager,
            ActHistoricProcessInstanceManger actHistoricProcessInstanceManger,
            AuthenticationService authenticationService,
            ProcessNumGenerator processNumGenerator,
            ProcessInstanceSubjectGenerator processInstanceSubjectGenerator){
        this.authenticationService = authenticationService;
        this.actProcessInstanceManager = actProcessInstanceManager;
        this.actHistoricProcessInstanceManger = actHistoricProcessInstanceManger;
        this.runtimeService = runtimeService;
        this.processNumGenerator = processNumGenerator;
        this.processInstanceSubjectGenerator = processInstanceSubjectGenerator;
    }

    @Transactional
    public ProcessInstance createProcessInstance(Process process, CommonProcessData processData){
        return runtimeService.createProcessInstanceBuilder()
                .variables(processData.toActStorage())
                .processDefinitionKey(process.getProcessName())
                .start();
    }

    public Boolean hasOngoingProcessInstance(String processName){
        return actProcessInstanceManager.hasOngoingProcessInstance(processName);
    }

    public CommonProcessData fillCtlProcessData(UserDTO userDTO, Process process, CommonProcessData processData){
        Map<String, Object> vars = processData.getCtlProcessData();

        String processNum = processNumGenerator.getProcessSequenceNum();
        String subject = processInstanceSubjectGenerator.getProcessInstanceSubject(process, userDTO, processData);

        vars.put(ProcessConstants.PROCESS_SEQUENCE_NUM, processNum);
        vars.put(ProcessConstants.SUBJECT, subject);

        return processData;
    }

    @Transactional
    public void clearAllProcessInstance(){
        actProcessInstanceManager.clearAllProcessInstance();
    }

    @Transactional
    public ProcessInstance getProcessInstanceByTask(Task task){
        return actProcessInstanceManager.getProcessInstanceByTask(task);
    }

    public ProcessInstance getProcessInstanceByProcessInstanceId(String processInstanceId){
        return actProcessInstanceManager.getProcessInstanceByProcessInstanceId(processInstanceId);
    }

    public Collection<ProcessInstance> listProcessInstanceByProcessName(String processName){
        return actProcessInstanceManager.listProcessInstanceByProcessName(processName);
    }

    @Transactional
    public void terminateProcessInstanceByProcessInstanceId(String processInstanceId){
        actProcessInstanceManager.terminateProcessInstanceByProcessInstanceId(processInstanceId);
    }

    @Transactional(readOnly = true)
    public ProcessInstanceDTO getProcessInstanceDTOByProcessInstanceId(String processInstanceId){
        ProcessInstance processInstance = actProcessInstanceManager.getProcessInstanceByProcessInstanceId(processInstanceId);
        return getProcessInstanceDTOByProcessInstance(processInstance);
    }

    public ProcessInstanceDTO getProcessInstanceDTOByProcessInstance(ProcessInstance processInstance){
        CommonProcessData commonProcessData = CommonProcessData.fromActStorage(processInstance.getProcessVariables());

        return new ProcessInstanceDTO(
                processInstance.getProcessInstanceId()
                , processInstance.getProcessDefinitionName()
                , authenticationService.authenticateByUsername(processInstance.getStartUserId())
                , formatter.format(processInstance.getStartTime())
                , commonProcessData.getProcessSequenceNum()
                , commonProcessData.getSubject()
                , PROCESS_UNFINISHED
        );
    }

    public ProcessInstanceDTO getProcessInstanceDTOByHistoricProcessInstance(HistoricProcessInstance historicProcessInstance){
        CommonProcessData commonProcessData = CommonProcessData.fromActStorage(historicProcessInstance.getProcessVariables());

        return new ProcessInstanceDTO(
                historicProcessInstance.getId()
                , historicProcessInstance.getProcessDefinitionName()
                , authenticationService.authenticateByUsername(historicProcessInstance.getStartUserId())
                , formatter.format(historicProcessInstance.getStartTime())
                , commonProcessData.getProcessSequenceNum()
                , commonProcessData.getSubject()
                , commonProcessData.containsKey(ABANDON) && (Boolean) commonProcessData.get(ABANDON)
                ? PROCESS_ABANDON : ProcessInstanceStatusEnum.PROCESS_FINISH
        );
    }

    public List<ProcessInstanceDTO> listProcessInstanceDTOByStarter(String username){
        return convertProcessInstance2Dto(listProcessInstanceByStarter(username));
    }

    public List<ProcessInstanceDTO> listHistoricProcessInstanceDTOByStarter(String username){
        return convertHistoricProcessInstance2Dto(listHistoricProcessInstanceByStarter(username));
    }

    public List<ProcessInstanceDTO> listProcessInstanceDTOByInvolvedUser(String username){
        return convertProcessInstance2Dto(listProcessInstanceByInvolvedUser(username));
    }

    public List<ProcessInstanceDTO> listProcessInstanceDTOByProcessName(String processName){
        return convertProcessInstance2Dto(listProcessInstanceByProcessName(processName));
    }

    public List<ProcessInstanceDTO> listHistoricProcessInstanceDTOByInvolvedUser(String username){
        return convertHistoricProcessInstance2Dto(listHistoricProcessInstanceByInvolvedUser(username));
    }

    public Collection<ProcessInstance> listProcessInstanceByStarter(String username){
        return actProcessInstanceManager.listProcessInstanceByStarter(username);
    }

    public Collection<HistoricProcessInstance> listHistoricProcessInstanceByStarter(String username){
        return actHistoricProcessInstanceManger.listHistoricProcessInstanceByStarter(username);
    }

    public Collection<ProcessInstance> listProcessInstanceByInvolvedUser(String username){
        return actProcessInstanceManager.listProcessInstanceByInvolvedUser(username);
    }

    public Collection<HistoricProcessInstance> listHistoricProcessInstanceByInvolvedUser(String username){
        return actHistoricProcessInstanceManger.listHistoricProcessInstanceByInvolvedUser(username);
    }

    public List<ProcessInstanceDTO> convertProcessInstance2Dto(Collection<ProcessInstance> processInstances){
        if(CollectionUtils.isEmpty(processInstances)) {
            return Lists.newArrayList();
        }

        return processInstances.stream()
                .map(this::getProcessInstanceDTOByProcessInstance)
                .sorted(Comparator.comparing(ProcessInstanceDTO::getStartTime, String::compareTo))
                .collect(Collectors.toList());
    }

    public List<ProcessInstanceDTO> convertHistoricProcessInstance2Dto(Collection<HistoricProcessInstance> processInstances){
        if(CollectionUtils.isEmpty(processInstances)) {
            return Lists.newArrayList();
        }

        return processInstances.stream()
                .map(this::getProcessInstanceDTOByHistoricProcessInstance)
                .sorted(Comparator.comparing(ProcessInstanceDTO::getStartTime, String::compareTo))
                .collect(Collectors.toList());
    }

    public List<ProcessInstanceDTO> listProcessInstanceByStarterAndKeyword(UserDTO userDTO, Collection<ProcessInstanceStatusEnum> processInstanceStatus){
        List<ProcessInstanceDTO> processInstanceDTOList = new ArrayList<>();
        if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_UNFINISHED)){
            List<ProcessInstanceDTO> unfinished = listProcessInstanceDTOByStarter(userDTO.getUserName());
            processInstanceDTOList.addAll(unfinished);
        }
        if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_ABANDON) || processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_FINISH)){
            List<ProcessInstanceDTO> historicProcessInstances = listHistoricProcessInstanceDTOByStarter(userDTO.getUserName());
            if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_ABANDON)) {
                processInstanceDTOList.addAll(getAbandonProcessInstance(historicProcessInstances));
            }
            if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_FINISH)){
                processInstanceDTOList.addAll(getFinishProcessInstance(historicProcessInstances));
            }
        }
        return processInstanceDTOList;
    }

    public List<ProcessInstanceDTO> listProcessInstanceByKeyword(UserDTO userDTO, Collection<ProcessInstanceStatusEnum> processInstanceStatus){
        List<ProcessInstanceDTO> processInstanceDTOList = new ArrayList<>();
        if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_UNFINISHED)){
            List<ProcessInstanceDTO> unfinished = listProcessInstanceDTOByInvolvedUser(userDTO.getUserName());
            processInstanceDTOList.addAll(unfinished);
        }
        if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_ABANDON) || processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_FINISH)){
            List<ProcessInstanceDTO> historicProcessInstances = listHistoricProcessInstanceDTOByInvolvedUser(userDTO.getUserName());
            if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_ABANDON)) {
                processInstanceDTOList.addAll(getAbandonProcessInstance(historicProcessInstances));
            }
            if (processInstanceStatus.contains(ProcessInstanceStatusEnum.PROCESS_FINISH)){
                processInstanceDTOList.addAll(getFinishProcessInstance(historicProcessInstances));
            }
        }
        return processInstanceDTOList;
    }

    private List<ProcessInstanceDTO> getAbandonProcessInstance(List<ProcessInstanceDTO> historicProcessInstances){
        return historicProcessInstances.stream()
                .filter(processInstanceDTO -> processInstanceDTO.getProcessInstanceStatusEnum().equals(ProcessInstanceStatusEnum.PROCESS_ABANDON))
                .collect(Collectors.toList());
    }

    private List<ProcessInstanceDTO> getFinishProcessInstance(List<ProcessInstanceDTO> historicProcessInstances){
        return historicProcessInstances.stream()
                .filter(processInstanceDTO -> processInstanceDTO.getProcessInstanceStatusEnum().equals(ProcessInstanceStatusEnum.PROCESS_FINISH))
                .collect(Collectors.toList());
    }

}
