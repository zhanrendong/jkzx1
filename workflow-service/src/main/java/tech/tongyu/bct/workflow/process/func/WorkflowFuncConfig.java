package tech.tongyu.bct.workflow.process.func;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tech.tongyu.bct.workflow.process.func.action.cap.CapImplConfig;
import tech.tongyu.bct.workflow.process.func.action.trade.TradeImplConfig;

@Configuration
@ComponentScan(basePackageClasses = WorkflowFuncConfig.class)
@Import({CapImplConfig.class, TradeImplConfig.class})
public class WorkflowFuncConfig {
}
