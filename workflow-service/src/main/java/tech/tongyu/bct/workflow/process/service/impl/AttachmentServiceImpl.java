package tech.tongyu.bct.workflow.process.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.util.FileUtils;
import tech.tongyu.bct.workflow.auth.AuthenticationService;
import tech.tongyu.bct.workflow.dto.AttachmentDTO;
import tech.tongyu.bct.workflow.process.manager.AttachmentManager;
import tech.tongyu.bct.workflow.process.service.AttachmentService;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author yongbin
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    private AttachmentManager attachmentManager;

    @Autowired
    public AttachmentServiceImpl(AttachmentManager attachmentManager) {
        this.attachmentManager = attachmentManager;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentDTO createAttachment(MultipartFile file) {
        AttachmentDTO attachment = attachmentManager.createAttachment(file.getOriginalFilename());
        return attachmentManager.createAttachmentFile(attachment.getAttachmentId(), file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentDTO modifyAttachment(String attachmentId, MultipartFile file) {
        AttachmentDTO attachment = attachmentManager.getAttachmentByAttachmentId(attachmentId);
        //FileUtils.delete(attachment.getAttachmentPath());

        return attachmentManager.modifyAttachmentFile(attachment, file);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachmentByAttachmentId(String attachmentId) {
        AttachmentDTO attachment = attachmentManager.getAttachmentByAttachmentId(attachmentId);
        FileUtils.delete(attachment.getAttachmentPath());
        attachmentManager.deleteAttachmentByAttachmentId(attachmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachmentByProcessInstanceId(String processInstanceId) {
        attachmentManager.listAttachmentByProcessInstanceId(processInstanceId)
                .forEach(attachment -> FileUtils.delete(attachment.getAttachmentPath()));
        attachmentManager.deleteAttachmentByProcessInstanceId(processInstanceId);
    }

    @Override
    public Collection<AttachmentDTO> listAttachment() {
        return attachmentManager.listAttachment();
    }

    @Override
    public Collection<AttachmentDTO> listAttachmentByProcessInstanceId(String processInstanceId) {
        return attachmentManager.listAttachmentByProcessInstanceId(processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentDTO modifyAttachment(String attachmentId, String processInstanceId) {
        return attachmentManager.modifyAttachmentByProcessInstanceId(attachmentId, processInstanceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public AttachmentDTO getAttachmentByAttachmentId(String attachmentId) {
        return attachmentManager.getAttachmentByAttachmentId(attachmentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllAttachment() {
        attachmentManager.deleteAllAttachment();
    }
}
