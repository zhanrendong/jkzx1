package tech.tongyu.bct.document.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.cm.product.iov.ProductTypeEnum;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.document.dao.dbo.PoiTemplate;
import tech.tongyu.bct.document.dao.repl.intel.PoiTemplateRepo;
import tech.tongyu.bct.document.dto.DocProcessStatusEnum;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.poi.PoiTemplateDTO;
import tech.tongyu.bct.document.poi.TradeTypeEnum;
import tech.tongyu.bct.document.service.PoiTemplateService;
import tech.tongyu.bct.document.service.TradeDocumentService;
import tech.tongyu.bct.document.util.TransfromUtil;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.reference.service.MarginService;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.dto.trade.TradeDTO;
import tech.tongyu.bct.trade.dto.trade.TradePositionDTO;
import tech.tongyu.bct.trade.service.TradeLCMService;
import tech.tongyu.bct.trade.service.TradeService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static tech.tongyu.bct.document.dictory.Element.ELEMENT_MAP;
import static tech.tongyu.bct.document.service.impl.DocumentConstants.*;
import static tech.tongyu.bct.document.service.impl.DocumentConstants.SETTLE_AMOUNT;

@Service
public class PoiDocumentServiceImpl implements PoiTemplateService {

    private PoiTemplateRepo poiTemplateRepo;
    private TradeService tradeService;
    private MarketDataService marketDataService;
    private AccountService accountService;
    private MarginService marginService;
    private TradeLCMService tradeLCMService;
    private TradeDocumentService tradeDocumentService;

    @Autowired
    public PoiDocumentServiceImpl(PoiTemplateRepo poiTemplateRepo,
                                  TradeService tradeService,
                                  MarketDataService marketDataService,
                                  AccountService accountService,
                                  MarginService marginService,
                                  TradeLCMService tradeLCMService,
                                  TradeDocumentService tradeDocumentService){
        this.poiTemplateRepo = poiTemplateRepo;
        this.tradeService = tradeService;
        this.marketDataService = marketDataService;
        this.accountService = accountService;
        this.marginService = marginService;
        this.tradeLCMService = tradeLCMService;
        this.tradeDocumentService = tradeDocumentService;
    }

    @Override
    public PoiTemplateDTO findPoiTemplateFile(String poiTemplateId) {
        PoiTemplate poiTemplate = poiTemplateRepo.findById(UUID.fromString(poiTemplateId))
                .orElseThrow(() -> new IllegalArgumentException("invalid uuid"));
        return toPoiTemplateDTO(poiTemplate);
    }

    @Override
    public Tuple2<String, Object> getSettlement(String tradeId, String positionId, String partyName) {

        // 获取交易信息
        OffsetDateTime dateTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        TradeDTO trade = tradeService.getByTradeId(tradeId, dateTime, dateTime);
        Optional<TradePositionDTO> positionDTO = tradeService.getByTradeId(tradeId, dateTime, dateTime).getPositions()
                .stream()
                .filter(p -> p.getPositionId().equals(positionId))
                .findFirst();

        // 根据交易信息选择对应的模板
        ProductTypeEnum productType = positionDTO.get().getProductType();
        boolean annualized = positionDTO.get().getAsset().get("annualized").asBoolean();
        String tradeType = productType.toString();
        StringBuffer stringBuffer = new StringBuffer(tradeType);
        if (annualized) {
            tradeType = stringBuffer.append("_ANNUALIZED").toString();
        }
        Map<String, Object> map = clearData(trade, positionDTO.get(), tradeType);
        // 增加结算补充信息
        Map<String, Object> newMap = addSettleInfo(map, tradeId, positionId);
        Optional<PoiTemplate> poiTemplate = poiTemplateRepo.findByTradeTypeAndDocType(TradeTypeEnum.valueOf(productType.toString()), DocTypeEnum.SETTLE_NOTIFICATION);

        String filePath = poiTemplate.map(PoiTemplate::getFilePath).orElseThrow(() -> new CustomException("没有找到模板文件"));

        String typeSuffix = poiTemplate.get().getTypeSuffix();
        StringBuffer file = new StringBuffer(partyName);

        // 拼接生成文档的名字
        String fileName = file.append("_").append(tradeId).append("_").append(positionId).append("结算通知书").append(".").append(typeSuffix).toString();
        Object doc;
        if ("docx".equals(typeSuffix)) {
            doc = docxPOI(newMap, filePath);
        }else {
            doc = docPOI(newMap, filePath);
        }
        tradeDocumentService.updatePositionDocStatus(positionId, DocProcessStatusEnum.DOWNLOADED);
        return new Tuple2<>(fileName, doc);
    }

