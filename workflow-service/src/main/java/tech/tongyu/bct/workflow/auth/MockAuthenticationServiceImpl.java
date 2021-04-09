package tech.tongyu.bct.workflow.auth;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.workflow.dto.UserDTO;

@Component
public class MockAuthenticationServiceImpl implements AuthenticationService{

    private UserManager userManager;

    @Autowired
    public MockAuthenticationServiceImpl(UserManager userManager){
        this.userManager = userManager;
    }

    @Override
    public UserDTO authenticateByUsername(String username) {
        tech.tongyu.bct.auth.dto.UserDTO userDTO = userManager.getUserByUserName(username);
        return new UserDTO(userDTO.getUsername(), userDTO.getRoleName());
    }

    @Override
    public UserDTO authenticateByUserId(String userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserDTO authenticateCurrentUser() {
        tech.tongyu.bct.auth.dto.UserDTO userDTO = userManager.getCurrentUser();
        return new UserDTO(userDTO.getUsername(), userDTO.getRoleName());
    }
}
