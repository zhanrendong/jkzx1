package tech.tongyu.bct.auth.service;

import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.dto.UserStatusDTO;

import java.util.Collection;
import java.util.List;

public interface UserService {
    UserDTO authUserCreate(String username, String password, String userType, String nickName, String externalType, String externalAccount, List<String> roleIds);
    UserStatusDTO authUserLock(String username);
    UserStatusDTO authUserUnlock(String username);
    UserStatusDTO authUserExpire(String username);
    UserStatusDTO authUserUnexpire(String username);
    UserDTO authUserUpdate(String userId, String username, String nickName, String userType, String contactEmail, String departmentId);
    Boolean                     authUserRevoke(String userId);
    UserDTO authUserPasswordChange(String userId, String password);
    Collection<UserDTO>         authUserList();
    UserDTO authUserByNameGet(String username);
    Collection<String>          authUserListByBookCanRead();
    Boolean authIsUserValid(String username);
    Boolean authUserLogout(String userId);
}