    @Override
    public Tuple2<String, Object> getSupplementary(String tradeId, String partyName, String marketInterruptionMessage, String earlyTerminationMessage) {

        // 获取交易信息
        OffsetDateTime dateTime = OffsetDateTime.of(LocalDateTime.now(), ZoneOffset.UTC);
        TradeDTO trade = tradeService.getByTradeId(tradeId, dateTime, dateTime);
        TradePositionDTO tradePositionDTO = trade.getPositions().get(0);

        // 根据交易判断使用的文档模板
        ProductTypeEnum productType = tradePositionDTO.getProductType();
        boolean annualized = tradePositionDTO.getAsset().get("annualized").asBoolean();
        String tradeType = productType.toString();
        StringBuffer stringBuffer = new StringBuffer(tradeType);
        if (annualized) {
            tradeType = stringBuffer.append("_ANNUALIZED").toString();
        }
        Map<String, Object> map = clearData(trade, tradePositionDTO, tradeType);
        map.put("${marketInterruptionMessage}", marketInterruptionMessage);
        map.put("${earlyTerminationMessage}", earlyTerminationMessage);
        // 增加保证金信息
        Map<String, Object> newMap = addMargin(map, partyName);
        // 找到文件保存路径
        Optional<PoiTemplate> poiTemplate = poiTemplateRepo.findByTradeTypeAndDocType(TradeTypeEnum.valueOf(productType.toString()), DocTypeEnum.SUPPLEMENTARY_AGREEMENT);
        String filePath = poiTemplate.map(PoiTemplate::getFilePath).orElseThrow(() -> new CustomException("没有找到模板文件"));
        String typeSuffix = poiTemplate.get().getTypeSuffix();
        StringBuffer file = new StringBuffer(partyName);

        // 拼接生成文档的名字
        String fileName = file.append("_").append(tradeId).append("交易确认书").append(".").append(typeSuffix).toString();
        Object doc;
        if ("docx".equals(typeSuffix)) {
            doc = docxPOI(newMap, filePath);
        }else {
            doc = docPOI(newMap, filePath);
        }
        tradeDocumentService.updateTradeDocStatus(tradeId, DocProcessStatusEnum.DOWNLOADED);
        return new Tuple2<>(fileName, doc);
    }

    private XWPFDocument docxPOI(Map<String, Object> map, String filePath) {

        try{
            XWPFDocument xwpfDocument = new XWPFDocument(new FileInputStream(new File(filePath)));
            Iterator<XWPFParagraph> paragraphsIterator = xwpfDocument.getParagraphsIterator();
            // 编辑文本
            while (paragraphsIterator.hasNext()) {
                XWPFParagraph xwpfParagraph = paragraphsIterator.next();
                List<XWPFRun> runs = xwpfParagraph.getRuns();
                StringBuffer temp = new StringBuffer();
                for (XWPFRun run : runs) {
                    String oneParaString = run.getText(run.getTextPosition());
                    if (StringUtils.isBlank(oneParaString)) {
                        continue;
                    }
                    temp = temp.append(run.getText(run.getTextPosition()));
                    if (!temp.toString().contains("$")){
                        run.setText(oneParaString, 0);
                    }else {
                        if (temp.toString().contains("}")){
                            String before = temp.toString().substring(temp.indexOf("$"), temp.indexOf("}")+1);
                            if(Objects.isNull(map.get(before))){
                                run.setText(temp.toString(), 0);
                            }else {
                                String after =  temp.toString().replace(before, map.get(before).toString());
                                run.setText(after, 0);
                            }
                            temp = new StringBuffer();
                        }else {
                            run.setText("", 0);
                        }
                    }
                }
            }
            // 编辑表格
            Iterator<XWPFTable> tablesIterator = xwpfDocument.getTablesIterator();
            while (tablesIterator.hasNext()) {
                XWPFTable table = tablesIterator.next();
                table.getRows().forEach(row -> {
                    row.getTableCells().forEach(cell -> {
                        String content = cell.getText();
                        if (content.contains("$")) {
                            String temp = content.substring(content.indexOf("$"), content.indexOf("}")+1);
                            if(Objects.isNull(map.get(temp))){
                                String cont = content.replace(temp, "未找到该字段");
                                cell.removeParagraph(0);
                                cell.setText(cont);
                            }else {
                                String cont = content.replace(temp, Objects.toString(map.get(temp), ""));
                                cell.removeParagraph(0);
                                cell.setText(cont);
                            }
                        }
                    });
                });
            }
            return xwpfDocument;
        } catch (IOException e) {
            throw new CustomException("docx模板未找到");
        }
    }

