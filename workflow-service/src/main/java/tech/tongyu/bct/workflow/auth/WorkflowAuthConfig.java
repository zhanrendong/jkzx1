package tech.tongyu.bct.workflow.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import tech.tongyu.bct.acl.config.AuthClientConfig;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.manager.ManagerConfig;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.workflow.process.manager.ConversionManager;

import java.util.Collection;

@Configuration
@ComponentScan(basePackageClasses = WorkflowAuthConfig.class)
@Import({ManagerConfig.class, AuthClientConfig.class})
public class WorkflowAuthConfig implements DevLoadedTest {

    @Autowired
    private UserManager userManager;

    @Autowired
    private ConversionManager conversionManager;

    @Bean
    @Primary
    public UserDetailsService authUserDetailsService(){
        return username -> {
            UserDTO userDTO = userManager.getUserByUserName(username);

            return new UserDetails() {
                @Override
                public Collection<? extends GrantedAuthority> getAuthorities() {
                    return conversionManager.conversionByUsername(username);
                }

                @Override
                public String getPassword() {
                    return userDTO.getPassword();
                }

                @Override
                public String getUsername() {
                    return username;
                }

                @Override
                public boolean isAccountNonExpired() {
                    return userDTO.getExpired();
                }

                @Override
                public boolean isAccountNonLocked() {
                    return userDTO.getLocked();
                }

                @Override
                public boolean isCredentialsNonExpired() {
                    return userDTO.getExpired();
                }

                @Override
                public boolean isEnabled() {
                    return userDTO.getLocked();
                }
            };
        };
    }
}
