package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.trade.dao.dbo.QuotePosition;

import java.util.List;
import java.util.UUID;

public interface QuotePositionRepo extends JpaRepository<QuotePosition, UUID> {

    void deleteByQuotePrcId(String quotePrcId);

    Boolean existsByQuotePrcId(String quotePrcId);

    List<QuotePosition> findByQuotePrcIdIn(List<String> quotePrcIds);

}
