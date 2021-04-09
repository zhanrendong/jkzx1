package tech.tongyu.bct.workflow.process.func.action.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.Task;
import tech.tongyu.bct.workflow.process.func.TaskAction;
import tech.tongyu.bct.workflow.process.manager.self.RequestManager;

import java.util.List;
import java.util.Objects;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class TradeCounterSignTaskAction implements TaskAction {

    private RequestManager requestManager;

    @Autowired
    public TradeCounterSignTaskAction(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    private static Logger logger = LoggerFactory.getLogger(TradeCounterSignTaskAction.class);

    @Override
    public String getActionName(String taskName, ProcessData processData) {
        String message;
        if (processData.getCtlProcessData().containsKey(CONFIRMED)) {
            message = "会签成功";
        } else {
            message = "会签失败";
        }
        return message;
    }

    @Override
    public CommonProcessData execute(Process process, Task task, UserDTO userDTO, CommonProcessData elderProcessData, CommonProcessData processData, String starter) {
        logger.info("\t====> {} 完成任务 [会签: {}]", userDTO.getUserName(), processData.getCtlProcessData().get(CONFIRMED));
        String subject = elderProcessData.getSubject();
        String processSequenceNum = elderProcessData.getProcessSequenceNum();
        elderProcessData.replace(CommonProcessData.CTL, processData.getCtlProcessData());
        elderProcessData.put(CONFIRMED, processData.getCtlProcessData().get(CONFIRMED));
        elderProcessData.put(COMMENT, processData.getCtlProcessData().get(COMMENT));
        List<String> assigneeList = (List<String>) processData.get(ACT_LIST_NAME);
        if ((Boolean)processData.getCtlProcessData().get(CONFIRMED)) {
            //此情况产生于前已节点为会签节点,下一节点也为会签节点
            if ((Integer)elderProcessData.get(UP) == assigneeList.size()) {
                elderProcessData.put(UP, 1);
            } else {
                elderProcessData.put(UP, (Integer)elderProcessData.get(UP) + 1);
            }
        } else {
            elderProcessData.put(DOWN, (Integer)elderProcessData.get(DOWN) + 1);
        }
        elderProcessData.setProcessCtlData(SUBJECT, subject);
        elderProcessData.setProcessCtlData(PROCESS_SEQUENCE_NUM, processSequenceNum);

        if((Boolean) elderProcessData.getCtlProcessData().get(CONFIRMED)
                && Objects.equals(process.getMaxSequence(), task.getSequence())
                && assigneeList.size() == (Integer) elderProcessData.get(UP)) {
            requestManager.callService(starter, process.getProcessName(), elderProcessData.getBusinessProcessData());
        }
        return elderProcessData;
    }
}
