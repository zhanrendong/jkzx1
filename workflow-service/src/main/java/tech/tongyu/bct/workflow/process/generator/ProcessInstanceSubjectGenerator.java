package tech.tongyu.bct.workflow.process.generator;

import org.springframework.stereotype.Component;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.process.Process;

import java.util.Map;

import static tech.tongyu.bct.workflow.process.ProcessConstants.*;
/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
@Component
public class ProcessInstanceSubjectGenerator {

    public String getProcessInstanceSubject(Process process, UserDTO userDTO, CommonProcessData processData){
        switch (process.getProcessName()){
            case FUND_APPROVAL:
                return processData.getBusinessProcessData().get(CLIENT_ID) + FUND_SUBJECT;
            case TRADE_APPROVAL:
                Map<String, Object> trade = (Map<String, Object>) processData.getBusinessProcessData().get(TRADE);
                return trade.get(TRADE_ID) + TRADE_SUBJECT;
            case CREDIT_APPROVAL:
                return processData.getBusinessProcessData().get(LEGAL_NAME) + CREDIT_SUBJECT;
            case ACCOUNT_APPROVAL:
                return processData.getBusinessProcessData().get(LEGAL_NAME) + ACCOUNT_SUBJECT;
            default:
                throw new CustomException("不支持的流程[" + process.getProcessName() + "]");
        }
    }

}
