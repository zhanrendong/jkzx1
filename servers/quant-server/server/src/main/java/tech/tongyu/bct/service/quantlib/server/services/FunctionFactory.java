package tech.tongyu.bct.service.quantlib.server.services;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi;
import tech.tongyu.bct.service.quantlib.common.annotations.BctQuantApi2;

import java.lang.reflect.Method;
import java.util.Set;

public class FunctionFactory {
    private static final Logger logger = LoggerFactory.getLogger(FunctionFactory.class);
    private static void registerOne(
            String api, String help, Class<?> clazz, String actualName,
            String[] paramNames, String[] paramTypes, String[] paramHelps,
            String retName, String retType, String retHelp
    ) throws Exception {
        Method m = null;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(actualName)) {
                m = method;
                break;
            }
        }
        if (m == null)
            throw new Exception("FunctionFactory::registerOneFunction(): Failed to find method name "
                    + actualName + " in class " + clazz.getName());
        FunctionCache.FuncDescription description = new FunctionCache.FuncDescription();
        description.method = api;
        description.func = m;
        description.description = help;
        description.retName = retName;
        description.retType = retType;
        description.retDescription = retHelp;
        if(paramNames==null || paramNames.length==0) {
            description.args = new FunctionCache.FuncParam[]{};
        } else {
            int n = paramNames.length;
            FunctionCache.FuncParam[] params = new FunctionCache.FuncParam[n];
            for(int i=0; i<n; ++i) {
                FunctionCache.FuncParam p = new FunctionCache.FuncParam();
                p.name = paramNames[i];
                p.type = paramTypes[i];
                p.description = paramHelps[i];
                params[i] = p;
            }
            description.args = params;
        }
        FunctionCache.Instance.register(description);
    }
    private static void autoWire() throws Exception {
        logger.info("Autowiring API functions with BctQuantApi annotation");
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("tech.tongyu.bct.service.quantlib"))
                        .setScanners(new MethodAnnotationsScanner())
        );
        Set<Method> apis = reflections.getMethodsAnnotatedWith(BctQuantApi.class);
        for (Method m : apis) {
            BctQuantApi info = m.getAnnotation(BctQuantApi.class);
            logger.info("  Autowiring API: " + info.name());
            FunctionCache.FuncDescription description = new FunctionCache.FuncDescription();
            description.method = info.name();
            description.func = m;
            description.description = Translator.Instance.getTranslation(info.description(),
                    info.name() + ":description");
            description.retName = info.retName();
            description.retType = info.retType();
            description.retDescription = info.retDescription();
            description.addIdInput = info.addIdInput();
            if (info.argNames().length == 0) {
                description.args = new FunctionCache.FuncParam[]{};
            } else {
                int n = info.argNames().length;
                FunctionCache.FuncParam[] params = new FunctionCache.FuncParam[n];
                for(int i=0; i<n; ++i) {
                    FunctionCache.FuncParam p = new FunctionCache.FuncParam();
                    p.name = info.argNames()[i];
                    p.type = info.argTypes()[i];

                    String hint = info.name() + ":" + "arg" + i + ":description";
                    p.description = Translator.Instance.getTranslation(info.argDescriptions()[i], hint);

                    params[i] = p;
                }
                description.args = params;
            }
            FunctionCache.Instance.register(description);
        }
    }
    private static void autoWire2() throws Exception {
        logger.info("Autowiring API functions with BctQuantApi2 annotation");
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("tech.tongyu.bct.service.quantlib"))
                        .setScanners(new MethodAnnotationsScanner())
        );
        Set<Method> apis = reflections.getMethodsAnnotatedWith(BctQuantApi2.class);
        for (Method m : apis) {
            BctQuantApi2 info = m.getAnnotation(BctQuantApi2.class);
            logger.info("  Autowiring API: " + info.name());
            FunctionCache.FuncDescription description = new FunctionCache.FuncDescription();
            description.method = info.name();
            description.func = m;
            description.description = Translator.Instance.getTranslation(info.description(),
                    info.name() + ":description");
            description.retName = info.retName();
            description.retType = info.retType();
            description.retDescription = info.retDescription();
            description.addIdInput = info.addIdInput();
            if (info.args().length == 0) {
                description.args = new FunctionCache.FuncParam[]{};
            } else {
                int n = info.args().length;
                FunctionCache.FuncParam[] params = new FunctionCache.FuncParam[n];
                for(int i=0; i<n; ++i) {
                    FunctionCache.FuncParam p = new FunctionCache.FuncParam();
                    p.name = info.args()[i].name();
                    p.type = info.args()[i].type();

                    String hint = info.name() + ":" + "arg" + i + ":description";
                    p.description = Translator.Instance.getTranslation(info.args()[i].description(), hint);

                    p.required = info.args()[i].required();

                    params[i] = p;
                }
                description.args = params;
            }
            FunctionCache.Instance.register(description);
        }
    }
    /*
    public static double[][] testMatrix(double[][] x) {
        double[][] m = new double[x.length][];
        for (int i = 0; i<x.length; ++i) {
            m[i] = new double[x[i].length];
            for (int j = 0; j < x[i].length; ++j)
                m[i][j] = x[i][j] + 1.0;
        }
        return m;
    }
    */
    public static void registerAll() throws Exception {
        autoWire();
        autoWire2();
        logger.info("Registering APIs manually");
        logger.info("Finished adding RPC APIs");
    }
}