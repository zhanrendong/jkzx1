package tech.tongyu.bct.trade.dao.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.trade.dao.dbo.TradePositionIndex;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradePositionIndexRepo extends JpaRepository<TradePositionIndex, UUID> {

    void deleteByTradeId(String tradeId);

    Boolean existsByPositionId(String positionId);

    Optional<TradePositionIndex> findByPositionId(String positionId);

    List<TradePositionIndex> findByCounterPartyName(String counterPartyName);

    List<TradePositionIndex> findByProductTypeInAndLcmEventTypeNotIn(List<ProductTypeEnum> productTypes,
                                                                     List<LCMEventTypeEnum> lcmEventTypes);

}
