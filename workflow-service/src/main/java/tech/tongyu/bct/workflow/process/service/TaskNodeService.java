package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.ProcessDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
public interface TaskNodeService {

    /**
     * modify task - approve group relations
     * @param processName name of process
     * @param taskList list of task
     */
    void bindTaskAndApproveGroup(String processName, Collection<Map<String, Object>> taskList);

}
