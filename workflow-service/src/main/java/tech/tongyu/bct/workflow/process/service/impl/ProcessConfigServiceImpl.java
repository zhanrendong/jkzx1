package tech.tongyu.bct.workflow.process.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.workflow.dto.ProcessConfigDTO;
import tech.tongyu.bct.workflow.dto.ProcessConfigStatusDTO;
import tech.tongyu.bct.workflow.process.manager.self.ProcessConfigManager;
import tech.tongyu.bct.workflow.process.service.ProcessConfigService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Service
public class ProcessConfigServiceImpl implements ProcessConfigService {

    private ProcessConfigManager processConfigManager;

    @Autowired
    public ProcessConfigServiceImpl(ProcessConfigManager processConfigManager) {
        this.processConfigManager = processConfigManager;
    }

    @Override
    public Collection<ProcessConfigDTO> listProcessConfigByProcessName(String processName) {
        return processConfigManager.listProcessConfigByProcessName(processName);
    }

    private void modifyProcessConfig(String id, boolean status) {
        processConfigManager.modifyProcessConfigByConfigId(id, status);
    }

    @Override
    public void modifyProcessConfig(Collection<ProcessConfigStatusDTO> processConfigList) {
        if(CollectionUtils.isEmpty(processConfigList)){
            return;
        }
        processConfigList.stream()
                .peek(processConfigStatusDTO -> this.modifyProcessConfig(processConfigStatusDTO.getConfigId(), processConfigStatusDTO.getStatus()))
                .collect(Collectors.toSet());
    }
}
