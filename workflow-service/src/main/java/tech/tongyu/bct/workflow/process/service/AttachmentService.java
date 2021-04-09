package tech.tongyu.bct.workflow.process.service;

import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.workflow.dto.AttachmentDTO;

import java.util.Collection;
import java.util.List;

/**
 * @author yongbin
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface AttachmentService {

    /**
     * create attachment
     * @param file file
     * @return attachment dto
     */
    AttachmentDTO createAttachment(MultipartFile file);

    /**
     * modify attachment
     * @param attachmentId attachment's id
     * @param file file
     * @return attachment dto
     */
    AttachmentDTO modifyAttachment(String attachmentId, MultipartFile file);

    /**
     * delete attachment by attachment's id
     * @param attachmentId attachment's id
     */
    void deleteAttachmentByAttachmentId(String attachmentId);

    /**
     * delete attachment by process instance's id
     * @param processInstanceId process instance's id
     */
    void deleteAttachmentByProcessInstanceId(String processInstanceId);

    /**
     * list attachment
     * @return collection of attachment
     */
    Collection<AttachmentDTO> listAttachment();

    /**
     * list attachment
     * @param processInstanceId process instance's id
     * @return collection of attachment
     */
    Collection<AttachmentDTO> listAttachmentByProcessInstanceId(String processInstanceId);

    /**
     * modify attachment
     * @param attachmentId  attachment's id
     * @param processInstanceId process instance's id
     * @return attachment dto
     */
    AttachmentDTO modifyAttachment(String attachmentId, String processInstanceId);

    /**
     * get attachment by attachment id
     * @param attachmentId attachment's id
     * @return attachment dto
     */
    AttachmentDTO getAttachmentByAttachmentId(String attachmentId);

    /**
     * delete all attachment
     */
    void deleteAllAttachment();
}
