package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.TaskNodeDTO;

import java.util.List;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 * @author yongbin
 */
public interface DynamicBpmnModelService {

    /**
     * create bpmn model with the list of task nodes and the process name
     * @param processName process's name
     * @param taskNodeDTOList list of task nodes
     */
    void createDynamicBpmnModel(String processName, List<TaskNodeDTO> taskNodeDTOList);

}
