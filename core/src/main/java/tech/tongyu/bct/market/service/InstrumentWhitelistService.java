package tech.tongyu.bct.market.service;

import tech.tongyu.bct.market.dto.InstrumentWhitelistDTO;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface InstrumentWhitelistService {
    Optional<InstrumentWhitelistDTO> getInstrumentWhitelist(String instrumentId);

    List<InstrumentWhitelistDTO> listInstrumentWhitelist();

    Optional<InstrumentWhitelistDTO> deleteInstrumentWhitelist(String instrumentId);

    Boolean deleteAllInstrumentWhitelist();

    InstrumentWhitelistDTO saveInstrumentWhitelist(String venueCode, String instrumentId, Double notionalLimit);

    default Boolean inWhitelist(String instrumentId) {
        return getInstrumentWhitelist(instrumentId).isPresent();
    }

    List<InstrumentWhitelistDTO> listInstrumentWhitelistPaged(Pageable page);

    List<InstrumentWhitelistDTO> listInstrumentWhitelistPaged(List<String> instrumentIds, Pageable page);

    Long countInstrumentWhitelist();

    Long countInstrumentWhitelist(List<String> instrumentIds);

    List<InstrumentWhitelistDTO> searchInstruments(String instrumentIdPart);
}
