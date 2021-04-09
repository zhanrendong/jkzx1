package tech.tongyu.bct.document.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.TimeUtils;
import tech.tongyu.bct.document.dao.dbo.BctTemplate;
import tech.tongyu.bct.document.dao.dbo.PoiTemplate;
import tech.tongyu.bct.document.dao.repl.intel.BctTemplateRepo;
import tech.tongyu.bct.document.dao.repl.intel.PoiTemplateRepo;
import tech.tongyu.bct.document.dto.*;
import tech.tongyu.bct.document.ext.dto.BctTemplateDTO;
import tech.tongyu.bct.document.ext.dto.CategoryEnum;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.ext.service.BctDocumentService;
import tech.tongyu.bct.document.poi.PoiTemplateDTO;
import tech.tongyu.bct.document.poi.TradeTypeEnum;
import tech.tongyu.bct.document.service.DocumentService;
import tech.tongyu.bct.document.service.TradeDocumentService;
import tech.tongyu.bct.document.util.TransfromUtil;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.reference.service.MarginService;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.service.TradeLCMService;
import tech.tongyu.bct.trade.service.TradeService;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.document.service.impl.DocumentConstants.*;

@Service
public class BctDocumentServiceImpl implements BctDocumentService {

    private final BctTemplateRepo bctTemplateRepo;
    private PoiTemplateRepo poiTemplateRepo;

    private TradeService tradeService;
    private MarketDataService marketDataService;
    private TradeLCMService tradeLCMService;
    private TradeDocumentService tradeDocumentService;
    private DocumentService documentService;
    private AccountService accountService;
    private MarginService marginService;

    @Autowired
    public BctDocumentServiceImpl(BctTemplateRepo bctTemplateRepo,
                                  PoiTemplateRepo poiTemplateRepo,
                                  TradeService tradeService,
                                  MarketDataService marketDataService,
                                  TradeLCMService tradeLCMService,
                                  AccountService accountService,
                                  TradeDocumentService tradeDocumentService,
                                  DocumentService documentService,
                                  MarginService marginService) {
        this.bctTemplateRepo = bctTemplateRepo;
        this.poiTemplateRepo = poiTemplateRepo;
        this.tradeService = tradeService;
        this.marketDataService = marketDataService;
        this.tradeLCMService = tradeLCMService;
        this.accountService = accountService;
        this.tradeDocumentService = tradeDocumentService;
        this.documentService = documentService;
        this.marginService = marginService;
    }

    @Override
    public BctTemplateDTO docTemplateCreate(BctTemplateDTO dto) {
        return toDto(bctTemplateRepo.saveAndFlush(toDbo(dto)));
    }

    @Override
    public PoiTemplateDTO docPoiTemplateCreate(TradeTypeEnum tradeType, DocTypeEnum docType) {
        Optional<PoiTemplate> template = poiTemplateRepo.findByTradeTypeAndDocType(tradeType, docType);
        if(template.isPresent()){
            throw new CustomException("该模板已经存在");
        }
        PoiTemplate poiTemplate = new PoiTemplate();
        poiTemplate.setDocType(docType);
        poiTemplate.setTradeType(tradeType);
        return toPoiTemplateDTO(poiTemplateRepo.save(poiTemplate));
    }

    @Override
    public Optional<PoiTemplateDTO> getPoiTemplateDTO(UUID poiTemplateId) {
        return Optional.of(toPoiTemplateDTO(poiTemplateRepo.findById(poiTemplateId).get()));
    }

    @Override
    public PoiTemplateDTO updatePoiTemplateDTO(PoiTemplateDTO poiTemplateDTO) {
        return toPoiTemplateDTO(poiTemplateRepo.save(toPoiTemplateDbo(poiTemplateDTO)));
    }

