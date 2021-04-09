package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.tongyu.bct.document.dao.dbo.TradeDocument;
import tech.tongyu.bct.document.dto.DocProcessStatusEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradeDocumentRepo extends JpaRepository<TradeDocument, UUID> {

    Optional<TradeDocument> findByTradeId(String tradeId);

    @Query(value = "select t from TradeDocument t where t.tradeId=:tradeId and t.bookName=:bookName " +
            "and t.partyName=:partyName and t.docProcessStatus=:docProcessStatus " +
            "and t.tradeDate>=:startDate and t.tradeDate<=:endDate")
    List<TradeDocument> search(String tradeId, String bookName, String partyName, DocProcessStatusEnum docProcessStatus,
                               LocalDate startDate, LocalDate endDate);
}
