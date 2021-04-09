package tech.tongyu.bct.workflow.dto;

/**
 * @author yongbin
 * - mailto: wuyongbin@tongyu.tech
 */
public class IndexDTO {

    public IndexDTO(String indexName, String indexClass) {
        this.indexName = indexName;
        this.indexClass = indexClass;
    }

    private String indexName;
    private String indexClass;

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

    @Override
    public String toString() {
        return "IndexDTO{" +
                "indexName='" + indexName + '\'' +
                ", indexClass='" + indexClass + '\'' +
                '}';
    }
}
