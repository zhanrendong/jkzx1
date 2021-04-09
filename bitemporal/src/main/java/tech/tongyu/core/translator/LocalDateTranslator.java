package tech.tongyu.core.translator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateTranslator implements Translator<LocalDate> {
    private static DateTimeFormatter format = DateTimeFormatter.ISO_DATE;

    @Override
    public LocalDate translate(String in) {
        return LocalDate.parse(in, format);
    }
}
