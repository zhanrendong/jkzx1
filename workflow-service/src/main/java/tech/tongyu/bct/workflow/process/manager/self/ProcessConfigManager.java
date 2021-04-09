package tech.tongyu.bct.workflow.process.manager.self;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.dto.ProcessConfigDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.repo.ProcessConfigRepo;
import tech.tongyu.bct.workflow.process.repo.ProcessRepo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessConfigDbo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessDbo;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ProcessConfigManager {

    private ProcessConfigRepo processConfigRepo;
    private ProcessRepo processRepo;

    @Autowired
    public ProcessConfigManager(
            ProcessConfigRepo processConfigRepo
            , ProcessRepo processRepo) {
        this.processConfigRepo = processConfigRepo;
        this.processRepo = processRepo;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Collection<ProcessConfigDTO> listProcessConfigByProcessName(String processName){
        ProcessDbo processDbo = processRepo.findValidProcessDboByProcessName(processName)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processName));
        Collection<ProcessConfigDbo> processConfigDbos = processConfigRepo.findValidProcessConfigDbosByProcessId(processDbo.getId());
        return toDTO(processConfigDbos);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    protected ProcessDbo getProcessDboByProcessName(String processName){
        return processRepo.findValidProcessDboByProcessName(processName)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processName));
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void createProcessConfig(String configName, String configNickName, String processName){
        ProcessDbo processDbo = getProcessDboByProcessName(processName);
        ProcessConfigDbo processConfigDbo = new ProcessConfigDbo();
        processConfigDbo.setConfigName(configName);
        processConfigDbo.setConfigNickName(configNickName);
        processConfigDbo.setProcessId(processDbo.getId());
        processConfigDbo.setStatus(true);
        processConfigRepo.save(processConfigDbo);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void modifyProcessConfigByConfigId(String configId, Boolean status){
        ProcessConfigDbo processConfigDbo = processConfigRepo.getOne(configId);
        processConfigDbo.setStatus(status);
        processConfigRepo.save(processConfigDbo);
    }

    @Transactional(rollbackFor = RuntimeException.class)
    public void deleteAll(){
        processConfigRepo.deleteAll();
    }

    /**
     * there are no duplicate config with the same name related to one process.
     * @param processId process's id
     * @param configName config's name
     * @return true or false, whether process's config is enabled or not
     */
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Boolean getProcessConfigStatus(String processId, String configName){
        return processConfigRepo.findStatusByProcessIdAndConfigName(processId, configName);
    }

    private static Collection<ProcessConfigDTO> toDTO(Collection<ProcessConfigDbo> processConfigDbos){
        return processConfigDbos.stream().map(dbo ->
            new ProcessConfigDTO(
                   dbo.getId()
                   , dbo.getConfigName()
                   , dbo.getProcessId()
                   , dbo.getStatus())
        ).collect(Collectors.toList());
    }
}
