package tech.tongyu.bct.workflow.process.manager.self;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.dto.ProcessPersistenceDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.manager.Converter;
import tech.tongyu.bct.workflow.process.repo.ProcessRepo;
import tech.tongyu.bct.workflow.process.repo.entities.ProcessDbo;

@Component
public class ProcessPersistenceManager {

    private ProcessRepo processRepo;

    @Autowired
    public ProcessPersistenceManager(ProcessRepo processRepo){
        this.processRepo = processRepo;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ProcessPersistenceDTO getProcessPersistenceDTOByProcessName(String processName){
        return Converter.convertProcessDbo2Dto(processRepo.findValidProcessDboByProcessName(processName)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME, processName)));
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public Boolean hasProcess(String processName){
        return processRepo.countValidProcessByProcessName(processName) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveProcess(String processName, boolean status){
        ProcessDbo processDbo = new ProcessDbo(processName, status);
        processRepo.save(processDbo);
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public void deleteAll(){
        processRepo.deleteAll();
    }

}
