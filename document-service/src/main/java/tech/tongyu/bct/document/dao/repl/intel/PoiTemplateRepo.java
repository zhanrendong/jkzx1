package tech.tongyu.bct.document.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.document.dao.dbo.PoiTemplate;
import tech.tongyu.bct.document.ext.dto.DocTypeEnum;
import tech.tongyu.bct.document.poi.TradeTypeEnum;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PoiTemplateRepo extends JpaRepository<PoiTemplate, UUID> {

    Optional<PoiTemplate> findByTradeTypeAndDocType(TradeTypeEnum tradeType, DocTypeEnum docType);

}
