package tech.tongyu.bct.user.preference.service;

import org.junit.BeforeClass;
import org.junit.Test;
import tech.tongyu.bct.user.preference.dao.repo.intel.PreferenceRepo;
import tech.tongyu.bct.user.preference.dto.PreferenceDTO;
import tech.tongyu.bct.user.preference.mock.MockPreferenceRepository;
import tech.tongyu.bct.user.preference.service.impl.PreferenceServiceImpl;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class PreferenceServiceTest {
    static PreferenceRepo repo = new MockPreferenceRepository();
    static PreferenceService preferenceService = new PreferenceServiceImpl(repo);

    @BeforeClass
    public static void init() {
        preferenceService.createPreference("test", Arrays.asList("a"), Arrays.asList("b"));
    }

    @Test
    public void testPreferenceService() {
        Optional<PreferenceDTO> test = preferenceService.getPreference("hello");
        assertTrue(!test.isPresent());
        test = preferenceService.getPreference("test");
        assertTrue(test.isPresent());
        assertTrue(test.get().getUserName().equals("test"));
        assertTrue(test.get().getVolSurfaceInstrumentIds().equals(Arrays.asList("a")));
        assertTrue(test.get().getDividendCurveInstrumentIds().equals(Arrays.asList("b")));
        try {
            preferenceService.createPreference("test", Arrays.asList(), Arrays.asList());
            assertTrue(false);
        } catch (Exception e) {
        }
        preferenceService.addVolInstruments("test", Arrays.asList("a", "b"));
        test = preferenceService.getPreference("test");
        Set<String> vols = test.get().getVolSurfaceInstrumentIds().stream().collect(Collectors.toSet());
        assertTrue(vols.equals(Arrays.asList("a", "b").stream().collect(Collectors.toSet())));
        preferenceService.deleteDividendInstruments("test", Arrays.asList("b", "c"));
        test = preferenceService.getPreference("test");
        assertTrue(test.get().getDividendCurveInstrumentIds().size() == 0);
        preferenceService.addVolInstruments("hello", Arrays.asList("c"));
        test = preferenceService.getPreference("hello");
        assertTrue(test.isPresent());
        assertTrue(test.get().getVolSurfaceInstrumentIds().stream().collect(Collectors.toSet()).equals(
                Arrays.asList("c").stream().collect(Collectors.toSet())));
    }
}
