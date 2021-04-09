package tech.tongyu.bct.document.ext.service;

import tech.tongyu.bct.document.ext.dto.BctTemplateDTO;
import tech.tongyu.bct.document.ext.dto.CategoryEnum;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.poi.PoiTemplateDTO;
import tech.tongyu.bct.document.poi.TradeTypeEnum;

import java.io.StringWriter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * BCT3的文档服务。该服务基于通用文档服务
 * １．一个＂模板文件夹＂只有一个＂模板＂;
 * ２．一个＂模板文件夹＂代表一个类别;
 * ３．一个＂模板＂关联一个＂模板文件＂;
 * @author lvbing
 */
public interface BctDocumentService {
    String SCHEMA = "documentService";

    BctTemplateDTO docTemplateCreate(BctTemplateDTO dto);

    List<BctTemplateDTO> docBctTemplateList(CategoryEnum category);

    Optional<BctTemplateDTO> getBctTemplateDTO(UUID bctTemplateId);

    BctTemplateDTO updateBctTemplateDTO(BctTemplateDTO dto);

    BctTemplateDTO getBctTemplateDTOByDocType(DocTypeEnum docTypeEnum);

    BctTemplateDTO getBctTemplateDTOByDocTypeAndTransactType(DocTypeEnum docTypeEnum, String transactType);

    StringWriter getSettlement(String tradeId, String positionId);

    StringWriter getSupplementary(String tradeId, String description7, String description8);

    PoiTemplateDTO docPoiTemplateCreate(TradeTypeEnum tradeType, DocTypeEnum docType);

    Optional<PoiTemplateDTO> getPoiTemplateDTO(UUID poiTemplateId);

    PoiTemplateDTO updatePoiTemplateDTO(PoiTemplateDTO poiTemplateDTO);

    List<PoiTemplateDTO> docPoiTemplateList();

    String deletePoiTemplate(UUID poiTemplateId);
}
