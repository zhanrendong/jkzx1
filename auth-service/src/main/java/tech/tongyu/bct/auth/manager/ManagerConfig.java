package tech.tongyu.bct.auth.manager;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.auth.dao.DaoConfig;
import tech.tongyu.bct.dev.DevLoadedTest;

@Configuration
@ComponentScan(basePackageClasses = ManagerConfig.class)
@Import(DaoConfig.class)
public class ManagerConfig implements DevLoadedTest {
}
