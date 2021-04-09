package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.document.dao.dbo.TemplateContent;

import java.util.UUID;

public interface TemplateContentRepo  extends JpaRepository<TemplateContent, UUID> {
}
