package tech.tongyu.bct.workflow.process.trigger;

import java.util.Map;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */

public interface Index {

    /**
     * 获取指标名称
     * @return 指标名称
     */
    String getIndexName();

    /**
     * 指标获取方法
     * @param data 传入参数
     * @return 具体指标数据
     */
    Object execute(Map<String, Object> formData, Map<String, Object> data);

    /**
     * 指标返回参数类型
     * @return 具体指标数据类型
     */
    Class<?> getClassType();
}
