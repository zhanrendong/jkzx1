package tech.tongyu.bct.workflow.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public class AssigneeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> assigneeList;
    /**
     * 最大允许通过数
     * 影响：
     * 1.达到这个数值会签完成,所有Task实例清除
     * 2.跳转网关 -> true
     */
    private Integer upMax;
    /**
     * 最大允许失败数
     * 影响
     * 1.达到这个数值会签完成,所有Task实例清除
     * 2.跳转网关 -> false
     */
    private Integer downMax;

    public AssigneeDTO(List<String> assigneeList, Integer upMax, Integer downMax) {
        this.assigneeList = assigneeList;
        this.upMax = upMax;
        this.downMax = downMax;
    }

    public List<String> getAssigneeList() {
        return assigneeList;
    }

    public void setAssigneeList(List<String> assigneeList) {
        this.assigneeList = assigneeList;
    }

    public Integer getUpMax() {
        return upMax;
    }

    public void setUpMax(Integer upMax) {
        this.upMax = upMax;
    }

    public Integer getDownMax() {
        return downMax;
    }

    public void setDownMax(Integer downMax) {
        this.downMax = downMax;
    }
}
