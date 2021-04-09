package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.HistoricProcessInstanceDTO;
import tech.tongyu.bct.workflow.dto.HistoricProcessInstanceData;
import tech.tongyu.bct.workflow.dto.UserDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface HistoricProcessInstanceService {

    /**
     * clear all historic process instance
     */
    void  clearAllHistoricProcessInstance();

    /**
     * list historic process instance by user
     * @param userDTO user
     * @return collection of historic process instance
     */
    Collection<HistoricProcessInstanceDTO> listHistoricProcessInstanceByUser(UserDTO userDTO);

    /**
     * get historic process instance's data by process instance's id
     * @param processInstanceId process instance's id
     * @return historic process instance's data
     */
    HistoricProcessInstanceData getHistoricProcessInstanceDataByProcessInstanceId(String processInstanceId);
}
