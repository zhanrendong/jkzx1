package tech.tongyu.core.postgres;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public class BaseRelationalEntity {

    @Id
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "entity_doc", columnDefinition = "jsonb")
    @Type(type = "PGJson")
    private JsonNode entityDoc;

    public UUID getEntityId() {
        return entityId;
    }

    public BaseRelationalEntity setEntityId(UUID entityId) {
        this.entityId = entityId;
        return this;
    }

    public JsonNode getEntityDoc() {
        return entityDoc;
    }

    public BaseRelationalEntity setEntityDoc(JsonNode entityDoc) {
        this.entityDoc = entityDoc;
        return this;
    }
}