    @Override
    public List<PoiTemplateDTO> docPoiTemplateList() {
        return poiTemplateRepo.findAll()
                .stream()
                .map(this::toPoiTemplateDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String deletePoiTemplate(UUID poiTemplateId) {
        Optional<PoiTemplate> poiTemplate = poiTemplateRepo.findById(poiTemplateId);
        if(poiTemplate.isPresent()){
            poiTemplate.get().setFileName("");
            poiTemplate.get().setTypeSuffix("");
            poiTemplate.get().setFilePath("");
            poiTemplateRepo.save(poiTemplate.get());
            return "SUCCESS";
        }else {
            throw new CustomException("没有找到该模板");
        }
    }

    @Override
    public List<BctTemplateDTO> docBctTemplateList(CategoryEnum category) {
        return bctTemplateRepo.findByCategory(category)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<BctTemplateDTO> getBctTemplateDTO(UUID bctTemplateId) {
        return bctTemplateRepo.findById(bctTemplateId).map(this::toDto);
    }

    @Override
    public BctTemplateDTO updateBctTemplateDTO(BctTemplateDTO dto) {
        return toDto(bctTemplateRepo.saveAndFlush(toDbo(dto)));
    }

    @Override
    public BctTemplateDTO getBctTemplateDTOByDocType(DocTypeEnum docTypeEnum){
        return toDto(bctTemplateRepo.findByDocType(docTypeEnum));
    }

    @Override
    public BctTemplateDTO getBctTemplateDTOByDocTypeAndTransactType(DocTypeEnum docTypeEnum, String transactType) {
        return toDto(bctTemplateRepo.findByDocTypeAndTransactType(docTypeEnum, transactType));
    }

    @Override
    public StringWriter getSettlement(String tradeId, String positionId) {
        OffsetDateTime dateTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);

        Optional<SettlementDTO> settlementDTO = tradeService.getByTradeId(tradeId, dateTime, dateTime).getPositions()
                .stream()
                .filter(t -> Objects.equals(positionId, t.getPositionId()))
                .map(p -> BctDocumentServiceImpl.getSettlementDTO(p, tradeId))
                .findFirst();

        String counterPartyName = tradeService.getByTradeId(tradeId, dateTime, dateTime).getPositions().get(0).getCounterPartyName();

        String instrumentName = marketDataService.getInstrument(
                settlementDTO.get().getUnderlyerInstrumentId()).get().toInstrumentInfoDTO().getName();


        Map<String, Object> settleData = new HashMap<>();
        settleData.put(PARTY_NAME, counterPartyName);

        List<SettlementDTO> settleLegs = tradeLCMService.listLCMEventsByTradeIdAndPositionId(tradeId, positionId).stream()
                .filter(t -> Objects.equals(t.getLcmEventType(), LCMEventTypeEnum.UNWIND_PARTIAL)
                        || Objects.equals(t.getLcmEventType(), LCMEventTypeEnum.UNWIND)
                        || Objects.equals(t.getLcmEventType(), LCMEventTypeEnum.EXERCISE)
                        || Objects.equals(t.getLcmEventType(), LCMEventTypeEnum.EXPIRATION)
                        || Objects.equals(t.getLcmEventType(), LCMEventTypeEnum.KNOCK_OUT)
                        || Objects.equals(t.getLcmEventType(), LCMEventTypeEnum.SNOW_BALL_EXERCISE))
                .map(s -> getSettlementDTO(s.lcmEventType, s, instrumentName, settlementDTO.get()))
                .collect(Collectors.toList());

        settleData.put(LEGS, settleLegs);

        JsonNode data = JsonUtils.mapper.valueToTree(settleData);

        try {
            tradeDocumentService.updatePositionDocStatus(positionId, DocProcessStatusEnum.DOWNLOADED);
        } catch (Exception e) {
            throw new RuntimeException("文档生成状态修改失败，" + e);
        }
        return getTemplate(DocTypeEnum.SETTLE_NOTIFICATION, data);
    }
    @Override
    public StringWriter getSupplementary(String tradeId, String description7, String description8) {

        OffsetDateTime dateTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        TradeDTO trade = tradeService.getByTradeId(tradeId, dateTime, dateTime);
        TradePositionDTO tradePositionDTO = trade.getPositions().get(0);

        String partyName = tradePositionDTO.getCounterPartyName();
        Timestamp expirationDate = TimeUtils.getTimestamp(tradePositionDTO.getAsset().get(EXPIRATION_DATE).asText(),
                TimeUtils.getTimezone("Asia/Shanghai"));
        Timestamp settlementDate = TimeUtils.getTimestamp(tradePositionDTO.getAsset().get(SETTLEMENT_DATE).asText(),
                TimeUtils.getTimezone("Asia/Shanghai"));
        int endDay = (int) (expirationDate.getTime() - settlementDate.getTime())/(60*60*24*1000);

        Map<String, Object> suppleData = new HashMap<>();
        suppleData.put(TRADE_ID, tradeId);
        suppleData.put(PARTY_NAME, partyName);
        suppleData.put(END_DAY, endDay);
        suppleData.put(DESCRIPTION7, description7);
        suppleData.put(DESCRIPTION8, description8);
        suppleData.put(DIRECTION, tradePositionDTO.getAsset().get(DIRECTION) == null ? "-"
                : TransfromUtil.transfrom(tradePositionDTO.getAsset().get(DIRECTION).asText()));

        String instrumentName = marketDataService.getInstrument(
                tradePositionDTO.getAsset().get(UNDERLYER_INSTRUMENT_ID).asText())
                .get().toInstrumentInfoDTO().getName();
        Double margin = accountService.getAccountByLegalName(partyName).getMargin().doubleValue();
        List<String> accountIds = new ArrayList<>();
        accountIds.add(accountService.getAccountByLegalName(partyName).getAccountId());
        Object maintenanceMargin = null;
        try {
            maintenanceMargin = marginService.getMargins(accountIds).get(0).getMaintenanceMargin().doubleValue();
        } catch (Exception e) {
            maintenanceMargin = "未找到保证金信息";
        }
        Object finalMaintenanceMargin = maintenanceMargin;
        List<SupplementaryAgreementDTO> supple = trade.getPositions()
                .stream()
                .map(s -> BctDocumentServiceImpl.getSupplementaryAgreement(s, instrumentName, margin, finalMaintenanceMargin))
                .collect(Collectors.toList());

        suppleData.put(LEGS, supple);

        //表2
        List<ProductTypeEnum> productTypeEnumList = trade.getPositions()
                .stream()
                .map(p -> p.getProductType())
                .collect(Collectors.toList());
        List<Map<String, Object>> items = trade.getPositions()
                .stream()
                .map(p -> BctDocumentServiceImpl.getSuppleRider(productTypeEnumList, p))
                .collect(Collectors.toList());

        List<Map<String, List<Object>>> agreement = new ArrayList<>();
        items.forEach(i -> {
            List<Object> list = new ArrayList<>();
            for(Map.Entry<String, Object> entry : i.entrySet()){
                if (Objects.equals(entry.getKey(), ID)){
                    list.add(0, i.get(ID));
                    continue;
                }
                list.add(entry.getValue());
            }
            Map<String, List<Object>> map = new HashMap<>();
            map.put(LEN,list);
            agreement.add(map);
        });

        suppleData.put(ITEMS,agreement);

        Set<String> menu = new HashSet<>();
        items.forEach(i -> {
            Set<String> strings = i.keySet();
            strings.remove(ID);
            menu.addAll(strings);
        });
        List<String> menus = new ArrayList<>(menu);
        menus.add(0, LEG);

        suppleData.put(MENUS, menus);

        JsonNode data = JsonUtils.mapper.valueToTree(suppleData);
        try {
            tradeDocumentService.updateTradeDocStatus(tradeId, DocProcessStatusEnum.DOWNLOADED);
        } catch (Exception e) {
            throw new RuntimeException("文档生成状态修改失败，" + e);
        }
        return getTemplate(DocTypeEnum.SUPPLEMENTARY_AGREEMENT, data);
    }

    private StringWriter getTemplate(DocTypeEnum docTypeEnum, JsonNode data) {
        BctTemplateDTO bctTemplate = getBctTemplateDTOByDocTypeAndTransactType(docTypeEnum, "EUROPEAN");
        StringWriter stringWriter = new StringWriter();
        documentService.genDocument(data, bctTemplate.getDirectoryId(), bctTemplate.getGroupName(), stringWriter);
        return stringWriter;
    }

    private static SupplementaryAgreementDTO getSupplementaryAgreement(TradePositionDTO tradePositionDTO, String instrumentName, Double margin, Object maintenanceMargin){
        JsonNode asset = tradePositionDTO.getAsset();
        String[] leg = tradePositionDTO.getPositionId().split("_");
        Object quantity = "-";
        Object notionalAmount = "-";
        if (tradePositionDTO.getQuantity() != null){
            quantity = tradePositionDTO.getQuantity().doubleValue();
        }else {
            if (asset.get(NOTIONAL_AMOUNT_TYPE).asText().equals(LOT)){
                if (asset.get(NOTIONAL_AMOUNT) != null){
                    notionalAmount = asset.get(NOTIONAL_AMOUNT).asDouble() + "手";
                }
            }else {
                if (asset.get(NOTIONAL_AMOUNT) != null) {
                    notionalAmount = asset.get(NOTIONAL_AMOUNT).asDouble() + "元";
                }
            }

        }
        Object strike = "-";
        if (asset.get(STRIKE_TYPE) != null){
            if (asset.get(STRIKE_TYPE).asText().equals(PERCENT)){
                if (asset.get(STRIKE) != null){
                    strike = asset.get(STRIKE).asDouble() * 100 + "%";
                }
            }else {
                if (asset.get(STRIKE) != null){
                    strike = asset.get(STRIKE).asDouble();
                }
            }
        }
        Object frontPremium = '-';
        if (asset.get(PREMIUM_TYPE) != null) {
            if (asset.get(PREMIUM_TYPE).asText().equals(PERCENT)){
                if (asset.get(FRONT_PREMIUM) != null){
                    frontPremium = asset.get(FRONT_PREMIUM).asDouble() * 100 + "%";
                }
            }else {
                if (asset.get(FRONT_PREMIUM) != null){
                    frontPremium = asset.get(FRONT_PREMIUM).asDouble();
                }
            }
        }
        return new SupplementaryAgreementDTO(
                Integer.parseInt(leg[leg.length - 1]) + 1,
                instrumentName,
                asset.get(UNDERLYER_INSTRUMENT_ID).asText(),
                TransfromUtil.transfrom(asset.get(DIRECTION).asText()),
                TransfromUtil.transfrom(tradePositionDTO.getProductType().toString()),
                asset.get(OPTION_TYPE) == null ? "-" : TransfromUtil.transfrom(asset.get(OPTION_TYPE).asText()),
                asset.get(EFFECTIVE_DATE).asText(),
                asset.get(EXPIRATION_DATE).asText(),
                asset.get(INITIAL_SPOT) == null ? "-" : asset.get(INITIAL_SPOT),
                strike,
                asset.get(SPECIFIED_PRICE) == null ? "-" : TransfromUtil.transfrom(asset.get(SPECIFIED_PRICE).asText()),
                quantity,
                notionalAmount,
                frontPremium,
                margin,
                maintenanceMargin
        );
    }

    private static Map<String, Object> getSuppleRider(List<ProductTypeEnum> typeEnumList, TradePositionDTO tradePositionDTO) {
        JsonNode asset = tradePositionDTO.getAsset();
        String[] leg = tradePositionDTO.getPositionId().split("_");
        Map<String, Object> map = new HashMap<>();
        map.put(ID, Integer.parseInt(leg[leg.length - 1]) + 1);
        map.put("期权种类", TransfromUtil.transfrom(tradePositionDTO.getProductType().toString()));
        if (asset.get(OBSERVATION_TYPE) != null && !asset.get(OBSERVATION_TYPE).asText().equals(NULL)){
            map.put("障碍观察类型", TransfromUtil.transfrom(asset.get(OBSERVATION_TYPE).asText()));
        }
        if (asset.get(REBATE_TYPE) != null && !asset.get(REBATE_TYPE).asText().equals(NULL)){
            map.put("补偿支付方式", TransfromUtil.transfrom(asset.get(REBATE_TYPE).asText()));
        }
        if (asset.get(PAYMENT) != null && !asset.get(PAYMENT).asText().equals(NULL)){
            map.put("行权收益",  asset.get(PAYMENT));
        }
        if (asset.get(LOW_STRIKE) != null && !asset.get(LOW_STRIKE).asText().equals(NULL)){
            map.put("低行权价",  asset.get(LOW_STRIKE));
        }
        if (asset.get(HIGH_STRIKE) != null && !asset.get(HIGH_STRIKE).asText().equals(NULL)){
            map.put("高行权价",  asset.get(HIGH_STRIKE));
        }
        if (asset.get(MINIMUM_PREMIUM) != null && !asset.get(MINIMUM_PREMIUM).asText().equals(NULL)){
            map.put("保底收益",  asset.get(MINIMUM_PREMIUM));
        }
        if (asset.get(PREMIUM_TYPE) != null){
            if (asset.get(PREMIUM_TYPE).asText().equals(PERCENT)){
                if (asset.get(PREMIUM) != null){
                    map.put("实际期权费", asset.get(PREMIUM).asDouble() * 100 + "%");
                }
            }else {
                if (asset.get(PREMIUM) != null){
                    map.put("实际期权费", asset.get(PREMIUM).asDouble());
                }
            }
        }
        if (asset.get(PREMIUM_TYPE) != null){
            if (asset.get(PREMIUM_TYPE).asText().equals(PERCENT)){
                if (asset.get(FRONT_PREMIUM) != null){
                    map.put("合约期权费", asset.get(FRONT_PREMIUM).asDouble() * 100 + "%");
                }
            }else {
                if (asset.get(FRONT_PREMIUM) != null){
                    map.put("合约期权费", asset.get(FRONT_PREMIUM).asDouble());
                }
            }
        }
        if (asset.get(KNOCK_DIRECTION) != null && !asset.get(KNOCK_DIRECTION).asText().equals(NULL)){
            map.put("敲出方向",  TransfromUtil.transfrom(asset.get(KNOCK_DIRECTION).asText()));
        }
        if (asset.get(BARRIER) != null && !asset.get(BARRIER).asText().equals(NULL)){
            map.put("障碍价",  asset.get(BARRIER));
        }
        if (asset.get(REBATE) != null && !asset.get(REBATE).asText().equals(NULL)){
            map.put("敲出补偿",  asset.get(REBATE));
        }
        if (asset.get(OBSERVATION_STEP) != null && !asset.get(OBSERVATION_STEP).asText().equals(NULL)){
            map.put("观察频率", asset.get(OBSERVATION_STEP));
        }
        if (asset.get(STEP) != null && !asset.get(STEP).asText().equals(NULL)){
            map.put("逐步调整步长(%)",  asset.get(STEP));
        }
        if (asset.get(COUPON_PAYMENT) != null && !asset.get(COUPON_PAYMENT).asText().equals(NULL)) {
            map.put("收益/coupon(%)",  asset.get(COUPON_PAYMENT));
        }
        if (asset.get(FIXED_PAYMENT) != null && !asset.get(FIXED_PAYMENT).asText().equals(NULL)){
            map.put("到期未敲出固定收益",  asset.get(FIXED_PAYMENT));
        }
        if (asset.get(AUTOCALL_PAYMENT_TYPE) != null && !asset.get(AUTOCALL_PAYMENT_TYPE).asText().equals(NULL)){
            map.put("到期未敲出收益类型",  asset.get(AUTOCALL_PAYMENT_TYPE));
        }
        if (asset.get(AUTOCALL_STRIKE) != null && !asset.get(AUTOCALL_STRIKE).asText().equals(NULL)){
            map.put("到期未敲出行权价格",  asset.get(AUTOCALL_STRIKE));
        }
        if (asset.get(OBSERVATION_DATES) != null && !asset.get(OBSERVATION_DATES).asText().equals(NULL)){
            map.put("观察日",  asset.get(OBSERVATION_DATES));
        }
        if (asset.get(LOW_PAYMENT) != null && !asset.get(LOW_PAYMENT).asText().equals(NULL)){
            map.put("低行权收益",  asset.get(LOW_PAYMENT));
        }
        if (asset.get(HIGH_PAYMENT) != null && !asset.get(HIGH_PAYMENT).asText().equals(NULL)){
            map.put("高行权收益",  asset.get(HIGH_PAYMENT));
        }
        if (asset.get(STRIKE1) != null && !asset.get(STRIKE1).asText().equals(NULL)){
            map.put("行权价1",  asset.get(STRIKE1));
        }
        if (asset.get(STRIKE2) != null && !asset.get(STRIKE2).asText().equals(NULL)){
            map.put("行权价2",  asset.get(STRIKE2));
        }
        if (asset.get(STRIKE3) != null && !asset.get(STRIKE3).asText().equals(NULL)){
            map.put("行权价3",  asset.get(STRIKE3));
        }
        if (asset.get(STRIKE4) != null && !asset.get(STRIKE4).asText().equals(NULL)){
            map.put("行权价4",  asset.get(STRIKE4));
        }
        if (asset.get(PAYMENT1) != null && !asset.get(PAYMENT1).asText().equals(NULL)){
            map.put("行权收益1",  asset.get(PAYMENT1));
        }
        if (asset.get(PAYMENT2) != null && !asset.get(PAYMENT2).asText().equals(NULL)){
            map.put("行权收益2",  asset.get(PAYMENT2));
        }
        if (asset.get(PAYMENT3) != null && !asset.get(PAYMENT3).asText().equals(NULL)){
            map.put("行权收益3",  asset.get(PAYMENT3));
        }
        if (asset.get(LOW_PARTICIPATION_RATE) != null && !asset.get(LOW_PARTICIPATION_RATE).asText().equals(NULL)){
            map.put("低参与率",  asset.get(LOW_PARTICIPATION_RATE));
        }
        if (asset.get(HIGH_PARTICIPATION_RATE) != null && !asset.get(HIGH_PARTICIPATION_RATE).asText().equals(NULL)){
            map.put("高参与率",  asset.get(HIGH_PARTICIPATION_RATE));
        }
        if (asset.get(PARTICIPATION_RATE1) != null && !asset.get(PARTICIPATION_RATE1).asText().equals(NULL)){
            map.put("参与率1",  asset.get(PARTICIPATION_RATE1));
        }
        if (asset.get(PARTICIPATION_RATE2) != null && !asset.get(PARTICIPATION_RATE2).asText().equals(NULL)){
            map.put("参与率2",  asset.get(PARTICIPATION_RATE2));
        }
        if (asset.get(LOW_BARRIER) != null && !asset.get(LOW_BARRIER).asText().equals(NULL)){
            map.put("低障碍价",  asset.get(LOW_BARRIER));
        }
        if (asset.get(HIGH_BARRIER) != null && !asset.get(HIGH_BARRIER).asText().equals(NULL)){
            map.put("高障碍价",  asset.get(HIGH_BARRIER));
        }
        if (asset.get(LOW_REBATE) != null && !asset.get(LOW_REBATE).asText().equals(NULL)){
            map.put("低敲出补偿价",  asset.get(LOW_REBATE));
        }
        if (asset.get(HIGH_REBATE) != null && !asset.get(HIGH_REBATE).asText().equals(NULL)){
            map.put("高敲出补偿价",  asset.get(HIGH_REBATE));
        }
        return map;
    }

    private static SettlementDTO getSettlementDTO(LCMEventTypeEnum typeEnum, LCMEventDTO lcmEventDTO, String instrumentName, SettlementDTO settlementDTO) {
        Object notionalOldValue = "-";
        Object numOfOption = "-";
        Object settlement = "-";
        Object underlyerPrice = lcmEventDTO.getEventDetail().get(UNDERLYER_PRICE) == null ? "-" : lcmEventDTO.getEventDetail().get(UNDERLYER_PRICE);
        Object settleAmount = lcmEventDTO.getEventDetail().get(SETTLE_AMOUNT) == null ? "-" : lcmEventDTO.getEventDetail().get(SETTLE_AMOUNT);
        if (Objects.equals(typeEnum, LCMEventTypeEnum.UNWIND_PARTIAL)
                || Objects.equals(typeEnum, LCMEventTypeEnum.UNWIND)){
            notionalOldValue = lcmEventDTO.getEventDetail().get(NOTIONAL_OLD_VALUE);
            settlement = Double.parseDouble(lcmEventDTO.getEventDetail().get(UNWIND_AMOUNT).toString());
            numOfOption = Double.parseDouble(notionalOldValue.toString()) - (Double) settlement;
        }else if (Objects.equals(typeEnum, LCMEventTypeEnum.KNOCK_OUT)
                || Objects.equals(typeEnum, LCMEventTypeEnum.EXPIRATION)
                || Objects.equals(typeEnum, LCMEventTypeEnum.EXERCISE)
                || Objects.equals(typeEnum, LCMEventTypeEnum.SNOW_BALL_EXERCISE) ){
            notionalOldValue = 0;
            numOfOption = 0;
            settlement = lcmEventDTO.getEventDetail().get(SETTLE_AMOUNT);
        }
        return new SettlementDTO(
                lcmEventDTO.getTradeId(),
                settlementDTO.getId(),
                instrumentName,
                settlementDTO.getUnderlyerInstrumentId(),
                settlementDTO.getDirection(),
                settlementDTO.getProductType(),
                settlementDTO.getOptionType(),
                settlementDTO.getStrike(),
                settlementDTO.getExpirationDate(),
                settlementDTO.getSettlementDate(),
                notionalOldValue,
                settlement,
                numOfOption,
                underlyerPrice,
                settleAmount

        );
    }

    private static SettlementDTO getSettlementDTO(TradePositionDTO tradePositionDTO, String tradeId){
        String[] leg = tradePositionDTO.getPositionId().split("_");
        JsonNode asset = tradePositionDTO.getAsset();
        String strike = asset.get(STRIKE) == null ? "-" : asset.get(STRIKE).asText();
        String strikeType = asset.get(STRIKE_TYPE) == null ? null : asset.get(STRIKE_TYPE).asText();
        if(!strike.equals("-") && StringUtils.isNotBlank(strikeType) &&  strikeType.equals(PERCENT)){
            Double strikDoulbe = Double.valueOf(strike) * 100d;
            strike = strikDoulbe + "%";
        }
        return new SettlementDTO(
                tradeId,
                Integer.parseInt(leg[leg.length - 1]) + 1,
                "null",
                asset.get(UNDERLYER_INSTRUMENT_ID).asText(),
                TransfromUtil.transfrom(asset.get(DIRECTION).asText()),
                TransfromUtil.transfrom(tradePositionDTO.getProductType().toString()),
                asset.get(OPTION_TYPE) == null ? "-" : TransfromUtil.transfrom(asset.get(OPTION_TYPE).asText()),
                strike,
                asset.get(EXPIRATION_DATE).asText(),
                asset.get(SETTLEMENT_DATE).asText(),
                "-",
                "-",
                "-",
                "-",
                "-"
        );
    }

    private BctTemplateDTO toDto(BctTemplate dbo) {
        assert null != dbo;

        BctTemplateDTO result = new BctTemplateDTO(dbo.getUuid());
        BeanUtils.copyProperties(dbo, result);
        return result;
    }

    private BctTemplate toDbo(BctTemplateDTO dto) {
        assert null != dto;

        BctTemplate result = new BctTemplate();
        if (dto.getUuid() != null) {
            result.setUuid(dto.getUuid());
        }
        BeanUtils.copyProperties(dto, result);

        return result;
    }

    private PoiTemplate toPoiTemplateDbo(PoiTemplateDTO poiTemplateDTO) {
        return new PoiTemplate(
                poiTemplateDTO.getUuid(),
                poiTemplateDTO.getTradeType(),
                poiTemplateDTO.getDocType(),
                poiTemplateDTO.getTypeSuffix(),
                poiTemplateDTO.getFileName(),
                poiTemplateDTO.getFilePath()
        );
    }

    private PoiTemplateDTO toPoiTemplateDTO(PoiTemplate poiTemplate) {
        return new PoiTemplateDTO(
                poiTemplate.getUuid(),
                poiTemplate.getTradeType(),
                poiTemplate.getDocType(),
                poiTemplate.getTypeSuffix(),
                poiTemplate.getFileName(),
                poiTemplate.getUpdatedAt(),
                poiTemplate.getFilePath());
    }
}
