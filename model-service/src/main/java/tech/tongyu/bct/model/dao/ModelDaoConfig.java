package tech.tongyu.bct.model.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.model.dao.dbo.ModelData;

@Configuration
@ComponentScan(basePackageClasses = ModelDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = ModelData.class)
public class ModelDaoConfig {
}
