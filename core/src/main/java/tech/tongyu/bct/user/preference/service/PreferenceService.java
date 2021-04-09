package tech.tongyu.bct.user.preference.service;

import tech.tongyu.bct.user.preference.dto.PreferenceDTO;

import java.util.List;
import java.util.Optional;

public interface PreferenceService {
    String SCHEMA = "userPreferenceService";

    PreferenceDTO createPreference(String userName, List<String> volInstruments, List<String> dividendInstruments);

    Optional<PreferenceDTO> getPreference(String userName);

    PreferenceDTO addVolInstruments(String userName, List<String> volInstruments);

    PreferenceDTO addDividendInstruments(String userName, List<String> dividendInstruments);

    PreferenceDTO deleteVolInstruments(String userName, List<String> volInstruments);

    PreferenceDTO deleteDividendInstruments(String userName, List<String> dividendInstruments);
}
