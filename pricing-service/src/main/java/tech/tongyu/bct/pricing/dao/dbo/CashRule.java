package tech.tongyu.bct.pricing.dao.dbo;


import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.pricing.service.PricingService;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = PricingService.SCHEMA)
public class CashRule {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    // each pe can have only one cash rule
    // WARNING: this is a restriction
    @Column(nullable = false, unique = true)
    private String pricingEnvironmentId;

    @Column(nullable = false)
    private Boolean discounted;

    @Column
    private String curveName;

    @Enumerated(EnumType.STRING)
    @Column
    private InstanceEnum curveInstance;

    public CashRule() {
    }

    public CashRule(String pricingEnvironmentId, Boolean discounted, String curveName, InstanceEnum curveInstance) {
        this.pricingEnvironmentId = pricingEnvironmentId;
        this.discounted = discounted;
        this.curveName = curveName;
        this.curveInstance = curveInstance;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPricingEnvironmentId() {
        return pricingEnvironmentId;
    }

    public void setPricingEnvironmentId(String pricingEnvironmentId) {
        this.pricingEnvironmentId = pricingEnvironmentId;
    }

    public Boolean getDiscounted() {
        return discounted;
    }

    public void setDiscounted(Boolean discounted) {
        this.discounted = discounted;
    }

    public String getCurveName() {
        return curveName;
    }

    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    public InstanceEnum getCurveInstance() {
        return curveInstance;
    }

    public void setCurveInstance(InstanceEnum curveInstance) {
        this.curveInstance = curveInstance;
    }
}
