package tech.tongyu.bct.market.api;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.response.RpcResponseListPaged;
import tech.tongyu.bct.common.api.response.RpcResponseWithDiagnostics;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.InstrumentDTO;
import tech.tongyu.bct.market.dto.InstrumentTypeEnum;
import tech.tongyu.bct.market.dto.InstrumentWhitelistDTO;
import tech.tongyu.bct.market.service.InstrumentWhitelistService;
import tech.tongyu.bct.market.service.MarketDataService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InstrumentWhitelistApi {

    InstrumentWhitelistService instrumentWhitelistService;
    MarketDataService marketDataService;

    @Autowired
    public InstrumentWhitelistApi(
            InstrumentWhitelistService instrumentWhitelistService,
            MarketDataService marketDataService
    ) {
        this.marketDataService = marketDataService;
        this.instrumentWhitelistService = instrumentWhitelistService;
    }

    private static Logger logger = LoggerFactory.getLogger(InstrumentWhitelistApi.class);

    @BctMethodInfo(
            description = "批量添加/更新标的物白名单列表",
            retDescription = "添加/更新成功的标的物及添加/更新失败的标的物的失败原因",
            retName = "List of InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public RpcResponseWithDiagnostics<List<InstrumentWhitelistDTO>, List<String>> mktInstrumentWhitelistUpload(
            @BctMethodArg(description = "用户上传白名单文件(csv格式)") MultipartFile file)
            throws IllegalStateException {
        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new InputStreamReader(file.getInputStream()));
            if (!file.isEmpty()) {
                logger.info(String.format("start backfilling security whitelist from file (size=%s)", file.getSize()));
                Stream<String> linesFiltered = filterLines(buffer.lines());
                return backfillWhitelist(linesFiltered);
            } else {
                throw new IllegalArgumentException("文件不能为空");
            }
        } catch (IOException e) {
            logger.error("failed to call -> uploadInstrumentWhitelist message ={} parameter ={} ", e.getMessage(), file.getName());
            throw new IllegalStateException("读取文件失败");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }

    }

    @BctMethodInfo(
            description = "批量添加/更新标的物白名单列表",
            retDescription = "批量添加/更新标的物白名单列表",
            retName = "List of InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public RpcResponseWithDiagnostics<List<InstrumentWhitelistDTO>, List<String>> mktAllInstrumentWhitelistSave(
            @BctMethodArg(description = "用户上传白名单数据(csv格式)") List<String> content) {
        Stream<String> linesFiltered = filterLines(content.stream());
        return backfillWhitelist(linesFiltered);
    }

    @BctMethodInfo(
            description = "批量添加/更新标的物白名单列表",
            retDescription = "批量添加/更新标的物白名单列表",
            retName = "List of InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public List<InstrumentWhitelistDTO> mktInstrumentWhitelistSaveBatched(
            @BctMethodArg(description = "待更新的标的物白名单信息") List<Map<String, Object>> content) {
        return content
                .stream()
                .map(c -> {
                    InstrumentWhitelistDTO dto = JsonUtils.mapper.convertValue(c, InstrumentWhitelistDTO.class);
                    return instrumentWhitelistService.saveInstrumentWhitelist(
                            dto.getVenueCode(), dto.getInstrumentId(), dto.getNotionalLimit());
                })
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "保存一条白名单记录",
            retDescription = "白名单记录",
            retName = "InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public InstrumentWhitelistDTO mktInstrumentWhitelistSave(
            @BctMethodArg(description = "交易所代码") String venueCode,
            @BctMethodArg(description = "标的物ID") String instrumentId,
            @BctMethodArg(description = "名义金额上限") Number notionalLimit) {
        return instrumentWhitelistService.saveInstrumentWhitelist(venueCode, instrumentId, notionalLimit.doubleValue());
    }

    @BctMethodInfo(
            description = "获取一条标的物白名单记录",
            retDescription = "白名单记录",
            retName = "InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public InstrumentWhitelistDTO mktInstrumentWhitelistGet(
            @BctMethodArg(description = "标的物ID") String instrumentId) {
        return instrumentWhitelistService.getInstrumentWhitelist(instrumentId)
                .orElseThrow(() -> new IllegalStateException(String.format("标的物%s在白名单中不存在", instrumentId)));
    }

    @BctMethodInfo(
            description = "获取所有标的物白名单记录",
            retDescription = "白名单记录列表",
            retName = "List of InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public List<InstrumentWhitelistDTO> mktInstrumentWhitelistList() {
        return instrumentWhitelistService.listInstrumentWhitelist();
    }

    @BctMethodInfo(
            description = "分页获取所有标的物白名单记录",
            retDescription = "白名单记录分页列表",
            retName = "paged InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public RpcResponseListPaged<InstrumentWhitelistDTO> mktInstrumentWhitelistListPaged(
            @BctMethodArg(description = "标的物ID列表") List<String> instrumentIds,
            @BctMethodArg(description = "当前页数", required = false) Integer page,
            @BctMethodArg(description = "每页数量", required = false) Integer pageSize) {
        Pageable p;
        if (Objects.isNull(page) && Objects.isNull(pageSize)) {
            p = null;
        } else if (!Objects.isNull(page) && !Objects.isNull(pageSize)) {
            p = PageRequest.of(page, pageSize);
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    "分页检索必须同时提供当前页数和每页数量。若不需分页，则页数和每页数量必须同时为空。");
        }
        if (instrumentIds.isEmpty()) {
            return new RpcResponseListPaged(
                    instrumentWhitelistService.listInstrumentWhitelistPaged(p),
                    instrumentWhitelistService.countInstrumentWhitelist());
        } else {
            return new RpcResponseListPaged(
                    instrumentWhitelistService.listInstrumentWhitelistPaged(instrumentIds, p),
                    instrumentWhitelistService.countInstrumentWhitelist(instrumentIds));
        }
    }

    @BctMethodInfo(
            description = "删除所有标的物白名单记录",
            retDescription = "是否成功删除",
            retName = "true or false",
            service = "market-data-service"
    )
    @Transactional
    public Boolean mktAllInstrumentWhitelistDelete() {
        return instrumentWhitelistService.deleteAllInstrumentWhitelist();
    }

    @BctMethodInfo(
            description = "删除标的物白名单记录",
            retDescription = "删除的标的物白名单记录",
            retName = "InstrumentWhitelistDTO",
            returnClass = InstrumentWhitelistDTO.class,
            service = "market-data-service"
    )
    @Transactional
    public List<InstrumentWhitelistDTO> mktInstrumentWhitelistDelete(
            @BctMethodArg(description = "标的物ID列表") List<String> instrumentIds) {
        return instrumentIds.stream()
                .flatMap(i ->
                        instrumentWhitelistService.deleteInstrumentWhitelist(i)
                                .map(l -> Arrays.asList(l).stream()).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    @BctMethodInfo(
            description = "搜索以instrumentIdPart开头的Instrument",
            retName = "instrumentIds",
            retDescription = "以instrumentIdPart开头的标的物代码",
            service = "market-data-service"
    )
    public List<String> mktInstrumentWhitelistSearch(
            @BctMethodArg(description = "instrument id的开始部分") String instrumentIdPart,
            @BctMethodArg(description = "是否过滤场内期权标的", required = false) Boolean excludeOption
    ) {
        List<InstrumentWhitelistDTO> instrumentWhitelistDTOS = instrumentWhitelistService.searchInstruments(instrumentIdPart);
        if (excludeOption != null && excludeOption) {
            InstrumentTypeEnum[] optionInstrument = {
                    InstrumentTypeEnum.FUTURES_OPTION,
                    InstrumentTypeEnum.INDEX_OPTION,
                    InstrumentTypeEnum.STOCK_OPTION,
            };
            List<String> instrumentIds = instrumentWhitelistDTOS.stream().map(InstrumentWhitelistDTO::getInstrumentId).collect(Collectors.toList());
            return marketDataService.listInstruments(instrumentIds, null).stream()
                    .filter(v -> !ArrayUtils.contains(optionInstrument, v.getInstrumentType()))
                    .map(InstrumentDTO::getInstrumentId).collect(Collectors.toList());
        }
        return instrumentWhitelistService.searchInstruments(instrumentIdPart).stream()
                .map(InstrumentWhitelistDTO::getInstrumentId).collect(Collectors.toList());
    }

    private RpcResponseWithDiagnostics<List<InstrumentWhitelistDTO>, List<String>> backfillWhitelist(Stream<String> lines) {
        List<InstrumentWhitelistDTO> success = new LinkedList<>();
        List<String> failure = new LinkedList<>();
        lines.forEach(line -> {
            Optional<InstrumentWhitelistDTO> result = parseAndSaveInstrumentWhitelist(line);
            if (result.isPresent()) {
                success.add(result.get());
            } else {
                failure.add(line);
            }
        });
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

    private Optional<InstrumentWhitelistDTO> parseAndSaveInstrumentWhitelist(String line) {
        try {
            String[] columns = line.split(",");
            if (columns.length == 2) {
                String venueCode = columns[0].trim();
                String instrumentId = columns[1].trim();
                Double notionalLimit = Double.POSITIVE_INFINITY;
                return Optional.of(instrumentWhitelistService.saveInstrumentWhitelist(venueCode, instrumentId, notionalLimit));
            } else if (columns.length > 2) {
                String venueCode = columns[0].trim();
                String instrumentId = columns[1].trim();
                Double notionalLimit = Double.valueOf(columns[2]);
                return Optional.of(instrumentWhitelistService.saveInstrumentWhitelist(venueCode, instrumentId, notionalLimit));
            } else {
                logger.error(String.format("%s白名单格式错误", line));
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error(String.format("%s白名单格式错误", line));
            return Optional.empty();
        }
    }

    private Stream<String> filterLines(Stream<String> lines) {
        return lines.map(i -> i.trim()).filter(i -> !i.isEmpty());
    }
}
