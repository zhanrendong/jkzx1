package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.DoubleUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.workflow.dto.ConditionBusinessDTO;
import tech.tongyu.bct.workflow.dto.ConditionDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.enums.SymbolEnum;
import tech.tongyu.bct.workflow.process.repo.ConditionRepo;
import tech.tongyu.bct.workflow.process.repo.TriggerConditionRepo;
import tech.tongyu.bct.workflow.process.repo.entities.ConditionDbo;
import tech.tongyu.bct.workflow.process.repo.entities.TriggerConditionDbo;
import tech.tongyu.bct.workflow.process.trigger.Index;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class ConditionManager {

    private ConditionRepo conditionRepo;
    private IndexManager indexManager;
    private TriggerConditionRepo triggerConditionRepo;

    @Autowired
    public ConditionManager(ConditionRepo conditionRepo
            , IndexManager indexManager
            , TriggerConditionRepo triggerConditionRepo) {
        this.conditionRepo = conditionRepo;
        this.indexManager = indexManager;
        this.triggerConditionRepo = triggerConditionRepo;
    }

    @Transactional(rollbackFor = Exception.class)
    public ConditionDTO createCondition(String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String,Object> leftValue, Map<String,Object> rightValue){
        return toConditionDTO(
                conditionRepo.saveAndFlush(
                        new ConditionDbo(
                                description
                                , leftIndex
                                , rightIndex
                                , JsonUtils.objectToJsonString(leftValue)
                                , JsonUtils.objectToJsonString(rightValue)
                                , symbolEnum)
                )
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public Collection<String> createCondition(Collection<ConditionBusinessDTO> conditionDTOs){
        List<ConditionDbo> conditionDbos = conditionDTOs.stream().map(conditionDTO -> new ConditionDbo(
                conditionDTO.getDescription()
                , conditionDTO.getLeftIndex()
                , conditionDTO.getRightIndex()
                , JsonUtils.objectToJsonString(conditionDTO.getLeftValue())
                , JsonUtils.objectToJsonString(conditionDTO.getRightValue())
                , SymbolEnum.valueOf(conditionDTO.getSymbol())
        )).collect(Collectors.toList());
        return conditionRepo.saveAll(conditionDbos)
                .stream()
                .map(ConditionDbo::getId)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ConditionDTO modifyCondition(String conditionId, String leftIndex, String rightIndex, String description, SymbolEnum symbolEnum, Map<String,Object> leftValue, Map<String,Object> rightValue){
        ConditionDbo conditionDbo = conditionRepo.findValidConditionDboByConditionId(conditionId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.TRIGGER_ERROR));
        conditionDbo.setLeftIndex(leftIndex);
        conditionDbo.setRightIndex(rightIndex);
        conditionDbo.setDescription(description);
        conditionDbo.setSymbolEnum(symbolEnum);
        conditionDbo.setLeftIndexValue(JsonUtils.objectToJsonString(leftValue));
        conditionDbo.setRightIndexValue(JsonUtils.objectToJsonString(rightValue));
        return toConditionDTO(conditionRepo.saveAndFlush(conditionDbo));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCondition(String conditionId){
        triggerConditionRepo.deleteValidTriggerConditionDboByConditionId(conditionId);
        conditionRepo.deleteValidConditionDboByConditionId(conditionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteConditionByTriggerId(String triggerId) {
        Collection<TriggerConditionDbo> triggerConditionDbos = triggerConditionRepo.findValidTriggerConditionDboByTriggerId(Lists.newArrayList(triggerId));
        if (CollectionUtils.isNotEmpty(triggerConditionDbos)) {
            Set<String> conditionIds = triggerConditionDbos.stream()
                    .map(TriggerConditionDbo::getConditionId)
                    .collect(Collectors.toSet());
            triggerConditionRepo.deleteValidTriggerConditionDboByTriggerId(triggerId);
            conditionRepo.deleteValidConditionDboByConditionId(conditionIds);
        }
    }

    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public Collection<ConditionDTO> listCondition(){
        return toConditionDTO(conditionRepo.findValidConditionDbo());
    }

    public Boolean testCondition(ConditionDTO conditionDTO, Map<String, Object> data){
        Index leftIndex = indexManager.getIndex(conditionDTO.getLeftIndex().getIndexClass());
        Object left = leftIndex.execute(data, conditionDTO.getLeftValue());

        Index rightIndex = indexManager.getIndex(conditionDTO.getRightIndex().getIndexClass());
        Object right = rightIndex.execute(data, conditionDTO.getRightValue());

        switch (conditionDTO.getSymbol()){
            case GT:
                return ((BigDecimal) left).compareTo((BigDecimal) right) == 1;
            case LT:
                return ((BigDecimal) left).compareTo((BigDecimal) right) == -1;
            case GE:
                return ((BigDecimal) left).compareTo((BigDecimal) right) > -1;
            case LE:
                return ((BigDecimal) left).compareTo((BigDecimal) right) < 1;
            case EQ:
                return Objects.equals(getStringOrBigDecimal(leftIndex.getClassType(), left), getStringOrBigDecimal(rightIndex.getClassType(), right));
            case NE:
                return !Objects.equals(getStringOrBigDecimal(leftIndex.getClassType(), left), getStringOrBigDecimal(rightIndex.getClassType(), right));
            default:
                throw new UnsupportedOperationException("unknown symbol");
        }
    }

    private ConditionDTO toConditionDTO(ConditionDbo conditionDbo){
        return new ConditionDTO(
                conditionDbo.getId()
                , conditionDbo.getDescription()
                , indexManager.getIndexDTO(conditionDbo.getLeftIndex())
                , indexManager.getIndexDTO(conditionDbo.getRightIndex())
                , JsonUtils.fromJson(conditionDbo.getLeftIndexValue())
                , JsonUtils.fromJson(conditionDbo.getRightIndexValue())
                , conditionDbo.getSymbolEnum()
        );
    }

    public Collection<ConditionDTO> toConditionDTO(Collection<ConditionDbo> conditionDbos){
        if (CollectionUtils.isEmpty(conditionDbos)) {
            return Lists.newArrayList();
        }
        return conditionDbos.stream()
                .map(condition -> toConditionDTO(condition))
                .collect(Collectors.toList());
    }

    private static  <T> T getStringOrBigDecimal(Class<T> val, Object o){
        try {
            if (String.class.isAssignableFrom(val)) {
                return (T) o.toString();
            }
            return (T) new BigDecimal(o.toString());
        } catch (Exception e) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_TYPE_ERROR, e);
        }
    }
}
