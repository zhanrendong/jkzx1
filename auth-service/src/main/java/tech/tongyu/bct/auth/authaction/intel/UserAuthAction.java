package tech.tongyu.bct.auth.authaction.intel;

import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.dto.UserStatusDTO;
import tech.tongyu.bct.auth.enums.UserTypeEnum;

import java.util.Collection;
import java.util.List;

public interface UserAuthAction {

    UserDTO createUser(String username, String nickName, String contactEmail, String password
            , UserTypeEnum userType, String departmentId, List<String> roleIds);

    UserDTO getUserByUsername(String username);

    UserDTO getCurrentUser();

    Collection<UserDTO> listUsers();

    Collection<String> listUsersByBookCanRead();

    UserStatusDTO updateUserLocked(String username, Boolean locked);

    UserStatusDTO updateUserExpired(String username, Boolean expired);

    UserDTO updatePassword(String userId, String password);

    UserDTO updateOwnPassword(String oldPassword, String newPassword);

    UserDTO updateUserAttributes(String userId, String username, String nickName
            , UserTypeEnum userType, String contactEmail, String departmentId);

    void revokeUser(String userId);

    Boolean isUserValid(String username);

    void logoutUser(String userId);
}
