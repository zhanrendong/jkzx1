package tech.tongyu.bct.rpc.json.http.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.request.JsonRpcRequest;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.quant.service.QuantService;
import tech.tongyu.bct.rpc.json.http.client.util.HttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuantServiceImpl implements QuantService {
    private HttpClient httpClient;

    private final String QUANT_SERVICE = "quant-service";

    @Autowired
    public QuantServiceImpl(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Object execute(JsonRpcRequest req) {
        String PATH = "/api/rpc";
        String token = "";
        return httpClient.postTemplate(QUANT_SERVICE, PATH, JsonUtils.objectToJsonString(req), token);
    }

    @Override
    public List<Object> parallel(List<JsonRpcRequest> tasks) {
        String PARALLEL = "/api/rpc/parallel";
        String token = "";
        return (List<Object>)httpClient.postTemplate(QUANT_SERVICE, PARALLEL,
                JsonUtils.objectToJsonString(tasks), token);
    }

    @Override
    public String qlVolSurfaceAtmPwcCreate(LocalDate val, double spot, List<LocalDate> expiries,
                                           List<Double> vols) {
        Map<String, Object> params = new HashMap<>();
        params.put("val", val.toString());
        params.put("spot", spot);
        params.put("expiries", expiries);
        params.put("vols", vols);
        params.put("daysInYear", 365.);
        String QL_VOL_SURFACE_ATM_PWC_CREATE = "qlVolSurfaceAtmPwcCreate";
        JsonRpcRequest req = new JsonRpcRequest(QL_VOL_SURFACE_ATM_PWC_CREATE, params);
        return (String)execute(req);
    }

    @Override
    public String qlCurveFromSpotRates(LocalDate val, List<LocalDate> expiries, List<Double> rates) {
        Map<String, Object> params = new HashMap<>();
        params.put("val", val);
        params.put("spotDate", val);
        params.put("ts", expiries);
        params.put("rs", rates);
        params.put("basis", "ACT365");
        String QL_CURVE_FROM_SPOT_RATES = "qlCurveFromSpotRates";
        JsonRpcRequest req = new JsonRpcRequest(QL_CURVE_FROM_SPOT_RATES, params);
        return (String)execute(req);
    }

    @Override
    public JsonNode qlObjectInfo(String handle) {
        Map<String, Object> params = new HashMap<>();
        String OBJECT = "object";
        params.put(OBJECT, handle);
        String QL_OBJECT_INFO= "qlObjectInfo";
        JsonRpcRequest req = new JsonRpcRequest(QL_OBJECT_INFO, params);
        return JsonUtils.mapper.valueToTree(execute(req));
    }

    @Override
    public JsonNode qlObjectSerialize(String handle) {
        return qlObjectInfo(handle);
    }

    @Override
    public String qlObjectDeserialize(String id, JsonNode objJson) {
        String entity = JsonUtils.objectToJsonString(objJson);
        String API_OBJECT = "/api/object/";
        String encoded;
        try {
            encoded = URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("对象ID非法: %s", id));
        }
        return (String)httpClient.putTemplate(QUANT_SERVICE, API_OBJECT,
                encoded, entity, "");
    }

    @Override
    public String qlVolSurfaceInterpolatedStrikeCreate(LocalDate val, double spot, List<LocalDate> expiries,
            List<Double> strikes, List<List<Double>> vols, double daysInYear) {
        Map<String, Object> params = new HashMap<>();
        params.put("val", val.toString());
        params.put("spot", spot);
        params.put("strikes", strikes);
        params.put("expiries", expiries);
        params.put("vols", vols);
        params.put("daysInYear", daysInYear);
        String QL_VOL_SURFACE_INTERPOLATED_STRIKE_CREATE = "qlVolSurfaceInterpolatedStrikeCreate";
        JsonRpcRequest req = new JsonRpcRequest(QL_VOL_SURFACE_INTERPOLATED_STRIKE_CREATE, params);
        return (String)execute(req);
    }

    @Override
    public void createCalendar(String name, List<LocalDate> holidays) {
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED,
                "Remote quant-service function createCalendar not implemented");
    }

    @Override
    public void deleteCalendar(String name) {
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED,
                "Remote quant-service function deleteCalendar not implemented");
    }

    @Override
    public void mergeHolidays(String name, List<LocalDate> holidays) {
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED,
                "Remote quant-service function mergeHolidays not implemented");
    }

    @Override
    public void removeHolidays(String name, List<LocalDate> holidays) {
        throw new CustomException(ErrorCode.NOT_IMPLEMENTED,
                "Remote quant-service function removeHolidays not implemented");
    }
}
