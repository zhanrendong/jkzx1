package tech.tongyu.bct.workflow.process.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.service.ProcessInstanceService;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@RestController
public class ProcessImageApi {

    private ProcessInstanceService processInstanceService;

    @Autowired
    public ProcessImageApi(ProcessInstanceService processInstanceService) {
        this.processInstanceService = processInstanceService;
    }

    /**
     * 生成流程图(仅支持仍存活的实例)
     * @param processInstanceId 流程实例ID
     * @param httpServletResponse response entity
     */
    @GetMapping("/{processInstanceId}/image")
    public void wkProcessInstanceImageGet(@PathVariable String processInstanceId, HttpServletResponse httpServletResponse){
        if (StringUtils.isEmpty(processInstanceId)) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.UNKNOWN_PROCESS_NAME);
        }
        try {
            byte[] imageBytes = processInstanceService.getProcessInstanceImageBytes(processInstanceId);
            httpServletResponse.setContentType("image/svg+xml");
            OutputStream outputStream = httpServletResponse.getOutputStream();
            outputStream.write(imageBytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.IMAGE_CREATE_FAIL,ex);
        }
    }
}
