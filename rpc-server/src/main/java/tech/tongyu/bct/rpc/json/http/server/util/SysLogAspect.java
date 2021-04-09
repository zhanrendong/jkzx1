package tech.tongyu.bct.rpc.json.http.server.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.auth.common.user.LoginUser;
import tech.tongyu.bct.auth.config.ExternalConfig;
import tech.tongyu.bct.auth.dto.ErrorLogDTO;
import tech.tongyu.bct.auth.dto.SysLogDTO;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.SM4Utils;
import tech.tongyu.bct.rpc.json.http.server.manager.ErrorLogManager;
import tech.tongyu.bct.rpc.json.http.server.manager.SysLogManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统日志，切面处理类
 *
 * @author yyh
 */
@Aspect
@Component
public class SysLogAspect {

    private static final Logger logger=LoggerFactory.getLogger(SysLogAspect.class);

    @Autowired
    private SysLogManager sysLogManager;
    @Autowired
    private ErrorLogManager errorLogManager;
    @Autowired(required = false)
    private ExternalConfig externalConfig;

    @Pointcut("@annotation(tech.tongyu.bct.common.api.annotation.BctMethodInfo)")
    public void logPointCut() {
    // springAop切点标签方法 参考(https://www.cnblogs.com/liaojie970/p/7883687.html)
    }

    @Pointcut("execution(* tech.tongyu.bct.auth.controller.AuthController.authLogin(..))")
    public void loginPointCut() {
    }

    @Pointcut("execution(* tech.tongyu.bct.auth.controller.AuthController.authEncryptedLogin(..))")
    public void encryptedLoginPointCut() {
    }

    @Pointcut("execution(* tech.tongyu.bct.auth.controller.AuthController.authEncryptedChangePassword(..))")
    public void encryptedChangePasswordPointCut() {
    }

