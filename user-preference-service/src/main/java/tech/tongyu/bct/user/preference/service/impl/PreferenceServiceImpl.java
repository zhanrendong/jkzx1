package tech.tongyu.bct.user.preference.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.user.preference.dao.dbo.Preference;
import tech.tongyu.bct.user.preference.dao.repo.intel.PreferenceRepo;
import tech.tongyu.bct.user.preference.dto.PreferenceDTO;
import tech.tongyu.bct.user.preference.service.PreferenceService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PreferenceServiceImpl implements PreferenceService {
    PreferenceRepo preferenceRepo;

    @Autowired
    public PreferenceServiceImpl(PreferenceRepo preferenceRepo) {
        this.preferenceRepo = preferenceRepo;
    }

    @Override
    public PreferenceDTO createPreference(String userName, List<String> volInstruments, List<String> dividendInstruments) {
        Optional<Preference> byUserName = preferenceRepo.findByUserName(userName);
        if (byUserName.isPresent()) {
            throw new IllegalArgumentException(String.format("用户%s的偏好已经存在", userName));
        } else {
            return doCreatePreference(userName, volInstruments, dividendInstruments);
        }
    }

    @Override
    public Optional<PreferenceDTO> getPreference(String userName) {
        return preferenceRepo.findByUserName(userName).map(this::toDTO);
    }

    @Override
    public PreferenceDTO addVolInstruments(String userName, List<String> volInstruments) {
        return preferenceRepo.findByUserName(userName)
                .map(user -> {
                    List<String> existing = user.getVolSurfaceInstrumentIds();
                    List<String> newInst = Stream.concat(existing.stream(), volInstruments.stream())
                            .collect(Collectors.toSet())
                            .stream().collect(Collectors.toList());
                    user.setVolSurfaceInstrumentIds(newInst);
                    return toDTO(preferenceRepo.save(user));
                }).orElseGet(() -> doCreatePreference(userName, volInstruments, Arrays.asList()));
    }

    @Override
    public PreferenceDTO addDividendInstruments(String userName, List<String> dividendInstruments) {
        return preferenceRepo.findByUserName(userName)
                .map(user -> {
                    List<String> existing = user.getDividendCurveInstrumentIds();
                    List<String> newInst = Stream.concat(existing.stream(), dividendInstruments.stream())
                            .collect(Collectors.toSet())
                            .stream().collect(Collectors.toList());
                    user.setDividendCurveInstrumentIds(newInst);
                    return toDTO(preferenceRepo.save(user));
                })
                .orElseGet(() -> doCreatePreference(userName, Arrays.asList(), dividendInstruments));
    }

    @Override
    public PreferenceDTO deleteVolInstruments(String userName, List<String> volInstruments) {
        return preferenceRepo.findByUserName(userName)
                .map(user -> {
                    List<String> existing = user.getVolSurfaceInstrumentIds();
                    List<String> newInst = existing.stream()
                            .filter(i -> !volInstruments.contains(i))
                            .collect(Collectors.toList());
                    user.setVolSurfaceInstrumentIds(newInst);
                    return toDTO(preferenceRepo.save(user));
                }).orElseThrow(() -> new IllegalArgumentException(String.format("用户%s的偏好不存在", userName)));
    }

    @Override
    public PreferenceDTO deleteDividendInstruments(String userName, List<String> dividendInstruments) {
        return preferenceRepo.findByUserName(userName)
                .map(user -> {
                    List<String> existing = user.getDividendCurveInstrumentIds();
                    List<String> newInst = existing.stream()
                            .filter(i -> !dividendInstruments.contains(i))
                            .collect(Collectors.toList());
                    user.setDividendCurveInstrumentIds(newInst);
                    return toDTO(preferenceRepo.save(user));
                }).orElseThrow(() -> new IllegalArgumentException(String.format("用户%s的偏好不存在", userName)));
    }

    PreferenceDTO toDTO(Preference pref) {
        return new PreferenceDTO(pref.getUserName(), pref.getVolSurfaceInstrumentIds(), pref.getDividendCurveInstrumentIds());
    }

    PreferenceDTO doCreatePreference(String userName, List<String> volInstruments, List<String> dividendInstruments) {
        Preference pref = new Preference();
        pref.setUserName(userName);
        pref.setVolSurfaceInstrumentIds(volInstruments);
        pref.setDividendCurveInstrumentIds(dividendInstruments);
        preferenceRepo.save(pref);
        return toDTO(pref);
    }
}
