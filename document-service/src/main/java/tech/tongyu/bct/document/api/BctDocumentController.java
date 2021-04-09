package tech.tongyu.bct.document.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.tongyu.bct.common.api.response.JsonRpcResponse;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.document.common.Constant;
import tech.tongyu.bct.document.dto.*;
import tech.tongyu.bct.document.ext.dto.BctTemplateDTO;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.ext.service.BctDocumentService;
import tech.tongyu.bct.document.service.DocumentService;
import tech.tongyu.bct.report.client.dto.ValuationReportDTO;
import tech.tongyu.bct.report.client.service.ClientReportService;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/bct/download")
public class BctDocumentController {

    private DocumentService documentService;
    private BctDocumentService bctDocumentService;
    private ClientReportService clientReportService;

    @Autowired
    public BctDocumentController(DocumentService documentService,
                                 BctDocumentService bctDocumentService,
                                 ClientReportService clientReportService) {
        this.documentService = documentService;
        this.bctDocumentService = bctDocumentService;
        this.clientReportService = clientReportService;
    }

    private static void checkFileName(String fileName){
        if (StringUtils.isBlank(fileName)){
            throw new CustomException("请输入文件名fileName");
        }
    }

    private static String pickTemplateName(String fileName){
        String picked;
        switch (fileName){
            case Constant.CSV_POSITION:
                picked = Constant.EXCHANGE_FLOW_TEMPLATE;
                break;
            case Constant.XLSX_MARGIN:
                picked = Constant.BATCH_UPDATE_MARGIN_TEMPLATE;
                break;
            case Constant.DOCX_POI:
                picked = Constant.REPLACE_TRADE_DOC;
                break;
            default:
                throw new CustomException("没有找到该文件");
        }
        return picked;
    }

    private static InputStream getInputStream(String resource){
        return BctDocumentController.class.getClassLoader().getResourceAsStream(resource);
    }

    private static void download(InputStream inputStream, String outputFileName, HttpServletResponse httpServletResponse) throws IOException{
        httpServletResponse.setCharacterEncoding(Constant.UTF_8);
        httpServletResponse.setHeader(Constant.CONTENT_DISPOSITION, getCommonContentDisposition(outputFileName));
        httpServletResponse.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        IOUtils.copy(inputStream, httpServletResponse.getOutputStream());
    }

    private static String getCommonContentDisposition(String filename){
        return String.format("%s%s", Constant.COMMON_CONTENT_DISPOSITION, utf8UrlEncode(filename));
    }

