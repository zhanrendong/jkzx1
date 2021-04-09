package tech.tongyu.bct.service.quantlib.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import tech.tongyu.bct.service.quantlib.common.utils.JsonMapper;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Converter {
    private static Pattern p = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}.*");

    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Object toOutput(String type, Object o, String objectId) throws Exception {
        if (type.equals("Handle")) {
            String id = objectId == null ? Utils.genID(o) : objectId;
            ObjectCache.Instance.put(id, o);
            return id;
        } else if (type.equals("DateTime")) {
            return ((LocalDateTime) o).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } else if (type.equals("Enum")) {
            return o.toString();
        } else if (type.equals("ArrayDouble")) {
            List<Object> ret = new ArrayList<>();
            for (double x : (double[]) o) {
                ret.add(x);
            }
            return ret;
        } else if (type.equals("ArrayDateTime")) {
            List<Object> ret = new ArrayList<>();
            for (LocalDateTime d : (LocalDateTime[]) o) {
                ret.add(d.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            return ret;
        } else if (type.equals("ArrayBoolean")) {
            List<Object> ret = new ArrayList<>();
            for (boolean b : (boolean[]) o) {
                ret.add(b);
            }
            return ret;
        } else if (type.equals("ArrayString")) {
            List<Object> ret = new ArrayList<>();
            for (String s : (String[]) o) {
                ret.add(s);
            }
            return ret;
        } else if (type.equals("Json")) {
            if (o instanceof String) {
                Map<String, Object> js = (Map<String, Object>) JsonMapper.fromJson(new HashMap<String, Object>().getClass().getName(), (String)o);
                return js;
            } else if (o instanceof Map<?, ?>) {
                String js = objectMapper.writeValueAsString(o);
                return (Map<String, Object>)JsonMapper.fromJson(new HashMap<String, Object>().getClass().getName(), js);
            } else {
                throw new Exception("Cannot convert the output to Json");
            }
        } else if (type.equals("Matrix")) {
            List<Object> ret = new ArrayList<>();
            double[][] m = (double[][])o;
            for (double[] r : m) {
                List<Object> row = new ArrayList<>();
                for (double x : r) {
                    row.add(x);
                }
                ret.add(row);
            }
            return ret;
        } else if (type.equals("Table")) {
            Map<String, Object> ret = new HashMap<>();
            Map<String, List<Object>> t = (Map<String, List<Object>>)o;
            for (Map.Entry<String, List<Object>> entry: t.entrySet()) {
                ret.put(entry.getKey(), entry.getValue());
            }
            return ret;
        } else if (type.startsWith("{")) {
            return mapToJson(type, (Map<String, Object>) o);
        } else
            return o;
    }

    private static Map<String, Object> mapToJson(String type, Map<String, Object> o) throws Exception {
        Map<String, Object> typeMap = (Map<String, Object>)JsonMapper.fromJson(new HashMap<String, Object>().getClass().getName(), type);
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : o.entrySet()) {
            String key = entry.getKey();
            String outputType = (String)typeMap.get(key);
            if (outputType == null)
                throw new Exception("Cannot find the type of output " + key);
            Object value = entry.getValue();
            ret.put(key, toOutput(outputType, value, null));
        }
        return ret;
    }

    public static Object toInput(String type, Object param) throws Exception {
        Object ret;
        if (type.equals("Handle")) {
            ret = ObjectCache.Instance.get((String) param);
            if (ret == null)
                throw new Exception("The handle " + param + " is missing in the repository");
        } else if (type.equals("DateTime")) {
            String dtStr = (String) param;
            if (dtStr.endsWith("Z") || dtStr.endsWith("z"))
                ret = LocalDateTime.ofInstant(Instant.parse(dtStr), ZoneOffset.UTC);
            else if (dtStr.length() == 10) // YYYY-MM-DD
                ret = LocalDateTime.of(LocalDate.parse(dtStr), LocalTime.MIDNIGHT);
            else
                ret = LocalDateTime.parse(dtStr);
        } else if (type.equals("Enum")) {
            ret = EnumCache.Instance.get(((String) param).toUpperCase());
            if (ret == null)
                throw new Exception("The enum " + param + " cannot be found in the enum repository");
        } else if (param instanceof ArrayList) {
            ArrayList input = (ArrayList) param;
            if (type.equals("ArrayDouble")) {
                double[] ds = new double[input.size()];
                for (int i = 0; i < input.size(); ++i) {
                    ds[i] = (((Number) input.get(i))).doubleValue();
                }
                ret = ds;
            } else if (type.equals("ArrayDateTime")) {
                LocalDateTime[] ds = new LocalDateTime[input.size()];
                for (int i = 0; i < input.size(); ++i) {
                    String dtStr = (String)input.get(i);
                    if (dtStr.endsWith("Z") || dtStr.endsWith("z"))
                        ds[i] = LocalDateTime.ofInstant(Instant.parse(dtStr), ZoneOffset.UTC);
                    else if (dtStr.length() == 10) // YYYY-MM-DD
                        ds[i] = LocalDateTime.of(LocalDate.parse(dtStr), LocalTime.MIDNIGHT);
                    else
                        ds[i] = LocalDateTime.parse(dtStr);
                }
                ret = ds;
            } else if (type.equals("ArrayBoolean")) {
                boolean[] bs = new boolean[input.size()];
                for (int i = 0; i < input.size(); ++i) {
                    bs[i] = (boolean) input.get(i);
                }
                ret = bs;
            } else if (type.equals("ArrayString")) {
                String[] ss = new String[input.size()];
                for (int i = 0; i < input.size(); ++i) {
                    ss[i] = (String) input.get(i);
                }
                ret = ss;
            } else if (type.equals("ArrayHandle")) {
                Object[] os = new Object[input.size()];
                for (int i = 0; i < input.size(); ++i) {
                    os[i] = toInput("Handle", input.get(i));
                }
                ret = os;
            } else if (type.equals("ArrayEnum")) {
                Object[] os = new Object[input.size()];
                for (int i = 0; i < input.size(); ++i) {
                    os[i] = toInput("Enum", input.get(i));
                }
                ret = os;
            } else if (type.equals("Matrix")) {
                double [][] m = new double[input.size()][];
                for (int i = 0; i < input.size(); ++i) {
                    ArrayList<Object> r = (ArrayList<Object>)input.get(i);
                    m[i] = new double[r.size()];
                    for(int j = 0; j < r.size(); ++j)
                        m[i][j] = ((Number)r.get(j)).doubleValue();
                }
                return m;
            } else
                throw new Exception("Input is an array, but is not of double, datetime, string or boolean type");
        } else if (type.equals("Json")) {
            ret = jsonToMap((Map<String, Object>) param);
        } else if (type.startsWith("{")) {
            ret = jsonToMap(type, (Map<String, Object>) param);
        } else {
            ret = param;
        }
        return ret;
    }

    public static Object toDefaultInput(String type) {
        Object ret;
        switch (type) {
            case "Double":
                return 0.0;
            case "Integer":
                return 0;
            case "String":
                return "";
            default:
                return null;
        }
    }

    private static Map<String, Object> jsonToMap(String type, Map<String, Object> param) throws Exception {
        Map<String, Object> typeMap = (Map<String, Object>)JsonMapper.fromJson(new HashMap<String, Object>().getClass().getName(), type);
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String key = entry.getKey();
            String valueType = (String)typeMap.get(key);
            if (valueType == null)
                throw new Exception("The type of input key " + key + "cannot be found");
            Object value = entry.getValue();
            ret.put(key, toInput(valueType, value));
        }
        return ret;
    }

    // convert input json into a map WITHOUT type info
    // we have to guess the type ...
    // we support scalar values only (string/datetime/number/handle/enum)
    // we don't support nested jason
    private static Map<String, Object> jsonToMap(Map<String, Object> param) throws Exception {
        Map<String, Object> ret = new HashMap<>();
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Object o;
            if (value instanceof String) {
                // handle
                o = ObjectCache.Instance.get(key);
                if (o == null) {
                    // try enum
                    o = EnumCache.Instance.get(((String) value).toUpperCase());
                    if (o == null) {
                        // try datetime
                        if (p.matcher((String) value).matches()) {
                            try {
                                o = LocalDateTime.parse((String) value);
                            } catch (Exception e) {
                                o = null;
                            }
                        }
                        else {
                            o = (String) value;
                        }
                    }
                }
            } else if (value instanceof Number) {
                o = ((Number)value).doubleValue();
            } else if (value instanceof Boolean) {
                o = value;
            } else
                o = null;
            if (o!=null)
                ret.put(key, o);
        }
        return ret;
    }
}