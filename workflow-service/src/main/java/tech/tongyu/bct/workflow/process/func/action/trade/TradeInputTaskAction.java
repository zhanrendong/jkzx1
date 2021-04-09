package tech.tongyu.bct.workflow.process.func.action.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.Task;
import tech.tongyu.bct.workflow.process.func.TaskAction;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;

@Component
public class TradeInputTaskAction implements TaskAction {

    private static Logger logger = LoggerFactory.getLogger(TradeInputTaskAction.class);

    @Override
    public String getActionName(String taskName, ProcessData processData) {
        String message;
        if (processData.getCtlProcessData().containsKey(ABANDON)
                && !((Boolean) processData.getCtlProcessData().get(ABANDON))) {
            message = "修改";
        } else if (processData.getCtlProcessData().containsKey(ABANDON)
                && (Boolean) processData.getCtlProcessData().get(ABANDON)) {
            message = "废弃";
        } else {
            message = "录入";
        }
        return message;
    }

    @Override
    public CommonProcessData execute(Process process, Task task, UserDTO userDTO, CommonProcessData elderProcessData, CommonProcessData processData, String starter) {
        logger.info("\t====> {} 完成任务 [confirmed: {}]", userDTO.getUserName(), processData.getCtlProcessData().get(ABANDON));
        elderProcessData.replace(CommonProcessData.BUSINESS, processData.getBusinessProcessData());
//        elderProcessData.replace(CommonProcessData.CTL, processData.getCtlProcessData());
//        elderProcessData.replace("confirmed", processData.getCtlProcessData().get("confirmed"));
        elderProcessData.put(ABANDON, processData.getCtlProcessData().get(ABANDON));
        elderProcessData.put(COMMENT, processData.getCtlProcessData().get(COMMENT));
        elderProcessData.setProcessCtlData(COMMENT, processData.getCtlProcessData().get(COMMENT));
        elderProcessData.setProcessCtlData(ABANDON, processData.getCtlProcessData().get(ABANDON));
        elderProcessData.setProcessCtlData(TASK_HISTORY, getActionName(task.getTaskName(), elderProcessData));
        return elderProcessData;
    }
}