    private static String utf8UrlEncode(String content){
        try{
            return URLEncoder.encode(content, Constant.UTF_8);
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException(e);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public void handleException(RuntimeException e, HttpServletResponse response)throws UnsupportedEncodingException{
        response.setHeader(Constant.STATUS_TEXT, URLEncoder.encode(e.getMessage(), Constant.UTF_8));
    }

    @GetMapping("/template")
    public void downloadMargin(@RequestParam("fileName") String fileName, HttpServletResponse response) throws IOException {
        checkFileName(fileName);
        download(getInputStream(fileName), pickTemplateName(fileName), response);
    }

    @GetMapping("/bct-template")
    public void downloadTemplateFile(
            @RequestParam("templateId") String templateId,
            @RequestParam(value = "partyDoc", required = false) Boolean partyDoc,
            HttpServletResponse response) throws IOException{
        UUID id = UUID.fromString(templateId);

        List<TemplateDTO> templates;
        if (Objects.isNull(partyDoc)) {
            BctTemplateDTO bctTemplate = bctDocumentService.getBctTemplateDTO(id).orElseThrow(() -> new IllegalArgumentException("invalid uuid"));

            templates = documentService.getDirectory(bctTemplate.getDirectoryId())
                    .orElseThrow(() -> new IllegalArgumentException("invalid templateId")).getTemplates();
        } else {
            templates = documentService.getDirectory(id).orElseThrow(() -> new IllegalArgumentException("invalid templateId")).getTemplates();
        }

        if (templates.isEmpty()) {
            throw new CustomException("can not find template");
        }

        TemplateDTO template = templates.get(0);

        InputStream fileContent = Objects.isNull(partyDoc)
                ? new ByteArrayInputStream(documentService.getTemplateContent(template.getUuid()).getBytes(StandardCharsets.UTF_8))
                : new ByteArrayInputStream(documentService.getTemplateDoc(template.getUuid()));

        download(fileContent, template.getFileName(), response);
    }

    private static String getValuationReportName(String valuationReportName, LocalDate valuationDate, String suffix){
        return String.format("%s_%s估值报告.%s", valuationReportName, valuationDate.toString(), suffix);
    }

    @GetMapping("/valuationReport")
    public void downloadValuationReport(
            @RequestParam("valuationReportId") String valuationReportId, HttpServletResponse response) throws IOException{
        ValuationReportDTO dto = clientReportService.getValuationReport(UUID.fromString(valuationReportId));

        BctTemplateDTO bctTemplate = bctDocumentService.getBctTemplateDTOByDocType(DocTypeEnum.VALUATION_REPORT);
        UUID dirId = bctTemplate.getDirectoryId();
        String group = bctTemplate.getGroupName();
        JsonNode data = dto.getContent();
        //fetch enabled template.
        TemplateDTO template = null;
        TemplateDirectoryDTO dir = documentService.getDirectory(dirId)
                .orElseThrow(() -> new IllegalArgumentException("invalid directory id."));
        List<TemplateDTO> templates = dir.getTemplates();
        if (CollectionUtils.isEmpty(templates)) {
            throw new IllegalArgumentException("templates in this directory is empty.");
        }
        template = templates.get(0);
        String fullFileName = getValuationReportName(dto.getLegalName(), dto.getValuationDate(), template.getTypeSuffix());
        download(new ByteArrayInputStream(documentService.genByteDocument(data, dirId, group)), fullFileName, response);
    }

    private static String getSettlementReportName(String partyName, String tradeId, String positionId){
        return String.format("%s_%s_%s结算通知书", partyName, tradeId, positionId);
    }

    private static String getTradeConfirmation(String partyName, String tradeId){
        return String.format("%s_%s交易确认书", partyName, tradeId);
    }

    @GetMapping("/settlement")
    public void downloadSettlement(@RequestParam("tradeId") String tradeId,
                                                @RequestParam("positionId")String positionId,
                                                @RequestParam("partyName") String partyName,
                                                HttpServletResponse response) throws IOException{
        if (StringUtils.isBlank(tradeId)){
            throw new CustomException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(positionId)){
            throw new CustomException("请输入持仓编号positionId");
        }
        if (StringUtils.isBlank(partyName)){
            throw new CustomException("请输入交易对手名");
        }

        StringWriter stringWriter = bctDocumentService.getSettlement(tradeId, positionId);
        getTemplate(getSettlementReportName(partyName, tradeId, positionId), stringWriter, DocTypeEnum.SETTLE_NOTIFICATION, response);
    }

    @GetMapping("/supplementary_agreement")
    public void downloadSupplementary(@RequestParam("tradeId") String tradeId,
                                                   @RequestParam("partyName") String partyName,
                                                   @RequestParam("description7") String description7,
                                                   @RequestParam("description8") String description8,
                                                   HttpServletResponse response) throws IOException{
        if (StringUtils.isBlank(tradeId)){
            throw new CustomException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(partyName)){
            throw new CustomException("请输入交易对手名");
        }
        if (StringUtils.isBlank(description7)){
            throw new CustomException("描述7不能为空");
        }
        if (StringUtils.isBlank(description8)){
            throw new CustomException("描述8不能为空");
        }

        StringWriter stringWriter = bctDocumentService.getSupplementary(tradeId, description7, description8);
        getTemplate(getTradeConfirmation(partyName, tradeId), stringWriter, DocTypeEnum.SUPPLEMENTARY_AGREEMENT, response);
    }

    private void getTemplate(String fileName, StringWriter stringWriter, DocTypeEnum typeEnum, HttpServletResponse response) throws IOException{
        BctTemplateDTO bctTemplate = bctDocumentService.getBctTemplateDTOByDocTypeAndTransactType(typeEnum, "EUROPEAN");
        TemplateDirectoryDTO templateDirectoryDTO = documentService.getDirectory(bctTemplate.getDirectoryId())
                .orElseThrow(() -> new IllegalArgumentException("目录id无效" + bctTemplate.getDirectoryId()));
        List<TemplateDTO> templates = templateDirectoryDTO.getTemplates();
        if (null == templates || templates.size() < 1) {
            throw new IllegalArgumentException("当前目录下没有模板" + bctTemplate.getDirectoryId());
        }
        TemplateDTO template = templates.get(0);
        String fullFileName = fileName + "." + template.getTypeSuffix();
        download(new ByteArrayInputStream(stringWriter.getBuffer().toString().getBytes(StandardCharsets.UTF_8)), fullFileName, response);
    }

}

