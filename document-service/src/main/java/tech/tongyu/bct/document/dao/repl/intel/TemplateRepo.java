package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.document.dao.dbo.Template;

import java.util.UUID;

public interface TemplateRepo extends JpaRepository<Template, UUID> {
}
