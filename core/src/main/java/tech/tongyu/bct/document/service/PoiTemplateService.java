package tech.tongyu.bct.document.service;

import io.vavr.Tuple2;
import tech.tongyu.bct.document.poi.PoiTemplateDTO;

public interface PoiTemplateService {

    PoiTemplateDTO findPoiTemplateFile(String poiTemplateId);

    Tuple2<String, Object> getSupplementary(String tradeId, String partyName, String marketInterruptionMessage, String earlyTerminationMessage);

    Tuple2<String, Object> getSettlement(String tradeId, String positionId, String partyName);
}


