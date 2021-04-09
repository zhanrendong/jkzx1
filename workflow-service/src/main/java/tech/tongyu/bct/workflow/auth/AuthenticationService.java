package tech.tongyu.bct.workflow.auth;

import tech.tongyu.bct.workflow.dto.UserDTO;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface AuthenticationService {

    /**
     * authenticate user by user name
     * @param username user's name
     * @return UserDTO (with candidate group)
     */
    UserDTO authenticateByUsername(String username);

    /**
     * authenticate user by user id
     * @param userId user's id
     * @return UserDTO (with candidate group)
     */
    UserDTO authenticateByUserId(String userId);

    /**
     * authenticate current user
     * @return UserDTO (with candidate group)
     */
    UserDTO authenticateCurrentUser();
}
