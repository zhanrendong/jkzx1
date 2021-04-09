package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.document.dao.dbo.DictionaryEntry;

import java.util.List;
import java.util.UUID;

public interface DictionaryEntryRepo  extends JpaRepository<DictionaryEntry, UUID> {

    @Query("SELECT d FROM DictionaryEntry d JOIN d.tags t WHERE t IN (:tags)")
    List<DictionaryEntry> findDictionaryEntriesByTags(List<String> tags);

    List<DictionaryEntry> findByDicGroup(String dicGroup);
}
