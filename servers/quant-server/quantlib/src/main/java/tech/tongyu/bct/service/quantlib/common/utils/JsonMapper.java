package tech.tongyu.bct.service.quantlib.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;

public class JsonMapper {
    public static final ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    @BctQuantApi(
            name = "qlObjectToJson",
            description = "Get a handle's Json representation",
            argNames = {"object"},
            argTypes = {"Handle"},
            argDescriptions = {"The object to be serialized"},
            retName = "json",
            retType = "String",
            retDescription = "Json string"
    )
    public static String toJson(Object o) {
        String str = null;
        try{
            str = mapper.writeValueAsString(o);
        }catch (Exception e){
            throw new CustomException("Failed to encode as JSON: " + e.getMessage());
        }
        return str;
    }

    @BctQuantApi(
            name = "qlObjectInfo",
            description = "Get a handle's information",
            argNames = {"object"},
            argTypes = {"Handle"},
            argDescriptions = {"The object to be serialized"},
            retName = "json",
            retType = "Json",
            retDescription = "Json string"
    )
    public static String info(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    @BctQuantApi(
            name = "qlObjectFromJson",
            description = "Create a handle from a Json string",
            argNames = {"type", "json"},
            argTypes = {"String", "String"},
            argDescriptions = {"Java canonical name for the type", "Json string"},
            retName = "handle", retType = "Handle", retDescription = "A new handle from string"
    )
    public static Object fromJson(String type, String json) throws Exception {
        Object obj = null;
        try{
            obj = mapper.readValue(json, Class.forName(type));
        }catch (Exception e){
            throw new CustomException("Failed to decode: " + e.getMessage());
        }
        return obj;
    }
}