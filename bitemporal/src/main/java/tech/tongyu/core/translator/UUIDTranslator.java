package tech.tongyu.core.translator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class UUIDTranslator implements Translator<UUID> {

    @Override
    public UUID translate(String in) {
        return UUID.fromString(in);
    }
}
