package tech.tongyu.bct.trade.dao.dbo;

import tech.tongyu.bct.common.jpa.HasUuid;
import tech.tongyu.bct.trade.service.TradeService;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = TradeService.SCHEMA)
public class PositionBook implements HasUuid {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    //trade's natural key
    @Column(nullable = false, unique = true)
    private String bookName;

    public PositionBook(){}

    public PositionBook(String bookName){
        this.bookName = bookName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
