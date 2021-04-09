package tech.tongyu.bct.workflow.process.utils;

import com.google.common.collect.Lists;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class DiagramUtils {

    public static List<String> getHighLightedFlows(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances) {
        // 用以保存高亮的线flowId
        List<String> highFlows = Lists.newArrayList();
        if(CollectionUtils.isEmpty(historicActivityInstances)) {
            return highFlows;
        }

        // 遍历历史节点
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // 取出已执行的节点
            HistoricActivityInstance activityImpl = historicActivityInstances.get(i);

            // 用以保存后续开始时间相同的节点
            List<FlowNode> sameStartTimeNodes = Lists.newArrayList();

            // 获取下一个节点（用于连线）
            FlowNode sameActivityImpl = getNextFlowNode(bpmnModel, historicActivityInstances, i, activityImpl);

            // 将后面第一个节点放在时间相同节点的集合里
            if(sameActivityImpl != null) {
                sameStartTimeNodes.add(sameActivityImpl);
            }

            // 循环后面节点，看是否有与此后继节点开始时间相同的节点，有则添加到后继节点集合
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                // 后续第一个节点
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);
                // 后续第二个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);
                if (activityImpl1.getStartTime().getTime() != activityImpl2.getStartTime().getTime()) {
                    break;
                }

                // 如果第一个节点和第二个节点开始时间相同保存
                FlowNode sameActivityImpl2 = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
                sameStartTimeNodes.add(sameActivityImpl2);
            }

            // 得到节点定义的详细信息
            FlowNode actImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());
            // 取出节点的所有出去的线，对所有的线进行遍历
            List<SequenceFlow> pvmTransitions = actImpl.getOutgoingFlows();
            for (SequenceFlow pvmTransition : pvmTransitions) {
                // 获取节点
                FlowNode pvmActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(pvmTransition.getTargetRef());

                // 不是后继节点
                if(!sameStartTimeNodes.contains(pvmActivityImpl)) {
                    continue;
                }

                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                highFlows.add(pvmTransition.getId());
            }
        }

        //返回高亮的线
        return highFlows;
    }

    private static FlowNode getNextFlowNode(BpmnModel bpmnModel, List<HistoricActivityInstance> historicActivityInstances, int i, HistoricActivityInstance activityImpl) {
        // 保存后一个节点
        FlowNode sameActivityImpl = null;

        // 如果当前节点不是用户任务节点，则取排序的下一个节点为后续节点
        if(!"userTask".equals(activityImpl.getActivityType())) {
            // 是最后一个节点，没有下一个节点
            if(i == historicActivityInstances.size()) {
                return sameActivityImpl;
            }
            // 不是最后一个节点，取下一个节点为后继节点
            // 找到紧跟在后面的一个节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i + 1).getActivityId());
            // 返回
            return sameActivityImpl;
        }

        // 遍历后续节点，获取当前节点后续节点
        for (int k = i + 1; k <= historicActivityInstances.size() - 1; k++) {
            // 后续节点
            HistoricActivityInstance activityImp2 = historicActivityInstances.get(k);
            // 都是userTask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
            if("userTask".equals(activityImp2.getActivityType()) && activityImpl.getStartTime().getTime() == activityImp2.getStartTime().getTime()) {
                continue;
            }
            // 找到紧跟在后面的一个节点
            sameActivityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());
            break;
        }
        return sameActivityImpl;
    }
}
