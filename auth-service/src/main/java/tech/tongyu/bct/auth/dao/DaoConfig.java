package tech.tongyu.bct.auth.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.auth.dao.entity.UserDbo;
import tech.tongyu.bct.dev.DevLoadedTest;

@Configuration
@ComponentScan(basePackageClasses = DaoConfig.class)
@EnableJpaRepositories(basePackageClasses = DaoConfig.class)
@EntityScan(basePackageClasses = UserDbo.class)
public class DaoConfig implements DevLoadedTest {
    public interface LIMIT{
        int LENGTH_ROLE_REMARK = 1000;
        int LENGTH_DEFAULT = 255;
    }

}
