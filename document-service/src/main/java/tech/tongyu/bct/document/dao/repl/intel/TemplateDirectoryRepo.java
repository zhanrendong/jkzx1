package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.document.dao.dbo.TemplateDirectory;

import java.util.List;
import java.util.UUID;

public interface TemplateDirectoryRepo extends JpaRepository<TemplateDirectory, UUID> {

    @Query("SELECT d FROM TemplateDirectory d JOIN d.tags t WHERE t IN (:tags)")
    List<TemplateDirectory> findDirectoriesByTags(List<String> tags);
}
