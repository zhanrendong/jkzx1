package tech.tongyu.bct.rpc.json.http.server.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.auth.dao.entity.UserDbo;
import tech.tongyu.bct.dev.DevLoadedTest;
import tech.tongyu.bct.rpc.json.http.server.dao.entity.SysLogDbo;

@Configuration
@ComponentScan(basePackageClasses = RpcDaoConfig.class)
@EnableJpaRepositories(basePackageClasses = RpcDaoConfig.class)
@EntityScan(basePackageClasses = SysLogDbo.class)
public class RpcDaoConfig implements DevLoadedTest {
    public interface LIMIT{
        int LENGTH_ROLE_REMARK = 1000;
        int LENGTH_DEFAULT = 255;
    }

}
