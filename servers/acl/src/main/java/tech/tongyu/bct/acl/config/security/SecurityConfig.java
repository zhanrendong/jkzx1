package tech.tongyu.bct.acl.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tech.tongyu.bct.acl.config.filter.JwtAuthenticationTokenFilter;
import tech.tongyu.bct.dev.DevLoadedTest;

@Configuration
@ComponentScan(basePackageClasses = SecurityConfig.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements DevLoadedTest {

    private AuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private SessionRegistry sessionRegistry;


    @Autowired
    public SecurityConfig(AuthenticationEntryPoint jwtAuthenticationEntryPoint
            , JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter, SessionRegistry sessionRegistry){
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
        this.sessionRegistry = sessionRegistry;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .anonymous().disable()
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .headers().cacheControl()
                .and()
                .frameOptions().disable();

        httpSecurity
                .csrf().disable()
                .exceptionHandling()
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.GET, PathConstants.ARRAY_GET_PATHS).permitAll()
                    .antMatchers(HttpMethod.POST, PathConstants.ARRAY_POST_PATHS).permitAll()
                .anyRequest().authenticated();

        httpSecurity.sessionManagement().maximumSessions(1).expiredUrl("/").sessionRegistry(sessionRegistry);
    }
}
