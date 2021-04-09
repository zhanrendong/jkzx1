package tech.tongyu.bct.cm.reference.elemental;

import java.util.UUID;

public interface Account {
    UUID accountUUID();

    String accountCode();

    String accountName();
}
