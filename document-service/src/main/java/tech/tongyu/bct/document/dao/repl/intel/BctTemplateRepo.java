package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.document.dao.dbo.BctTemplate;
import tech.tongyu.bct.document.ext.dto.CategoryEnum;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;

import java.util.List;
import java.util.UUID;

public interface BctTemplateRepo extends JpaRepository<BctTemplate, UUID> {

    List<BctTemplate> findByCategory(CategoryEnum category);

    BctTemplate findByDocType(DocTypeEnum docTypeEnum);

    BctTemplate findByDocTypeAndTransactType(DocTypeEnum docTypeEnum, String transactType);
}
