package tech.tongyu.bct.user.preference.dao;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.user.preference.dao.dbo.Preference;


@Configuration
@ComponentScan(basePackageClasses = PreferenceDataDaoConfig.class)
@EnableJpaRepositories
@EntityScan(basePackageClasses = Preference.class)
public class PreferenceDataDaoConfig {
}
