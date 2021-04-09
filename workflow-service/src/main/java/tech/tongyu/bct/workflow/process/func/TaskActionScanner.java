package tech.tongyu.bct.workflow.process.func;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface TaskActionScanner {

    /**
     * get implementation of TaskAction by given className
     * @param className class name of the class with the interface TaskAction
     * @return TaskAction object
     */
    TaskAction getTaskAction(String className);
}
