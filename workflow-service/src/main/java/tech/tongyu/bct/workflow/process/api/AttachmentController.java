package tech.tongyu.bct.workflow.process.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.tongyu.bct.workflow.dto.AttachmentDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.service.AttachmentService;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URLEncoder;

/**
 * @author yongbin
 */
@Controller
@RequestMapping("/bct/download")
public class AttachmentController {

    private AttachmentService attachmentService;

    @Autowired
    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/attachment")
    public ResponseEntity<?> downloadTradeAttachment(
            @RequestParam("attachmentId") String attachmentId,
            HttpServletResponse response){
        try{
            AttachmentDTO attachment = attachmentService.getAttachmentByAttachmentId(attachmentId);
            Resource file = new FileSystemResource(attachment.getAttachmentPath());
            if (!file.exists()) {
                throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_NOT_FOUND);
            }
            response.reset();
            String contentDisposition = "attachment;filename*=UTF-8''" + URLEncoder.encode(attachment.getAttachmentName(), "UTF-8");
            response.addHeader("Content-Disposition", contentDisposition);
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("UTF-8");
            response.setContentLengthLong(file.contentLength());
            byte[] bytes = new byte[4096];
            byte[] result = (byte[]) Array.newInstance(byte.class, new Long(file.contentLength()).intValue());
            int count;
            int counter = 0;
            OutputStream os = response.getOutputStream();
            InputStream inputStream = file.getInputStream();
            while ((count = inputStream.read(bytes)) != -1) {
                System.arraycopy(bytes, 0, result, 4096 * counter, count);
                counter++;
            }
            os.write(result);
            os.flush();
        } catch (Exception e) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_DOWNLOAD_ERROR);
        }
        return null;
    }

}
