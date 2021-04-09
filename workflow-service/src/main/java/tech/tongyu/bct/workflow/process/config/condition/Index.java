package tech.tongyu.bct.workflow.process.config.condition;

import tech.tongyu.bct.workflow.process.config.FormData;

public interface Index<T> {

    /**
     * every index should have a name.
     * @return name of index
     */
    String getName();

    /**
     *
     * @param formData form data
     * @return the type of the result of the calculation should be defined when implementation
     */
    T calculate(FormData formData);
}
