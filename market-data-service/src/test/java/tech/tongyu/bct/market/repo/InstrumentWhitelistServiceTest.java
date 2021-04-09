package tech.tongyu.bct.market.repo;

import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.api.response.RpcResponseWithDiagnostics;
import tech.tongyu.bct.market.api.InstrumentWhitelistApi;
import tech.tongyu.bct.market.dao.repo.intel.InstrumentWhitelistRepo;
import tech.tongyu.bct.market.dto.InstrumentWhitelistDTO;
import tech.tongyu.bct.market.service.InstrumentWhitelistService;
import tech.tongyu.bct.market.service.MarketDataService;
import tech.tongyu.bct.market.service.impl.InstrumentWhitelistServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class InstrumentWhitelistServiceTest {
    InstrumentWhitelistRepo repo;

    InstrumentWhitelistService instrumentWhitelistService;

    InstrumentWhitelistApi instrumentWhitelistApi;

    MarketDataService marketDataService;

    {
        repo = new MockInstrumentWhitelistRepo();
        instrumentWhitelistService = new InstrumentWhitelistServiceImpl(repo);
        instrumentWhitelistApi = new InstrumentWhitelistApi(instrumentWhitelistService, marketDataService);
    }

    @Test
    public void testGetInstrumentWhitelist() {
        Double nl1 = 100.0;
        instrumentWhitelistService.saveInstrumentWhitelist("c","a1", nl1);
        Optional<InstrumentWhitelistDTO> w1 = instrumentWhitelistService.getInstrumentWhitelist("a1");
        assertTrue(w1.isPresent());
        assertTrue(w1.get().getNotionalLimit().equals(nl1));
        Double nl2 = Double.POSITIVE_INFINITY;
        instrumentWhitelistService.saveInstrumentWhitelist("c","a2", nl2);
        w1 = instrumentWhitelistService.getInstrumentWhitelist("a2");
        assertTrue(w1.get().getNotionalLimit() > nl1);
    }

    @Test
    public void testDeleteWhitelist() {
        instrumentWhitelistService.saveInstrumentWhitelist("c","b1", 1000.0);
        Optional<InstrumentWhitelistDTO> w1 = instrumentWhitelistService.getInstrumentWhitelist("b1");
        assertTrue(w1.isPresent());
        Optional<InstrumentWhitelistDTO> w2 = instrumentWhitelistService.deleteInstrumentWhitelist("b1");
        assertTrue(w2.get().getNotionalLimit().equals(w1.get().getNotionalLimit()));
        Optional<InstrumentWhitelistDTO> w3 = instrumentWhitelistService.getInstrumentWhitelist("b1");
        assertFalse(w3.isPresent());
    }

    @Test
    public void testDeleteAndGetAll() {
        instrumentWhitelistService.saveInstrumentWhitelist("c","z", 1000.0);
        List<InstrumentWhitelistDTO> all = instrumentWhitelistService.listInstrumentWhitelist();
        assertFalse(all.isEmpty());
        instrumentWhitelistService.deleteAllInstrumentWhitelist();
        all = instrumentWhitelistService.listInstrumentWhitelist();
        assertTrue(all.isEmpty());
    }

    @Test
    public void testUploadApi() throws IOException {
        instrumentWhitelistApi.mktAllInstrumentWhitelistDelete();
        //c1 has both id and value, c2 only has id and should save a default value as Double.Positive_Infinity
        String file1Content = new StringBuffer().append("d,c1,100.0\n").append("d,c2\n").toString();
        MultipartFile file1 = toMockFile(file1Content);
        RpcResponseWithDiagnostics<List<InstrumentWhitelistDTO>, List<String>> r1 =
                instrumentWhitelistApi.mktInstrumentWhitelistUpload(file1);
        List<InstrumentWhitelistDTO> result1 = r1.getResult();
        List<String> diagnostics1 = r1.getDiagnostics();
        assertTrue(result1.size() == 2);
        assertTrue(diagnostics1.isEmpty());
        InstrumentWhitelistDTO c1 = instrumentWhitelistApi.mktInstrumentWhitelistGet("c1");
        assertTrue(c1.getInstrumentId().equals("c1"));
        assertTrue(c1.getNotionalLimit() == 100.0);
        InstrumentWhitelistDTO c2 = instrumentWhitelistApi.mktInstrumentWhitelistGet("c2");
        assertTrue(c2.getInstrumentId().equals("c2"));
        assertTrue(c2.getNotionalLimit() == Double.POSITIVE_INFINITY);

        //c3 is new and should be added, c2 exists, but value should be updated
        String file2Content = new StringBuffer().append("d,c2,200.0\n").append("d,c3,100.0\n").toString();
        MultipartFile file2 = toMockFile(file2Content);
        RpcResponseWithDiagnostics<List<InstrumentWhitelistDTO>, List<String>> r2 =
                instrumentWhitelistApi.mktInstrumentWhitelistUpload(file2);
        List<InstrumentWhitelistDTO> result2 = r2.getResult();
        List<String> diagnostics2 = r2.getDiagnostics();
        assertTrue(result2.size() == 2);
        assertTrue(diagnostics2.isEmpty());
        c2 = instrumentWhitelistApi.mktInstrumentWhitelistGet("c2");
        assertTrue(c2.getInstrumentId().equals("c2"));
        assertTrue(c2.getNotionalLimit() == 200.0);
        InstrumentWhitelistDTO c3 = instrumentWhitelistApi.mktInstrumentWhitelistGet("c3");
        assertTrue(c3.getInstrumentId().equals("c3"));
        assertTrue(c3.getNotionalLimit() == 100.0);

        //each line and field should be trimmed before saving and error should be reported
        String errorLine = "d,c5,abc";
        String file3Content = new StringBuffer()
                .append(" d, c4 , 200.0 \n")
                .append(" ")
                .append(errorLine+"\n")
                .append("d, c5, 300.3, abc")
                .toString();
        MultipartFile file3 = toMockFile(file3Content);
        RpcResponseWithDiagnostics<List<InstrumentWhitelistDTO>, List<String>> r3 =
                instrumentWhitelistApi.mktInstrumentWhitelistUpload(file3);
        List<InstrumentWhitelistDTO> result3 = r3.getResult();
        List<String> diagnostics3 = r3.getDiagnostics();
        assertTrue(result3.size() == 2);
        assertTrue(diagnostics3.size() == 1);
        InstrumentWhitelistDTO c4 = instrumentWhitelistApi.mktInstrumentWhitelistGet("c4");
        assertTrue(c4.getInstrumentId().equals("c4"));
        assertTrue(c4.getNotionalLimit() == 200.0);
        assertTrue(diagnostics3.get(0).equals(errorLine));
        InstrumentWhitelistDTO c5 = instrumentWhitelistApi.mktInstrumentWhitelistGet("c5");
        assertTrue(c5.getInstrumentId().equals("c5"));
        assertTrue(c5.getNotionalLimit() == 300.3);
    }

    private MultipartFile toMockFile(String str) throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        MultipartFile file = new MockMultipartFile("test", input);
        return file;
    }

}
