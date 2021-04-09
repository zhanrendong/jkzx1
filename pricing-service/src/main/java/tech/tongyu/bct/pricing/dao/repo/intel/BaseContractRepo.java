package tech.tongyu.bct.pricing.dao.repo.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.pricing.dao.dbo.BaseContract;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BaseContractRepo extends JpaRepository<BaseContract, UUID> {
    List<BaseContract> findByPositionIdOrderByBaseContractValidStartDesc(String positionId);

    @Query(value = "select b from BaseContract b where " +
            "b.positionId in :positionIds and " +
            "b.baseContractMaturity > :valuationDate and " +
            "b.baseContractValidStart <= :valuationDate")
    List<BaseContract> findByPositionIds(List<String> positionIds, LocalDate valuationDate);

    @Query(value = "select b from BaseContract b where " +
            "b.baseContractMaturity > :valuationDate and " +
            "b.baseContractValidStart <= :valuationDate")
    List<BaseContract> findAll(LocalDate valuationDate);

    @Modifying
    @Transactional
    List<BaseContract> deleteByPositionId(String positionId);

    Optional<BaseContract> findByPositionIdAndBaseContractId(String positionId, String baseContractId);
}
