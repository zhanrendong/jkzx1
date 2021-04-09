package tech.tongyu.bct.market.dao.repo.intel;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.market.dao.dbo.InstrumentWhitelist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstrumentWhitelistRepo extends JpaRepository<InstrumentWhitelist, UUID> {
    Optional<InstrumentWhitelist> findByInstrumentId(String instrumentId);

    List<InstrumentWhitelist> findAllByOrderByInstrumentIdAsc(Pageable p);

    List<InstrumentWhitelist> findByInstrumentIdInOrderByInstrumentIdAsc(List<String> instrumentIds, Pageable p);

    Long countByInstrumentIdIn(List<String> instrumentIds);

    List<InstrumentWhitelist> findByInstrumentIdStartingWith(String idParts);
}
