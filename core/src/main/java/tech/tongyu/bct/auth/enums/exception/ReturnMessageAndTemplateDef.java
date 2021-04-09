package tech.tongyu.bct.auth.enums.exception;

public interface ReturnMessageAndTemplateDef {
    enum Errors{

        INVALID_USER_TYPE("00011", "无法识别的用户类型[%s]", true),
        INVALID_ITEM_TYPE("00013", "无法识别的项目类型[%s]", true),
        INVALID_SETTING_TYPE("00014", "无法识别的设置类型[%s]", true),
        INVALID_RESOURCE_TYPE("00015", "无法识别的资源类型[%s]", true),
        INVALID_RESOURCE_PERMISSION_TYPE("00016", "无法识别的资源权限类型[%s]", true),
        INVALID_CATEGORY_TYPE("00017", "无法识别的目录类型[%s]", true);


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
