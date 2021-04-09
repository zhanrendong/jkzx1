package tech.tongyu.bct.workflow.process.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.workflow.dto.ProcessConfigDTO;
import tech.tongyu.bct.workflow.dto.ProcessConfigStatusDTO;
import tech.tongyu.bct.workflow.process.service.ProcessConfigService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class ProcessConfigApi {

    private ProcessConfigService processConfigService;

    @Autowired
    public ProcessConfigApi(ProcessConfigService processConfigService) {
        this.processConfigService = processConfigService;
    }

    /**
     * batch modify process config
     * @param configList list of process config {configId: "", status: (Boolean)}
     * @return success
     */
    @BctMethodInfo
    public String wkProcessConfigModify(
            @BctMethodArg List<Map<String, Object>> configList){
        Collection<ProcessConfigStatusDTO> processConfigStatusDTOCollection = ProcessConfigStatusDTO.ofList(configList);
        processConfigService.modifyProcessConfig(processConfigStatusDTOCollection);
        return "success";
    }

    @BctMethodInfo
    public Collection<ProcessConfigDTO> wkProcessConfigList(
            @BctMethodArg String processName){
        return processConfigService.listProcessConfigByProcessName(processName);
    }

}
