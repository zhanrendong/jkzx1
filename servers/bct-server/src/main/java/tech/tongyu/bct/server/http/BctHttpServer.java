package tech.tongyu.bct.server.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import tech.tongyu.bct.market.service.MarketDataServiceConfig;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@Import(value = {MarketDataServiceConfig.class})
@ComponentScan(basePackages = "tech.tongyu.bct")
@EnableJpaRepositories(basePackages = "tech.tongyu.bct")
@EntityScan(basePackages = "tech.tongyu.bct")
public class BctHttpServer {
    public static void main(String[] args) {
        SpringApplication.run(BctHttpServer.class, args);
    }
}
