package tech.tongyu.bct.workflow.process.func.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.tongyu.bct.workflow.dto.UserDTO;
import tech.tongyu.bct.workflow.dto.process.CommonProcessData;
import tech.tongyu.bct.workflow.dto.process.ProcessData;
import tech.tongyu.bct.workflow.process.Process;
import tech.tongyu.bct.workflow.process.Task;
import tech.tongyu.bct.workflow.process.func.Action;
import tech.tongyu.bct.workflow.process.func.Modifier;

@Component
public class MockActionAndModifier implements Action, Modifier {

    private static Logger logger = LoggerFactory.getLogger(MockActionAndModifier.class);


    @Override
    public CommonProcessData execute(Process process, Task task, UserDTO userDTO, CommonProcessData elderProcessData, CommonProcessData processData, String starter) {
        logger.info("\t===> 执行命令");
        return elderProcessData;
    }

    @Override
    public ProcessData modify(UserDTO userDTO, ProcessData processData) {
        logger.info("\t===> 修改参数");
        return processData;
    }
}
