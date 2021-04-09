package tech.tongyu.bct.document.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import io.vavr.Tuple2;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import tech.tongyu.bct.common.bo.MailConfigBO;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.EmailUtils;
import tech.tongyu.bct.document.dao.dbo.EmailConfigInfo;
import tech.tongyu.bct.document.dao.repl.intel.EmailSettingRepo;
import tech.tongyu.bct.document.dto.*;
import tech.tongyu.bct.document.ext.dto.BctTemplateDTO;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.ext.service.BctDocumentService;
import tech.tongyu.bct.document.service.DocumentService;
import tech.tongyu.bct.document.service.EmailService;
import tech.tongyu.bct.document.service.PoiTemplateService;
import tech.tongyu.bct.document.service.TradeDocumentService;
import tech.tongyu.bct.report.client.dto.ValuationReportDTO;
import tech.tongyu.bct.report.client.service.ClientReportService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private MailConfigBO mailConfig;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private BctDocumentService bctDocumentService;
    @Autowired
    private ClientReportService clientReportService;
    @Autowired
    private EmailSettingRepo emailSettingRepo;
    @Autowired
    private TradeDocumentService tradeDocumentService;
    @Autowired
    private PoiTemplateService poiTemplateService;

    private static EmailConfigInfoDTO emailConfigInfoDTO;

    @Override
    public EmailConfigInfoDTO findCurrentEmailConfigInfo() {
        return transToDTO(emailSettingRepo.findCurrentConfigInfo());
    }

    private EmailConfigInfoDTO transToDTO(EmailConfigInfo currentConfigInfo){
        return currentConfigInfo == null ? null : new EmailConfigInfoDTO(currentConfigInfo.getEmailAddress(),
                currentConfigInfo.getEmailPassword(),
                currentConfigInfo.getEmailServerHost(),
                currentConfigInfo.getEmailServerPort());
    }

    @Override
    @Transactional
    public void emlSaveOrUpdateEmailConfigInfo(String emailAddress, String emailPassword, String emailServerHost, String emailServerPort, String createdBy) {
        emailConfigInfoDTO = null;
        emailSettingRepo.deleteAll();
        EmailConfigInfo info = new EmailConfigInfo(emailAddress, emailPassword, emailServerHost, emailServerPort);
        info.setCreatedBy(createdBy);
        emailSettingRepo.save(info);
    }

    @Override
    public void emlSendValuationReport(String tos, String ccs, String bccs, String valuationReportId) throws Exception {
        checkEmailConfig();
        File file = null;
        FileWriter writer = null;
        try {
            UUID id = UUID.fromString(valuationReportId);
            ValuationReportDTO dto = clientReportService.getValuationReport(id);
            BctTemplateDTO bctTemplate = bctDocumentService.getBctTemplateDTOByDocType(DocTypeEnum.VALUATION_REPORT);

            UUID dirId = bctTemplate.getDirectoryId();
            String group = bctTemplate.getGroupName();
            JsonNode data = dto.getContent();

            //fetch enabled template.
            TemplateDTO template = null;
            {
                TemplateDirectoryDTO dir = documentService.getDirectory(dirId)
                        .orElseThrow(() -> new IllegalArgumentException("invalid directory id."));

                List<TemplateDTO> templates = dir.getTemplates();
                if (null == templates || templates.size() < 1) {
                    throw new IllegalArgumentException("templates in this directory is empty.");
                }

                template = templates.get(0);
            }
            StringBuffer fileName = new StringBuffer(dto.getLegalName());

            String fullFileName = fileName.append("_").append(dto.getValuationDate()).append("估值报告").append(".").append(template.getTypeSuffix()).toString();
            StringWriter out = new StringWriter();

            documentService.genDocument(data, dirId, group, out);

            File fileDir = new File(mailConfig.getFileDir());
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            file = new File(fileDir, fullFileName);
            writer = new FileWriter(file);
            writer.write(out.toString());
            writer.close();

            List<File> files = Lists.newArrayList(file);
            String content = "您好，附件是场外交易的估值报告，请注意查收";
            String subject = dto.getValuationDate() + "估值报告";
            EmailUtils.sendEmail(tos, ccs, bccs, subject, content, files, mailConfig);
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    @Override
    public void emlSendSettleReport(String tos, String tradeId, String positionId, String partyName)  throws Exception{
        generateAndSendDoc(TradeDocTypeEnum.SETTLEMENT_DOC, tos, tradeId, positionId, partyName ,"", "");
        tradeDocumentService.updatePositionDocStatus(positionId, DocProcessStatusEnum.SENT);
    }

    @Override
    public void emlSendSupplementaryAgreementReport(String tos, String tradeId, String partyName, String marketInterruptionMessage, String earlyTerminationMessage)  throws Exception{
        generateAndSendDoc(TradeDocTypeEnum.SUPPLEMENTARY_DOC, tos, tradeId, null, partyName, marketInterruptionMessage, earlyTerminationMessage);
        tradeDocumentService.updateTradeDocStatus(tradeId, DocProcessStatusEnum.SENT);
    }

    private void generateAndSendDoc(TradeDocTypeEnum tradeDocTypeEnum, String tos, String tradeId, String positionId, String partyName, String marketInterruptionMessage, String earlyTerminationMessage) throws Exception{
        checkEmailConfig();

        File file = null;
        FileOutputStream writer = null;
        try {
            Tuple2<String, Object> result;
            if (tradeDocTypeEnum == TradeDocTypeEnum.SETTLEMENT_DOC){
                result = poiTemplateService.getSettlement(tradeId, positionId, partyName);
            }else {
                result = poiTemplateService.getSupplementary(tradeId, partyName, marketInterruptionMessage, earlyTerminationMessage);
            }
            String fullFileName = result._1();
            File fileDir = new File(mailConfig.getFileDir());
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            file = new File(fileDir, fullFileName);
            writer = new FileOutputStream(file);

            if (result._2() instanceof XWPFDocument){
                ((XWPFDocument)(result._2())).write(writer);
            }else if (result._2() instanceof HWPFDocument){
                ((HWPFDocument)(result._2())).write(writer);
            }else {
                throw new CustomException("生成文档失败");
            }
            writer.close();

            List<File> files = Lists.newArrayList(file);
            String content = "您好，附件是场外交易的" + fullFileName + "，请注意查收";
            String subject = fullFileName;
            EmailUtils.sendEmail(tos, null, null, subject, content, files, mailConfig);
        } finally {
            if (file != null && file.exists()) {
                file.delete();
            }
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void checkEmailConfig() throws CustomException{
        emailConfigInfoDTO = Optional.ofNullable(emailConfigInfoDTO).orElseGet(this::findCurrentEmailConfigInfo);
        Optional.ofNullable(emailConfigInfoDTO).orElseThrow(() -> new CustomException("请先配置发件人邮箱"));
        BeanUtils.copyProperties(emailConfigInfoDTO, mailConfig);
        mailConfig.setEmailPassword(new String(Base64Utils.decodeFromString(emailConfigInfoDTO.getEmailPassword())));
    }
}
