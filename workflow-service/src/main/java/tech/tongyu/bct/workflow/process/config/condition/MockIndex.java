package tech.tongyu.bct.workflow.process.config.condition;

import tech.tongyu.bct.workflow.process.config.FormData;

public class MockIndex implements Index<String>{

    @Override
    public String getName() {
        return "hello";
    }

    @Override
    public String calculate(FormData formData) {
        return "hello";
    }
}
