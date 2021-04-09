package tech.tongyu.bct.workflow.process.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activiti.engine.HistoryService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HistoricTaskManger {

    private HistoryService historyService;

    @Autowired
    public HistoricTaskManger(HistoryService historyService){
        this.historyService = historyService;
    }

    public List<HistoricTaskInstance> listHistoricTaskInstanceByProcessInstanceId(String processInstanceId){

        return historyService.createHistoricTaskInstanceQuery()
                .finished()
                .includeProcessVariables()
                .processInstanceId(processInstanceId)
                .list();
    }

    public Map<String, CommonProcessData> getHistoricCommonProcessDataByProcessInstanceId(String processInstanceId){
        List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();

        Map<String, List<HistoricVariableInstance>> map = Maps.newHashMap();
        historicVariableInstances.stream()
                .filter(historicVariableInstance -> !Objects.isNull(historicVariableInstance.getTaskId()))
                .peek(historicVariableInstance -> {
                    String taskId = historicVariableInstance.getTaskId();
                    if(map.containsKey(taskId)){
                        List<HistoricVariableInstance> variableInstances = map.get(taskId);
                        variableInstances.add(historicVariableInstance);
                    }
                    else{
                        map.put(taskId, Lists.newArrayList(historicVariableInstance));
                    }
                })
                .collect(Collectors.toSet());
        Map<String, CommonProcessData> resultMap = Maps.newHashMap();
        map.forEach((taskId, list) -> {
            Map<String, Object> processVars = Maps.newHashMap();
            list.stream()
                    .forEach(historicVariableInstance -> {
                        processVars.put(historicVariableInstance.getVariableName(), historicVariableInstance.getValue());
                    });
            resultMap.put(taskId, CommonProcessData.fromActStorage(processVars));
        });
        return resultMap;
    }

}
