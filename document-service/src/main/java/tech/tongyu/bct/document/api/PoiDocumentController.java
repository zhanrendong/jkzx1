package tech.tongyu.bct.document.api;

import io.vavr.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.document.common.Constant;
import tech.tongyu.bct.document.dictory.Element;
import tech.tongyu.bct.document.poi.PoiTemplateDTO;
import tech.tongyu.bct.document.service.PoiTemplateService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.Map;

@RestController
@RequestMapping("/bct/download/poi")
public class PoiDocumentController {

    private PoiTemplateService poiTemplateService;

    @Autowired
    public PoiDocumentController(PoiTemplateService poiTemplateService) {
        this.poiTemplateService = poiTemplateService;
    }


    @GetMapping("/poi-template")
    public void downloadTemplate(@RequestParam("poiTemplateId") String id, HttpServletResponse response) throws Exception{
        if (StringUtils.isEmpty(id)){
            throw new CustomException("poiTemplateId不能为空");
        }
        PoiTemplateDTO poiTemplateDTO = poiTemplateService.findPoiTemplateFile(id);
        FileInputStream fileInputStream = new FileInputStream(new File(poiTemplateDTO.getFilePath()));
        String contentDisposition = "attachment;filename*=UTF-8''" + URLEncoder.encode(poiTemplateDTO.getFileName(), "UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", contentDisposition);
        // prepare for header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        IOUtils.copy(fileInputStream, response.getOutputStream());
    }

    @ExceptionHandler(RuntimeException.class)
    public void handleException(RuntimeException e, HttpServletResponse response)throws UnsupportedEncodingException{
        response.setHeader(Constant.STATUS_TEXT, URLEncoder.encode(e.getMessage(), Constant.UTF_8));
    }

    @PostMapping("/element")
    public Map<String, Map<String, String>> getAllElement() {
        return Element.ELEMENT_MAP;
    }

    @GetMapping("/settlement")
    public void downloadSettlement(@RequestParam("tradeId") String tradeId,
                                   @RequestParam("positionId") String positionId,
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
        Tuple2<String, Object> result = poiTemplateService.getSettlement(tradeId, positionId, partyName);
        response.setCharacterEncoding("UTF-8");
        String contentDisposition = "attachment;filename*=UTF-8''" + URLEncoder.encode(result._1(), "UTF-8");
        response.setHeader("Content-Disposition", contentDisposition);
        ServletOutputStream outputStream = response.getOutputStream();
        if (result._2() instanceof XWPFDocument){
            ((XWPFDocument)(result._2())).write(outputStream);
        }else if (result._2() instanceof HWPFDocument){
            ((HWPFDocument)(result._2())).write(outputStream);
        }
        outputStream.flush();
    }

    @GetMapping("/supplementary_agreement")
    public void downloadSupplementary(@RequestParam("tradeId") String tradeId,
                                      @RequestParam("partyName") String partyName,
                                      @RequestParam("marketInterruptionMessage") String marketInterruptionMessage,
                                      @RequestParam("earlyTerminationMessage") String earlyTerminationMessage,
                                      HttpServletResponse response) throws IOException{
        if (StringUtils.isBlank(tradeId)){
            throw new CustomException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(partyName)){
            throw new CustomException("请输入交易对手名");
        }
        if (StringUtils.isBlank(marketInterruptionMessage)){
            throw new CustomException("请输入市场中断事件处理");
        }
        if (StringUtils.isBlank(earlyTerminationMessage)){
            throw new CustomException("请输入提前终止期权交易情形");
        }
        Tuple2<String, Object> result = poiTemplateService.getSupplementary(tradeId, partyName, marketInterruptionMessage, earlyTerminationMessage);
        response.setCharacterEncoding("UTF-8");
        String contentDisposition = "attachment;filename*=UTF-8''" + URLEncoder.encode(result._1(), "UTF-8");
        response.setHeader("Content-Disposition", contentDisposition);
        ServletOutputStream outputStream = response.getOutputStream();
        if (result._2() instanceof XWPFDocument){
            ((XWPFDocument)(result._2())).write(outputStream);
        }else if (result._2() instanceof HWPFDocument){
            ((HWPFDocument)(result._2())).write(outputStream);
        }
        outputStream.flush();
    }
}