    private HWPFDocument docPOI(Map<String, Object> map, String filePath){

        try{
            HWPFDocument doc = new HWPFDocument(new FileInputStream(new File(filePath)));
            Range range = doc.getRange();
            map.forEach((k,v) -> range.replaceText(k, Objects.toString(v, "")));
            return doc;
        }catch (IOException e){
            throw new CustomException("doc模板未找到");
        }
    }

    private Map<String, Object> clearData(TradeDTO trade, TradePositionDTO tradePositionDTO, String tradeType) {

        // 获取合约代码名称
        String instrumentName = marketDataService.getInstrument(
                tradePositionDTO.getAsset().get(UNDERLYER_INSTRUMENT_ID).asText())
                .get().toInstrumentInfoDTO().getName();

        // 拼接trade信息
        Map<String, Object> data = new HashMap<>();
        data.put("${tradeId}", trade.getTradeId());
        data.put("${tradeDate}", trade.getTradeDate());
        data.put("${tradeStatus}", trade.getTradeStatus());
        data.put("${trader}", trade.getTrader());
        data.put("${bookName}", trade.getBookName());
        data.put("${comment}", trade.getComment());
        data.put("${partyName}", trade.getPartyName());
        data.put("${partyCode}", trade.getPartyCode());
        data.put("${salesCode}", trade.getSalesCode());
        data.put("${salesCommission}", trade.getSalesCommission());
        data.put("${salesName}", trade.getSalesName());

        // 拼接position信息
        data.put("${instrumentName}", instrumentName);
        data.put("${counterPartyAccountCode}", tradePositionDTO.getCounterPartyAccountCode());
        data.put("${counterPartyAccountName}", tradePositionDTO.getCounterPartyAccountName());
        data.put("${counterPartyCode}", tradePositionDTO.getCounterPartyCode());
        data.put("${counterPartyName}", tradePositionDTO.getCounterPartyName());
        data.put("${lcmEventType}", tradePositionDTO.getLcmEventType());
        data.put("${positionAccountCode}", tradePositionDTO.getPositionAccountCode());
        data.put("${positionAccountName}", tradePositionDTO.getPositionAccountName());
        data.put("${productType}", TransfromUtil.transfromPOI(tradePositionDTO.getProductType().toString()));

        // 拼接asset信息
        JsonNode asset = tradePositionDTO.getAsset();
        Map<String, Object> map = JsonUtils.fromJson(asset.toString());
        Map<String, String> element = ELEMENT_MAP.get(tradeType);
        if (Objects.isNull(element)) {
            throw new CustomException("未找到该交易类型的模板:" + tradeType);
        }
        element.forEach((k,v) -> {
            if (asset.get(k) == null){
                data.put(v, TransfromUtil.transfromPOI("null"));
            }else {
                data.put(v, TransfromUtil.transfromPOI(String.valueOf(map.get(k))));
            }
        });
        return data;
    }

    private Map<String, Object> addMargin(Map<String, Object> map, String partyName){
        Map<String, Object> tradeInfo = new HashMap<>();
        tradeInfo.putAll(map);
        try {
            Double margin = accountService.getAccountByLegalName(partyName).getMargin().doubleValue();
            tradeInfo.put("margin", margin);
            return tradeInfo;
        } catch (Exception e){
            return map;
        }
    }

