package tech.tongyu.bct.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.authaction.intel.UserAuthAction;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.dto.UserStatusDTO;
import tech.tongyu.bct.auth.enums.UserTypeEnum;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.service.ApiParamConstants;
import tech.tongyu.bct.auth.service.UserService;
import tech.tongyu.bct.auth.utils.CommonUtils;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private UserAuthAction userAuthAction;

    @Autowired
    public UserServiceImpl(
            UserAuthAction userAuthAction){
        this.userAuthAction = userAuthAction;
    }

    @Override
    @BctMethodInfo(
            description = "创建用户，根据创建的用户，需要创建一般用户权限(CREATE_USER)或者创建脚本用户权限(CREATE_SCRIPT_USER)",
            retName = "user",
            retDescription = "用户信息",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserDTO authUserCreate(
            @BctMethodArg(name = ApiParamConstants.USERNAME, description = "用户名") String username,
            @BctMethodArg(name = ApiParamConstants.PASSWORD, description = "密码") String password,
            @BctMethodArg(name = ApiParamConstants.USER_TYPE, description = "用户类型.enum value: 一般用户(NORMAL)/脚本用户(SCRIPT)") String userType,
            @BctMethodArg(name = ApiParamConstants.NICK_NAME, description = "昵称", required = false) String nickName,
            @BctMethodArg(name = ApiParamConstants.CONTACT_EMAIL, description = "联系邮箱", required = false) String contactEmail,
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_ID, description = "部门ID") String departmentId,
            @BctMethodArg(name = ApiParamConstants.ROLE_IDS, description = "角色ID列表") List<String> roleIds
    ) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USERNAME, username);
            put(ApiParamConstants.PASSWORD, password);
            put(ApiParamConstants.USER_TYPE, userType);
            put(ApiParamConstants.DEPARTMENT_ID, departmentId);
        }});

        return userAuthAction.createUser(username, nickName, contactEmail, password, UserTypeEnum.of(userType), departmentId, roleIds);
    }

    @Override
    @BctMethodInfo(
            description = "通过用户名获取用户信息，需要查看用户权限(READ_USER)",
            retName = "user",
            retDescription = "用户信息",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    public UserDTO authUserByNameGet(
            @BctMethodArg(name = ApiParamConstants.USERNAME, description = "用户名") String username) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USERNAME, username);
        }});

        return userAuthAction.getUserByUsername(username);
    }

    @Override
    @BctMethodInfo(
            description = "获取用户列表",
            retName = "users",
            retDescription = "用户信息列表",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    public Collection<UserDTO> authUserList() {
        return userAuthAction.listUsers();
    }

    @Override
    @BctMethodInfo(
            description = "获取全部可以读取交易簿的用户",
            retName = "users",
            retDescription = "用户名列表",
            service = "auth-service"
    )
    @Transactional
    public Collection<String> authUserListByBookCanRead() {
        return userAuthAction.listUsersByBookCanRead();
    }

    @Override
    public Boolean authIsUserValid(String username) {
        return userAuthAction.isUserValid(username);
    }

    @Override
    @BctMethodInfo(
            description = "锁定用户，使被锁定用户无法登陆，需要锁定用户权限(LOCK_USER)",
            retName = "success or failure",
            retDescription = "用户状态",
            returnClass = UserStatusDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserStatusDTO authUserLock(
            @BctMethodArg(name = ApiParamConstants.USERNAME, description = "用户名") String username) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USERNAME, username);
        }});

        return userAuthAction.updateUserLocked(username, true);
    }

    @Override
    @BctMethodInfo(
            description = "解锁用户，使用户可以登陆，需要解锁用户权限(UNLOCK_USER)",
            retName = "success or failure",
            retDescription = "用户状态",
            returnClass = UserStatusDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserStatusDTO authUserUnlock(@BctMethodArg(
            name = ApiParamConstants.USERNAME, description = "用户名") String username) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USERNAME, username);
        }});

        return userAuthAction.updateUserLocked(username, false);
    }

    @Override
    @BctMethodInfo(
            description = "过期用户，更新密码后可登陆，需要过期用户权限(EXPIRE_USER)",
            retName = "success or failure",
            retDescription = "用户状态",
            returnClass = UserStatusDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserStatusDTO authUserExpire(@BctMethodArg(
            name = ApiParamConstants.USERNAME, description = "用户名") String username) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USERNAME, username);
        }});

        return userAuthAction.updateUserExpired(username, true);
    }

    @Override
    @BctMethodInfo(
            description = "取消用户过期，需要取消过期用户权限(UNEXPIRE_USER)",
            retName = "success or failure",
            retDescription = "用户状态",
            returnClass = UserStatusDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserStatusDTO authUserUnexpire(
            @BctMethodArg(name = ApiParamConstants.USERNAME, description = "用户名") String username) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USERNAME, username);
        }});

        return userAuthAction.updateUserExpired(username, false);
    }

    @BctMethodInfo(
            description = "获取当前登陆用户",
            retName = "user",
            retDescription = "用户信息",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    public UserDTO authCurrentUserGet() {
        return userAuthAction.getCurrentUser();
    }

    @Override
    @BctMethodInfo(
            description = "修改用户密码，需要更新密码权限(CHANGE_PASSWORD)",
            retName = "user",
            retDescription = "用户信息",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserDTO authUserPasswordChange(
            @BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId,
            @BctMethodArg(name = ApiParamConstants.PASSWORD, description = "密码") String password) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USER_ID, userId);
            put(ApiParamConstants.PASSWORD, password);
        }});
        if (!CommonUtils.checkPasswordLength(password)){
            throw new CustomException(ReturnMessageAndTemplateDef.Errors.PASSWORD_LENGTH_INVALID.getMessage());
        }
        return userAuthAction.updatePassword(userId, password);
    }

    @Override
    @BctMethodInfo(
            description = "更新用户信息，需要更新用户信息权限(UPDATE_USER)",
            retName = "user",
            retDescription = "用户信息",
            returnClass = UserDTO.class,
            service = "auth-service"
    )
    @Transactional
    public UserDTO authUserUpdate(
            @BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId,
            @BctMethodArg(name = ApiParamConstants.USERNAME, description = "用户名") String username,
            @BctMethodArg(required = false, name = ApiParamConstants.NICK_NAME, description = "昵称") String nickName,
            @BctMethodArg(name = ApiParamConstants.USER_TYPE, description = "用户类型.enum value: 一般用户(NORMAL)/脚本用户(SCRIPT)") String userType,
            @BctMethodArg(required = false, name = ApiParamConstants.CONTACT_EMAIL, description = "联系邮箱") String contactEmail,
            @BctMethodArg(name = ApiParamConstants.DEPARTMENT_ID, description = "部门ID") String departmentId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USER_ID, userId);
            put(ApiParamConstants.USERNAME, username);
            put(ApiParamConstants.USER_TYPE, userType);
            put(ApiParamConstants.DEPARTMENT_ID, departmentId);
        }});

        return userAuthAction.updateUserAttributes(userId, username.trim(), nickName, UserTypeEnum.of(userType)
                , contactEmail, departmentId);
    }

    @Override
    @BctMethodInfo(
            description = "删除用户，需要删除用户信息权限(DELETE_USER)",
            retName = "success or failure",
            retDescription = "删除结果",
            service = "auth-service"
    )
    @Transactional
    public Boolean authUserRevoke(@BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USER_ID, userId);
        }});

        userAuthAction.revokeUser(userId);
        return true;
    }

    @Override
    @BctMethodInfo(
            description = "用户注销",
            retName = "success or failure",
            retDescription = "用户注销",
            service = "auth-service",
            enableLogging = true
    )
    @Transactional
    public Boolean authUserLogout(@BctMethodArg(name = ApiParamConstants.USER_ID, description = "用户ID") String userId) {

        CommonUtils.checkBlankParam(new HashMap<String, String>() {{
            put(ApiParamConstants.USER_ID, userId);
        }});

        userAuthAction.logoutUser(userId);
        return true;
    }
}
