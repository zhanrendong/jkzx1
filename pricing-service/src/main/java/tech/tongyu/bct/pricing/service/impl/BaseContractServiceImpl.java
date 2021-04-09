package tech.tongyu.bct.pricing.service.impl;

import io.vavr.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.market.dto.*;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.pricing.convert.BctTradeToQuantModel;
import tech.tongyu.bct.pricing.dao.dbo.BaseContract;
import tech.tongyu.bct.pricing.dao.repo.intel.BaseContractRepo;
import tech.tongyu.bct.pricing.dto.BaseContractDTO;
import tech.tongyu.bct.pricing.service.BaseContractService;
import tech.tongyu.bct.quant.library.priceable.Position;
import tech.tongyu.bct.quant.library.priceable.Priceable;
import tech.tongyu.bct.quant.library.priceable.equity.EquityIndex;
import tech.tongyu.bct.quant.library.priceable.feature.HasExpiry;
import tech.tongyu.bct.quant.library.priceable.feature.HasUnderlyer;
import tech.tongyu.bct.trade.service.TradeService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BaseContractServiceImpl implements BaseContractService {
    private MarketDataService marketDataService;
    private TradeService tradeService;
    private BaseContractRepo baseContractRepo;

    @Autowired
    public BaseContractServiceImpl(MarketDataService marketDataService,
                                   TradeService tradeService, BaseContractRepo baseContractRepo) {
        this.marketDataService = marketDataService;
        this.tradeService = tradeService;
        this.baseContractRepo = baseContractRepo;
    }

    private Optional<BaseContract> findOne(String positionId, LocalDate valuationDate) {
        List<BaseContract> baseContracts = baseContractRepo
                .findByPositionIdOrderByBaseContractValidStartDesc(positionId);
        for (BaseContract b : baseContracts) {
            if (!Objects.isNull(b) && !b.getBaseContractValidStart().isAfter(valuationDate))
                return Optional.of(b);
        }
        return Optional.empty();
    }

    private BaseContractDTO convert(BaseContract baseContract) {
        return new BaseContractDTO(baseContract.getUuid(), baseContract.getPositionId(),
                baseContract.getInstrumentId(), baseContract.getExpiry(),
                baseContract.getBaseContractId(), baseContract.getBaseContractMaturity(),
                baseContract.getBaseContractValidStart(), baseContract.getHedgingContractId());
    }

    @Override
    public Optional<BaseContractDTO> find(String positionId, LocalDate valuationDate) {
        return findOne(positionId, valuationDate)
                .map(this::convert);
    }

    /*@Override
    @Transactional
    public BaseContractDTO save(String positionId, LocalDate expiry, String baseContractId,
                                String hedgingCotnractId, LocalDate baseContractValidStart) {
        // check position's underlyer and expiry
        Optional<Position> found = mockTradeService.getPosition(positionId);
        if (!found.isPresent()) {
            throw new CustomException(ErrorCode.MISSING_ENTITY, String.format("Position %s 不存在", positionId));
        }
        Priceable asset = found.get().getPriceable();
        if (!(asset instanceof HasUnderlyer)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "基础合约只对期权产品有效。输入为线性产品。");
        }
        Priceable underlyer = ((HasUnderlyer) asset).getUnderlyer();
        if (!(underlyer instanceof EquityIndexFutures)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "基础合约只对期权产品有效，且期权标的物必须为股指。");
        }

        if (!(asset instanceof HasExpiry)) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "基础合约只对期权产品有效，且期权标的物必须为股指。");
        }
        String instrumentId = ((EquityIndexFutures)underlyer).getInstrumentId();
        LocalDate expiry = ((HasExpiry) asset).getExpirationDate();

        // check if base contract is valid
        Optional<InstrumentDTO> baseContractInstrument = marketDataService.getInstrument(baseContractId);
        if (!baseContractInstrument.isPresent())
            throw new CustomException(ErrorCode.MISSING_ENTITY, "基础合约不存在：代码" + baseContractId);
        if (baseContractInstrument.get().getAssetClass() != AssetClassEnum.EQUITY
                || baseContractInstrument.get().getInstrumentType() != InstrumentTypeEnum.INDEX_FUTURES)
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "基础合约必须为股指期货");
        LocalDate maturity = ((EquityIndexFuturesInfo)baseContractInstrument.get().getInstrumentInfo()).getMaturity();

        BaseContract baseContract =  new BaseContract(positionId, instrumentId, expiry,
                baseContractId, maturity, baseContractValidStart, hedgingCotnractId);
        Optional<BaseContract> existing = findOne(positionId, baseContractValidStart);
        existing.ifPresent(b -> baseContract.setUuid(b.getUuid()));
        BaseContract saved = baseContractRepo.save(baseContract);
        return convert(saved);
    }*/

    @Override
    public List<BaseContractDTO> createOrUpdate(List<BaseContractDTO> baseContractDTOS, LocalDate valuationDate) {
        if (CollectionUtils.isNotEmpty(baseContractDTOS)){
            BaseContractDTO contract = baseContractDTOS.stream().findAny()
                    .orElseThrow(() -> new CustomException("请录入基础合约信息"));
            checkInstrumentStatus(contract.getBaseContractId());
            checkInstrumentStatus(contract.getHedgingContractId());
        }
        List<BaseContract> toSave = new ArrayList<>();
        // three types:
        //   new position id: this is error since all ops must come after list() which will include all legit positions
        //   update -> position id exists -> base contract also exists -> update valid start
        //                                -> different base contract
        List<String> positionIds = baseContractDTOS.stream()
                .map(BaseContractDTO::getPositionId)
                .distinct()
                .collect(Collectors.toList());
        Map<String, BaseContractDTO> lookupTable = buildLookupTable(positionIds, valuationDate);
        // new positions (not yet in db) -> create new base contracts
        List<BaseContractDTO> toCreate = baseContractDTOS.stream()
                .filter(b -> !lookupTable.containsKey(b.getPositionId()))
                .collect(Collectors.toList());
        /*if (toCreate.size() > 0) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("position %s 不在可以设定基础和约的列表中。请从prcBaseContractsList结果中选取。",
                            toCreate.get(0).getPositionId()));
        }*/
        // to update: partition those already in db into:
        //    1. different base contract ids (change base contract)
        //    2. same contract id -> user wants to update valid start or change hedging contract
        Map<Boolean, List<BaseContractDTO>> grouped = baseContractDTOS.stream()
                .filter(b -> lookupTable.containsKey(b.getPositionId()))  // already in db
                .collect(Collectors.partitioningBy(b -> {
                    String positionId = b.getPositionId();
                    BaseContractDTO existed = lookupTable.get(positionId);
                    return existed.getBaseContractId().equals(b.getBaseContractId());
                }));
        List<BaseContractDTO> newBaseContracts = grouped.get(false);
        List<BaseContractDTO> newValidStart = grouped.get(true);

        // get valid base contracts
        //   includes: 1. those not yet in db 2. those will be changed
        List<String> newBaseContractIds = Stream.concat(toCreate.stream().map(BaseContractDTO::getBaseContractId),
                newBaseContracts.stream().map(BaseContractDTO::getBaseContractId))
                .distinct()
                .collect(Collectors.toList());

        Map<String, InstrumentDTO> baseContractMap = marketDataService
                .listInstruments(newBaseContractIds, null).stream()
                .filter(i -> i.getInstrumentType() == InstrumentTypeEnum.INDEX_FUTURES
                        && i.getAssetClass() == AssetClassEnum.EQUITY)
                .collect(Collectors.toMap(
                        InstrumentDTO::getInstrumentId,
                        i -> i
                ));
        // new ones (not yet in db)
        for (BaseContractDTO bdto : toCreate) {
            String baseContractId = bdto.getBaseContractId();
            if (!baseContractMap.containsKey(baseContractId)) {
                continue;
            }
            String positionId = bdto.getPositionId();
            String instrumentId = bdto.getInstrumentId();
            LocalDate expiry = bdto.getExpiry();

            if (Objects.isNull(expiry)) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("基础合约：未设置position %s 到期日", positionId));
            }

            InstrumentDTO baseContractInstrument = baseContractMap.get(baseContractId);
            LocalDate maturity = ((EquityIndexFuturesInfo)baseContractInstrument.getInstrumentInfo()).getMaturity();
            LocalDate validStart = Objects.isNull(bdto.getBaseContractValidStart()) ?
                    valuationDate : bdto.getBaseContractValidStart();
            String hedgingContractId = Objects.isNull(bdto.getHedgingContractId()) ?
                    baseContractId : bdto.getHedgingContractId();
            BaseContract baseContract =  new BaseContract(positionId, instrumentId, expiry,
                    baseContractId, maturity, validStart, hedgingContractId);
            toSave.add(baseContract);
        }

        // update base contract, but not position info
        for (BaseContractDTO bdto : newBaseContracts) {
            String positionId = bdto.getPositionId();
            BaseContractDTO existing = lookupTable.get(positionId);
            String baseContractId = bdto.getBaseContractId();
            if (!baseContractMap.containsKey(baseContractId)) {
                continue;
            }
            InstrumentDTO baseContractInstrument = baseContractMap.get(baseContractId);
            LocalDate maturity = ((EquityIndexFuturesInfo)baseContractInstrument.getInstrumentInfo()).getMaturity();
            LocalDate validStart = Objects.isNull(bdto.getBaseContractValidStart()) ?
                    valuationDate : bdto.getBaseContractValidStart();
            String hedgingContractId = Objects.isNull(bdto.getHedgingContractId()) ?
                    baseContractId : bdto.getHedgingContractId();
            BaseContract baseContract =  new BaseContract(positionId, existing.getInstrumentId(),
                    existing.getExpiry(), baseContractId, maturity, validStart, hedgingContractId);
            baseContract.setUuid(lookupTable.get(positionId).getUuid());
            toSave.add(baseContract);
        }

        // position exits, base contract exists => only update hedging contract and valid start
        for (BaseContractDTO bdto : newValidStart) {
            String positionId = bdto.getPositionId();
            BaseContractDTO existing = lookupTable.get(positionId);

            if (Objects.isNull(bdto.getBaseContractValidStart()) && Objects.isNull(bdto.getHedgingContractId())) {
                continue; // nothing to update
            }

            LocalDate validStart = Objects.isNull(bdto.getBaseContractValidStart()) ?
                    existing.getBaseContractValidStart() : bdto.getBaseContractValidStart();
            String hedgingContractId = Objects.isNull(bdto.getHedgingContractId()) ?
                    existing.getBaseContractId() : bdto.getHedgingContractId();
            BaseContract baseContract =  new BaseContract(positionId, existing.getInstrumentId(),
                    existing.getExpiry(), existing.getBaseContractId(),
                    existing.getBaseContractMaturity(), validStart, hedgingContractId);
            baseContract.setUuid(lookupTable.get(positionId).getUuid());
            toSave.add(baseContract);
        }
        toSave.forEach(contract -> baseContractRepo.findByPositionIdAndBaseContractId
                (contract.getPositionId(), contract.getBaseContractId())
                .ifPresent(baseContract -> contract.setUuid(baseContract.getUuid())));
        return baseContractRepo.saveAll(toSave).stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    // returning a map from position id to base contract
    // the map contains all configured positions in the db
    public Map<String, BaseContractDTO> buildLookupTable(List<String> positionIds, LocalDate valuationDate) {
        List<BaseContract> baseContracts;
        if (Objects.isNull(positionIds) || positionIds.size() == 0) {
            baseContracts = baseContractRepo.findAll(valuationDate);
        } else {
            baseContracts = baseContractRepo.findByPositionIds(positionIds, valuationDate);
        }
        Map<String, BaseContractDTO> lookupTable = new HashMap<>();
        for (BaseContract b : baseContracts) {
            String pid = b.getPositionId();
            BaseContractDTO dto = convert(b);
            if (lookupTable.containsKey(pid)) {
                if (dto.getBaseContractValidStart().isAfter(b.getBaseContractValidStart()))
                    lookupTable.put(pid, dto);
            } else {
                lookupTable.put(pid, dto);
            }
        }
        return lookupTable;
    }

    @Override
    public List<BaseContractDTO> list(LocalDate valuationDate) {
        // find all positions that require base contracts
        List<Position> indexOptions = tradeService.findAll().stream()
                .map(BctTradeToQuantModel::from)
                .map(Tuple2::_2)
                .flatMap(Collection::stream)
                .filter(p -> p.getPriceable() instanceof HasUnderlyer
                        && p.getPriceable() instanceof HasExpiry
                        && ((HasUnderlyer) p.getPriceable()).getUnderlyer() instanceof EquityIndex)
                .collect(Collectors.toList());
        Map<String, BaseContractDTO> lookupTable = buildLookupTable(
                indexOptions.stream().map(Position::getPositionId).distinct().collect(Collectors.toList()),
                valuationDate);
        return indexOptions.stream()
                .map(i -> {
                    if (lookupTable.containsKey(i.getPositionId())) {
                        return lookupTable.get(i.getPositionId());
                    } else {
                        Priceable underlyer = ((HasUnderlyer)(i.getPriceable())).getUnderlyer();
                        String instrumentId = ((EquityIndex) underlyer).getInstrumentId();
                        LocalDate expiry = ((HasExpiry)(i.getPriceable())).getExpirationDate();
                        return new BaseContractDTO(null, i.getPositionId(), instrumentId, expiry,
                                null, null, null, null);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BaseContractDTO> deleteByPositionId(String positionId) {
        return baseContractRepo.deleteByPositionId(positionId).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private void checkInstrumentStatus(String instrumentId){
        LocalDate nowDate = LocalDate.now();
        InstrumentDTO baseInstrument = marketDataService.getInstrument(instrumentId)
                .orElseThrow(() -> new CustomException(String.format("标的物信息不存在,%s", instrumentId)));
        InstrumentInfoDTO baseInstrumentInfo = baseInstrument.toInstrumentInfoDTO();
        if (baseInstrumentInfo.getMaturity().isBefore(nowDate)){
            throw new CustomException(String.format("标的物:%s,已经过期,过期日期:%s",
                    instrumentId, baseInstrumentInfo.getMaturity().toString()));
        }
    }
}
