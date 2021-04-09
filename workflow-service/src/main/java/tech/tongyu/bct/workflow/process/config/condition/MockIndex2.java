package tech.tongyu.bct.workflow.process.config.condition;

import tech.tongyu.bct.workflow.process.config.FormData;

public class MockIndex2 implements Index<Double> {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Double calculate(FormData formData) {
        return 100_0000.;
    }
}
