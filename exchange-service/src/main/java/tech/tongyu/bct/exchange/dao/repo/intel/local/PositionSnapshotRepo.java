package tech.tongyu.bct.exchange.dao.repo.intel.local;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.exchange.dao.dbo.local.PositionSnapshot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionSnapshotRepo extends JpaRepository<PositionSnapshot, UUID> {

//    select p from PositionSnapshot p where p.bookId=:bookId
    List<PositionSnapshot> findByBookId(String bookId);

    List<PositionSnapshot> findByInstrumentId(String instrumentId);

//    select p from PositionSnapshot p where p.instrumentId=:instrumentId and p.bookId=:bookId
    Optional<PositionSnapshot> findByBookIdAndInstrumentId(String bookId, String instrumentId);

//    select p from PositionSnapshot p where p.dealDate=:dealDate and p.bookId=:bookId and p.instrumentId=:instrumentId

//    select p from PositionSnapshot p where p.bookId=:bookId and p.dealDate<=:dealDate

//    select p from PositionSnapshot p where p.dealDate=:dealDate and p.bookId=:bookId


}
