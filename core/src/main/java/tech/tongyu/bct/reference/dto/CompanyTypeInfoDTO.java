package tech.tongyu.bct.reference.dto;

public class CompanyTypeInfoDTO {
    private String uuid;
    private String futuresId;
    private String levelOneType;
    private String levelTwoType;
    private String classifyName;
    private String clientName;
    private String nocId;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFuturesId() {
        return futuresId;
    }

    public void setFuturesId(String futuresId) {
        this.futuresId = futuresId;
    }

    public String getLevelOneType() {
        return levelOneType;
    }

    public void setLevelOneType(String levelOneType) {
        this.levelOneType = levelOneType;
    }

    public String getLevelTwoType() {
        return levelTwoType;
    }

    public void setLevelTwoType(String levelTwoType) {
        this.levelTwoType = levelTwoType;
    }

    public String getClassifyName() {
        return classifyName;
    }

    public void setClassifyName(String classifyName) {
        this.classifyName = classifyName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getNocId() {
        return nocId;
    }

    public void setNocId(String nocId) {
        this.nocId = nocId;
    }
}
