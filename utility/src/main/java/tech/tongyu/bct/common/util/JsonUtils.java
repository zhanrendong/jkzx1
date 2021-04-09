package tech.tongyu.bct.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonUtils {
    public static class SerializationException extends RuntimeException{
        SerializationException(String message) {
            super(message);
        }

        SerializationException(Exception e){
            super(e);
        }
    }

    public static final ObjectMapper mapper;
    public static final String BCT_JACKSON_TYPE_TAG = "@class";

    private static final TypeReference<HashMap<String, Object>> typeRef
            = new TypeReference<HashMap<String, Object>>() {
    };

    static class MapNullValueSerializer extends StdSerializer<Object> {
        public MapNullValueSerializer(){
            this(null);
        }

        public MapNullValueSerializer(Class<Object> t){
            super(t);
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeNull();
        }
    }

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()).registerModule(new ParameterNamesModule());
        mapper.getSerializerProvider().setNullValueSerializer(new MapNullValueSerializer());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    public static String objectToJsonString(Object o) {
        String str;
        try {
            str = mapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PARSING_ERROR,
                    "failed to encode object as JSON: " + e.getMessage());
        }
        return str;
    }

    public static Map<String, Object> mapFromJsonString(String context) {
        Map<String, Object> resultMap = null;
        if (context != null) {
            try {
                resultMap = mapper.readValue(context, typeRef);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.PARSING_ERROR,
                        "failed to decode: " + e.getMessage());
            }
        }
        return resultMap;
    }

    public static Object jsonNodeWithTypeTagToObject(JsonNode json) {
        if (json instanceof NullNode) {
            return null;
        } else if (json instanceof ArrayNode) {
            return StreamSupport.stream(json.spliterator(), false)
                    .map(j -> jsonNodeWithTypeTagToObject(j))
                    .collect(Collectors.toList());
        } else {
            try {
                String assetCls = json.get(BCT_JACKSON_TYPE_TAG).asText();
                ((ObjectNode) json).remove(BCT_JACKSON_TYPE_TAG);
                Class<?> instanceClass = Class.forName(assetCls);
                return JsonUtils.mapper.readValue(json.toString(), instanceClass);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                throw new IllegalStateException(e);
            }
        }
    }

    public static String toJson(Object o) throws SerializationException {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }

    private static JavaType getMapType(Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(HashMap.class, elementClasses);
    }

    public static Map<String, Object> fromJson(String context) throws SerializationException {
        Map<String, Object> resultMap = null;
        if (StringUtils.isNotBlank(context)) {
            JavaType type = getMapType(String.class, Object.class);
            try {
                resultMap = mapper.readValue(context, type);
            } catch (IOException e) {
                throw new SerializationException(String.format("Failed to decode: [%s]", context));
            }
        }
        return resultMap;
    }

    public static <T> T fromJson(Class<T> clazz, String context) throws SerializationException {
        try{
            return mapper.readValue(context, clazz);
        }catch (IOException e){
            throw new SerializationException(String.format("Failed to decode: [%s]", context));
        }
    }
}
