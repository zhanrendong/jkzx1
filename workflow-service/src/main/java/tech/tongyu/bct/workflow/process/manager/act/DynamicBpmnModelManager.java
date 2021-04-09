package tech.tongyu.bct.workflow.process.manager.act;

import com.google.common.collect.Lists;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.process.ProcessConstants;
import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class DynamicBpmnModelManager {

    private static final String ABANDON_EQUALS_TRUE = "${abandon==true}";
    private static final String ABANDON_EQUALS_FALSE = "${abandon==false}";
    private static final String CONFIRMED_EQUALS_TRUE = "${confirmed==true}";
    private static final String CONFIRMED_EQUALS_FALSE = "${confirmed==false}";
    private static final String COUNTER_SIGN_CONDITION = "${up == AssigneeDTO.upMax|| down == AssigneeDTO.downMax}";

    /**
     * 构建任务节点
     * @param id 节点Id
     * @param name 节点名称
     * @param candidateGroup 可审批组
     * @return 用户任务
     */
    private static UserTask createUserTask(String id, String name, String candidateGroup){
        List<String> candidateGroups=new ArrayList<>();
        candidateGroups.add(candidateGroup);
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setCandidateGroups(candidateGroups);
        return userTask;
    }

    /**
     * 构建任务节点
     * @param id 节点Id
     * @param name 节点名称
     * @return 会签用户任务
     */
    private static UserTask createCounterSignUserTask(String id, String name){
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee("${" + ProcessConstants.ACT_MUIT_VAR_NAME + "}");
        // 获取多实例配置
        MultiInstanceLoopCharacteristics characteristics = new MultiInstanceLoopCharacteristics();
        // 设置集合变量，统一设置成users
        characteristics.setInputDataItem("${" + ProcessConstants.ACT_MUIT_LIST_NAME + "}");
        // 设置变量
        characteristics.setElementVariable(ProcessConstants.ACT_MUIT_VAR_NAME);
        // 设置为同时接收（false 表示不按顺序执行）
        characteristics.setSequential(false);
        // 设置条件（暂时处理成，全部会签完转下步）
        characteristics.setCompletionCondition(COUNTER_SIGN_CONDITION);
        userTask.setLoopCharacteristics(characteristics);
        return userTask;
    }

    /**
     * 构建连线
     * @param from 出发节点
     * @param to 下一节点
     * @param name 连线名称
     * @param conditionExpression 条件表达式
     * @return 连线
     */
    private static SequenceFlow createSequenceFlow(String from, String to, String name, String conditionExpression) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        flow.setName(name);
        if(StringUtils.isNotEmpty(conditionExpression)){
            flow.setConditionExpression(conditionExpression);
        }
        return flow;
    }

    /**
     * 排他网关
     * @param id 网关Id
     * @return 网关
     */
    private static ExclusiveGateway createExclusiveGateway(String id, String name) {
        ExclusiveGateway exclusiveGateway = new ExclusiveGateway();
        exclusiveGateway.setId(id);
        exclusiveGateway.setName(name);
        return exclusiveGateway;
    }

    /**
     * 创建开始节点
     * @return 开始节点
     */
    private static StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        startEvent.setName("发起流程");
        return startEvent;
    }

    /**
     * 创建结束节点
     * @return 结束节点
     */
    private static EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        endEvent.setName("结束流程");
        return endEvent;
    }

    /**
     * 创建废弃节点
     * @return 废弃节点
     */
    private static EndEvent createAbandonEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("abandon");
        endEvent.setName("废弃流程");
        return endEvent;
    }

    private static String getSequenceFlowName(String from, String to ){
        return String.format("%s -> %s", from, to);
    }

    public BpmnModel createDynamicBpmnModel(tech.tongyu.bct.workflow.process.Process process){
        org.activiti.bpmn.model.Process actProcess = new Process();
        actProcess.setId(process.getProcessName());
        actProcess.setName(process.getProcessName());

        tech.tongyu.bct.workflow.process.Task inputTask = process.getInputTask();
        tech.tongyu.bct.workflow.process.Task modifyTask = process.getModifyTask();
        List<tech.tongyu.bct.workflow.process.Task> reviewTaskList = process.listReviewTask();

        actProcess.setCandidateStarterGroups(Lists.newArrayList(inputTask.getCandidateGroup(process.getProcessName())));
        //以下为创建规则
        //网关id:有先为两个userTask的id组合
        //userTaskId:传入时顺序内部构造 例 _1

        //开始
        actProcess.addFlowElement(createStartEvent());
        //开始 -> 录入
        actProcess.addFlowElement(createSequenceFlow("start", inputTask.getActTaskId(), "录入", ""));
        //录入
        actProcess.addFlowElement(createUserTask(inputTask.getActTaskId(), inputTask.getTaskName(),inputTask.getCandidateGroup(process.getProcessName())));
        //录入-> 复核
        tech.tongyu.bct.workflow.process.Task firstReviewTask = reviewTaskList.get(0);
        actProcess.addFlowElement(createSequenceFlow(inputTask.getActTaskId(), firstReviewTask.getActTaskId(), getSequenceFlowName(inputTask.getTaskName(), firstReviewTask.getTaskName()), ""));
        //复核0(默认为第一个复核)
        actProcess.addFlowElement(createUserTask(firstReviewTask.getActTaskId(), firstReviewTask.getTaskName(),firstReviewTask.getCandidateGroup(process.getProcessName())));
        //复核0 -> 复核网关-0
        actProcess.addFlowElement(createSequenceFlow(firstReviewTask.getActTaskId(), firstReviewTask.getActTaskId() + modifyTask.getActTaskId(), getSequenceFlowName(firstReviewTask.getTaskName(), modifyTask.getTaskName()), ""));
        //复核网关-0
        actProcess.addFlowElement(createExclusiveGateway(firstReviewTask.getActTaskId() + modifyTask.getActTaskId(), "判断是否应当录入"));
        //复核失败 -> 修改
        actProcess.addFlowElement(createSequenceFlow(firstReviewTask.getActTaskId() + modifyTask.getActTaskId(), modifyTask.getActTaskId(), "退回修改",CONFIRMED_EQUALS_FALSE));
        //修改
        actProcess.addFlowElement(createUserTask(modifyTask.getActTaskId(), modifyTask.getTaskName(),modifyTask.getCandidateGroup(process.getProcessName())));
        //修改 -> 废弃网关
        actProcess.addFlowElement(createSequenceFlow(modifyTask.getActTaskId(), modifyTask.getActTaskId() + firstReviewTask.getActTaskId(), "", ""));
        //废弃网关
        actProcess.addFlowElement(createExclusiveGateway(modifyTask.getActTaskId() + firstReviewTask.getActTaskId(), "判断是否废弃"));
        //流程废弃 -> 废弃
        actProcess.addFlowElement(createSequenceFlow(modifyTask.getActTaskId() + firstReviewTask.getActTaskId(), "abandon", "废弃",ABANDON_EQUALS_TRUE));
        //废弃
        actProcess.addFlowElement(createAbandonEvent());
        //修改完成 -> 复核
        actProcess.addFlowElement(createSequenceFlow(modifyTask.getActTaskId() + firstReviewTask.getActTaskId(), firstReviewTask.getActTaskId(), "修改完成", ABANDON_EQUALS_FALSE));

        //只有一个复核则通过则结束
        int i = 1;
        while(reviewTaskList.size() > i){
            //复核网关-i-1 -> 复核-i
            actProcess.addFlowElement(createSequenceFlow(reviewTaskList.get(i-1).getActTaskId() + modifyTask.getActTaskId(), reviewTaskList.get(i).getActTaskId(), "通过", CONFIRMED_EQUALS_TRUE));
            //复核i
            if (Objects.equals(reviewTaskList.get(i).getTaskType(), TaskTypeEnum.COUNTER_SIGN_DATA)) {
                //会签节点 -> 全部通过即通过
                actProcess.addFlowElement(createCounterSignUserTask(reviewTaskList.get(i).getActTaskId(), reviewTaskList.get(i).getTaskName()));
            } else {
                //一般复核节点 -> 一人通过即通过
                actProcess.addFlowElement(createUserTask(reviewTaskList.get(i).getActTaskId(), reviewTaskList.get(i).getTaskName(), reviewTaskList.get(i).getCandidateGroup(process.getProcessName())));
            }
            //复核i -> 复核网关i
            actProcess.addFlowElement(createSequenceFlow(reviewTaskList.get(i).getActTaskId(), reviewTaskList.get(i).getActTaskId() + modifyTask.getActTaskId(), "", ""));
            //复核网关i
            actProcess.addFlowElement(createExclusiveGateway(reviewTaskList.get(i).getActTaskId() + modifyTask.getActTaskId(), "判断是否应当录入"));
            //复核失败
            actProcess.addFlowElement(createSequenceFlow(reviewTaskList.get(i).getActTaskId() + modifyTask.getActTaskId(), modifyTask.getActTaskId(), "退回修改",CONFIRMED_EQUALS_FALSE));
            i++;
        }
        //复核成功 -> 结束
        actProcess.addFlowElement(createSequenceFlow(reviewTaskList.get(i-1).getActTaskId() + modifyTask.getActTaskId(), "end", "通过",CONFIRMED_EQUALS_TRUE));
        //结束
        actProcess.addFlowElement(createEndEvent());

        BpmnModel model = new BpmnModel();
        model.addProcess(actProcess);
        return model;
    }

}
