package tech.tongyu.bct.document.dao.dbo;

import org.hibernate.annotations.Type;
import tech.tongyu.bct.document.service.DocumentService;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = DocumentService.SCHEMA)
public class TemplateContent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Lob
    @Type(type = "text")
    @Column
    private String content;

    @Lob
    @Column(name="doc")
    @Type(type="org.hibernate.type.BinaryType")
    private byte[] doc;

    public TemplateContent() {
    }

    public TemplateContent(String content) {
        this.content = content;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getDoc() {
        return doc;
    }

    public void setDoc(byte[] doc) {
        this.doc = doc;
    }
}
