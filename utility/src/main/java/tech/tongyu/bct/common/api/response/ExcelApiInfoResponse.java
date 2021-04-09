package tech.tongyu.bct.common.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class ExcelApiInfoResponse {
    public static class ExcelArgInfo {
        private final String name;
        private final String description;
        private final String type;

        public ExcelArgInfo(String name, String description, String type) {
            this.name = name;
            this.description = description;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String method;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String retType;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String retName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<ExcelArgInfo> args;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String error;

    public ExcelApiInfoResponse(String method, String description, String retType,
                                String retName, List<ExcelArgInfo> args) {
        this.method = method;
        this.description = description;
        this.retType = retType;
        this.retName = retName;
        this.args = args;
        this.error = null;
    }

    public ExcelApiInfoResponse(String error) {
        this.method = null;
        this.description = null;
        this.retType = null;
        this.retName = null;
        this.args = null;
        this.error = error;
    }

    public String getMethod() {
        return method;
    }

    public String getDescription() {
        return description;
    }

    public String getRetType() {
        return retType;
    }

    public String getRetName() {
        return retName;
    }

    public List<ExcelArgInfo> getArgs() {
        return args;
    }

    public String getError() {
        return error;
    }
}
