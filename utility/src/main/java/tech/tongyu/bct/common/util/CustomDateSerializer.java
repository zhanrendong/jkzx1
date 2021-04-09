package tech.tongyu.bct.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CustomDateSerializer extends StdSerializer<Date> {

    public CustomDateSerializer() {
        this(null);
    }

    public CustomDateSerializer(Class<Date> clazz) {
        super(clazz);
    }

    @Override
    public void serialize (Date value, JsonGenerator gen, SerializerProvider arg2)
            throws IOException {
        gen.writeString(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(
                        ZoneId.of("Asia/Shanghai"))
                        .format(value.toInstant()));
    }
}
