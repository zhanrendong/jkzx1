package tech.tongyu.bct.service.quantlib.server.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by jinkepeng on 2017/6/13.
 */
@Configuration
public class ApplicationConfiguration {

    @Autowired
    private Environment environment;

    public String applicationName() {
        return environment.getProperty("spring.application.name");
    }

    public int httpPort() {
        return environment.getProperty("server.port", Integer.class);
    }
}
