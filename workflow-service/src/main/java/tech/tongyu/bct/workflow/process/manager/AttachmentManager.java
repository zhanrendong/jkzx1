package tech.tongyu.bct.workflow.process.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.util.FileUtils;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.AttachmentDTO;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;
import tech.tongyu.bct.workflow.process.repo.AttachmentRepo;
import tech.tongyu.bct.workflow.process.repo.entities.AttachmentDbo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AttachmentManager {

    @Value("${spring.upload.location}")
    private String FILE_PATH;

    private AttachmentRepo attachmentRepo;
    private AuthenticationService authenticationService;

    @Autowired
    public AttachmentManager(AttachmentRepo attachmentRepo
            , AuthenticationService authenticationService) {
        this.attachmentRepo = attachmentRepo;
        this.authenticationService = authenticationService;
    }

    public AttachmentDTO createAttachment(String fileName){
        AttachmentDbo attachmentDbo = new AttachmentDbo(
                null
                , fileName
                , null
                , new Date()
                , new Date()
                , authenticationService.authenticateCurrentUser().getUserName()
        );
        return toAttachmentDTO(attachmentRepo.saveAndFlush(attachmentDbo));
    }

    public AttachmentDTO createAttachmentFile(String attachmentId, MultipartFile file){
        String path = FILE_PATH + File.separator + attachmentId.replace("-","");
        try {
            if(!new File(path).exists()) {
                FileUtils.createFile(path);
            }
            FileOutputStream writer = new FileOutputStream(path);
            writer.write(file.getBytes());
        } catch (Exception e) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_UPLOAD_ERROR);
        }
        AttachmentDbo attachmentDbo = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_NOT_FOUND));
        attachmentDbo.setAttachmentPath(path);

        return toAttachmentDTO(attachmentRepo.saveAndFlush(attachmentDbo));
    }

    public void deleteAttachmentByAttachmentId(String attachmentId){
        attachmentRepo.deleteAttachmentDboById(attachmentId);
    }

    public void deleteAttachmentByProcessInstanceId(String processInstanceId){
        attachmentRepo.deleteAllByProcessInstanceId(processInstanceId);
    }

    public AttachmentDTO getAttachmentByAttachmentId(String attachmentId){
        return toAttachmentDTO(attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_NOT_FOUND)));
    }

    public AttachmentDTO modifyAttachmentByProcessInstanceId(String attachmentId, String processInstanceId){
        AttachmentDbo attachmentDbo = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_NOT_FOUND));
        attachmentDbo.setProcessInstanceId(processInstanceId);
        return toAttachmentDTO(attachmentRepo.saveAndFlush(attachmentDbo));
    }

    public AttachmentDTO modifyAttachmentFile(AttachmentDTO attachment, MultipartFile file){
        //文件覆盖
        File attachmentFile = new File(attachment.getAttachmentPath());
        if (!attachmentFile.exists()) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_NOT_FOUND);
        }
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(file.getInputStream(), attachmentFile);
        } catch (IOException e) {
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_UPLOAD_AGAIN_ERROR);
        }

        AttachmentDbo attachmentDbo = attachmentRepo.findById(attachment.getAttachmentId())
                .orElseThrow(() -> new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.FILE_NOT_FOUND));
        attachmentDbo.setAttachmentName(file.getOriginalFilename());
        return toAttachmentDTO(attachmentRepo.saveAndFlush(attachmentDbo));
    }

    public List<AttachmentDTO> listAttachment(){
        return toAttachmentDTOList(attachmentRepo.findAll());
    }

    public List<AttachmentDTO> listAttachmentByProcessInstanceId(String processInstanceId){
        return toAttachmentDTOList(attachmentRepo.findAllByProcessInstanceId(processInstanceId));
    }

    public void deleteAllAttachment(){
        attachmentRepo.deleteAll();
    }

    public List<AttachmentDTO> toAttachmentDTOList(List<AttachmentDbo> attachmentDbos){
        return attachmentDbos.stream().map(attachmentDbo -> new AttachmentDTO(
                attachmentDbo.getId()
                , attachmentDbo.getAttachmentName()
                , attachmentDbo.getCreatedBy()
                , attachmentDbo.getProcessInstanceId()
                , attachmentDbo.getAttachmentPath()))
                .collect(Collectors.toList());
    }

    public AttachmentDTO toAttachmentDTO(AttachmentDbo attachmentDbo){
        return new AttachmentDTO(
                attachmentDbo.getId()
                , attachmentDbo.getAttachmentName()
                , attachmentDbo.getCreatedBy()
                , attachmentDbo.getProcessInstanceId()
                , attachmentDbo.getAttachmentPath());
    }
}
