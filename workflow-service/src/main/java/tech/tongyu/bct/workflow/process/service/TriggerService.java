package tech.tongyu.bct.workflow.process.service;

import tech.tongyu.bct.workflow.dto.ConditionBusinessDTO;
import tech.tongyu.bct.workflow.dto.ConditionDTO;
import tech.tongyu.bct.workflow.dto.IndexDTO;
import tech.tongyu.bct.workflow.dto.TriggerDTO;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.enums.OperationEnum;
import tech.tongyu.bct.workflow.process.enums.SymbolEnum;

import java.util.Collection;
import java.util.Map;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public interface TriggerService {

    /**
     * 创建触发器
     * @param triggerName 触发器名称
     * @param description 触发器描述
     * @param operation 条件运算类型
     * @return
     */
    TriggerDTO createTrigger(String triggerName, String description, OperationEnum operation);

    /**
     * 更新触发器
     * @param triggerId 触发器ID
     * @param triggerName 触发器名称
     * @param description 触发器描述
     * @param operation 条件运算类型
     */
    void modifyTrigger(String triggerId, String triggerName, String description, OperationEnum operation);

    /**
     * 删除触发器
     * @param triggerId 触发器ID
     */
    void deleteTrigger(String triggerId);

    /**
     * 查询触发器列表
     * @return
     */
    Collection<TriggerDTO> listTrigger();

    /**
     * 根据流程定义名称查询触发器列表
     * @return
     */
    Collection<TriggerDTO> listTrigger(String processName);

    /**
     * 根据触发器ID获得触发器
     * @return
     */
    TriggerDTO getTrigger(String triggerId);

    /**
     * 查询指标列表
     * @return 指标DTO列表
     */
    Collection<IndexDTO> listIndex();

    /**
     * 创建条件
     * @param leftIndex 左侧指标
     * @param rightIndex 右侧指标
     * @param description 条件描述
     * @param symbolEnum 指标运算类型
     * @param leftValue  左侧参数
     * @param rightValue  右侧参数
     * @return 条件DTO
     */
    ConditionDTO createCondition(String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String,Object> leftValue, Map<String,Object> rightValue);

    /**
     * 创建条件(批量)
     * @param conditions 条件列表
     * @return 条件DTO列表
     */
    Collection<String> createCondition(Collection<ConditionBusinessDTO> conditions);

    /**
     * 创建条件
     * @param conditionId 条件ID
     * @param leftIndex 左侧指标
     * @param rightIndex 右侧指标
     * @param description 条件描述
     * @param symbolEnum 指标运算类型
     * @param leftValue  左侧参数
     * @param rightValue  右侧参数
     */
    void modifyCondition(String conditionId, String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String,Object> leftValue, Map<String,Object> rightValue);

    /**
     * 删除条件
     * @param conditionId 条件ID
     */
    void deleteCondition(String conditionId);

    /**
     * 删除条件
     * @param triggerId 触发器ID
     */
    void deleteConditionByTriggerId(String triggerId);

    /**
     * 查询条件列表
     * @return
     */
    Collection<ConditionDTO> listCondition();

    /**
     * 绑定流程与触发器
     * @param processName 流程定义名称
     * @param triggerId 触发器ID
     */
    void bindProcessTrigger(String processName, String triggerId);

    /**
     * 解除绑定流程与触发器
     * @param processName 流程定义名称
     */
    void unbindProcessTrigger(String processName);
    /**
     * 绑定触发器与条件
     * @param triggerId 触发器ID
     * @param conditionIds 条件ID列表
     */
    void bindTriggerCondition(String triggerId, Collection<String> conditionIds);

    /**
     * 验证Process是否可以触发
     * @param processName
     * @param data
     * @return
     */
    Boolean validProcessTrigger(String processName, Map<String, Object> data);

}
