package tech.tongyu.bct.trade.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.cm.product.asset.Asset;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.product.iov.feature.EffectiveDateFeature;
import tech.tongyu.bct.cm.product.iov.feature.OptionExerciseFeature;
import tech.tongyu.bct.cm.product.iov.feature.UnderlyerFeature;
import tech.tongyu.bct.cm.product.iov.impl.SpreadsInstrument;
import tech.tongyu.bct.cm.reference.elemental.Account;
import tech.tongyu.bct.cm.reference.elemental.Party;
import tech.tongyu.bct.cm.reference.impl.SimpleAccount;
import tech.tongyu.bct.cm.reference.impl.SimpleParty;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.trade.dao.dbo.TradePositionIndex;
import tech.tongyu.bct.trade.dao.repo.TradePositionIndexRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.manager.PositionManager;
import tech.tongyu.bct.trade.service.AssetTransformer;
import tech.tongyu.bct.trade.service.PositionService;
import tech.tongyu.bct.trade.service.impl.lcm.OpenOptionPositionProcessor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PositionServiceImpl implements PositionService {

    private PositionManager positionManager;

    private List<AssetTransformer> assetTransformers;

    private TradePositionIndexRepo tradePositionIndexRepo;

    private OpenOptionPositionProcessor openOptionProcessor;

    @Autowired
    public PositionServiceImpl(PositionManager positionManager,
                               List<AssetTransformer> assetTransformers,
                               TradePositionIndexRepo tradePositionIndexRepo,
                               OpenOptionPositionProcessor openOptionProcessor) {
        this.positionManager = positionManager;
        this.assetTransformers = assetTransformers;
        this.openOptionProcessor = openOptionProcessor;
        this.tradePositionIndexRepo = tradePositionIndexRepo;
    }

    @Override
    @Transactional
    public void createPositionIndex(TradeDTO tradeDto, TradePositionDTO positionDto) {
        String positionId = positionDto.getPositionId();
        if (tradePositionIndexRepo.existsByPositionId(positionId)) {
            TradePositionIndex index = tradePositionIndexRepo.findByPositionId(positionId)
                    .orElseThrow(() -> new CustomException(String.format("持仓编号[%s],持仓索引不存在", positionId)));
            BctTradePosition bctPosition = transToBctPosition(positionDto);
            InstrumentOfValue instrument = bctPosition.getAsset().instrumentOfValue();
            if (instrument instanceof EffectiveDateFeature) {
                LocalDate effectiveDate = ((EffectiveDateFeature) instrument).effectiveDate();
                index.setEffectiveDate(effectiveDate);
            }
            tradePositionIndexRepo.save(index);
            return;
        }
        createTradePositionIndex(tradeDto, positionDto);
    }

    @Override
    @Transactional
    public void createPosition(TradeDTO tradeDto, TradePositionDTO positionDto, LCMEventDTO eventDto) {
        BctTradePosition position = transToBctPosition(positionDto);
        position.setLcmEventType(LCMEventTypeEnum.OPEN);
        eventDto.setLcmEventType(LCMEventTypeEnum.OPEN);
        eventDto.setPositionId(positionDto.getPositionId());
        if (LCMEventTypeEnum.KNOCK_IN.equals(positionDto.getLcmEventType())) {
            position.setLcmEventType(LCMEventTypeEnum.KNOCK_IN);
            eventDto.setLcmEventType(LCMEventTypeEnum.KNOCK_IN);
        }

        positionManager.upsert(position);
//        openOptionProcessor.process(null, position, eventDto);
        positionDto.setLcmEventType(LCMEventTypeEnum.OPEN);
        createTradePositionIndex(tradeDto, positionDto);
    }

    private void createTradePositionIndex(TradeDTO tradeDto, TradePositionDTO positionDto) {
        TradePositionIndex tradePositionIndex = new TradePositionIndex();
        BeanUtils.copyProperties(positionDto, tradePositionIndex);
        BeanUtils.copyProperties(tradeDto, tradePositionIndex);

        BctTradePosition bctPosition = transToBctPosition(positionDto);
        InstrumentOfValue instrument = bctPosition.getAsset().instrumentOfValue();
        if (instrument instanceof EffectiveDateFeature) {
            LocalDate effectiveDate = ((EffectiveDateFeature) instrument).effectiveDate();
            tradePositionIndex.setEffectiveDate(effectiveDate);
        }
        if (instrument instanceof OptionExerciseFeature) {
            tradePositionIndex.setExpirationDate(((OptionExerciseFeature) instrument).absoluteExpirationDate());
        }
        if (instrument instanceof UnderlyerFeature) {
            String instrumentId = ((UnderlyerFeature) instrument).underlyer().instrumentId();
            if (ProductTypeEnum.SPREAD_EUROPEAN == positionDto.getProductType() ||
                    ProductTypeEnum.RATIO_SPREAD_EUROPEAN == positionDto.getProductType()) {
                String instrumentIds[] = instrumentId.split(SpreadsInstrument.BASKET_ID_JOINER);
                tradePositionIndex.setInstrumentId(instrumentIds[0]);
                tradePositionIndexRepo.save(tradePositionIndex);
                tradePositionIndex.setInstrumentId(instrumentIds[1]);
                tradePositionIndexRepo.save(tradePositionIndex);
                return;
            }
            tradePositionIndex.setInstrumentId(instrumentId);
        }
        tradePositionIndexRepo.save(tradePositionIndex);
    }

    @Override
    public BctTradePosition fromDTO(TradePositionDTO positionDTO) {
        return transToBctPosition(positionDTO);
    }


    @Override
    public TradePositionDTO toDTO(BctTradePosition positionDTO) {
        return transToPositionDTO(positionDTO);
    }

    @Override
    public TradePositionDTO getByPositionId(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(positionId)) {
            throw new IllegalArgumentException(String.format("持仓ID%s不存在", positionId));
        }
        return positionManager.getByPositionId(positionId)
                .map(this::toDTO)
                .orElseThrow(() -> new IllegalArgumentException(String.format("持仓ID%s:数据不存在", positionId)));
    }

    @Override
    public List<TradePositionDTO> getByPositionIds(List<String> positionIds, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (CollectionUtils.isEmpty(positionIds)) {
            throw new IllegalArgumentException("持仓ID不存在");
        }
        return positionManager.getByPositionIds(positionIds)
                .map(bctTradePositions ->
                        bctTradePositions.stream().map(this::toDTO).collect(Collectors.toList())
                )
                .orElseThrow(() -> new IllegalArgumentException("数据不存在"));
    }

    @Override
    public BctTradePosition getBctPositionById(String positionId, OffsetDateTime validTime, OffsetDateTime transactionTime) {
        if (StringUtils.isBlank(positionId)) {
            throw new IllegalArgumentException(String.format("持仓ID%s不存在", positionId));
        }
        return positionManager.getByPositionId(positionId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("持仓ID%s:数据不存在", positionId)));
    }

    @Override
    public Boolean isPositionExpired(List<String> positionIds, LocalDate date) {
        return positionIds.stream()
                .anyMatch(pid -> {
                    BctTradePosition bctPosition = positionManager.getByPositionId(pid).get();
                    InstrumentOfValue iov = bctPosition.asset.instrumentOfValue();
                    if (iov instanceof OptionExerciseFeature) {
                        LocalDate unadjustedExpirationDate = ((OptionExerciseFeature) iov).absoluteExpirationDate();
                        return unadjustedExpirationDate.isEqual(date);
                    } else {
                        return false;
                    }
                });
    }

    @Override
    public Boolean isCounterPartyEquals(List<String> positionIds, String counterPartyCode) {
        for (String positionId : positionIds) {
            BctTradePosition bctPosition = getBctPositionById(positionId);
            Party counterParty = bctPosition.getCounterparty();
            if (Objects.nonNull(counterParty)) {
                if (counterPartyCode.equals(counterParty.partyCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    private BctTradePosition transToBctPosition(TradePositionDTO positionDto) {
        BctTradePosition bctPosition = new BctTradePosition();
        BeanUtils.copyProperties(positionDto, bctPosition);
        //TODO TODO (http://jira.tech.tongyu/OTMS-XXXX):所有的party和account,暂时不做处理
        Party counterParty = new SimpleParty(UUID.randomUUID(), positionDto.getCounterPartyCode(), positionDto.getCounterPartyName());
        SimpleAccount positionAccount = new SimpleAccount(UUID.randomUUID(), positionDto.getPositionAccountCode(), positionDto.getPositionAccountName());
        SimpleAccount counterPartyAccount = new SimpleAccount(UUID.randomUUID(), positionDto.getCounterPartyAccountCode(), positionDto.getPositionAccountName());
        Asset<InstrumentOfValue> asset = convertAsset(positionDto);
        bctPosition.setAsset(asset);
        bctPosition.setCounterparty(counterParty);
        bctPosition.setPositionAccount(positionAccount);
        bctPosition.setCounterpartyAccount(counterPartyAccount);

        return bctPosition;
    }


    private TradePositionDTO transToPositionDTO(BctTradePosition bctPosition) {
        TradePositionDTO positionDTO = new TradePositionDTO();
        BeanUtils.copyProperties(bctPosition, positionDTO);
        Party counterParty = bctPosition.getCounterparty();
        if (counterParty != null) {
            positionDTO.setCounterPartyCode(counterParty.partyCode());
            positionDTO.setCounterPartyName(counterParty.partyName());
        }
        Account positionAccount = bctPosition.getPositionAccount();
        if (positionAccount != null) {
            positionDTO.setPositionAccountCode(positionAccount.accountCode());
            positionDTO.setPositionAccountName(positionAccount.accountName());
        }
        Account counterPartyAccount = bctPosition.getCounterpartyAccount();
        if (counterPartyAccount != null) {
            positionDTO.setCounterPartyAccountCode(counterPartyAccount.accountCode());
            positionDTO.setCounterPartyAccountName(counterPartyAccount.accountName());
        }
        Asset<InstrumentOfValue> asset = bctPosition.getAsset();
        if (asset != null) {
            Object assetObj = assetTransformers.stream()
                    .flatMap(transformer -> transformer.transform(bctPosition).map(Stream::of).orElse(Stream.empty()))
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException(String.format("无法找到（%s）的解析器", positionDTO.productType)));
            positionDTO.setAsset(JsonUtils.mapper.valueToTree(assetObj));
            InstrumentOfValue instrument = asset.instrumentOfValue();
            positionDTO.setProductType(instrument.productType());
            positionDTO.setAssetClass(instrument.assetClassType());
        }
        return positionDTO;
    }

    private Asset<InstrumentOfValue> convertAsset(TradePositionDTO positionDTO) {

        return assetTransformers.stream()
                .flatMap(transformer -> transformer.transform(positionDTO).map(Stream::of).orElse(Stream.empty()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format("无法找到（%s）的解析器", positionDTO.productType)));
    }
}
