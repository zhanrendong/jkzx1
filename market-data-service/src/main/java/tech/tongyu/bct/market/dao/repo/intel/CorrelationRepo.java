package tech.tongyu.bct.market.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.market.dao.dbo.Correlation;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CorrelationRepo extends JpaRepository<Correlation, UUID> {
    Optional<Correlation> findByInstrumentId1AndInstrumentId2(String instrumentId1, String instrumentId2);
    @Modifying
    @Transactional
    List<Correlation> deleteByInstrumentId1AndInstrumentId2(String instrumentId1, String instrumentId2);
}
