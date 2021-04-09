package tech.tongyu.bct.quant.service;

import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.common.api.request.JsonRpcRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface QuantService {
    Object execute(JsonRpcRequest task);

    List<Object> parallel(List<JsonRpcRequest> tasks);

    String qlVolSurfaceAtmPwcCreate(LocalDate val, double spot, List<LocalDate> expiries,
                                    List<Double> vols);

    String qlVolSurfaceInterpolatedStrikeCreate(LocalDate val, double spot, List<LocalDate> expiries,
            List<Double> strikes, List<List<Double>> vols, double daysInYear);

    String qlCurveFromSpotRates(LocalDate val, List<LocalDate> expiries, List<Double> rates);

    @Deprecated
    JsonNode qlObjectInfo(String handle);

    JsonNode qlObjectSerialize(String handle);

    String qlObjectDeserialize(String id, JsonNode objJson);

    void createCalendar(String name, List<LocalDate> holidays);

    void deleteCalendar(String name);

    void mergeHolidays(String name, List<LocalDate> holidays);

    void removeHolidays(String name, List<LocalDate> holidays);
}
