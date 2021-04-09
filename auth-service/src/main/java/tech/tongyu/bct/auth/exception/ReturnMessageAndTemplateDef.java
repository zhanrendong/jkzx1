package tech.tongyu.bct.auth.exception;

import tech.tongyu.bct.common.util.CollectionUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public interface ReturnMessageAndTemplateDef {
    enum Errors{
        ILLEGAL_REQUEST_BODY        ("00001", "方法调用格式错误, request body不能为null、空字符串或者仅包含空格的字符串",      false),
        CAPTCHA_NOT_VALID            ("00002", "验证码错误，请输入正确的验证码"                            ,      false),
        USER_EXISTS                 ("00003", "用户[%s]已经存在, 请使用其他的用户名",    true),
        UNKNOWN_ERROR               ("00004", "未知错误, 请联系开发人员以便于解决该问题, 请帮助将相应日志内容[%s]给予开发人员, 谢谢",           true),
        EMPTY_PARALLEL_PARAMS       ("00005", "parallel接口参数集合列表为空, 请参考parallel接口调用格式",                                     false),
        USERNAME_OR_PASSWORD_NOT_VALID("00006", "用户名或密码为空",               true),
        DUPLICATE_ROLE_NAME("00008", "该角色名[%s]已经存在, 请输入其他的角色名", true),
        REMOVE_REVOKED_ROLE("00009", "删除角色操作失败: 尝试删除一个已经被删除的角色[%s]", true),
        REMOVE_NOT_EXISTED_ROLE("00010", "删除角色操作失败: 尝试删除一个不存在的角色[%s]", true),
        INVALID_USER_TYPE("00011", "无法识别的用户类型[%s]", true),
        UNSUPPORTED_USER_TYPE("00012", "无法支持的用户类型[%s]", true),
        INVALID_ITEM_TYPE("00013", "无法识别的项目类型[%s]", true),
        INVALID_SETTING_TYPE("00014", "无法识别的设置类型[%s]", true),
        INVALID_RESOURCE_TYPE("00015", "无法识别的资源类型[%s]", true),
        INVALID_RESOURCE_PERMISSION_TYPE("00016", "无法识别的资源权限类型[%s]", true),
        INVALID_CATEGORY_TYPE("00017", "无法识别的目录类型[%s]", true),
        DATABASE_ERROR              ("00007", "数据库错误", false),
        DUPLICATE_RESOURCE("00018", "父资源下已存在同名同类型的资源[%s:%s]", true),
        MISSING_RESOURCE_PERMISSIONS("00019", "资源权限列表为空, 请检查输入参数", true),
        MISSING_ROLE("00020", "不存在需要的角色[%s]", true),
        MISSING_RESOURCE("00021", "不存在所需的资源[%s %s %s]", true),
        NO_SUCH_USER("00022", "不存在该用户[%s]", true),
        INCORRECT_PASSWORD("00023", "输入的密码不正确", false),
        UPDATE_REVOKED_ROLE("00024", "更新角色操作失败: 尝试更新一个已经被删除的角色[%s]", true),
        UPDATE_NOT_EXISTED_ROLE("00025", "更新角色操作失败: 尝试更新一个不存在的角色[%s]", true),
        EMPTY_PARAM_PERMISSIONS_INFO("00026", "需要添加的资源权限列表输入为空，请检查", false),
        CONSISTENCY_ERROR_RESOURCE_PERMISSIONS_INFO_BLANK_ROLENAME("00027", "资源权限信息[ResourcePermissionInfo]构造失败, 角色名为空", false),
        CONSISTENCY_ERROR_RESOURCE_PERMISSIONS_INFO_BLANK_RESOURCE_TYPE("00028", "资源权限信息[ResourcePermissionInfo]构造失败, 资源类型为空", false),
        CONSISTENCY_ERROR_RESOURCE_PERMISSIONS_INFO_BLANK_RESOURCE_NAME("00029", "资源权限信息[ResourcePermissionInfo]构造失败, 资源ID为空", false),
        CONSISTENCY_ERROR_RESOURCE_PERMISSIONS_INFO_BLANK_RESOURCE_PERMISSION_TYPE("00029", "资源权限信息[ResourcePermissionInfo]构造失败, 资源权限类型列表为空", false),
        FORBIDDEN_REVOKE_ROOT_RESOURCE("00030", "禁止删除ROOT资源节点，这会导致系统权限完全不可用，如对此有疑问，请联系开发人员", false),
        EMPTY_PAGE_COMPONENT_ID_LIST("00031", "空的页面组件ID列表", false),
        MISSING_PAGE_COMPONENT("00032", "未发现页面组件[%s]", true),
        EMPTY_PAGE_COMPONENT_LIST("00033", "空的页面组件列表", false),
        EMPTY_ROLENAME_LIST("00034", "空的角色列表", false),
        UNAUTHORIZATION_ACTION("00035", "您没有权限对该%s(%s)进行%s操作，请联系管理员", true),
        MISSING_DEPARTMENT("00036", "无法识别的部门, 请检查部门查询参数[%s]是否正确，该部门是否已经被删除?", true),
        EXISTING_SAME_NAME_DEPARTMENT( "00037", "无法在相同的部门下挂钩多个同名部门，系统将会无法区分这两个同名部门[%s %s]", true),
        MULTIPLE_COMPANY_INFO("00038", "本系统不支持多个公司同时使用, 无法同时存储多个公司信息", false),
        MISSING_COMPANY_INFO("00039", "公司信息缺失, 请先初始化公司信息", false),
        MISSING_PARAM_RESOURCE_NAME("00040", "资源名称列表为空，请检查输入参数", false),
        MULTIPLE_RESOURCE_WITH_SAME_NAME("00041", "当前系统不支持资源树中的多个资源具有相同的名称，请检查数据", false),
        LIST_RESOURCE_GROUP_ERROR("00042", "不支持列出资源组, 目前支持列出资源的资源类型为[交易簿、投资组合]", false),
        DELETE_RESOURCE_GROUP_ERROR("00043", "不支持仅根据资源名称与资源类型删除资源组，目前该接口支持删除的资源类型为[交易簿、投资组合]", false),
        REDIS_ERROR_INFO("00044","Redis出现错误,错误信息：%s",false),
        MODIFY_RESOURCE_GROUP_ERROR("00045", "不支持仅根据资源名称与资源类型修改资源组，目前该接口支持修改的资源类型为[交易簿、投资组合]", false),
        MISSING_RESOURCE_BY_ID("00040", "id为[%s]的资源不存在", true),
        DELETE_ADMIN("00041", "无法删除admin用户", false),
        DELETE_ROOT_RESOURCE_NOT_PERMITTED("00050", "不能删除Root资源", false),
        MOVE_DEPT_RESOURCE_TO_NONE_DEPT_RESOURCE("00053", "不能将部门资源移动到非部门资源下", false),
        DELETE_COMPANY_NOT_PERMITTED("00058", "不能删除公司", false),
        DELETE_NONE_EMPTY_DEPARTMENT("00063", "部门[%s]包含子部门，不能删除", true),
        WRONG_RESOURCE_TYPE("00066", "该接口只能用于交易簿和投资组合", false),
        DUPLICATE_NON_GROUP_RESOURCE("00071", "已存在同名同类型的资源[%s:%s]", true),
        WEAKEN_PERMISIONS_OF_ADMIN("00075", "不能削弱admin用户对公司的权限", false),
        FORBIDDEN_DELETE_ADMIN_ROLE_FOR_ADMIN("00080", "不能删除admin用户的admin角色", false),
        MISSING_ROLE_PERMISSIONS("00081", "角色权限列表为空, 请检查输入参数", true),
        PARAM_IS_MALFORMED("00088", "参数结构错误", false),
        BOOK_IN_COMPANY_NOT_PERMITTED("00092", "不能在公司下创建交易簿", false),
        EXPIRE_OR_LOCK_ADMIN("00098", "不能使admin用户密码过期或锁定admin用户", false),
        EMPTY_PARAM("00105", "参数%s不能为空", true),
        UPDATE_ADMIN_NAME("00110", "不能修改admin用户的用户名", false),
        DELETE_ADMIN_ROLE("00115", "不能删除admin角色", false),
        UPDATE_ADMIN_ROLE_NAME("00120", "不能修改admin角色的名称", false),
        UPDATE_ADMIN_DEPARTMENT("00125", "不能修改admin用户的部门", false),
        EXPIRE_OR_LOCK_SCRIPT("00130", "脚本用户不能过期", false),
        CAN_NOT_MOVE_TO_CHILDREN_NODE("00135", "不能将部门移到其下属部门下", false),
        USER_LOGIN_FAILED("00136", "登录失败：用户名或密码错误，请重新输入!", false),
        TEXT_DECRYPTED_FAILED("00137", "加密内容无法解密", false),
        USERNAME_OR_PASSWORD_INCORRECT("00138", "用户名或密码不正确", false),
        PASSWORD_LENGTH_INVALID("00139", "无效的密码长度", false)
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
            String[] templateParams = new String[0];
            if(isTemplate){
                templateParams = CollectionUtils.strListToStrArray(Arrays.stream(params)
                        .map(param -> Objects.isNull(param) ? "null" : param.toString())
                        .collect(Collectors.toList()));
            }
            return isTemplate
                    ? String.format(message, (Object[]) templateParams)
                    : message;
        }
    }
}
