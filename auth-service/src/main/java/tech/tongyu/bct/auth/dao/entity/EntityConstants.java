package tech.tongyu.bct.auth.dao.entity;

public interface EntityConstants {

    /*------------------------ common  --------------------------*/
    String ID = "id";
    String SORT = "sort";
    /*------------------------ common  --------------------------*/

    /*------------------------ schema  --------------------------*/
    String AUTH_SERVICE = "auth_service";
    /*------------------------ schema  --------------------------*/

    /*------------------------ resource db  --------------------------*/
    String INDEX_RESOURCE__RESOURCE_NAME_RESOURCE_TYPE = "auth_idx_resource_name__resource_type";
    String AUTH_RESOURCE = "auth_resource";
    String RESOURCE_NAME = "resource_name";
    String RESOURCE_TYPE = "resource_type";
    String PARENT_ID = "parent_id";
    /*------------------------ resource db  --------------------------*/

    /*------------------------ role resource permission db  --------------------------*/
    String ROLE_RESOURCE_PERMISSION = "auth_role_resource_permission";
    String INDEX_ROLE_RESOURCE_PERMISSION__ROLE_ID = "auth_idx_role_resource_permission__role_id";
    String INDEX_ROLE_RESOURCE_PERMISSION__RESOURCE_ID = "auth_idx_role_resource_permission__resource_id";
    /*------------------------ role resource permission db  --------------------------*/

    /*------------------------ resource permission db  --------------------------*/
    String INDEX_RESOURCE_PERMISSION__USER_ID = "auth_idx_resource_permission__user_id";
    String INDEX_RESOURCE_PERMISSION__RESOURCE_ID = "auth_idx_resource_permission__resource_id";

    String RESOURCE_PERMISSION = "auth_resource_permission";
    String ROLE_ID = "role_id";
    String RESOURCE_ID = "resource_id";
    String RESOURCE_PERMISSION_TYPE = "resource_permission_type";
    /*------------------------ resource permission db  --------------------------*/

    /*------------------------ user db  --------------------------*/
    String INDEX_USER__USERNAME = "auth_idx_user__username";
    String INDEX_USER__USER_TYPE = "auth_idx_user__user_type";
    String INDEX_USER__LOCKED = "auth_idx_user__locked";
    String INDEX_USER__EXPIRED = "auth_idx_user__expired";
    String AUTH_USER = "auth_user";
    String USERNAME = "username";
    String NICK_NAME = "nick_name";
    String PASSWORD = "password";
    String EXTERNAL_ACCOUNT = "external_account";
    String EXTERNAL_TYPE = "external_type";
    String USER_TYPE = "user_type";
    String LOCKED = "locked";
    String EXPIRED = "expired";
    String TIMES_OF_LOGIN_FAILURE = "times_of_login_failure";
    String PASSWORD_EXPIRED_TIMESTAMP = "password_expired_timestamp";
    String DEPARTMENT_ID = "department_id";
    /*------------------------ user db  --------------------------*/

    /*------------------------ user_role db  --------------------------*/
    String AUTH_USER_ROLE = "auth_user_role";
    String USER_ID = "user_id";
    String INDEX_USER_ROLE__USER_ID_ROLE_ID = "auth_idx_user_role__user_id_role_id";
    /*------------------------ user_role db  --------------------------*/

    /*------------------------ role db  --------------------------*/
    String INDEX_ROLE__ROLE_NAME = "auth_idx_role__role_name";
    String AUTH_ROLE = "auth_role";
    String ROLE_NAME = "role_name";
    String REMARK = "remark";
    String ALIAS = "alias";
    /*------------------------ role db  --------------------------*/

    /*------------------------ department db  --------------------------*/
    String INDEX_DEPARTMENT__DEPARTMENT_NAME = "auth_idx_department__department_name";
    String INDEX_DEPARTMENT__DEPARTMENT_TYPE = "auth_idx_department__department_type";
    String AUTH_DEPARTMENT = "auth_department";
    String DEPARTMENT_NAME = "department_name";
    String DEPARTMENT_TYPE = "department_type";
    String DESCRIPTION = "description";
    /*------------------------ department db  --------------------------*/

    /*------------------------ company db  --------------------------*/
    String AUTH_COMPANY = "auth_company";
    String COMPANY_NAME = "company_name";
    String COMPANY_TYPE = "company_type";
    String UNIFIED_SOCIAL_CREDIT_CODE = "unified_social_credit_code";
    String LEGAL_PERSON = "legalPerson";
    String CONTACT_EMAIL = "contactEmail";
    /*------------------------ company db  --------------------------*/

    /*------------------------ page component db  --------------------------*/
    String AUTH_PAGE_COMPONENT = "auth_page_component";
    String AUTH_PAGE_PERMISSION = "auth_page_permission";
    String PAGE_NAME = "page_name";
    String ORDER = "order";
    String CHILDREN = "children";
    String PAGE_COMPONENT_ID = "page_component_id";
    /*------------------------ page component db  --------------------------*/

    /*------------------------ sys_log db  --------------------------*/
    String SYS_LOG = "sys_log";
    /*------------------------ sys_log db  --------------------------*/

    /*------------------------ auth_ipaddr_valid_token db  --------------------------*/
    String IP_TOKEN_BIND = "auth_ipaddr_valid_token";
    String IP_USER_BIND = "auth_ip_valid_username";
}
