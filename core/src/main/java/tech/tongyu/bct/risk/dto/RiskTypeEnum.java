package tech.tongyu.bct.risk.dto;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public enum RiskTypeEnum {
    /**
     * 用户输入的数据，包括实时的定价结果
     */
    MOCK,
    /**
     * 系统中查询出的客观数据或统计数据
     */
    FACT,
    /**
     * 跟随业务规则调整的各类限额
     */
    LIMIT,
    ;
}
