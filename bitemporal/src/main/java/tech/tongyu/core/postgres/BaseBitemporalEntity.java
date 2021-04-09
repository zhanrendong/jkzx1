package tech.tongyu.core.postgres;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import tech.tongyu.core.postgres.type.PGJson;
import tech.tongyu.core.postgres.type.PGTsTzRange;
import tech.tongyu.core.postgres.type.TsTzRange;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
@TypeDefs({
        @TypeDef(name = "PGJson", typeClass = PGJson.class),
        @TypeDef(name = "PGTsTzRange", typeClass = PGTsTzRange.class)
})
public class BaseBitemporalEntity {

    @Id
    @Column(name = "entity_uuid", nullable = false)
    private UUID entityVersionId;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "entity_status", nullable = false, columnDefinition = "smallint")
    private int entityStatus;

    @Column(name = "application_event_id")
    private UUID applicationEventId;

    @Column(name = "entity_doc", columnDefinition = "jsonb")
    @Type(type = "PGJson")
    private JsonNode entityDoc;

    @Column(name = "valid_range", nullable = false, columnDefinition = "tstzrange")
    @Type(type = "PGTsTzRange")
    private TsTzRange validRange;

    @Column(name = "system_range", nullable = false, columnDefinition = "tstzrange")
    @Type(type = "PGTsTzRange")
    private TsTzRange systemRange;

    public UUID getEntityVersionId() {
        return entityVersionId;
    }

    public void setEntityVersionId(UUID entityVersionId) {
        this.entityVersionId = entityVersionId;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public int getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(int entityStatus) {
        this.entityStatus = entityStatus;
    }

    public UUID getApplicationEventId() {
        return applicationEventId;
    }

    public void setApplicationEventId(UUID applicationEventId) {
        this.applicationEventId = applicationEventId;
    }

    public JsonNode getEntityDoc() {
        return entityDoc;
    }

    public void setEntityDoc(JsonNode entityDoc) {
        this.entityDoc = entityDoc;
    }

    public TsTzRange getValidRange() {
        return validRange;
    }

    public void setValidRange(TsTzRange validRange) {
        this.validRange = validRange;
    }

    public TsTzRange getSystemRange() {
        return systemRange;
    }

    public void setSystemRange(TsTzRange systemRange) {
        this.systemRange = systemRange;
    }
}
