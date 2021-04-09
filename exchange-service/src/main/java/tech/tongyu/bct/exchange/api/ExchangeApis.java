package tech.tongyu.bct.exchange.api;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.reader.UnicodeReader;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.service.ResourcePermissionService;
import tech.tongyu.bct.auth.service.ResourceService;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseWithDiagnostics;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.exchange.dto.*;
import tech.tongyu.bct.exchange.service.ExchangePortfolioService;
import tech.tongyu.bct.exchange.service.ExchangeService;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExchangeApis {
    private ExchangeService exchangeService;
    private ResourceService resourceService;
    private ExchangePortfolioService exchangePortfolioService;
    private ResourcePermissionService resourcePermissionService;

    @Autowired
    public ExchangeApis(ExchangeService exchangeService,
                        ResourceService resourceService,
                        ExchangePortfolioService exchangePortfolioService,
                        ResourcePermissionService resourcePermissionService) {
        this.exchangeService = exchangeService;
        this.resourceService = resourceService;
        this.exchangePortfolioService = exchangePortfolioService;
        this.resourcePermissionService = resourcePermissionService;
    }

    private static Logger logger = LoggerFactory.getLogger(ExchangeApis.class);

    @BctMethodInfo(
            description = "保存场内持仓变动记录",
            retDescription = "成功保存的场内持仓变动记录",
            retName = "positionRecordDTO",
            returnClass = PositionRecordDTO.class,
            service = "exchange-service"
    )
    public PositionRecordDTO excPositionRecordSave(
            @BctMethodArg(description = "交易簿ID") String bookId,
            @BctMethodArg(description = "标的物ID") String instrumentId,
            @BctMethodArg(description = "多头头寸") Number longPosition,
            @BctMethodArg(description = "空头头寸") Number shortPosition,
            @BctMethodArg(description = "净头寸") Number netPosition,
            @BctMethodArg(description = "总卖") Number totalSell,
            @BctMethodArg(description = "总买") Number totalBuy,
            @BctMethodArg(description = "历史买入量") Number historyBuyAmount,
            @BctMethodArg(description = "历史卖出量") Number historySellAmount,
            @BctMethodArg(description = "市值") Number marketValue,
            @BctMethodArg(description = "总盈亏") Number totalPnl,
            @BctMethodArg(description = "交易时间") String dealDate) {
        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());
        if (!bookNames.contains(bookId)) {
            throw new CustomException("该用户没有交易簿权限" + bookId);
        }
        LocalDate parsedSearchDate = DateTimeUtils.parseToLocalDate(dealDate);
        PositionRecordDTO positionRecordDTO = new PositionRecordDTO(
                bookId, instrumentId,
                new BigDecimal(longPosition.toString()),
                new BigDecimal(shortPosition.toString()),
                new BigDecimal(netPosition.toString()),
                new BigDecimal(totalSell.toString()),
                new BigDecimal(totalBuy.toString()),
                new BigDecimal(historyBuyAmount.toString()),
                new BigDecimal(historySellAmount.toString()),
                new BigDecimal(marketValue.toString()),
                new BigDecimal(totalPnl.toString())
                , parsedSearchDate);
        return exchangeService.savePosition(positionRecordDTO);
    }

    @BctMethodInfo(
            description = "批量保存场内持仓变动记录",
            retDescription = "成功保存的所有场内持仓变动记录",
            retName = "List of PositionRecordDTO",
            returnClass = PositionRecordDTO.class,
            service = "exchange-service"
    )
    public List<PositionRecordDTO> excPositionRecordSearch(
            @BctMethodArg(description = "查询日期") String searchDate
    ) {
        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());
        LocalDate parsedSearchDate = DateTimeUtils.parseToLocalDate(searchDate);
        return exchangeService.searchPositionRecord(parsedSearchDate).stream()
                .filter(v -> bookNames.contains(v.getBookId())).collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "按日期查询场内持仓变动记录(根据标的分组)",
            retDescription = "在查询日期之后的所有场内持仓变动记录(根据标的分组)",
            retName = "List of PositionRecordDTO",
            returnClass = PositionRecordDTO.class,
            service = "exchange-service"
    )
    public List<PositionRecordDTO> excPositionRecordSearchGroupByInstrumentId(
            @BctMethodArg(description = "查询日期") String searchDate
    ) {
        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());
        LocalDate parsedSearchDate = DateTimeUtils.parseToLocalDate(searchDate);
        return exchangeService.searchPositionRecordGroupByInstrumentId(parsedSearchDate, bookNames);
    }

    @BctMethodInfo(
            description = "查询场内持仓投资组合记录",
            retDescription = "场内持仓投资组合记录",
            retName = "List of PositionPortfolioRecordDTO",
            returnClass = PositionPortfolioRecordDTO.class,
            service = "exchange-service"
    )
    public List<PositionPortfolioRecordDTO> excGroupedPositionRecordSearch(
            @BctMethodArg(required = false, description = "投资组合名称列表") List<String> portfolioNames,
            @BctMethodArg(required = false, description = "交易簿列表") List<String> books,
            @BctMethodArg(required = false, description = "查询日期") String searchDate) {
        List<String> readableBooks = resourceService.authBookGetCanRead().stream()
                .map(ResourceDTO::getResourceName).distinct().collect(Collectors.toList());
        return exchangeService.searchGroupedPositionRecord(portfolioNames, books, searchDate, readableBooks);
    }

    @BctMethodInfo(
            description = "获取所有场内持仓信息",
            retDescription = "所有场内持仓信息",
            retName = "List of PositionSnapshotDTO",
            returnClass = PositionSnapshotDTO.class,
            service = "exchange-service"
    )
    public List<PositionSnapshotDTO> excPositionSnapshotListAll() {
        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());
        return exchangeService.findAllPositionSnapshot()
                .stream().filter(v -> bookNames.contains(v.getBookId())).collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "获取符合查询条件的所有场内交易流水标的",
            retDescription = "符合查询条件的所有场内交易流水标的",
            retName = "List of Instrument IDs",
            service = "exchange-service"
    )
    public List<String> excListAllInstrumentsInTradeRecords(
            @BctMethodArg(required = false, description = "条件") String criteria
    ) {
        return exchangeService.fuzzyQueryInstrumentsInTradeRecords(criteria);
    }

    @BctMethodInfo(
            description = "获取所有场内持仓信息",
            retDescription = "所有场内持仓信息",
            retName = "List of trade info",
            service = "exchange-service",
            tags = {BctApiTagEnum.Excel},
            excelType = BctExcelTypeEnum.Table,
            returnClass = PositionSnapshotDTO.class
    )
    public List<Map<String, Object>> excPositionSnapshotMapListAll() {
        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());
        List<PositionSnapshotDTO> dtoList = exchangeService.findAllPositionSnapshot()
                .stream().filter(v -> bookNames.contains(v.getBookId())).collect(Collectors.toList());
        return dtoList.stream()
                .map(dto -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("bookId", dto.getBookId());
                    map.put("instrumentId", dto.getInstrumentId());
                    map.put("longPosition", dto.getLongPosition().doubleValue());
                    map.put("shortPosition", dto.getShortPosition().doubleValue());
                    map.put("netPosition", dto.getNetPosition().doubleValue());
                    map.put("historyBuyAmount", dto.getHistoryBuyAmount().doubleValue());
                    map.put("historySellAmount", dto.getHistorySellAmount().doubleValue());
                    map.put("totalPnl", dto.getTotalPnl().doubleValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "获取所有场内持仓信息(根据标的分组)",
            retDescription = "所有场内持仓信息(根据标的分组)",
            retName = "List of PositionSnapshotDTO",
            returnClass = PositionSnapshotDTO.class,
            service = "exchange-service"
    )
    public List<PositionSnapshotDTO> excPositionSnapshotListAllGroupByInstrumentId() {
        return exchangeService.findPositionSnapshotGroupByInstrumentId();
    }

    @BctMethodInfo(
            description = "保存场内流水",
            retDescription = "成功保存的场内流水",
            retName = "TradeRecordDTO",
            returnClass = TradeRecordDTO.class,
            service = "exchange-service"
    )
    public TradeRecordDTO exeTradeRecordSave(
            @BctMethodArg(description = "交易簿ID") String bookId,
            @BctMethodArg(description = "交易ID") String tradeId,
            @BctMethodArg(description = "交易账户") String tradeAccount,
            @BctMethodArg(description = "标的物ID") String instrumentId,
            @BctMethodArg(description = "合约乘数") Number multiplier,
            @BctMethodArg(description = "交易数量") Number dealAmount,
            @BctMethodArg(description = "交易价格") Number dealPrice,
            @BctMethodArg(description = "交易时间") String dealTime,
            @BctMethodArg(description = "开/平", argClass = OpenCloseEnum.class) String openClose,
            @BctMethodArg(description = "买卖方向", argClass = InstrumentOfValuePartyRoleTypeEnum.class) String direction,
            @BctMethodArg(required = false, description = "投资组合名称列表") List<String> portfolioNames
    ) {
        //make sure all portfolioNames are existed
        if (!CollectionUtils.isEmpty(portfolioNames)) {
            portfolioNames.forEach(portfolioName -> {
                if (!exchangePortfolioService.existsByPortfolioName(portfolioName)) {
                    throw new CustomException(String.format("投资组合:[%s]不存在", portfolioName));
                }
            });
        }

        TradeRecordDTO tradeRecordDto = new TradeRecordDTO(bookId, tradeId, tradeAccount, instrumentId,
                new BigDecimal(multiplier.toString()),
                new BigDecimal(dealAmount.toString()),
                new BigDecimal(dealPrice.toString()),
                StringUtils.isBlank(dealTime) ? null : LocalDateTime.parse(dealTime),
                StringUtils.isBlank(openClose) ? null : OpenCloseEnum.valueOf(openClose),
                StringUtils.isBlank(direction) ? null : InstrumentOfValuePartyRoleTypeEnum.valueOf(direction));
        Optional<TradeRecordDTO> tradeRecord = exchangeService.findTradeRecordByTradeId(tradeId);
        if (tradeRecord.isPresent()) {
            throw new CustomException("流水数据已经存在:" + tradeId);
        }
        TradeRecordDTO tradeRecordDTO = exchangeService.saveTradeRecordWithoutNewTransaction(tradeRecordDto);
        exchangeService.savePositionSnapshotByTradeRecords(Arrays.asList(tradeRecordDTO));
        if (!CollectionUtils.isEmpty(portfolioNames)) {
            portfolioNames.forEach(name -> exchangePortfolioService.createExchangeTradePortfolioWithoutNewTransaction(tradeId, name));
        }
        tradeRecordDTO.setPortfolioNames(portfolioNames);
        return tradeRecordDTO;
    }

    @BctMethodInfo(
            description = "批量保存场内流水",
            retDescription = "是否成功保存",
            retName = "true or false",
            service = "exchange-service"
    )
    public Boolean exeTradeRecordSaveAll(
            @BctMethodArg(description = "场内流水数据", argClass = TradeRecordDTO.class) List<Map<String, Object>> tradeRecords
    ) {
        List<TradeRecordDTO> tradeExist = new ArrayList<>();
        List<TradeRecordDTO> tradeNoExist = new ArrayList<>();
        tradeRecords.forEach(
                tradeRecord -> {
                    TradeRecordDTO tradeRecordDto = JsonUtils.mapper.convertValue(tradeRecord, TradeRecordDTO.class);
                    Optional<TradeRecordDTO> trade = exchangeService.findTradeRecordByTradeId(tradeRecordDto.getTradeId());
                    if (trade.isPresent()) {
                        tradeExist.add(tradeRecordDto);
                    } else {
                        tradeNoExist.add(tradeRecordDto);
                    }
                }
        );
        List<TradeRecordDTO> tradeRecordNoExistList = tradeNoExist.stream().
                map(exchangeService::saveTradeRecordWithoutNewTransaction).collect(Collectors.toList());
        exchangeService.savePositionSnapshotByTradeRecords(tradeRecordNoExistList);
        if (tradeExist.size() != 0) {
            List<String> tradeIds = tradeExist.stream().map(TradeRecordDTO::getTradeId).collect(Collectors.toList());
            throw new CustomException("流水数据已经存在:" + tradeIds.toString());
        }
        return true;
    }

    @BctMethodInfo(
            description = "根据流水Id查询场内流水",
            retDescription = "场内流水",
            retName = "Optional<TradeRecordDTO>",
            returnClass = TradeRecordDTO.class,
            service = "exchange-service"
    )
    public Optional<TradeRecordDTO> exeTradeRecordSearchByTradeId(
            @BctMethodArg(description = "交易ID") String tradeId
    ) {
        if (StringUtils.isEmpty(tradeId)) {
            throw new CustomException("tradeId不能为空");
        }
        Optional<TradeRecordDTO> tradeRecordDTO = exchangeService.findTradeRecordByTradeId(tradeId);
        return tradeRecordDTO;
    }

    @BctMethodInfo(
            description = "查询场内流水",
            retDescription = "符合条件的场内流水",
            retName = "List of TradeRecordDTO",
            returnClass = TradeRecordDTO.class,
            service = "exchange-service"
    )
    public List<TradeRecordDTO> excTradeRecordSearch(
            @BctMethodArg(required = false, description = "标的物ID列表") List<String> instrumentIds,
            @BctMethodArg(description = "开始时间") String startTime,
            @BctMethodArg(description = "结束时间") String endTime
    ) {
        LocalDateTime startDateTime = StringUtils.isBlank(startTime) ? null : LocalDateTime.parse(startTime);
        LocalDateTime endDateTime = StringUtils.isBlank(endTime) ? null : LocalDateTime.parse(endTime);

        Set<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toSet());
        return exchangeService.searchTradeRecord(instrumentIds, startDateTime, endDateTime)
                .stream().filter(v -> bookNames.contains(v.getBookId())).collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "从文件批量导入场内流水",
            retDescription = "导入成功的场内流水及导入失败的场内流水的失败原因",
            retName = "List of TradeRecordDTOs and Diagnostics",
            returnClass = TradeRecordDTO.class,
            service = "exchange-service"
    )
    @Transactional
    public RpcResponseWithDiagnostics<List<TradeRecordDTO>, List<String>> exeTradeRecordUpload(
            @BctMethodArg(description = "具体文件") MultipartFile file
    ) throws IllegalStateException {
        BufferedReader buffer;
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }
            buffer = new BufferedReader(new UnicodeReader(file.getInputStream()));
            logger.info(String.format("start backFilling tradeRecord from file (size=%s)", file.getSize()));

            Stream<String> linesFiltered = buffer.lines().map(String::trim).filter(StringUtils::isNotEmpty);
            return backFillTradeReport(linesFiltered);
        } catch (IOException e) {
            logger.error("failed to call -> uploadTradeRecordList message ={} parameter ={} ", e.getMessage(), file.getName());
            throw new IllegalStateException("读取文件失败");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

    }

    @BctMethodInfo(
            description = "关联投资组合与场内流水",
            retDescription = "关联是否成功",
            retName = "true or false",
            service = "exchange-service"
    )
    public Boolean exeTradePortfolioCreate(
            @BctMethodArg(description = "交易ID") String tradeId,
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        return exchangePortfolioService.createExchangeTradePortfolioWithoutNewTransaction(tradeId, portfolioName);
    }

    @BctMethodInfo(
            description = "批量关联投资组合与场内流水",
            retDescription = "批量关联是否成功",
            retName = "true or false",
            service = "exchange-service"
    )
    public Boolean exeTradePortfolioCreateBatch(
            @BctMethodArg(description = "交易ID") String tradeId,
            @BctMethodArg(description = "投资组合名称列表") List<String> portfolioNames
    ) {
        if (StringUtils.isBlank(tradeId)) {
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (CollectionUtils.isEmpty(portfolioNames)) {
            throw new IllegalArgumentException("请输入投资组合名称portfolioName");
        }
        return exchangePortfolioService.createExchangeTradePortfolioBatch(tradeId, portfolioNames);
    }

    @BctMethodInfo(
            description = "删除已存在的投资组合与场内流水关联",
            retDescription = "删除关联是否成功",
            retName = "true or false",
            service = "exchange-service"
    )
    public Boolean exeTradePortfolioDelete(
            @BctMethodArg(description = "交易ID") String tradeId,
            @BctMethodArg(description = "投资组合名称") String portfolioName
    ) {
        return exchangePortfolioService.deleteExchangeTradePortfolio(tradeId, portfolioName);
    }

    @BctMethodInfo(
            description = "刷新投资组合与场内流水关联(关联不存在新建关联，关联存在，删除旧关联并添加新关联)",
            retDescription = "刷新是否成功",
            retName = "true or false",
            service = "exchange-service"
    )
    public Boolean exeTradePortfolioRefresh(
            @BctMethodArg(description = "交易ID列表") List<String> tradeIds,
            @BctMethodArg(description = "投资组合名称列表") List<String> portfolioNames
    ) {
        return exchangePortfolioService.refreshExchangeTradePortfolios(tradeIds, portfolioNames);
    }

    @BctMethodInfo(
            description = "列出所有交易流水和投资组合关联",
            retDescription = "所有交易流水和投资组合关联",
            retName = "all trades and their portfolios",
            service = "exchange-service"
    )
    public Map<String, List<String>> exePortfolioTradesList() {
        return exchangePortfolioService.listAllExchangePortfolioTrades();
    }

    private RpcResponseWithDiagnostics<List<TradeRecordDTO>, List<String>> backFillTradeReport(Stream<String> lines) {
        List<String> bookNames = resourceService.authBookGetCanRead().stream().map(ResourceDTO::getResourceName).collect(Collectors.toList());
        List<Boolean> writable = resourcePermissionService.authCan(ResourceTypeEnum.BOOK.name(), bookNames, ResourcePermissionTypeEnum.UPDATE_BOOK.name());
        for (int i = 0; i < bookNames.size(); i++) {
            if (!writable.get(i)) {
                bookNames.set(i, null);
            }
        }
        List<String> filteredBookNames = bookNames.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<TradeRecordDTO> success = new LinkedList<>();
        List<String> failure = new LinkedList<>();
        lines.forEach(line -> {
            String[] columns = line.split(",");
            if (columns.length < 3) {
                failure.add(line);
                return;
            }
            String tradeId = columns[2];
            try {
                TradeRecordDTO result = parseAndSaveTradeRecordList(columns, filteredBookNames);
                success.add(result);
            } catch (Exception e) {
                failure.add(String.format("%s`%s", tradeId, e.getMessage()));
            }
        });
        if (success.size() > 0) {
            exchangeService.savePositionSnapshotByTradeRecords(success);
        }

        return new RpcResponseWithDiagnostics() {

            @Override
            public Object getResult() {
                return success;
            }

            @Override
            public Object getDiagnostics() {
                return failure;
            }
        };
    }

    private TradeRecordDTO parseAndSaveTradeRecordList(String[] columns, List<String> writableBooks) {
        if (columns.length < 10) {
            throw new CustomException("格式错误，列数不足十列");
        }
        int i = 0;
        TradeRecordDTO tradeRecordDto = new TradeRecordDTO();
        tradeRecordDto.setBookId(columns[i++].trim());
        if (!writableBooks.contains(tradeRecordDto.getBookId())) {
            throw new CustomException(String.format("交易簿不存在或不可读写:{%s}", tradeRecordDto.getBookId()));
        }
        List<String> portfolioNames = Stream.of(columns[i++].split("\\|")).map(String::trim).collect(Collectors.toList());
        tradeRecordDto.setTradeId(columns[i++].trim());
        exchangeService.findTradeRecordByTradeId(tradeRecordDto.getTradeId()).ifPresent(v -> {
            throw new CustomException("成交ID重复");
        });
        tradeRecordDto.setTradeAccount(columns[i++].trim());
        tradeRecordDto.setInstrumentId(columns[i++].trim());
        String dealAmount = columns[i++].trim();
        String dealPrice = columns[i++].trim();
        String openClose = columns[i++].trim();
        String direction = columns[i++].trim();
        String dealTime = columns[i].trim();
        try {
            tradeRecordDto.setDealAmount(new BigDecimal(dealAmount));
        } catch (NumberFormatException e) {
            throw new CustomException(String.format("交易数量不是数值:{%s}", dealAmount));
        }
        try {
            tradeRecordDto.setDirection(InstrumentOfValuePartyRoleTypeEnum.valueOf(direction));
        } catch (IllegalArgumentException e) {
            throw new CustomException(String.format("买/卖仓只支持[%s]，实际取得值：{%s}", Arrays.toString(InstrumentOfValuePartyRoleTypeEnum.values()), direction));
        }
        try {
            tradeRecordDto.setDealPrice(new BigDecimal(dealPrice));
            tradeRecordDto.setOpenClose(OpenCloseEnum.valueOf(openClose));
            tradeRecordDto.setDealTime(LocalDateTime.parse(dealTime));
        } catch (NumberFormatException e) {
            throw new CustomException(String.format("价格不是数值:{%s}", dealPrice));
        } catch (IllegalArgumentException e) {
            throw new CustomException(String.format("开/平仓只支持[%s]，拿到值：{%s}", Arrays.toString(OpenCloseEnum.values()), openClose));
        } catch (DateTimeParseException e) {
            throw new CustomException(String.format("交易时间格式错误：{%s}", dealTime));
        }
        TradeRecordDTO saved = exchangeService.saveTradeRecordWithNewTransaction(tradeRecordDto);

        portfolioNames.stream().filter(str -> StringUtils.isNotBlank(str))
                .forEach(portfolioName -> exchangePortfolioService
                        .createExchangeTradePortfolioWithNewTransaction(columns[2], portfolioName));
        return saved;
    }
}
