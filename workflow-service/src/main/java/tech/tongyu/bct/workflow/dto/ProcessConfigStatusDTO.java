package tech.tongyu.bct.workflow.dto;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static tech.tongyu.bct.workflow.process.ProcessConstants.CONFIG_ID;
import static tech.tongyu.bct.workflow.process.ProcessConstants.STATUS;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class ProcessConfigStatusDTO {

    private String configId;
    private Boolean status;

    private ProcessConfigStatusDTO(){}

    public static Collection<ProcessConfigStatusDTO> ofList(List<Map<String, Object>> configList){
        if(CollectionUtils.isEmpty(configList)){
            return Sets.newHashSet();
        }
        return configList.stream()
                .map(ProcessConfigStatusDTO::of)
                .collect(Collectors.toSet());
    }

    public static ProcessConfigStatusDTO of(Map<String, Object> configMap){
        if(Objects.isNull(configMap)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_CONFIG);
        }

        Object obConfigId = configMap.get(CONFIG_ID);
        if(Objects.isNull(obConfigId)
                || !(obConfigId instanceof String)
                || StringUtils.isBlank(obConfigId.toString())){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_CONFIG);
        }

        Object obStatus = configMap.get(STATUS);
        if(Objects.isNull(obStatus)
            || !(obStatus instanceof Boolean)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.INVALID_CONFIG);
        }

        ProcessConfigStatusDTO processConfigStatusDTO = new ProcessConfigStatusDTO();
        processConfigStatusDTO.setConfigId(obConfigId.toString());
        processConfigStatusDTO.setStatus((Boolean) obStatus);
        return processConfigStatusDTO;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
