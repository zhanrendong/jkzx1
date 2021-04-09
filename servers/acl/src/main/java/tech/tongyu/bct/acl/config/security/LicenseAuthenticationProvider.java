package tech.tongyu.bct.acl.config.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.acl.config.security.exception.JwtAuthenticationException;

import java.util.Objects;

@Component
public class LicenseAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        throw new JwtAuthenticationException();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return Objects.equals(authentication, JwtAuthentication.class);
    }
}