    private Map<String, Object> addSettleInfo(Map<String, Object> map, String tradeId, String positionId){
        Map<String, Object> settleInfo = new HashMap<>();
        settleInfo.putAll(map);
        try {
            // 获取所有的生命周期事件
            List<LCMEventDTO> lcmEventDTOS = tradeLCMService.listLCMEventsByTradeIdAndPositionId(tradeId, positionId);
            // 获取最新的事件
            List<LocalDateTime> localDateTimes = lcmEventDTOS.stream()
                    .map(LCMEventDTO::getCreatedAt)
                    .collect(Collectors.toList());
            localDateTimes.sort(LocalDateTime::compareTo);
            Optional<LCMEventDTO> lcmEventDTO = lcmEventDTOS.stream()
                    .filter(f -> f.createdAt.equals(localDateTimes.get(localDateTimes.size() - 1)))
                    .findAny();
            if (lcmEventDTO.get().getEventDetail() == null){
                settleInfo.put("${underlyerPrice}", TransfromUtil.transfromPOI("null"));
                settleInfo.put("${settleAmount}", TransfromUtil.transfromPOI("null"));
                settleInfo.put("${notionalOldValue}", TransfromUtil.transfromPOI("null"));
                settleInfo.put("${settlement}", TransfromUtil.transfromPOI("null"));
                settleInfo.put("${numOfOption}",TransfromUtil.transfromPOI("null"));
                return settleInfo;
            }
            if (lcmEventDTO.get().getEventDetail().get(UNDERLYER_PRICE) == null) {
                settleInfo.put("${underlyerPrice}", TransfromUtil.transfromPOI("null"));
            }else {
                settleInfo.put("${underlyerPrice}", TransfromUtil.transfromPOI(lcmEventDTO.get().getEventDetail().get(UNDERLYER_PRICE).toString()));
            }
            if (lcmEventDTO.get().getEventDetail().get(SETTLE_AMOUNT) == null) {
                settleInfo.put("${settleAmount}", TransfromUtil.transfromPOI("null"));
            }else {
                settleInfo.put("${settleAmount}", TransfromUtil.transfromPOI(lcmEventDTO.get().getEventDetail().get(SETTLE_AMOUNT).toString()));
            }

            Object notionalOldValue = "null";
            Object settlement = "null";
            Object numOfOption = "null";
            // 判断事件类型
            LCMEventTypeEnum typeEnum = lcmEventDTO.get().getLcmEventType();
            if (Objects.equals(typeEnum, LCMEventTypeEnum.UNWIND_PARTIAL)
                    || Objects.equals(typeEnum, LCMEventTypeEnum.UNWIND)){
                notionalOldValue = lcmEventDTO.get().getEventDetail().get(NOTIONAL_OLD_VALUE);
                settlement = lcmEventDTO.get().getEventDetail().get(UNWIND_AMOUNT);
                numOfOption = Double.parseDouble(notionalOldValue.toString()) - Double.parseDouble(settlement.toString());
            }else if (Objects.equals(typeEnum, LCMEventTypeEnum.KNOCK_OUT)
                    || Objects.equals(typeEnum, LCMEventTypeEnum.EXPIRATION)
                    || Objects.equals(typeEnum, LCMEventTypeEnum.EXERCISE)
                    || Objects.equals(typeEnum, LCMEventTypeEnum.SNOW_BALL_EXERCISE) ){
                notionalOldValue = 0;
                settlement = lcmEventDTO.get().getEventDetail().get(SETTLE_AMOUNT);
                numOfOption = 0;
            }

            settleInfo.put("${notionalOldValue}", TransfromUtil.transfromPOI(notionalOldValue.toString()));
            settleInfo.put("${settlement}", TransfromUtil.transfromPOI(settlement.toString()));
            settleInfo.put("${numOfOption}",TransfromUtil.transfromPOI(numOfOption.toString()));
            return settleInfo;
        } catch (Exception e){
            return map;
        }
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
