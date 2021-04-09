package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.document.dao.dbo.PositionDocument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionDocumentRepo extends JpaRepository<PositionDocument, UUID> {

    Optional<PositionDocument> findByPositionId(String positionId);

    List<PositionDocument> findByTradeId(String tradeId);

}
