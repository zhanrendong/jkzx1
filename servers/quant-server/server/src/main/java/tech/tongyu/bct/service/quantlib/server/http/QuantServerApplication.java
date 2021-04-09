package tech.tongyu.bct.service.quantlib.server.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tech.tongyu.bct.service.quantlib.server.services.ClassCache;
import tech.tongyu.bct.service.quantlib.server.services.EnumCache;
import tech.tongyu.bct.service.quantlib.server.services.FunctionFactory;
import tech.tongyu.bct.service.quantlib.server.services.Translator;

/**
 * Created by jinkepeng on 2017/6/13.
 */
@SpringBootApplication
@ComponentScan(basePackages = "tech.tongyu.bct.service.quantlib")
public class QuantServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(QuantServerApplication.class);

    public static void main(String[] args) throws Exception{
        logger.info("Setting display language");
        Translator.Instance.setLanguage("chinese");
        logger.info("Registering RPC APIs");
        FunctionFactory.registerAll();
        logger.info("Registering enums");
        EnumCache.Instance.initialize();
        logger.info("Registering serializable classes");
        ClassCache.Instance.initialize();
        SpringApplication.run(QuantServerApplication.class, args);
    }

}
