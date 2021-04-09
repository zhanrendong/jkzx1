package tech.tongyu.bct.user.preference.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.user.preference.dao.PreferenceDataDaoConfig;

@Configuration
@Import(value = {PreferenceDataDaoConfig.class})
@ComponentScan(basePackageClasses = PreferenceServiceConfig.class)
public class PreferenceServiceConfig {

}
