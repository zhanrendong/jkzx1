package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public interface PositionService {

    default TradePositionDTO getByPositionId(String positionId) {
        return getByPositionId(positionId, null, null);
    }

    default BctTradePosition getBctPositionById(String positionId) {
        return getBctPositionById(positionId, null, null);
    }

    default List<TradePositionDTO> getByPositionIds(List<String> positionIds) {
        return getByPositionIds(positionIds, null, null);
    }

    void createPositionIndex(TradeDTO tradeDto, TradePositionDTO positionDto);

    void createPosition(TradeDTO tradeDto, TradePositionDTO positionDto, LCMEventDTO eventDto);

    TradePositionDTO toDTO(BctTradePosition positionDTO);

    BctTradePosition fromDTO(TradePositionDTO positionDTO);

    Boolean isPositionExpired(List<String> positionIds, LocalDate date);

    Boolean isCounterPartyEquals(List<String> positionIds, String counterPartyCode);

    TradePositionDTO getByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    BctTradePosition getBctPositionById(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime);

    List<TradePositionDTO> getByPositionIds(List<String> positionIds, OffsetDateTime validTime, OffsetDateTime transactionTime);
}
