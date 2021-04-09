package tech.tongyu.bct.trade.service.mock;

import org.springframework.data.domain.Pageable;
import tech.tongyu.bct.market.dto.*;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MockMarketDataService implements MarketDataService {

    @Override
    public Optional<QuoteDTO> getQuote(String instrumentId, InstanceEnum instance, LocalDate valuationDate, ZoneId quoteTimezone) {
        return Optional.empty();
    }

    @Override
    public List<QuoteDTO> listLatestQuotes(List<String> instrumentIds) {
        return null;
    }

    @Override
    public List<QuoteDTO> listCloseQuotes(List<String> instrumentIds, LocalDate valuationDate, ZoneId quoteTimezone) {
        return null;
    }

    @Override
    public List<Optional<QuoteDTO>> listQuotes(List<QuoteLocator> locators) {
        return null;
    }

    @Override
    public Map<QuoteFieldLocator, Double> listQuotesByField(List<QuoteFieldLocator> locators) {
        return null;
    }

    @Override
    public List<CombinedQuoteDTO> listQuotes(List<String> instrumentIds, LocalDate valuationDate, ZoneId quoteTimezone) {
        return null;
    }

    @Override
    public Optional<QuoteDTO> deleteQuote(String instrumentId, InstanceEnum instance, LocalDate valuationDate, ZoneId quoteTimezone) {
        return Optional.empty();
    }

    @Override
    public QuoteDTO saveQuote(String instrumentId, InstanceEnum instance, Map<QuoteFieldEnum, Double> quote, LocalDate valuationDate, LocalDateTime quoteTimestamp, ZoneId quoteTimezone) {
        return null;
    }

    @Override
    public List<String> saveQuotes(List<QuoteDTO> quotes) {
        return null;
    }

    @Override
    public QuoteDTO saveQuote(QuoteDTO quoteDTO) {
        return null;
    }

    @Override
    public Optional<InstrumentDTO> getInstrument(String instrumentId) {
        InstrumentDTO instrumentDTO = new InstrumentDTO();
        instrumentDTO.setInstrumentId(instrumentId);
        instrumentDTO.setInstrumentType(InstrumentTypeEnum.STOCK);
        instrumentDTO.setAssetClass(AssetClassEnum.EQUITY);
        return Optional.of(instrumentDTO);
    }

    @Override
    public Optional<InstrumentDTO> deleteInstrument(String instrumentId) {
        return Optional.empty();
    }

    @Override
    public InstrumentDTO saveInstrument(String instrumentId, AssetClassEnum assetClassEnum, AssetSubClassEnum assetSubClass,
                                        InstrumentTypeEnum instrumentTypeEnum, InstrumentInfo instrumentInfo) {
        return null;
    }

    @Override
    public List<InstrumentDTO> listInstruments(Pageable p) {
        return null;
    }

    @Override
    public List<InstrumentDTO> listInstrumentsByTemplate(InstrumentDTO template) {
        return null;
    }

    @Override
    public long countInstruments() {
        return 0;
    }

    @Override
    public List<InstrumentDTO> listInstruments(List<String> instrumentIds, Pageable p) {
        return null;
    }

    @Override
    public long countInstruments(List<String> instrumentIds) {
        return 0;
    }

    @Override
    public List<InstrumentDTO> listInstruments(AssetClassEnum assetClass, InstrumentTypeEnum instrumentType, Pageable p) {
        return null;
    }

    @Override
    public long countInstruments(AssetClassEnum assetClass, InstrumentTypeEnum instrumentType) {
        return 0;
    }

    @Override
    public List<InstrumentDTO> searchInstruments(String instrumentIdPart, Optional<AssetClassEnum> assetClassEnum) {
        return null;
    }
}