    @Pointcut("execution(* tech.tongyu.bct.auth.controller.AuthController.authChangePassword(..))")
    public void changePasswordPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行方法
        Object result = point.proceed();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志(异常不影响接口返回)
        try {
            saveSysLog(point, time);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作日志保存未知异常:",e);
        }
        return result;
    }

    @Order(1)
    @AfterReturning(pointcut = "loginPointCut()", returning = "result")
    public void after(JoinPoint point, Object result) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志(异常不影响接口返回)
        try {
            ResponseEntity<String> res = (ResponseEntity<String>) result;
            if (res.getBody().contains("\"loginStatus\":true")){
                saveLoginInfo(point, time, false, true, res.getBody());
            }else {
                saveLoginInfo(point, time, false, false, res.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作日志保存未知异常:",e);
        }
    }

    @Order(1)
    @AfterReturning(pointcut = "encryptedLoginPointCut()", returning = "result")
    public void afterEncryptedLogin(JoinPoint point, Object result) throws Throwable {
        long beginTime = System.currentTimeMillis();
        //执行时长(毫秒)
        long time = System.currentTimeMillis() - beginTime;
        //保存日志(异常不影响接口返回)
        try {
            ResponseEntity<String> res = (ResponseEntity<String>) result;
            if (res.getBody().contains("\"loginStatus\":true")){
                saveLoginInfo(point, time, true, true, res.getBody());
            }else {
                saveLoginInfo(point, time, true, false, res.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作日志保存未知异常:",e);
        }
    }

    @Order(1)
    @Before("encryptedChangePasswordPointCut()")
    public void beforeEncryptedChangePassword(JoinPoint point) throws Throwable {
        try {
            saveChangePasswordLog(point, true);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作日志保存未知异常:",e);
        }
    }

    @Order(1)
    @Before("changePasswordPointCut()")
    public void beforeChangePassword(JoinPoint point) throws Throwable {
        //保存日志(异常不影响接口返回)
        try {
            saveChangePasswordLog(point, false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("操作日志保存未知异常:",e);
        }
    }

    private void saveSysLog(ProceedingJoinPoint joinPoint, long time) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        SysLogDTO sysLog = new SysLogDTO();
        BctMethodInfo bctMethodInfo = method.getAnnotation(BctMethodInfo.class);
        if (bctMethodInfo != null) {
            if (!bctMethodInfo.enableLogging()) {
                return;
            }
            //注解上的service描述
            sysLog.setService(bctMethodInfo.service());
            //注解上的描述
            sysLog.setOperation(bctMethodInfo.description());
        }
        //请求的方法名
        StringBuilder stringBuilder = new StringBuilder();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        methodName=stringBuilder.append(className).append(".").append(methodName).toString();
        sysLog.setMethod(methodName);
        Parameter[] parameters = method.getParameters();
        //请求的参数
        Object[] args = joinPoint.getArgs();
        //过滤掉 ServletRequest ServletResponse和MultipartFile 类型的参数
        List<Object> argValue = Arrays.stream(args).filter(t ->!( t instanceof ServletRequest) && !( t instanceof ServletResponse)&&!(t instanceof MultipartFile) ).collect(Collectors.toList());
        Map<String,Object> paramsMap=new HashMap<>();
        for (int i = 0; i < argValue.size(); i++) {
            if(i>parameters.length-1){
                break;
            }
            paramsMap.put(parameters[i].getName(),argValue.get(i));
        }
        sysLog.setParams(paramsMap);
        //用户名
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            sysLog.setUsername(authentication.getName());
        }
        sysLog.setExecutionTimeInMillis(time);

        if ("script".equals(sysLog.getUsername())){
            // script脚本用户操作不保存日志
            return;
        }
        logger.info("执行方法:{}, 请求参数:{}, 耗时:{}ms",methodName,paramsMap,time);
        //保存系统日志
        sysLogManager.save(sysLog);
    }

    private void saveLoginInfo(JoinPoint joinPoint, long time, Boolean isEncryptedLogin, Boolean isSucceed, String responseBody) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        StringBuilder stringBuilder = new StringBuilder();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = signature.getName();
        methodName=stringBuilder.append(className).append(".").append(methodName).toString();
        String loginUser = null;
        Parameter[] parameters = method.getParameters();
        //请求的参数
        Object[] args = joinPoint.getArgs();
        //过滤掉 ServletRequest ServletResponse和MultipartFile 类型的参数
        List<Object> argValue = Arrays.stream(args).filter(t ->!( t instanceof ServletRequest) && !( t instanceof ServletResponse)&&!(t instanceof MultipartFile) ).collect(Collectors.toList());
        SM4Utils sm4Utils = new SM4Utils();
        sm4Utils.setSecretKey(externalConfig.getSecretKey());
        sm4Utils.setIv(externalConfig.getIv());
        Map<String,Object> paramsMap = new HashMap<>();
        for (int i = 0; i < argValue.size(); i++) {
            if(i>parameters.length-1){
                break;
            }
            Map<String,Object> param = (Map<String,Object>) argValue.get(i);
            Object username = Optional.ofNullable(param.get(ApiParamConstants.USERNAME)).orElse(param.get(ApiParamConstants.USER_NAME));
            if (!Objects.isNull(username)) {
                try {
                    loginUser = sm4Utils.decryptData_ECB(username.toString());
                }catch (Exception e){
                    loginUser = username.toString();
                }
            }
            String password = param.get(ApiParamConstants.PASSWORD).toString();
            paramsMap.put(ApiParamConstants.PASSWORD, hashPassword(password));
            paramsMap.put(ApiParamConstants.USERNAME, username);

            if (isEncryptedLogin){
                LoginUser user = LoginUser.ofEncrypted(param, sm4Utils);
                if (!Objects.isNull(user))
                    loginUser = user.getUsername();
            }
        }

        if ("script".equals(loginUser)){
            // script脚本用户操作不保存日志
            return;
        }
        logger.info("执行方法:{}, 请求参数:{}, 耗时:{}ms", methodName, paramsMap, time);
        if (isSucceed){
            SysLogDTO sysLog = new SysLogDTO(loginUser, "用户登录", "auth-service", methodName, paramsMap, time);
            sysLogManager.save(sysLog);
        }else {
            ErrorLogDTO errorLogDTO = new ErrorLogDTO(loginUser, responseBody, methodName, paramsMap.toString(), null);
            errorLogManager.save(errorLogDTO);
        }
    }

    private void saveChangePasswordLog(JoinPoint joinPoint, Boolean isEncrypted) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            StringBuilder stringBuilder = new StringBuilder();
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            methodName=stringBuilder.append(className).append(".").append(methodName).toString();
            String loginUser = null;
            Parameter[] parameters = method.getParameters();
            //请求的参数
            Object[] args = joinPoint.getArgs();
            //过滤掉 ServletRequest ServletResponse和MultipartFile 类型的参数
            List<Object> argValue = Arrays.stream(args).filter(t ->!( t instanceof ServletRequest) && !( t instanceof ServletResponse)&&!(t instanceof MultipartFile) ).collect(Collectors.toList());
            SM4Utils sm4Utils = new SM4Utils();
            sm4Utils.setSecretKey(externalConfig.getSecretKey());
            sm4Utils.setIv(externalConfig.getIv());
            Map<String,Object> paramsMap = new HashMap<>();
            for (int i = 0; i < argValue.size(); i++) {
                if(i>parameters.length-1){
                    break;
                }
                Map<String,Object> param = (Map<String,Object>) argValue.get(i);
                Object username = Optional.ofNullable(param.get(ApiParamConstants.USERNAME)).orElse(param.get(ApiParamConstants.USER_NAME));
                if (!Objects.isNull(username)) {
                    try {
                        loginUser = sm4Utils.decryptData_ECB(username.toString());
                    }catch (Exception e){
                        loginUser = username.toString();
                    }
                }
                String newPassword = param.get(ApiParamConstants.NEW_PASSWORD).toString();
                String oldPassword = param.get(ApiParamConstants.OLD_PASSWORD).toString();
                paramsMap.put(ApiParamConstants.NEW_PASSWORD, newPassword);
                paramsMap.put(ApiParamConstants.OLD_PASSWORD, oldPassword);
                paramsMap.put(ApiParamConstants.USERNAME, username);

                if (isEncrypted){
                    if (!Objects.isNull(username)){
                        loginUser = sm4Utils.decryptData_CBC(username.toString());
                    }
                }
            }

            logger.info("执行方法:{}, 请求参数:{}", methodName, paramsMap);
            SysLogDTO sysLog = new SysLogDTO(loginUser, "用户修改密码", "auth-service", methodName, paramsMap, 0L);
            sysLogManager.save(sysLog);
    }

    public static String hashPassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(5));
    }
}
