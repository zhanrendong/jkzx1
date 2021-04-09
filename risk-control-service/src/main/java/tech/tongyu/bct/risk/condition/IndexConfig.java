package tech.tongyu.bct.risk.condition;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import tech.tongyu.bct.dev.DevLoadedTest;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

@Configuration("WorkflowIndexConfig")
@ComponentScan(basePackageClasses = IndexConfig.class)
public class IndexConfig implements DevLoadedTest {
}
