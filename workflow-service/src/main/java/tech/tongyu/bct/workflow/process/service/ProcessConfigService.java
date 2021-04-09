package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.ProcessConfigDTO;
import tech.tongyu.bct.workflow.dto.ProcessConfigStatusDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface ProcessConfigService {

    /**
     * list process config by process name
     * @param processName -> process name
     * @return list of process config
     */
    Collection<ProcessConfigDTO> listProcessConfigByProcessName(String processName);

    /**
     * enable or disable process config
     * @param processConfigList list of process config
     */
    void modifyProcessConfig(Collection<ProcessConfigStatusDTO> processConfigList);
}
