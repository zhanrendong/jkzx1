package tech.tongyu.bct.auth.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.auth.captcha.CaptchaConfig;
import tech.tongyu.bct.auth.service.impl.ServiceConfig;

@Configuration
@ComponentScan(basePackageClasses = ControllerConfig.class)
@Import({CaptchaConfig.class, ServiceConfig.class})
public class ControllerConfig {
}
