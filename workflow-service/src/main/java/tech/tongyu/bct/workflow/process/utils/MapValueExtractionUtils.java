package tech.tongyu.bct.workflow.process.utils;

import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.workflow.exceptions.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.workflow.exceptions.WorkflowCommonException;

import java.util.Map;
import java.util.Objects;

public class MapValueExtractionUtils {

    public static String getNonBlankStringFromMap(Map<String, Object> map, String key, String blankMessage){
        Object value = map.get(key);
        if(Objects.isNull(value) || StringUtils.isBlank(value.toString())){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_VALID, blankMessage);
        }
        return value.toString();
    }

    public static Boolean getBooleanFromMap(Map<String, Object> map, String key, String badTypeMessage){
        Object value = map.get(key);
        if(Objects.isNull(value) || !(value instanceof Boolean)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_VALID, badTypeMessage);
        }

        return (Boolean) value;
    }

    public static Integer getIntegerFromMap(Map<String, Object> map, String key, String badIntegerMessage){
        Object value = map.get(key);
        if(Objects.isNull(value) || !(value instanceof Integer)){
            throw new WorkflowCommonException(ReturnMessageAndTemplateDef.Errors.PARAM_NOT_VALID, badIntegerMessage);
        }
        return (Integer) value;

    }
}
