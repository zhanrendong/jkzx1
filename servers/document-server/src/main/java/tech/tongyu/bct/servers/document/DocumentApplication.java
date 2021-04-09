package tech.tongyu.bct.servers.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "tech.tongyu.bct")
@EntityScan("tech.tongyu.bct")
@EnableJpaRepositories("tech.tongyu.bct")
public class DocumentApplication {
    public static void main(String[] args) {
       SpringApplication.run(DocumentApplication.class, args);
    }
}
