package tech.tongyu.bct.market.api;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.*;
import tech.tongyu.bct.market.service.InstrumentWhitelistService;
import tech.tongyu.bct.market.service.MarketDataService;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.common.util.JsonUtils.BCT_JACKSON_TYPE_TAG;

@Service
public class MarketDataApi {
    private MarketDataService marketDataService;
    private InstrumentWhitelistService instrumentWhitelistService;

    @Autowired
    public MarketDataApi(MarketDataService marketDataService, InstrumentWhitelistService instrumentWhitelistService) {
        this.marketDataService = marketDataService;
        this.instrumentWhitelistService = instrumentWhitelistService;
    }

    @BctMethodInfo(
            description = "Save a quote",
            retName = "quote",
            retDescription = "instrument + valuation date",
            service = "market-data-service"
    )
    public String mktQuoteSave(
            @BctMethodArg(description = "The unique identifier of the instrument") String instrumentId,
            @BctMethodArg(description = "\\'close\\' or \\'intraday\\' instance") String instance,
            @BctMethodArg(description = "Quote in Json format, for example, {close: 1.2, open: 1.1}.")
                    Map<String, Object> quote,
            @BctMethodArg(description = "Quote valuation date", required = false) String valuationDate,
            @BctMethodArg(
                    description = "Timezone of the timestamp. Defaults to Asia/Shanghai",
                    required = false) String quoteTimezone) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, quoteTimezone);
        Map<QuoteFieldEnum, Double> fields = quote.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> QuoteFieldEnum.valueOf(e.getKey().toUpperCase()),
                        e -> ((Number) e.getValue()).doubleValue()
                ));
        ZonedDateTime now = DateTimeUtils.parse(null, quoteTimezone);
        QuoteDTO quoteDTO = marketDataService.saveQuote(instrumentId,
                InstanceEnum.valueOf(instance.toUpperCase()), fields,
                t.toLocalDate(), now.toLocalDateTime(), t.getZone());
        return quoteDTO.toString();
    }

    @BctMethodInfo(
            description = "批量保存行情",
            retName = "quote",
            retDescription = "被保存合约代码",
            service = "market-data-service"
    )
    public List<String> mktQuoteSaveBatch(
            @BctMethodArg(description = "一组行情", argClass = QuoteDTO.class) List<Map<String, Object>> quotes
    ) {
        ZonedDateTime t = DateTimeUtils.parse(null, null);
        return marketDataService.saveQuotes(quotes.stream()
                .peek(m -> {
                    // hackish
                    if (!m.containsKey("valuationDate")) {
                        m.put("valuationDate", t.toLocalDate());
                    }
                    if (!m.containsKey("quoteTimestamp")) {
                        m.put("quoteTimestamp", t.toLocalDateTime());
                    }
                    if (!m.containsKey("quoteTimezone")) {
                        m.put("quoteTimezone", t.getZone());
                    }
                    if (m.containsKey("quote")) {
                        Map<QuoteFieldEnum, Object> fields = ((Map<String, Object>) m.get("quote")).entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> QuoteFieldEnum.valueOf(e.getKey().toUpperCase()),
                                        e -> ((Number) e.getValue()).doubleValue()
                                ));
                        m.put("fields", fields);
                        m.remove("quote");
                    }
                })
                .map(q -> JsonUtils.mapper.convertValue(q, QuoteDTO.class))
                .collect(Collectors.toList()));
    }

    @BctMethodInfo(
            description = "获取行情",
            retName = "quote",
            retDescription = "quote fields",
            service = "market-data-service",
            tags = {BctApiTagEnum.Excel})
    public Double mktQuoteGet(
            @BctMethodArg(description = "合约代码") String instrumentId,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "行情字段（close, settle, last等）") String field,
            @BctMethodArg(description = "行情估值日",
                    excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDate,
            @BctMethodArg(description = "时区", required = false) String quoteTimezone) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, quoteTimezone);
        Optional<QuoteDTO> quote = marketDataService.getQuote(instrumentId,
                InstanceEnum.valueOf(instance.toUpperCase()), t.toLocalDate(), t.getZone());
        QuoteFieldEnum fieldEnum = QuoteFieldEnum.valueOf(field.toUpperCase());
        if (quote.isPresent())
            return quote.get().getFields().get(fieldEnum);
        else
            throw new CustomException(ErrorCode.MISSING_ENTITY, "cannot find quote for " + instrumentId
                    + ", instance " + instance + ", on " + valuationDate);
    }

    @BctMethodInfo(
            description = "获取多个标的物行情",
            retName = "quotes",
            retDescription = "list of quotes",
            returnClass = QuoteDTO.class,
            service = "market-data-service"
    )
    public List<QuoteDTO> mktQuotesList(
            @BctMethodArg(description = "合约代码列表") List<String> instrumentIds,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "行情估值日", required = false) String valuationDate,
            @BctMethodArg(description = "时区", required = false) String timezone
    ) {
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, timezone);
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        if (instanceEnum == InstanceEnum.INTRADAY) {
            return marketDataService.listLatestQuotes(instrumentIds);
        } else {
            return marketDataService.listCloseQuotes(instrumentIds, t.toLocalDate(), t.getZone());
        }
    }

    @BctMethodInfo(
            description = "Delete a quote, either of \\'close\\' or \\'intraday\\' instance",
            retName = "status",
            retDescription = "Whether the quote is deleted successfully",
            service = "market-data-service"
    )
    public String mktQuoteDelete(
            @BctMethodArg(description = "The unique identifier of the instrument") String instrumentId,
            @BctMethodArg(description = "\\'close\\' or \\'intraday\\' instance") String instance,
            @BctMethodArg(description = "The timestamp of the quote") String timestamp,
            @BctMethodArg(description = "Timezone of the timestamp. Defaults to Asia/Shanghai") String timezone) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        ZonedDateTime val = DateTimeUtils.parse(timestamp, timezone);
        Optional<QuoteDTO> dto = marketDataService.deleteQuote(instrumentId, instanceEnum,
                val.toLocalDate(), val.getZone());
        if (dto.isPresent())
            return dto.get().toString();
        else
            throw new CustomException(ErrorCode.MISSING_ENTITY, "failed to delete quote " + instrumentId
                    + ", instance " + instance + ", on " + val.toLocalDate());
    }

    @BctMethodInfo(
            description = "Create an instrument",
            retDescription = "Created instrument id",
            retName = "instrumentId",
            service = "market-data-service"
    )
    public String mktInstrumentCreate(
            @BctMethodArg(description = "Instrument id") String instrumentId,
            @BctMethodArg(description = "Asset class") String assetClass,
            @BctMethodArg(description = "Asset sub class") String assetSubClass,
            @BctMethodArg(description = "Instrument type") String instrumentType,
            @BctMethodArg(description = "Instrument info") Map<String, Object> instrumentInfo) {
        AssetClassEnum assetClassEnum = AssetClassEnum.valueOf(assetClass.toUpperCase());
        InstrumentInfo instInfo;
        InstrumentTypeEnum instrumentTypeEnum = InstrumentTypeEnum.valueOf(instrumentType.toUpperCase());
        switch (assetClassEnum) {
            case COMMODITY:
                switch (instrumentTypeEnum) {
                    case SPOT:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, CommoditySpotInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, CommoditySpotInfo.class);
                        break;
                    case FUTURES:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, CommodityFuturesInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, CommodityFuturesInfo.class);
                        break;
                    case FUTURES_OPTION:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, CommodityFuturesOptionInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, CommodityFuturesOptionInfo.class);
                        break;
                    default:
                        throw new CustomException("unknown commodity instrument type: " + instrumentType);
                }
                break;
            case EQUITY:
                switch (instrumentTypeEnum) {
                    case INDEX:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, EquityIndexInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, EquityIndexInfo.class);
                        break;
                    case STOCK:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, EquityStockInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, EquityStockInfo.class);
                        break;
                    case FUTURES:
                    case INDEX_FUTURES:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, EquityIndexFuturesInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, EquityIndexFuturesInfo.class);
                        break;
                    case INDEX_OPTION:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, EquityIndexOptionInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, EquityIndexOptionInfo.class);
                        break;
                    case STOCK_OPTION:
                        instrumentInfo.put(BCT_JACKSON_TYPE_TAG, EquityStockOptionInfo.class.getCanonicalName());
                        instInfo = JsonUtils.mapper.convertValue(instrumentInfo, EquityStockOptionInfo.class);
                        break;
                    default:
                        throw new CustomException("unknown equity instrument type: " + instrumentType);
                }
                break;
            default:
                throw new CustomException("unknown asset class: " + assetClass);
        }
        return marketDataService.saveInstrument(instrumentId, assetClassEnum,
                AssetSubClassEnum.valueOf(assetSubClass.toUpperCase()), instrumentTypeEnum, instInfo).toString();
    }

    @BctMethodInfo(
            description = "Delete an instrument by id",
            retDescription = "Number of instruments deleted (should be 1)",
            retName = "num",
            service = "market-data-service"
    )
    public String mktInstrumentDelete(
            @BctMethodArg(description = "Instrument to be deleted") String instrumentId) {
        Optional<InstrumentDTO> deleted = marketDataService.deleteInstrument(instrumentId);
        if (!deleted.isPresent())
            throw new CustomException(ErrorCode.MISSING_ENTITY, "failed to delete instrument " + instrumentId);
        return deleted.get().toString();
    }

    @BctMethodInfo(
            description = "Get an instrument's info",
            retName = "Instrument info",
            retDescription = "Instrument info",
            tags = {BctApiTagEnum.Excel},
            returnClass = InstrumentDTO.class,
            service = "market-data-service"
    )
    public InstrumentDTO mktInstrumentInfo(
            @BctMethodArg(description = "标的物合约代码") String instrumentId
    ) {
        Optional<InstrumentDTO> info = marketDataService.getInstrument(instrumentId);
        if (info.isPresent())
            return info.get();
        throw new CustomException(ErrorCode.MISSING_ENTITY, "failed to find instrument: " + instrumentId);
    }

    @BctMethodInfo(
            description = "列出标的物信息",
            retName = "paged InstrumentInfoDTO",
            retDescription = "标的物信息",
            returnClass = InstrumentDTO.class,
            service = "market-data-service"
    )
    public RpcResponseListPaged<InstrumentInfoDTO> mktInstrumentsListPaged(
            @BctMethodArg(description = "资产类型", required = false) String assetClass,
            @BctMethodArg(description = "资产子类别", required = false) String assetSubClass,
            @BctMethodArg(description = "合约种类", required = false) String instrumentType,
            @BctMethodArg(description = "合约代码列表", required = false) List<String> instrumentIds,
            @BctMethodArg(description = "当前页数", required = false) Integer page,
            @BctMethodArg(description = "每页数量", required = false) Integer pageSize) {
        if (!Objects.isNull(instrumentIds) && (!Objects.isNull(assetClass) || !Objects.isNull(instrumentType)))
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "不能同时指定合约代码和合约资产类型/合约种类");

        Pageable p;
        if (Objects.isNull(page) && Objects.isNull(pageSize)) {
            p = null;
        } else if (!Objects.isNull(page) && !Objects.isNull(pageSize)) {
            p = PageRequest.of(page, pageSize);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "分页检索必须同时提供当前页数和每页数量。若不需分页，则页数和每页数量必须同时为空。");
        }

        if (CollectionUtils.isEmpty(instrumentIds)) {
            InstrumentDTO template = new InstrumentDTO();
            template.setAssetClass(StringUtils.isBlank(assetClass) ? null : AssetClassEnum.valueOf(assetClass.toUpperCase()));
            template.setAssetSubClass(StringUtils.isBlank(assetSubClass) ? null : AssetSubClassEnum.valueOf(assetSubClass.toUpperCase()));
            template.setInstrumentType(StringUtils.isBlank(instrumentType) ? null : InstrumentTypeEnum.valueOf(instrumentType.toUpperCase()));
            List<InstrumentInfoDTO> instruments = marketDataService.listInstrumentsByTemplate(template).stream()
                    .map(InstrumentDTO::toInstrumentInfoDTO).collect(Collectors.toList());

            if (Objects.isNull(p)) {
                return new RpcResponseListPaged<>(instruments, instruments.size());
            } else {
                int start = page * pageSize;
                int end = Math.min(start + pageSize, instruments.size());
                return new RpcResponseListPaged<>(instruments.subList(start, end), instruments.size());
            }
        } else {
            return new RpcResponseListPaged<>(marketDataService.listInstruments(instrumentIds, p).stream()
                    .map(InstrumentDTO::toInstrumentInfoDTO).collect(Collectors.toList()),
                    marketDataService.countInstruments(instrumentIds));
        }
    }

    @BctMethodInfo(
            description = "获取多个标的物行情, 包括日内与收盘",
            retName = "paged CombinedQuoteDTO",
            retDescription = "多个标的物行情, 包括日内与收盘",
            returnClass = CombinedQuoteDTO.class,
            service = "market-data-service"
    )
    public RpcResponseListPaged<CombinedQuoteDTO> mktQuotesListPaged(
            @BctMethodArg(description = "合约代码列表", required = false) List<String> instrumentIds,
            @BctMethodArg(description = "行情估值日", required = false) String valuationDate,
            @BctMethodArg(description = "时区", required = false) String timezone,
            @BctMethodArg(description = "当前页数", required = false) Integer page,
            @BctMethodArg(description = "每页数量", required = false) Integer pageSize
    ) {
        if (!(Objects.isNull(page) == Objects.isNull(pageSize))) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "分页检索必须同时提供当前页数和每页数量。若不需分页，则页数和每页数量必须同时为空。");
        }
        List<String> instruments;
        if (Objects.isNull(instrumentIds) || instrumentIds.size() == 0) {
            instruments = marketDataService.listInstruments(null).stream()
                    .map(InstrumentDTO::getInstrumentId)
                    .collect(Collectors.toList());
        } else {
            instruments = instrumentIds;
        }
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, timezone);
        List<CombinedQuoteDTO> quotes = marketDataService.listQuotes(instruments, t.toLocalDate(), t.getZone());
        int pNum = Objects.isNull(page) ? 0 : page;
        int pSize = Objects.isNull(pageSize) ? quotes.size() : pageSize;
        int start = pNum * pSize;
        int end = Math.min(start + pSize, quotes.size());
        return new RpcResponseListPaged<>(quotes.subList(start, end), quotes.size());
    }

    @BctMethodInfo(
            description = "罗列系统中所有标的物代码",
            retName = "List of instrumentIds",
            retDescription = "所有标的物代码",
            service = "market-data-service"
    )
    public List<String> mktInstrumentIdsList() {
        return marketDataService.listInstruments(null).stream()
                .map(InstrumentDTO::getInstrumentId)
                .sorted()
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "根据资产类型搜索以instrumentIdPart开头的Instrument",
            retName = "List of instrumentIds",
            retDescription = "以instrumentIdPart开头的标的物代码",
            service = "market-data-service"
    )
    public List<String> mktInstrumentSearch(
            @BctMethodArg(description = "instrument id的开始部分") String instrumentIdPart,
            @BctMethodArg(description = "资产类型", required = false) String assetClass,
            @BctMethodArg(description = "是否排除期权标的物", required = false) Boolean excludeOption
    ) {
        Optional<AssetClassEnum> ac = Optional.ofNullable(assetClass).map(AssetClassEnum::valueOf);
        InstrumentTypeEnum[] optionInstrument = {
                InstrumentTypeEnum.FUTURES_OPTION,
                InstrumentTypeEnum.INDEX_OPTION,
                InstrumentTypeEnum.STOCK_OPTION,
        };
        return marketDataService.searchInstruments(instrumentIdPart, ac)
                .stream()
                .filter(v -> excludeOption == null || !excludeOption || !ArrayUtils.contains(optionInstrument, v.getInstrumentType()))
                .map(InstrumentDTO::getInstrumentId)
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "罗列系统中所有可交易的标的物代码",
            retName = "List of instrumentIds",
            retDescription = "所有标的物代码",
            service = "market-data-service"
    )
    public List<String> listTradableInstrument() {
        List<String> instrumentWhitelist = instrumentWhitelistService.listInstrumentWhitelist()
                .stream().map(v -> v.getInstrumentId()).collect(Collectors.toList());
        return marketDataService.listInstruments(null).stream()
                .filter(v -> {
                    InstrumentInfoDTO instrumentInfoDTO = v.toInstrumentInfoDTO();
                    boolean tradable = true;
                    if ((instrumentInfoDTO.getMaturity() != null
                            && LocalDate.now().isAfter(instrumentInfoDTO.getMaturity()))
                            || (instrumentInfoDTO.getExpirationDate() != null
                            && LocalDate.now().isAfter(instrumentInfoDTO.getExpirationDate()))) {
                        tradable = false;
                    }
                    return tradable && instrumentWhitelist.contains(v.getInstrumentId());
                })
                .map(InstrumentDTO::getInstrumentId)
                .sorted()
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "根据资产类型搜索以instrumentIdPart开头的可交易的Instrument",
            retName = "List of instrumentIds",
            retDescription = "以instrumentIdPart开头的可交易的标的物代码",
            service = "market-data-service"
    )
    public List<String> searchTradableInstrument(
            @BctMethodArg(description = "instrument id的开始部分") String instrumentIdPart,
            @BctMethodArg(description = "资产类型", required = false) String assetClass
    ) {
        List<String> instrumentWhitelist = instrumentWhitelistService.listInstrumentWhitelist()
                .stream().map(v -> v.getInstrumentId()).collect(Collectors.toList());
        Optional<AssetClassEnum> ac = Optional.ofNullable(assetClass).map(AssetClassEnum::valueOf);
        return marketDataService.searchInstruments(instrumentIdPart, ac)
                .stream()
                .filter(v -> {
                    InstrumentInfoDTO instrumentInfoDTO = v.toInstrumentInfoDTO();
                    boolean tradable = true;
                    if ((instrumentInfoDTO.getMaturity() != null
                            && LocalDate.now().isAfter(instrumentInfoDTO.getMaturity()))
                            || (instrumentInfoDTO.getExpirationDate() != null
                            && LocalDate.now().isAfter(instrumentInfoDTO.getExpirationDate()))) {
                        tradable = false;
                    }
                    return tradable && instrumentWhitelist.contains(v.getInstrumentId());
                })
                .map(i -> i.getInstrumentId())
                .collect(Collectors.toList());
    }
}
