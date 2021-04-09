package tech.tongyu.bct.document.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.validation.annotation.Validated;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.document.dto.EmailConfigInfoDTO;
import tech.tongyu.bct.document.service.EmailService;
import tech.tongyu.bct.report.client.dto.ValuationReportDTO;

import static tech.tongyu.bct.document.common.Constant.*;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class EmailApis {

    @Autowired
    private EmailService emailService;

    @BctMethodInfo(
            description = "保存用户邮箱信息",
            retDescription = "success",
            service = "document-service"
    )
    public String emlSaveOrUpdateEmailConfigInfo (
            @BctMethodArg(description = "用户邮箱") String emailAddress,
            @BctMethodArg(description = "用户邮箱密码") String emailPassword,
            @BctMethodArg(description = "邮箱服务器") String emailServerHost,
            @BctMethodArg(description = "邮箱服务器端口") String emailServerPort,
            @BctMethodArg(description = "创建人", required = false) String createdBy
    ) {
        if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(emailPassword) || StringUtils.isBlank(emailServerHost) || StringUtils.isBlank(emailServerPort)){
            throw new CustomException("邮箱配置参数不能为空");
        }
        if (!isEmail(emailAddress)){
            throw new CustomException("邮箱地址不正确");
        }
        emailService.emlSaveOrUpdateEmailConfigInfo(emailAddress, Base64Utils.encodeToString(emailPassword.getBytes()), emailServerHost, emailServerPort, createdBy);
        return SUCCESS;
    }

    @BctMethodInfo(
            description = "获取邮箱配置",
            retDescription = "邮箱配置信息",
            returnClass = EmailConfigInfoDTO.class,
            retName = "EmailConfigInfoDTO",
            service = "document-service"
    )
    public EmailConfigInfoDTO emlGetEmailConfigInfo () {
        return emailService.findCurrentEmailConfigInfo();
    }

    private static boolean isEmail(String emailAddress) {
        if (StringUtils.isEmpty(emailAddress))
            return false;
        return Pattern.compile(REGEX_EMAIL).matcher(emailAddress).matches();
    }

    @BctMethodInfo(
            description = "发送估值报告",
            retDescription = "发送信息",
            returnClass = ValuationReportDTO.class,
            retName = "ValuationReportDTO",
            service = "document-service"
    )
    public Map<String, Object> emlSendValuationReport(
            @BctMethodArg(description = "发送估值报告参数", argClass = ValuationReportDTO.class) @Validated List<Map<String, String>> params
    ) {
        HashMap<String, Object> result = Maps.newHashMap();
        if (CollectionUtils.isEmpty(params)) {
            throw new IllegalArgumentException("invalid params");
        }
        List<Map<String, Object>> errorList = Lists.newArrayList();
        params.stream().forEach(v -> {
            String tos = v.get("tos");
            String ccs = v.get("ccs");
            String bccs = v.get("bccs");
            String valuationReportId = v.get("valuationReportId");
            try {
                if (StringUtils.isEmpty(tos) || StringUtils.isEmpty(valuationReportId)) {
                    throw new IllegalArgumentException("valuationReportId or tos can not be empty");
                }
                emailService.emlSendValuationReport(tos, ccs, bccs, valuationReportId);
            } catch (Exception e) {
                String msg;
                if (e.getCause() != null) {
                    msg = e.getCause().getMessage();
                } else {
                    msg = e.getMessage();
                }
                Map<String, Object> errorInfo = Maps.newHashMap();
                errorInfo.put("reportId", valuationReportId);
                errorInfo.put("email", tos);
                errorInfo.put("error", msg);
                errorList.add(errorInfo);
            }
        });
        if (CollectionUtils.isEmpty(errorList)) {
            result.put(SUCCESS, SUCCESS);
        } else {
            result.put(ERROR, errorList);
        }
        return result;
    }

    @BctMethodInfo(
            description = "发送结算报告邮件",
            retDescription = "success",
            service = "document-service"
    )
    public String emlSendSettleReport(
            @BctMethodArg(description = "收件人邮箱")String tos,
            @BctMethodArg(description = "交易ID")String tradeId,
            @BctMethodArg(description = "持仓ID")String positionId,
            @BctMethodArg(description = "交易对手")String partyName) throws Exception{
        if (StringUtils.isBlank(tradeId) || StringUtils.isBlank(positionId) || StringUtils.isBlank(partyName)) {
            throw new IllegalArgumentException("positionId or tradeId can not be empty");
        }
        if (StringUtils.isBlank(tos)) {
            throw new IllegalArgumentException("收件人邮箱不能为空");
        }
        emailService.emlSendSettleReport(tos, tradeId, positionId, partyName);
        return SUCCESS;
    }

    @BctMethodInfo(
            description = "发送补充协议报告邮件",
            retDescription = "success",
            service = "document-service"
    )
    public String emlSendSupplementaryAgreementReport(
            @BctMethodArg(description = "收件人邮箱")String tos,
            @BctMethodArg(description = "交易ID")String tradeId,
            @BctMethodArg(description = "交易对手")String partyName,
            @BctMethodArg(description = "市场中断事件处理")String marketInterruptionMessage,
            @BctMethodArg(description = "提前终止期权交易情形")String earlyTerminationMessage) throws Exception{
        if (StringUtils.isBlank(tradeId) || StringUtils.isBlank(marketInterruptionMessage) || StringUtils.isBlank(earlyTerminationMessage) || StringUtils.isBlank(partyName)) {
            throw new IllegalArgumentException("tradeId or marketInterruptionMessage or earlyTerminationMessage can not be empty");
        }
        if (StringUtils.isBlank(tos)) {
            throw new IllegalArgumentException("收件人邮箱不能为空");
        }
        emailService.emlSendSupplementaryAgreementReport(tos, tradeId, partyName, marketInterruptionMessage, earlyTerminationMessage);
        return SUCCESS;
    }
}
