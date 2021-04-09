package tech.tongyu.bct.workflow.process.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.workflow.dto.AttachmentDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.service.AttachmentService;

import java.util.Collection;
import java.util.List;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
@Component
public class AttachmentApi {

    private AttachmentService attachmentService;

    @Autowired
    public AttachmentApi(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @BctMethodInfo(
            description = "create or update doc",
            retDescription = "attachmentDTO"
    )
    @Transactional(rollbackFor = Exception.class)
    public AttachmentDTO wkAttachmentUpload(
            @BctMethodArg(required = false) String attachmentId,
            @BctMethodArg MultipartFile file){
        if (StringUtils.isBlank(attachmentId)){
            return attachmentService.createAttachment(file);
        } else {
            return attachmentService.modifyAttachment(attachmentId, file);
        }
    }

    @BctMethodInfo
    @Transactional(rollbackFor = Exception.class)
    public String wkAttachmentDelete(
            @BctMethodArg(required = false) String attachmentId,
            @BctMethodArg(required = false) String processInstanceId){
        if (StringUtils.isBlank(attachmentId) && StringUtils.isBlank(processInstanceId)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        if (!StringUtils.isBlank(processInstanceId)){
            attachmentService.deleteAttachmentByProcessInstanceId(processInstanceId);
        }
        if (!StringUtils.isBlank(attachmentId)){
            attachmentService.deleteAttachmentByAttachmentId(attachmentId);
        }
        return "success";
    }

    @BctMethodInfo
    public Collection<AttachmentDTO> wkAttachmentList(
            @BctMethodArg(required = false) String processInstanceId){
        if (StringUtils.isBlank(processInstanceId)){
            return attachmentService.listAttachment();
        } else {
            return attachmentService.listAttachmentByProcessInstanceId(processInstanceId);
        }
    }

    @BctMethodInfo
    public AttachmentDTO wkAttachmentProcessInstanceBind(
            @BctMethodArg String processInstanceId,
            @BctMethodArg String attachmentId){
        if (StringUtils.isBlank(attachmentId) && StringUtils.isBlank(processInstanceId)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_FOUND);
        }
        return attachmentService.modifyAttachment(attachmentId, processInstanceId);
    }
}
