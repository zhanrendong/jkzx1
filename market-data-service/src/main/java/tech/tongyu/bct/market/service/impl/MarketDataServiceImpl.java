package tech.tongyu.bct.market.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dao.dbo.Instrument;
import tech.tongyu.bct.market.dao.dbo.QuoteClose;
import tech.tongyu.bct.market.dao.dbo.QuoteIntraday;
import tech.tongyu.bct.market.dao.repo.intel.InstrumentRepo;
import tech.tongyu.bct.market.dao.repo.intel.QuoteCloseRepo;
import tech.tongyu.bct.market.dao.repo.intel.QuoteIntradayRepo;
import tech.tongyu.bct.market.dto.*;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.pricing.common.QuoteFieldLocator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class MarketDataServiceImpl implements MarketDataService {
    private InstrumentRepo instrumentRepo;
    private QuoteCloseRepo quoteCloseRepo;
    private QuoteIntradayRepo quoteIntradayRepo;

    @Autowired
    public MarketDataServiceImpl(InstrumentRepo instrumentRepo,
                                 QuoteCloseRepo quoteCloseRepo,
                                 QuoteIntradayRepo quoteIntradayRepo) {
        this.instrumentRepo = instrumentRepo;
        this.quoteCloseRepo = quoteCloseRepo;
        this.quoteIntradayRepo = quoteIntradayRepo;
    }

    private QuoteDTO convertToDTO(QuoteClose q) {
        Map<QuoteFieldEnum, Double> fields = new HashMap<>();
        fields.put(QuoteFieldEnum.OPEN, q.getOpen());
        fields.put(QuoteFieldEnum.CLOSE, q.getClose());
        fields.put(QuoteFieldEnum.SETTLE, q.getSettle());
        fields.put(QuoteFieldEnum.HIGH, q.getHigh());
        fields.put(QuoteFieldEnum.LOW, q.getLow());
        return new QuoteDTO(q.getInstrumentId(), InstanceEnum.CLOSE, q.getValuationDate(),
                q.getQuoteTimestamp(), q.getQuoteTimezone(), fields);
    }

    private QuoteDTO convertToDTO(QuoteIntraday q) {
        Map<QuoteFieldEnum, Double> fieldsIntraday = new HashMap<>();
        fieldsIntraday.put(QuoteFieldEnum.OPEN, q.getOpen());
        fieldsIntraday.put(QuoteFieldEnum.YESTERDAY_CLOSE, q.getYesterdayClose());
        fieldsIntraday.put(QuoteFieldEnum.BID, q.getBid());
        fieldsIntraday.put(QuoteFieldEnum.ASK, q.getAsk());
        fieldsIntraday.put(QuoteFieldEnum.LAST, q.getLast());
        return new QuoteDTO(q.getInstrumentId(), InstanceEnum.INTRADAY, q.getValuationDate(),
                q.getQuoteTimestamp(), q.getQuoteTimezone(), fieldsIntraday);
    }

    private Optional<InstrumentDTO> convertToDTO(Instrument i) {
        try {
            return Optional.of(new InstrumentDTO(i.getInstrumentId(), i.getAssetClass(), i.getAssetSubClass(),
                    i.getInstrumentType(), JsonUtils.mapper.treeToValue(i.getInstrumentInfo(), InstrumentInfo.class)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<QuoteDTO> getQuote(String instrumentId, InstanceEnum instance,
                                       LocalDate valuationDate, ZoneId quoteTimezone) {
        switch (instance) {
            case CLOSE:
                List<QuoteClose> qcs = quoteCloseRepo.findQuotes(instrumentId, valuationDate);
                if (qcs.size() == 0)
                    return Optional.empty();
                return Optional.of(convertToDTO(qcs.get(0)));
            case INTRADAY:
                Optional<QuoteIntraday> qi = quoteIntradayRepo.findByInstrumentId(instrumentId);
                return qi.map(this::convertToDTO);
            default:
                return Optional.empty();
        }
    }

    @Override
    public List<QuoteDTO> listLatestQuotes(List<String> instrumentIds) {
        return quoteIntradayRepo.findAllByInstrumentIdInOrderByInstrumentIdAsc(instrumentIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuoteDTO> listCloseQuotes(List<String> instrumentIds,
                                          LocalDate valuationDate, ZoneId quoteTimezone) {
        Map<String, QuoteDTO> sortingMap = new HashMap<>();
        quoteCloseRepo.findQuotes(instrumentIds, valuationDate, quoteTimezone, null)
                .forEach(q -> {
                    QuoteDTO dto = convertToDTO(q);
                    if (sortingMap.containsKey(dto.getInstrumentId())) {
                        QuoteDTO existing = sortingMap.get(q.getInstrumentId());
                        if (existing.getValuationDate().isBefore(dto.getValuationDate())) {
                            sortingMap.put(q.getInstrumentId(), dto);
                        }
                    } else {
                        sortingMap.put(q.getInstrumentId(), dto);
                    }
                });
        return new ArrayList<>(sortingMap.values())
                .stream().sorted(Comparator.comparing(QuoteDTO::getInstrumentId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Optional<QuoteDTO>> listQuotes(List<QuoteLocator> locators) {
        return locators.parallelStream()
                .map(l -> getQuote(l.getInstrumentId(), l.getInstance(), l.getValuationDate(), l.getQuoteTimezone()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<QuoteFieldLocator, Double> listQuotesByField(List<QuoteFieldLocator> locators) {
        Map<QuoteFieldLocator, Double> ret = new HashMap<>();
        List<Optional<QuoteDTO>> quotes = listQuotes(locators.stream()
                .map(QuoteFieldLocator::toQuoteLocator)
                .collect(Collectors.toList()));
        for (int i = 0; i < locators.size(); ++i) {
            QuoteFieldEnum f = locators.get(i).getField();
            if (quotes.get(i).isPresent()) {
                QuoteDTO dto = quotes.get(i).get();
                if (!Objects.isNull(dto.getFields().get(f))) {
                    ret.put(locators.get(i), dto.getFields().get(f));
                }
            }
        }
        return ret;
    }

    @Override
    @Transactional
    public Optional<QuoteDTO> deleteQuote(String instrumentId, InstanceEnum instance,
                                          LocalDate valuationDate, ZoneId quoteTimezone) {
        switch (instance) {
            case CLOSE:
                List<QuoteClose> qcs = quoteCloseRepo.findQuotes(instrumentId, valuationDate,
                        quoteTimezone, PageRequest.of(0, 1));
                if (qcs.size() == 0)
                    return Optional.empty();
                quoteCloseRepo.delete(qcs.get(0));
                return Optional.of(convertToDTO(qcs.get(0)));
            case INTRADAY:
                List<QuoteIntraday> qis = quoteIntradayRepo.deleteByInstrumentId(instrumentId);
                if (qis.size() == 0)
                    return Optional.empty();
                return Optional.of(convertToDTO(qis.get(0)));
            default:
                return Optional.empty();
        }
    }

    @Override
    @Transactional
    public QuoteDTO saveQuote(String instrumentId, InstanceEnum instance,
                              Map<QuoteFieldEnum, Double> quote,
                              LocalDate valuationDate, LocalDateTime quoteTimestamp, ZoneId quoteTimezone) {
        switch (instance) {
            case CLOSE:
                QuoteClose qc = new QuoteClose(instrumentId, valuationDate,
                        quote.get(QuoteFieldEnum.CLOSE),
                        quote.get(QuoteFieldEnum.SETTLE),
                        quote.get(QuoteFieldEnum.OPEN),
                        quote.get(QuoteFieldEnum.LOW),
                        quote.get(QuoteFieldEnum.HIGH),
                        quoteTimestamp, quoteTimezone);
                List<QuoteClose> existedClose = quoteCloseRepo.findQuotes(instrumentId,
                        valuationDate, quoteTimezone, null);
                if (existedClose.size() > 0 && existedClose.get(0).getValuationDate().isEqual(valuationDate)) {
                    qc.setUuid(existedClose.get(0).getUuid());
                }
                QuoteClose savedClose = quoteCloseRepo.save(qc);
                return convertToDTO(savedClose);
            default:
                QuoteIntraday qi = new QuoteIntraday(instrumentId, valuationDate,
                        quote.get(QuoteFieldEnum.LAST),
                        quote.get(QuoteFieldEnum.BID),
                        quote.get(QuoteFieldEnum.ASK),
                        quote.get(QuoteFieldEnum.OPEN),
                        quote.get(QuoteFieldEnum.YESTERDAY_CLOSE),
                        quoteTimestamp, quoteTimezone);
                Optional<QuoteIntraday> existedIntraday = quoteIntradayRepo.findByInstrumentId(instrumentId);
                existedIntraday.ifPresent(e -> qi.setUuid(e.getUuid()));
                QuoteIntraday savedIntraday = quoteIntradayRepo.save(qi);
                return convertToDTO(savedIntraday);
        }
    }

    @Override
    @Transactional
    public QuoteDTO saveQuote(QuoteDTO quoteDTO) {
        return saveQuote(quoteDTO.getInstrumentId(), quoteDTO.getInstance(),
                quoteDTO.getFields(), quoteDTO.getValuationDate(),
                quoteDTO.getQuoteTimestamp(), quoteDTO.getQuoteTimezone());
    }

    @Override
    @Transactional
    public List<String> saveQuotes(List<QuoteDTO> quotes) {
        return quotes.stream()
                .map(this::saveQuote)
                .map(QuoteDTO::getInstrumentId)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<InstrumentDTO> getInstrument(String instrumentId) {
        return instrumentRepo.findByInstrumentId(instrumentId).flatMap(this::convertToDTO);
    }

    @Override
    public Optional<InstrumentDTO> deleteInstrument(String instrumentId) {
        List<Instrument> deleted = instrumentRepo.deleteByInstrumentId(instrumentId);
        if (deleted.size() == 0)
            return Optional.empty();
        return convertToDTO(deleted.get(0));
    }

    @Override
    public InstrumentDTO saveInstrument(String instrumentId, AssetClassEnum assetClassEnum, AssetSubClassEnum assetSubClass,
                                        InstrumentTypeEnum instrumentTypeEnum, InstrumentInfo instrumentInfo) {
        JsonNode info = JsonUtils.mapper.valueToTree(instrumentInfo);
        Instrument instrument = new Instrument(instrumentId, assetClassEnum, assetSubClass, instrumentTypeEnum, info);
        Optional<Instrument> existing = instrumentRepo.findByInstrumentId(instrumentId);
        existing.ifPresent(instrument1 -> instrument.setUuid(instrument1.getUuid()));
        Instrument saved = instrumentRepo.save(instrument);
        return convertToDTO(saved).get();
    }

    @Override
    public List<InstrumentDTO> listInstruments(Pageable p) {
        return instrumentRepo.findAllByOrderByInstrumentIdAsc(p).stream()
                .map(this::convertToDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstrumentDTO> listInstrumentsByTemplate(InstrumentDTO instrumentDTO) {
        Instrument template = new Instrument();
        BeanUtils.copyProperties(instrumentDTO, template);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return instrumentRepo.findAll(Example.of(template, exampleMatcher)).stream()
                .map(this::convertToDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(InstrumentDTO::getInstrumentId))
                .collect(Collectors.toList());
    }

    @Override
    public long countInstruments() {
        return instrumentRepo.count();
    }

    @Override
    public List<InstrumentDTO> listInstruments(List<String> instrumentIds, Pageable p) {
        return instrumentRepo.findByInstrumentIdInOrderByInstrumentIdAsc(instrumentIds, p).stream()
                .map(this::convertToDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstrumentDTO> searchInstruments(String instrumentIdPart, Optional<AssetClassEnum> assetClassEnum) {
        List<AssetClassEnum> assetClasses = assetClassEnum
                .map(ac -> Arrays.asList(ac))
                .orElseGet(() -> Stream.of(AssetClassEnum.values()).collect(Collectors.toList()));
        return instrumentRepo
                .findByInstrumentIdStartingWithAndAssetClassInOrderByInstrumentIdAsc(instrumentIdPart, assetClasses)
                .stream()
                .map(this::convertToDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public long countInstruments(List<String> instrumentIds) {
        return instrumentRepo.countByInstrumentIdIn(instrumentIds);
    }

    @Override
    public List<InstrumentDTO> listInstruments(AssetClassEnum assetClass,
                                               InstrumentTypeEnum instrumentType, Pageable p) {
        return instrumentRepo.findByAssetClassAndInstrumentType(assetClass, instrumentType, p).stream()
                .map(this::convertToDTO)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public long countInstruments(AssetClassEnum assetClass,
                                 InstrumentTypeEnum instrumentType) {
        return instrumentRepo.countByAssetClassAndInstrumentType(assetClass, instrumentType);
    }

    @Override
    public List<CombinedQuoteDTO> listQuotes(List<String> instrumentIds,
                                             LocalDate valuationDate, ZoneId quoteTimezone) {
        // just be cautious
        List<String> uniqueInstruments = instrumentIds.stream().distinct().collect(Collectors.toList());
        List<QuoteDTO> quotes = listLatestQuotes(uniqueInstruments);
        quotes.addAll(listCloseQuotes(uniqueInstruments, valuationDate, quoteTimezone));
        // combine intraday and close
        Map<String, CombinedQuoteDTO> combined = new HashMap<>();
        for (QuoteDTO q : quotes) {
            String instrumentId = q.getInstrumentId();
            CombinedQuoteDTO dto;
            if (!combined.containsKey(instrumentId)) {
                dto = new CombinedQuoteDTO();
                dto.setInstrumentId(instrumentId);
                // add instrument info
                Optional<InstrumentDTO> info = getInstrument(instrumentId);
                info.ifPresent(i -> {
                    InstrumentInfoDTO instrumentInfoDTO = i.toInstrumentInfoDTO();
                    dto.setAssetClass(instrumentInfoDTO.getAssetClass());
                    dto.setInstrumentType(instrumentInfoDTO.getInstrumentType());
                    dto.setInstrumentName(instrumentInfoDTO.getName());
                    dto.setExchange(instrumentInfoDTO.getExchange());
                    dto.setMaturity(Optional.ofNullable(instrumentInfoDTO.getMaturity()).orElse(instrumentInfoDTO.getExpirationDate()));
                    dto.setMultiplier(instrumentInfoDTO.getMultiplier());
                });
            } else {
                dto = combined.get(instrumentId);
            }
            q.getFields().forEach((k, v) -> {
                if (!Objects.isNull(v)) {
                    dto.setQuoteField(k, v);
                }
            });
            if (q.getInstance() == InstanceEnum.CLOSE) {
                dto.setCloseValuationDate(q.getValuationDate());
            } else {
                dto.setIntradayQuoteTimestamp(q.getQuoteTimestamp());
            }
            combined.put(instrumentId, dto);
        }
        return new ArrayList<>(combined.values())
                .stream().sorted(Comparator.comparing(CombinedQuoteDTO::getInstrumentId))
                .collect(Collectors.toList());
    }

}
