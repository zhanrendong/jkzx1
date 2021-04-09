package tech.tongyu.bct.risk.service;

import tech.tongyu.bct.risk.dto.*;

import java.util.Collection;
import java.util.Map;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public interface ConditionGroupService {

    /**
     * 创建条件组
     * @param conditionGroupName 条件组名称
     * @param description 条件组描述
     * @param operation 条件运算类型
     * @return
     */
    ConditionGroupDTO createConditionGroup(String conditionGroupName, String description, OperationEnum operation);

    /**
     * 更新条件组
     * @param conditionGroupId 条件组ID
     * @param conditionGroupName 条件组名称
     * @param description 触发器描述
     * @param operation 条件运算类型
     */
    void modifyConditionGroup(String conditionGroupId, String conditionGroupName, String description, OperationEnum operation);

    /**
     * 删除条件组
     * @param conditionGroupId 条件组ID
     */
    void deleteConditionGroup(String conditionGroupId);

    /**
     * 查询条件组列表
     * @return
     */
    Collection<ConditionGroupDTO> listConditionGroup();

    /**
     * 根据条件组ID获得条件组
     * @param conditionGroupId 条件组ID
     * @return
     */
    ConditionGroupDTO getConditionGroup(String conditionGroupId);

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
    ConditionDTO createCondition(String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String, Object> leftValue, Map<String, Object> rightValue);

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
    void modifyCondition(String conditionId, String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String, Object> leftValue, Map<String, Object> rightValue);

    /**
     * 删除条件
     * @param conditionId 条件ID
     */
    void deleteCondition(String conditionId);

    /**
     * 删除条件
     * @param conditionGroupId 条件组ID
     */
    void deleteConditionByConditionGroupId(String conditionGroupId);

    /**
     * 查询条件列表
     * @return
     */
    Collection<ConditionDTO> listCondition();

    /**
     * 绑定触发器与条件
     * @param conditionGroupId 条件组ID
     * @param conditionIds 条件ID列表
     */
    void bindConditionGroupAndCondition(String conditionGroupId, Collection<String> conditionIds);

    /**
     * 验证是否可以触发
     * @param conditionGroupId
     * @param data
     * @return
     */
    Boolean validCanTriggered(String conditionGroupId, Map<String, Object> data);

}
