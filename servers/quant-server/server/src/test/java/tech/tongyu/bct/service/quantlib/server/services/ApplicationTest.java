package tech.tongyu.bct.service.quantlib.server.services;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import tech.tongyu.bct.service.quantlib.server.http.QuantServerApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = QuantServerApplication.class)
@ActiveProfiles("test")
@WebAppConfiguration
public abstract class ApplicationTest {
}
