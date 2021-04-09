package tech.tongyu.bct.trade.service;

import tech.tongyu.bct.trade.dto.trade.QuotePrcDTO;
import tech.tongyu.bct.trade.dto.trade.QuotePositionDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface QuotePositionService {

    void deleteById(UUID uuid);

    void save(QuotePrcDTO quotePrcDto);

    void deleteByQuotePrcId(String quotePrcId);

    List<QuotePrcDTO> search(QuotePositionDTO quoteSearchDto, String comment,
                             LocalDate expirationStartDate, LocalDate expirationEndDate);

}
