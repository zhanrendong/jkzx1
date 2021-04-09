package tech.tongyu.bct.risk.excepitons;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public interface ReturnMessageAndTemplateDef {
    enum Errors{
        PARAM_NOT_FOUND(                                "000000001", "请求参数不可为空,请重新请求", false),
        GROUP_ERROR(                                    "000000002", "条件组或内部条件不存在", false),
        PARAM_TYPE_ERROR(                               "000000003", "指标类型转换错误:[%s]", true),
        GROUP_EXIST_ERROR(                              "000000004", "已存在同名条件组或内部条件", false),
        INDEX_DATA_ERROR(                               "000000005", "根据传入数据中未找到匹配数据,无法计算指标", false),
        ;


        private String detailedErrorCode;
        private String message;
        private Boolean isTemplate;

        Errors(String detailedErrorCode, String message, Boolean isTemplate){
            this.detailedErrorCode = detailedErrorCode;
            this.message = message;
            this.isTemplate = isTemplate;
        }

        public String getDetailedErrorCode(){
            return detailedErrorCode;
        }

        public String getMessage(Object... params){
            return isTemplate
                    ? String.format(message, params)
                    : message;
        }
    }
}
