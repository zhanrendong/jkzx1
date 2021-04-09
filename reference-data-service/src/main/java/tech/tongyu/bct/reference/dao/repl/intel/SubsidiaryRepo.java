package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.reference.dao.dbo.Subsidiary;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubsidiaryRepo extends JpaRepository<Subsidiary, UUID> {
    boolean existsBySubsidiaryName(String subsidiaryName);
    @Modifying
    @Transactional
    List<Subsidiary> deleteByUuid(UUID uuid);
}
