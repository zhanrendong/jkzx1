package tech.tongyu.bct.model.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.model.dao.ModelDaoConfig;

@Configuration
@Import(value = {ModelDaoConfig.class})
@ComponentScan(basePackageClasses = ModelServiceConfig.class)
public class ModelServiceConfig {
}
