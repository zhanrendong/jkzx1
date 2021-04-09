package tech.tongyu.bct.trade.dto.trade;

import tech.tongyu.bct.common.api.doc.BctField;

public class BookDTO {
    @BctField(name = "bookUUID", description = "交易簿唯一标识", type = "String")
    public String bookUUID;
    @BctField(name = "bookName", description = "交易簿名称", type = "String")
    public String bookName;

    public BookDTO(String bookUUID, String bookName) {
        this.bookUUID = bookUUID;
        this.bookName = bookName;
    }
}
