package tech.tongyu.bct.auth.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
//import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
//import org.springframework.session.web.http.CookieHttpSessionStrategy;
//import org.springframework.session.web.http.HttpSessionStrategy;
//import tech.tongyu.bct.common.config.ProfileConstants;

@Configuration
//@EnableCaching
//@EnableRedisHttpSession
//@Profile({ProfileConstants.PRODUCTION, ProfileConstants.TEST})
public class SessionAndCacheConfig {

//    @Bean
//    public HttpSessionStrategy httpSessionStrategy() {
//        return new CookieHttpSessionStrategy();
//    }
}
