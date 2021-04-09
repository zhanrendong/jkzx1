package tech.tongyu.bct.acl.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.web.context.WebApplicationContext;
import tech.tongyu.bct.acl.common.UserInfo;
import tech.tongyu.bct.dev.DevLoadedTest;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = AuthClientConfig.class)
public class AuthClientConfig implements DevLoadedTest {

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public UserInfo user() {
        return new UserInfo();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

}
