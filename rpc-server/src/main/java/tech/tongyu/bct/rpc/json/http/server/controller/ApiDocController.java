package tech.tongyu.bct.rpc.json.http.server.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.tongyu.bct.common.exception.CustomException;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URLEncoder;

@Controller
@RequestMapping("/bct/download")
public class ApiDocController {

    @GetMapping("/doc")
    public ResponseEntity<?> downloadApiDoc(HttpServletResponse response){
        try{
            Resource file = new FileSystemResource("api/api_doc.md");
            if (!file.exists())
                throw new CustomException("api_doc.md不存在");
            response.reset();
            String contentDisposition = "doc;filename*=UTF-8''" + URLEncoder.encode("api_doc.md", "UTF-8");
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
            throw new CustomException("api_doc.md生成失败");
        }
        return null;
    }

}
