package tech.tongyu.bct.workflow.dto;

import tech.tongyu.bct.workflow.process.enums.TaskTypeEnum;

import java.util.Collection;

/**
 * "task node"-like task node(including taskNodeDbo taskNodeDTO ModifiedTaskNode, etc) should implement
 * method signatures here.
 * @author yangyiwei
 *  - mailto: yangyiwei@tongyu.tech
 */
public interface TaskNode {

    /**
     * get task name
     * @return task's name
     */
    String getTaskName();

    /**
     * get task type
     * @return taskType(insertData, updateData, reviewData)
     */
    TaskTypeEnum getTaskType();

    /**
     * get sequence of task node
     * -1, -2 for insertData & updateData
     * 0, 1, 2, ... for reviewData
     * @return sequence of task node
     */
    Integer getSequence();

    /**
     * get action class
     * the string indicates the class implements ActionClass interface
     * @return String of action class
     */
    String getActionClass();

    Collection<String> getApproveGroupList();
}
