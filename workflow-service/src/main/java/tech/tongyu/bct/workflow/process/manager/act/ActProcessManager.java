package tech.tongyu.bct.workflow.process.manager.act;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ModifiedTaskDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.Process;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ActProcessManager {

    private static Logger logger = LoggerFactory.getLogger(ActProcessManager.class);
    private static final String BPMN_FILE_SUFFIX = ".bpmn";
    private static final String PROCESS_DEPLOYMENT_SUFFIX = "_deployment";


    private RepositoryService repositoryService;
    private DynamicBpmnModelManager dynamicBpmnModelManager;

    @Autowired
    public ActProcessManager(
            RepositoryService repositoryService,
            DynamicBpmnModelManager dynamicBpmnModelManager){
        this.repositoryService = repositoryService;
        this.dynamicBpmnModelManager = dynamicBpmnModelManager;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ProcessDefinition getProcessDefinitionByProcessName(String processName){
        return checkProcessDefinitionSingleResult(repositoryService.createProcessDefinitionQuery()
                .active()
                .processDefinitionKey(processName)
                .latestVersion()
                .list(), processName);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ProcessDefinition getProcessDefinitionByProcessDefinitionId(String processDefinitionId){
        return checkProcessDefinitionSingleResult(repositoryService.createProcessDefinitionQuery()
                .active()
                .processDefinitionId(processDefinitionId)
                .list(), processDefinitionId
        );
    }

    /**
     * 根据流程对象获取流程对象模型
     * @param processDefinitionId -> acitviti 中的流程定义ID
     * @return BpmnModel
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public BpmnModel getBpmnModelByProcessDefinitionId(String processDefinitionId){
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        if(Objects.isNull(bpmnModel)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.BPMN_MODEL_NOT_FOUND, processDefinitionId);
        }
        return bpmnModel;
    }

    private static ProcessDefinition checkProcessDefinitionSingleResult(List<ProcessDefinition> processDefinitions, String processName){
        if(CollectionUtils.isEmpty(processDefinitions)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processName);
        }
        if(processDefinitions.size() > 1) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.MULTI_PROCESS_BYNAME, processName);
        }
        return processDefinitions.get(0);
    }


    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<ProcessDefinition> listAllProcessDefinition(){
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .active()
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearAllProcessDefinition(){
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
        for(ProcessDefinition processDefinition  : processDefinitions){
            logger.info("> 清除流程: {}", processDefinition.getId());
            repositoryService.deleteDeployment(processDefinition.getDeploymentId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Deployment createBpmnModelDeployment(Process process){
        BpmnModel model = dynamicBpmnModelManager.createDynamicBpmnModel(process);
        new BpmnAutoLayout(model).execute();
        return repositoryService
                .createDeployment()
                .addBpmnModel(process.getProcessName() + BPMN_FILE_SUFFIX, model)
                .name(process.getProcessName() + PROCESS_DEPLOYMENT_SUFFIX)
                .deploy();
    }

    @Transactional(rollbackFor = Exception.class)
    public InputStream getBpmnFile(String processName, Deployment deployment){
        return repositoryService.getResourceAsStream(deployment.getId(), processName+ BPMN_FILE_SUFFIX);
    }

}
