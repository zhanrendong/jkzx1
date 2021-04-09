package tech.tongyu.bct.auth.authaction.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.auth.AuthConstants;
import tech.tongyu.bct.auth.authaction.intel.ResourcePermissionAuthAction;
import tech.tongyu.bct.auth.authaction.intel.UserAuthAction;
import tech.tongyu.bct.auth.cache.ResourcePermissionCacheManager;
import tech.tongyu.bct.auth.config.Constants;
import tech.tongyu.bct.auth.dto.ResourceDTO;
import tech.tongyu.bct.auth.dto.ResourcePermissionDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.dto.UserStatusDTO;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.enums.UserTypeEnum;
import tech.tongyu.bct.auth.exception.AuthServiceException;
import tech.tongyu.bct.auth.exception.AuthorizationException;
import tech.tongyu.bct.auth.exception.ReturnMessageAndTemplateDef;
import tech.tongyu.bct.auth.manager.DepartmentManager;
import tech.tongyu.bct.auth.manager.ResourceManager;
import tech.tongyu.bct.auth.manager.ResourcePermissionManager;
import tech.tongyu.bct.auth.manager.UserManager;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserAuthActionImpl implements UserAuthAction {

    private UserManager userManager;
    private ResourceManager resourceManager;
    private ResourcePermissionManager resourcePermissionManager;
    private DepartmentManager departmentManager;
    private ResourcePermissionAuthAction resourcePermissionAuthAction;
    private ResourcePermissionCacheManager resourcePermissionCacheManager;

    @Autowired
    public UserAuthActionImpl(
            UserManager userManager
            , ResourceManager resourceManager
            , ResourcePermissionManager resourcePermissionManager
            , DepartmentManager departmentManager
            , ResourcePermissionAuthAction resourcePermissionAuthAction
            , ResourcePermissionCacheManager resourcePermissionCacheManager){
        this.userManager = userManager;
        this.resourceManager = resourceManager;
        this.resourcePermissionManager = resourcePermissionManager;
        this.departmentManager = departmentManager;
        this.resourcePermissionAuthAction = resourcePermissionAuthAction;
        this.resourcePermissionCacheManager = resourcePermissionCacheManager;
    }

    @Override
    @Transactional
    public UserDTO createUser(String username, String nickName, String contactEmail, String password
            , UserTypeEnum userType, String departmentId, List<String> roleIds) {

        checkCreateUserPermission(userType);

        if (userManager.isUserExists(username)) {
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.USER_EXISTS, username);
        }

        UserDTO user = userManager.createUser(username, nickName, contactEmail, password, userType, departmentId);
        if (roleIds.size() == 0) {
            return user;
        }
        resourcePermissionAuthAction.modifyUserRole(user.getId(), roleIds);
        return user;
    }

    private void checkCreateUserPermission(UserTypeEnum userType) {
        if (UserTypeEnum.SCRIPT.equals(userType)) {
            if (!resourcePermissionAuthAction.hasPermission(
                    userManager.getCurrentUser().getId()
                    , resourceManager.getRootResource().getId()
                    , ResourcePermissionTypeEnum.CREATE_SCRIPT_USER))  {

                String companyName = departmentManager.getCompanyInfo().getCompanyName();
                throw new AuthorizationException(ResourceTypeEnum.DEPT, companyName, ResourcePermissionTypeEnum.CREATE_SCRIPT_USER);
            }
        } else {
            if(!resourcePermissionAuthAction.hasPermission(
                    userManager.getCurrentUser().getId()
                    , resourceManager.getRootResource().getId()
                    , ResourcePermissionTypeEnum.CREATE_USER)) {

                String companyName = departmentManager.getCompanyInfo().getCompanyName();
                throw new AuthorizationException(ResourceTypeEnum.DEPT, companyName, ResourcePermissionTypeEnum.CREATE_USER);
            }
        }
    }

    @Override
    public Collection<String> listUsersByBookCanRead() {

        String userId = userManager.getCurrentUser().getId();
        List<String> resourceIds = resourcePermissionManager
                .listResourcePermissionByUserIdsAndResourcePermissionType(userId, ResourcePermissionTypeEnum.READ_BOOK)
                .stream().map(ResourcePermissionDTO::getResourceId).collect(Collectors.toList());

        Collection<UserDTO> users = userManager.findAllValidUser();

        return resourcePermissionManager.listUserIdByResourceIdsAndResourcePermissionType(resourceIds, ResourcePermissionTypeEnum.CREATE_TRADE)
                .stream()
                .map(v -> users.stream().filter(u -> Objects.equals(u.getId(), v)).findAny().map(UserDTO::getUsername).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

    }

    @Override
    @Transactional
    public UserDTO getUserByUsername(String username) {
        UserDTO userDto = userManager.getCurrentUser();
        if(Objects.equals(userDto.getUsername(), username)){
            return userManager.getUserByUserName(username);
        }
        if(!resourcePermissionAuthAction.hasPermission(
                userDto.getId()
                , departmentManager.getDepartmentWithResource(userDto.getDepartmentId()).getResourceId()
                , ResourcePermissionTypeEnum.READ_USER)) {

            throw new AuthorizationException(ResourceTypeEnum.USER, username, ResourcePermissionTypeEnum.READ_USER);
        }

        return userManager.getUserByUserName(username);
    }

    @Override
    public UserDTO getCurrentUser() {
        return userManager.getCurrentUser();
    }

    @Override
    @Transactional
    public Collection<UserDTO> listUsers() {
        UserDTO userDto = userManager.getCurrentUser();

        Collection<String> resourceIds = resourcePermissionManager
                .listResourcePermissionByUserIdsAndResourcePermissionType(userDto.getId(), ResourcePermissionTypeEnum.READ_USER)
                .stream().map(ResourcePermissionDTO::getResourceId).collect(Collectors.toSet());

        Collection<String> departmentIds = resourceManager.listResourceByResourceId(resourceIds)
                .stream().map(ResourceDTO::getDepartmentId).collect(Collectors.toSet());

        return userManager.listUserByDepartmentId(departmentIds);
    }

    @Override
    public UserStatusDTO updateUserLocked(String username, Boolean locked) {
        if(!resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(
                locked ? ResourcePermissionTypeEnum.LOCK_USER : ResourcePermissionTypeEnum.UNLOCK_USER)) {

            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.UNLOCK_USER);
        }

        if (AuthConstants.ADMIN.equals(username))
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.EXPIRE_OR_LOCK_ADMIN);

        return userManager.updateUserLocked(username, locked);
    }

    @Override
    public UserStatusDTO updateUserExpired(String username, Boolean expired) {
        if(!resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(
                expired ? ResourcePermissionTypeEnum.EXPIRE_USER : ResourcePermissionTypeEnum.UNEXPIRE_USER)) {

            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.EXPIRE_USER);
        }

        if (AuthConstants.ADMIN.equals(username) && expired) {
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.EXPIRE_OR_LOCK_ADMIN);
        }

        if (Objects.equals(userManager.getUserByUserName(username).getUserType(), UserTypeEnum.SCRIPT)) {
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.EXPIRE_OR_LOCK_SCRIPT);
        }

        return userManager.updateUserExpired(username, expired);
    }

    @Override
    public UserDTO updatePassword(String userId, String password) {
        if(!resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.CHANGE_PASSWORD)) {
            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.CHANGE_PASSWORD);
        }
        return userManager.updatePassword(userId, password);
    }

    @Override
    public UserDTO updateOwnPassword(String oldPassword, String newPassword) {
        return userManager.updateOwnPassword(userManager.getCurrentUser().getUsername(), oldPassword, newPassword);
    }

    @Override
    public UserDTO updateUserAttributes(String userId, String username, String nickName, UserTypeEnum userType, String contactEmail, String departmentId) {
        if(!Objects.equals(userManager.getCurrentUser().getId(), userId)
            && !resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.UPDATE_USER)) {

            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.UPDATE_USER);
        }

        if (userManager.isOtherUserExists(userId, username, departmentId)) {
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.USER_EXISTS, username);
        }

        UserDTO userDTO = userManager.getUserByUserId(userId);
        if (AuthConstants.ADMIN.equals(userDTO.getUsername())
                && !AuthConstants.ADMIN.equals(username)) {

            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.UPDATE_ADMIN_NAME);
        }

        if (AuthConstants.ADMIN.equals(userDTO.getUsername()) && !departmentId.equals(userDTO.getDepartmentId())) {

            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.UPDATE_ADMIN_DEPARTMENT);
        }

        return userManager.updateUserAttributes(userId, username, nickName, userType, contactEmail, departmentId);
    }

    @Override
    public void revokeUser(String userId) {
        if(!resourcePermissionAuthAction.hasCompanyResourcePermissionForCurrentUser(ResourcePermissionTypeEnum.DELETE_USER)) {
            String companyName = departmentManager.getCompanyInfo().getCompanyName();
            throw new AuthorizationException(ResourceTypeEnum.COMPANY, companyName, ResourcePermissionTypeEnum.DELETE_USER);
        }

        UserDTO userDto = userManager.getUserByUserId(userId);
        if(Objects.equals(userDto.getUsername(), Constants.ADMIN))
            throw new AuthServiceException(ReturnMessageAndTemplateDef.Errors.DELETE_ADMIN);

        userManager.revokeUser(userId);
    }

    @Override
    public Boolean isUserValid(String username) {
        return userManager.isUserExists(username) && userManager.getUserByUserName(username).isLoginPermitted();
    }

    @Override
    public void logoutUser(String userId) {
        //todo:用户登出
    }

}
