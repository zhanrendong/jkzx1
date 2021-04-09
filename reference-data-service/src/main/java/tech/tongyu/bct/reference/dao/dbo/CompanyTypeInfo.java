package tech.tongyu.bct.reference.dao.dbo;


import tech.tongyu.bct.reference.service.CompanyTypeInfoService;
import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = CompanyTypeInfoService.SCHEMA)
public class CompanyTypeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    @Column
    private String futuresId;
    @Column
    private String levelOneType;
    @Column
    private String levelTwoType;
    @Column
    private String classifyName;
    @Column
    private String clientName;
    @Column
    private String nocId;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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
