package tech.tongyu.bct.market.dao.repo.intel;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.market.dao.dbo.Instrument;
import tech.tongyu.bct.market.dto.AssetClassEnum;
import tech.tongyu.bct.market.dto.InstrumentTypeEnum;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstrumentRepo extends JpaRepository<Instrument, UUID> {
    Optional<Instrument> findByInstrumentId(String instrumentId);

    List<Instrument> findAllByOrderByInstrumentIdAsc(Pageable p);

    List<Instrument> findByInstrumentIdStartingWithAndAssetClassInOrderByInstrumentIdAsc(String idParts, List<AssetClassEnum> assetClassEnums);

    // for paged queries only
    // mainly used by web
    List<Instrument> findByInstrumentIdInOrderByInstrumentIdAsc(List<String> instrumentIds, Pageable p);

    long countByInstrumentIdIn(List<String> instrumentIds);

    List<Instrument> findByAssetClassAndInstrumentType(AssetClassEnum assetClass,
                                                       InstrumentTypeEnum instrumentType, Pageable p);

    long countByAssetClassAndInstrumentType(AssetClassEnum assetClass, InstrumentTypeEnum instrumentType);

    @Modifying
    @Transactional
    List<Instrument> deleteByInstrumentId(String instrumentId);
}
