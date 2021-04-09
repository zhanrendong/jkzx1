package tech.tongyu.bct.service.quantlib.server.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

public enum Translator {
    Instance;

    private static final Logger logger = LoggerFactory.getLogger(Translator.class);

    private static Locale chinese = new Locale("zh", "CN");
    private static ResourceBundle chineseMessages = ResourceBundle.getBundle("MessageBundle", chinese);

    private static Locale current = null;

    private static ResourceBundle messages = null;

    public static void setLanguage(String language) {
        if (language.toLowerCase() == "chinese") {
            current = chinese;
            messages = chineseMessages;
        } else {
            current = null;
            messages = null;
        }
    }

    public static String getTranslation(String s, String hint) {
        //logger.info("Received translation request {} with hint {}", s, hint);
        if (current == null)
            return s;
        else {
            String translated = s;
            try {
                translated = messages.getString(s.toLowerCase());
                //logger.info("Translated {} to {}", s, translated);
            } catch (Exception e) {
                try {
                    translated = messages.getString(hint);
                    //logger.info("Translated hint {} to {}", hint, translated);
                } catch (Exception e2) {
                    //logger.info("Failed to translate");
                    translated = s;
                }
            }
            logger.debug("Translated {} to {}", s, translated);
            return translated;
        }
    }
}