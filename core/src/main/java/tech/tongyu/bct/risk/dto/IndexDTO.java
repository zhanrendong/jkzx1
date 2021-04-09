package tech.tongyu.bct.risk.dto;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public class IndexDTO {

    public IndexDTO(String indexName, String indexClass, RiskTypeEnum riskTypeEnum) {
        this.indexName = indexName;
        this.indexClass = indexClass;
        this.riskTypeEnum = riskTypeEnum;
    }

    private String indexName;
    private String indexClass;
    private RiskTypeEnum riskTypeEnum;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexClass() {
        return indexClass;
    }

    public void setIndexClass(String indexClass) {
        this.indexClass = indexClass;
    }

    public RiskTypeEnum getRiskTypeEnum() {
        return riskTypeEnum;
    }

    public void setRiskTypeEnum(RiskTypeEnum riskTypeEnum) {
        this.riskTypeEnum = riskTypeEnum;
    }

    @Override
    public String toString() {
        return "IndexDTO{" +
                "indexName='" + indexName + '\'' +
                ", indexClass='" + indexClass + '\'' +
                ", riskTypeEnum=" + riskTypeEnum +
                '}';
    }
}
