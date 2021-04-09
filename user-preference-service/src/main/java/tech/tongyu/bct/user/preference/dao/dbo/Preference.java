package tech.tongyu.bct.user.preference.dao.dbo;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.user.preference.service.PreferenceService;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = PreferenceService.SCHEMA)
public class Preference implements HasUuid {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column
    @ElementCollection
    private List<String> volSurfaceInstrumentIds;

    @Column
    @ElementCollection
    private List<String> dividendCurveInstrumentIds;

    @CreationTimestamp
    @Column
    private Instant createdAt;

    @UpdateTimestamp
    @Column
    private Instant updatedAt;

    public Preference() {
    }

    public Preference(String userName, List<String> volSurfaceInstrumentIds) {
        this.userName = userName;
        this.volSurfaceInstrumentIds = volSurfaceInstrumentIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getVolSurfaceInstrumentIds() {
        return volSurfaceInstrumentIds;
    }

    public void setVolSurfaceInstrumentIds(List<String> volSurfaceInstrumentIds) {
        this.volSurfaceInstrumentIds = volSurfaceInstrumentIds;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<String> getDividendCurveInstrumentIds() {
        return dividendCurveInstrumentIds;
    }

    public void setDividendCurveInstrumentIds(List<String> dividendCurveInstrumentIds) {
        this.dividendCurveInstrumentIds = dividendCurveInstrumentIds;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
