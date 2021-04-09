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
public class BaseTemporalEntity {

    @Id
    @Column(name = "entity_uuid", nullable = false)
    private UUID entityUuid;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "entity_status", nullable = false, columnDefinition = "smallint")
    private int entityStatus;

    @Column(name = "entity_doc", columnDefinition = "jsonb")
    @Type(type = "PGJson")
    private JsonNode entityDoc;

    @Column(name = "valid_range", nullable = false, columnDefinition = "tstzrange")
    @Type(type = "PGTsTzRange")
    private TsTzRange validRange;

    public UUID getEntityUuid() {
        return entityUuid;
    }

    public BaseTemporalEntity setEntityUuid(UUID entityUuid) {
        this.entityUuid = entityUuid;
        return this;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public BaseTemporalEntity setEntityId(UUID entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityStatus() {
        return entityStatus;
    }

    public BaseTemporalEntity setEntityStatus(int entityStatus) {
        this.entityStatus = entityStatus;
        return this;
    }

    public JsonNode getEntityDoc() {
        return entityDoc;
    }

    public BaseTemporalEntity setEntityDoc(JsonNode entityDoc) {
        this.entityDoc = entityDoc;
        return this;
    }

    public TsTzRange getValidRange() {
        return validRange;
    }

    public BaseTemporalEntity setValidRange(TsTzRange validRange) {
        this.validRange = validRange;
        return this;
    }
}
