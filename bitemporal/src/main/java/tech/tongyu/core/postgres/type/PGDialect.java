package tech.tongyu.core.postgres.type;

import org.hibernate.dialect.PostgreSQL95Dialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class PGDialect extends PostgreSQL95Dialect {

    public PGDialect() {
        super();
        registerColumnType(9000, "jsonb");
        registerColumnType(9001, "tstzrange");
        registerColumnType(9002, "uuid[]");
    }
}
