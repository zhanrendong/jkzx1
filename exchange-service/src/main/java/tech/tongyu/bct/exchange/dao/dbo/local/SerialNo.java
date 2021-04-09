package tech.tongyu.bct.exchange.dao.dbo.local;


import tech.tongyu.bct.common.api.doc.BctField;
import tech.tongyu.bct.exchange.service.ExchangeService;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = ExchangeService.SCHEMA)
public class SerialNo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @BctField(name = "uuid", description = "唯一标识", type = "String")
    private UUID uuid;

    @Column
    @BctField(name = "serialNoMax", description = "最大序列号", type = "Integer")
    private Integer serialNoMax;

    public SerialNo() {
    }

    public SerialNo(Integer serialNoMax) {
        this.serialNoMax = serialNoMax;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getSerialNoMax() {
        return serialNoMax;
    }

    public void setSerialNoMax(Integer serialNoMax) {
        this.serialNoMax = serialNoMax;
    }
}
