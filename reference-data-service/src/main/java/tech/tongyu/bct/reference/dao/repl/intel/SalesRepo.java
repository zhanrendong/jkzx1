package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.reference.dao.dbo.Sales;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesRepo extends JpaRepository<Sales, UUID> {
    @Query("select s.uuid as salesId, b.uuid as branchId, sub.uuid as subsidiaryId," +
            " s.salesName, b.branchName, sub.subsidiaryName, s.createdAt as saleCreatedTime " +
            "from Subsidiary sub, Branch b, Sales s " +
            "where sub.uuid = b.subsidiaryId and b.uuid = s.branchId")
    List<Tuple> listAll();

    boolean existsBySalesName(String salesName);
    boolean existsByBranchId(UUID branchId);

    List<Sales> findByBranchId(UUID branchId);

    List<Sales> findBySalesNameContaining(String similarSalesName);

    Optional<Sales> findByBranchIdAndSalesName(UUID branchId, String salesName);

    @Modifying
    @Transactional
    List<Sales> deleteByUuid(UUID uuid);
}
