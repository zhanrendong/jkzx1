package tech.tongyu.bct.workflow.process.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.workflow.process.repo.entities.AttachmentDbo;

import java.util.List;

public interface AttachmentRepo extends JpaRepository<AttachmentDbo, String> {

    @Modifying
    @Transactional
    void deleteAttachmentDboById(String attachmentId);

    @Modifying
    @Transactional
    void deleteAllByProcessInstanceId(String processInstanceId);

    List<AttachmentDbo> findAllByProcessInstanceId(String processInstanceId);
}
