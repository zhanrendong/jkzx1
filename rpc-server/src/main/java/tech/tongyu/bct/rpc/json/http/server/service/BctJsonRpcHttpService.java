package tech.tongyu.bct.rpc.json.http.server.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.auth.dto.ErrorLogDTO;
import tech.tongyu.bct.auth.dto.UserDTO;
import tech.tongyu.bct.auth.manager.UserManager;
import tech.tongyu.bct.auth.service.UserService;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.api.doc.BctFieldDto;
import tech.tongyu.bct.common.api.doc.BctFieldUtils;
import tech.tongyu.bct.common.api.response.ExcelApiInfoResponse;
import tech.tongyu.bct.common.api.response.RpcResponseWithDiagnostics;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.rpc.json.http.server.manager.ErrorLogManager;
import tech.tongyu.bct.rpc.json.http.server.util.ApiDocUtils;
import tech.tongyu.bct.rpc.json.http.server.util.ExceptionUtils;
import tech.tongyu.bct.rpc.json.http.server.util.SpringBeanUtils;
import tech.tongyu.bct.common.api.request.JsonRpcRequest;
import tech.tongyu.bct.common.api.response.JsonRpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class BctJsonRpcHttpService {
    private static Logger logger = LoggerFactory.getLogger(BctJsonRpcHttpService.class);

    @Autowired
    private ErrorLogManager errorLogManager;

    @Autowired
    private UserManager userManager;

    private Map<String, Method> methodCache;

    {
        methodCache = Maps.newConcurrentMap();
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("tech.tongyu.bct"))
                        .setScanners(new MethodAnnotationsScanner())
        );
        Set<Method> apis = reflections.getMethodsAnnotatedWith(BctMethodInfo.class);
        for(Method m : apis){
            methodCache.put(m.getName(), m);
        }
    }

    // excel api handling is crammed into this function
    // this is a hack for now. should be separated out.
    public JsonRpcResponse rpc(JsonRpcRequest req){
        if (req == null || req.getMethod() == null || req.getParams() == null ) {
            return new JsonRpcResponse(JsonRpcResponse.ErrorCode.MALFORMED_INPUT, "输入为空");
        }

        /*String uuid = null;
        if(logger.isDebugEnabled()) {
            logger.debug("call {} with params: {}", uuid, JsonUtils.toJson(data));
        }*/

        UserDTO userDTO = userManager.getCurrentUser();
        Object result;
        String methodName = req.getMethod();
        try {
            Map<String, Object> inputs = req.getParams();
            Method method = methodCache.get(methodName);
            if(method == null) {
                return new JsonRpcResponse(JsonRpcResponse.ErrorCode.METHOD_NOT_FOUND, "API不存在");
            }

            List<Object> params = new ArrayList<>();
            for(Parameter param: method.getParameters()) {
                if(inputs.containsKey(param.getName())){
                    params.add(inputs.get(param.getName()));
                }
                else {
                    BctMethodArg[] args = param.getDeclaredAnnotationsByType(BctMethodArg.class);
                    if (args[0].required()) {
                        return new JsonRpcResponse(JsonRpcResponse.ErrorCode.MISSING_PARAM,
                                "参数不存在: " + param.getName());
                    } else {
                        //pass null if not exist in inputs
                        params.add(null);
                    }
                }
            }

            result = method.invoke(SpringBeanUtils.getBean(method.getDeclaringClass()), params.toArray());

            if(result instanceof RpcResponseWithDiagnostics) {
                ArrayList diagnostics = (ArrayList) ((RpcResponseWithDiagnostics) result).getDiagnostics();
                if (CollectionUtils.isNotEmpty(diagnostics)) {
                    ErrorLogDTO errorLogDTO = new ErrorLogDTO();
                    errorLogDTO.setRequestMethod(methodName);
                    errorLogDTO.setRequestParams(limitStringSize(params.toString()));
                    errorLogDTO.setUsername(userDTO.getUsername());
                    errorLogDTO.setErrorMessage(limitStringSize(diagnostics.toString()));
                    errorLogManager.save(errorLogDTO);
                }
                return new JsonRpcResponse<>(((RpcResponseWithDiagnostics) result).getResult(),
                        ((RpcResponseWithDiagnostics) result).getDiagnostics());
            } else {
                return new JsonRpcResponse<>(result);
            }

            /*if(logger.isDebugEnabled()){
                logger.debug("result of call {} : {}", uuid, resultInfo);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            String msg;
            if(e instanceof InvocationTargetException){
                msg = ExceptionUtils.getTargetException(e).getMessage();
                if (((InvocationTargetException) e).getTargetException() instanceof CustomException
                        || ((InvocationTargetException) e).getTargetException() instanceof IllegalArgumentException) {
                    throw new CustomException(msg);
                }
            } else if (e.getCause() != null){
                msg = e.getCause().getMessage();
            } else {
                msg = e.getMessage();
            }
            ErrorLogDTO errorLogDTO = new ErrorLogDTO();
            errorLogDTO.setRequestMethod(methodName);
            errorLogDTO.setUsername(userDTO.getUsername());
            errorLogDTO.setRequestParams(limitStringSize(JsonUtils.mapper.valueToTree(req.getParams()).toString()));
            errorLogDTO.setErrorMessage(limitStringSize(e.getCause().toString()));
            StackTraceElement[] stackTrace = e.getCause().getStackTrace();
            String stackTraceMessage = "";
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getMethodName().equals(methodName)) {
                    stackTraceMessage += "文件路径：" + stackTraceElement.getClassName() +
                            "方法：" + stackTraceElement.getMethodName() +
                            "位置：" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber();
                    break;
                }
            }
            errorLogDTO.setErrorStackTrace(limitStringSize(stackTraceMessage));
            errorLogManager.save(errorLogDTO);
            return new JsonRpcResponse(JsonRpcResponse.ErrorCode.RUNTIME_ERROR, msg);
        }
    }

    public List<String> list() {
        List<String> l = new ArrayList<>(this.methodCache.keySet());
        Collections.sort(l);
        return l;
    }

    public List<String> listExcel() {
        return this.methodCache.values().stream()
                .filter(m -> {
                    List<BctApiTagEnum> tags = Arrays.asList(m.getAnnotation(BctMethodInfo.class).tags());
                    return tags.contains(BctApiTagEnum.Excel) || tags.contains(BctApiTagEnum.ExcelDelayed);
                })
                .map(m -> m.getName())
                .collect(Collectors.toList());
    }

    public JsonRpcResponse info(String methodName) {
        Method method = methodCache.get(methodName);
        if(method == null) {
            return new JsonRpcResponse(JsonRpcResponse.ErrorCode.METHOD_NOT_FOUND, "API不存在");
        }
        // get return info
        BctMethodInfo apiInfo = method.getAnnotation(BctMethodInfo.class);
        String retName = apiInfo.retName();
        String retType = method.getReturnType().getSimpleName();
        String retDescription = apiInfo.description();
        // get each argument's info
        List<Map<String, String>> args = new ArrayList<>();
        for(Parameter param: method.getParameters()) {
            BctMethodArg[] params = param.getDeclaredAnnotationsByType(BctMethodArg.class);
            String name = param.getName();
            String type = param.getType().getName();
            String description = params[0].description();
            Map<String, String>  arg = new HashMap<>();
            arg.put("name", name);
            arg.put("type", type);
            arg.put("description", description);
            args.add(arg);
        }
        // resulting json
        Map<String, Object> ret = new HashMap<>();
        ret.put("args", args);
        ret.put("retName", retName);
        ret.put("retType", retType);
        ret.put("description", retDescription);
        ret.put("method", methodName);
        return new JsonRpcResponse<>(ret);
    }

    public JsonRpcResponse listMethod(){
        List<Map<String, Object>> list = new ArrayList<>();
        methodCache.forEach((methodName, method) -> {
            BctMethodInfo apiInfo = method.getAnnotation(BctMethodInfo.class);
            BctApiTagEnum[] tags = apiInfo.tags();
            if (!Arrays.asList(tags).contains(BctApiTagEnum.Excel) &&
                    !Arrays.asList(tags).contains(BctApiTagEnum.ExcelDelayed)){
                String retName = apiInfo.retName();
                String retType = method.getReturnType().getSimpleName();
                String retDescription = apiInfo.description();
                String service = apiInfo.service();
                List<Map<String, Object>> args = new ArrayList<>();
                for(Parameter param: method.getParameters()) {
                    BctMethodArg[] params = param.getDeclaredAnnotationsByType(BctMethodArg.class);
                    String name = param.getName();
                    String type = param.getType().getName();
                    Boolean required = params.length > 0 ? params[0].required() : true;
                    String description = params.length > 0 ? params[0].description() : "";
                    List<BctFieldDto> bctFieldDtoList = Lists.newArrayList();
                    if(params.length > 0 && !Objects.equals(params[0].argClass(), Object.class)) {
                         bctFieldDtoList = BctFieldUtils.getFieldsDecorationFromDto(params[0].argClass());
                    }
                    Map<String, Object>  arg = new HashMap<>();
                    arg.put("name", name);
                    arg.put("type", type);
                    arg.put("required", required);
                    arg.put("description", description);
                    arg.put("class-info", bctFieldDtoList);
                    args.add(arg);
                }
                List<BctFieldDto> bctReturnDtoList = Lists.newArrayList();
                if(!Objects.equals(apiInfo.returnClass(), Object.class)) {
                    bctReturnDtoList = BctFieldUtils.getFieldsDecorationFromDto(apiInfo.returnClass());
                }
                Map<String, Object> ret = new HashMap<>();
                ret.put("args", args);
                ret.put("retName", retName);
                ret.put("retType", retType);
                ret.put("description", retDescription);
                ret.put("method", methodName);
                ret.put("service", service);
                ret.put("return-info", bctReturnDtoList);
                list.add(ret);
            }
        });

        ApiDocUtils.createApiDoc(list);

        return new JsonRpcResponse<>(list);
    }

    public ExcelApiInfoResponse infoExcel(String methodName) {
        Method method = methodCache.get(methodName);
        if(method == null) {
            return new ExcelApiInfoResponse("Excel API " + methodName + " 不存在");
        }
        // get return info
        BctMethodInfo apiInfo = method.getAnnotation(BctMethodInfo.class);
        String retName = apiInfo.retName();
        String retType = apiInfo.excelType() == BctExcelTypeEnum.JavaType ?
                method.getReturnType().getSimpleName() : apiInfo.excelType().name();
        String retDescription = apiInfo.description();
        //   use tags to decide whether the api should be exposed to excel
        BctApiTagEnum[] tags = apiInfo.tags();
        if (!Arrays.asList(tags).contains(BctApiTagEnum.Excel) &&
                !Arrays.asList(tags).contains(BctApiTagEnum.ExcelDelayed))
            return new ExcelApiInfoResponse(methodName + " 不是Excel API");
        // get each argument's info
        List<ExcelApiInfoResponse.ExcelArgInfo> args = new ArrayList<>();
        for(Parameter param: method.getParameters()) {
            BctMethodArg[] params = param.getDeclaredAnnotationsByType(BctMethodArg.class);
            String name = "".equals(params[0].excelName()) ? param.getName() : params[0].excelName();
            String type = BctExcelTypeEnum.JavaType.equals(params[0].excelType()) ?
                    param.getType().getSimpleName() :
                    params[0].excelType().name();
            String description = params[0].description();
            args.add(new ExcelApiInfoResponse.ExcelArgInfo(name, description, type));
        }
        // if tagged with ExcelDelayed, add an extra input to allow user to choose to delay execute
        if (Arrays.asList(tags).contains(BctApiTagEnum.ExcelDelayed)) {
            args.add(new ExcelApiInfoResponse.ExcelArgInfo("delayed", "是否延迟执行？",
                    BctExcelTypeEnum.Boolean.name()));
        }
        return new ExcelApiInfoResponse(methodName, retDescription, retType, retName, args);
    }

    public JsonRpcResponse parallel(List<JsonRpcRequest> requests) {
        List<CompletableFuture<JsonRpcResponse>> tasks = requests.stream().map(
                t -> CompletableFuture.supplyAsync(() ->{
                    try {
                        return rpc(t);
                    } catch (Exception e) {
                        return new JsonRpcResponse<>(JsonRpcResponse.ErrorCode.RUNTIME_ERROR,
                                Objects.isNull(e.getMessage()) ? "内部错误" : e.getMessage());
                    }
                })
        ).collect(Collectors.toList());
        List<JsonRpcResponse> results = tasks.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return new JsonRpcResponse<>(results);
    }

    private String limitStringSize(String string){
        if (string.length() > 4000) {
            string = string.substring(0,3999);
        }
        return string;
    }
}
