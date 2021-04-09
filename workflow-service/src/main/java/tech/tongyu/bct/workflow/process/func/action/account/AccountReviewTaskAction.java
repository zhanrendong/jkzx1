package tech.tongyu.bct.workflow.process.func.action.account;

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

import java.util.Objects;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;
/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class AccountReviewTaskAction implements TaskAction {

    private RequestManager requestManager;

    @Autowired
    public AccountReviewTaskAction(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    private static Logger logger = LoggerFactory.getLogger(AccountReviewTaskAction.class);

    @Override
    public String getActionName(String taskName, ProcessData processData) {
        Boolean b = true;
        Object t = processData.getCtlProcessData().get(CONFIRMED);
        if(Objects.isNull(t))
            b = false;
        else b = (Boolean) t;
        return b ? "复核通过" : "退回";
    }

    @Override
    public CommonProcessData execute(Process process, Task task, UserDTO userDTO, CommonProcessData elderProcessData, CommonProcessData processData, String starter) {
        logger.info("\t====> {} 完成任务 [confirmed: {}]", userDTO.getUserName(), processData.getCtlProcessData().get(CONFIRMED));
        String subject = elderProcessData.getSubject();
        String processSequenceNum = elderProcessData.getProcessSequenceNum();
        elderProcessData.replace(CommonProcessData.CTL, processData.getCtlProcessData());
        elderProcessData.put(CONFIRMED, processData.getCtlProcessData().get(CONFIRMED));
        elderProcessData.put(COMMENT, processData.getCtlProcessData().get(COMMENT));
        elderProcessData.setProcessCtlData(SUBJECT, subject);
        elderProcessData.setProcessCtlData(PROCESS_SEQUENCE_NUM, processSequenceNum);
        if((Boolean) elderProcessData.getCtlProcessData().get(CONFIRMED)
                && Objects.equals(process.getMaxSequence(), task.getSequence()))
            requestManager.callService(starter, process.getProcessName(), elderProcessData.getBusinessProcessData());
        return elderProcessData;
    }
}
