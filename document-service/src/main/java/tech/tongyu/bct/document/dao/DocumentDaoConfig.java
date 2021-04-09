package tech.tongyu.bct.document.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.document.dao.dbo.Template;

/**
 * 配置JPA.
 * @author hangzhi
 */
@Configuration
@ComponentScan(basePackageClasses = DocumentDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = Template.class)
public class DocumentDaoConfig {
}
