package tech.tongyu.bct.reference.dao.repl.intel;


import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.FundEventRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FundEventRecordRepo extends JpaRepository<FundEventRecord, UUID> {

    Long countByClientIdAndPaymentDate(String clientId, LocalDate paymentDate);

    List<FundEventRecord> findAllByClientIdOrderByUpdatedAtDesc(String clientId);

}
