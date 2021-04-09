package tech.tongyu.bct.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {

    private static Logger logger = LoggerFactory.getLogger(LogUtils.class);
    private static Boolean debug = true;

    public static void info(String message){
        if(debug) logger.info(message);
    }

    public static void springLoaded(Class<?> clazz){
        info("\t> 目标bean[" + clazz.getName() + "]已经装载到spring container中");
    }
}
