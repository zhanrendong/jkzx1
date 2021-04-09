package tech.tongyu.bct.rpc.json.http.server.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import tech.tongyu.bct.common.util.JsonUtils;

import javax.servlet.ServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ExceptionUtils {

    public static Exception getTargetException(InvocationTargetException e) {
        while (e.getTargetException() instanceof InvocationTargetException) {
            e = (InvocationTargetException) e.getTargetException();
        }
        return (Exception) e.getTargetException();
    }

    public static Exception getTargetException(Exception e) {
        if (e instanceof InvocationTargetException)
            e = getTargetException((InvocationTargetException) e);

        return e;
    }

    public static String getStackTraceInfo(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        try {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        } finally {
            printWriter.close();
        }
    }

    public static Map<String, Object> getRpcExceptionInfo(ServletRequest request, Exception e) {
        Map<String, Object> info = new HashMap<>();
        ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
        String body = StringUtils.toEncodedString(wrapper.getContentAsByteArray(), Charset.forName(wrapper.getCharacterEncoding()));
        Map<String, Object> params = JsonUtils.fromJson(body);
        info.put("errorType", e.getClass().getSimpleName());
        info.put("errorMessage", e.getMessage());
        if (params.get("method") != null) {
            info.put("requestMethod", params.get("method"));
        }
        if (params.get("params") != null) {
            info.put("requestParams", params.get("params"));
        }
        info.put("errorStackTrace", getStackTraceInfo(e));
        return info;
    }
}