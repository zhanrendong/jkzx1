package tech.tongyu.bct.rpc.json.http.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

@Component
public class HttpClient implements EnvironmentAware {
    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private static final String restRequestLog = "%s call service: %s with parameters: \n %s \n";
    private static final String restResponseLog = "%s service: %s returned result: \n %s \n";

    private static final String RESULT = "result";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";

    private static final String CALL_REMOTE_SERVER_ERROR = "远程调用失败: %s failed: %s";
    private static final String AUTHORIZATION_DESP = "Authorization";
    private static final String NO_SERVICEINSTANCE = "服务不存在: %s";
    private static final String BEARER = "Bearer ";

    private RestTemplate restTemplate;

    private String authService;
    private String quantService;
    private String tradeService;
    private String marketDataService;
    private String modelService;
    private String pricingService;
    private String reportService;
    private String staticDataService;
    private String collateralService;
    private String referenceDataService;
    private String bctServer;
    private String riskControlService;

    @Autowired
    public HttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getAuthService() {
        return authService;
    }

    public String getQuantService() {
        return quantService;
    }

    public String getTradeService() {
        return tradeService;
    }

    public String getMarketDataService() {
        return marketDataService;
    }

    public String getModelService() {
        return modelService;
    }

    public String getPricingService() {
        return pricingService;
    }

    public String getReportService() {
        return reportService;
    }

    public String getStaticDataService() {
        return staticDataService;
    }

    public String getCollateralService() {
        return collateralService;
    }

    public String getReferenceDataService() {
        return referenceDataService;
    }

    public String getBctServer(){
        return bctServer;
    }

    public String getRiskControlService() {
        return riskControlService;
    }

    @Override
    public void setEnvironment(Environment env) {
        authService = env.getProperty("AUTH_SERVICE", "http://localhost:16010");
        quantService = env.getProperty("QUANT_SERVICE", "http://localhost:16011");
        tradeService = env.getProperty("TRADE_SERVICE", "http://localhost:16000");
        marketDataService = env.getProperty("MARKET_DATA_SERVICE", "http://localhost:16015");
        modelService = env.getProperty("MODEL_SERVICE", "http://localhost:16017");
        pricingService = env.getProperty("PRICING_SERVICE", "http://localhost:16018");
        reportService = env.getProperty("REPORT_SERVICE", "http://localhost:16019");
        staticDataService = env.getProperty("STATIC_DATA_SERVICE", "http://localhost:16021");
        collateralService = env.getProperty("COLLATERAL_SERVICE", "http://localhost:16022");
        referenceDataService = env.getProperty("REFERENCE_DATA_SERVICE", "http://localhost:16000");
        bctServer = env.getProperty("BCT_SERVER", "http://localhost:16000");
        riskControlService = env.getProperty("RISK_CONTROL_SERVICE", "http://localhost:16000");
    }

    @SuppressWarnings({"unchecked"})
    public Object postTemplate(String serviceName, String router, String data, String token){
        String url = getUrl(serviceName) + router;
        HttpHeaders headers = generateHeader(token);
        HttpEntity<String> entity = new HttpEntity<String>(data, headers);
        ResponseEntity<Map> response;
        try{
            logger.debug(String.format(restRequestLog, Instant.now(),
                    serviceName, data));
            response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> res = response.getBody();
            logger.debug(String.format(restResponseLog, Instant.now(),
                    serviceName, JsonUtils.objectToJsonString(res)));
            return checkRes(res, serviceName);
        } catch (HttpStatusCodeException e){
            throw new CustomException(ErrorCode.REMOTE_ERROR, String.format(CALL_REMOTE_SERVER_ERROR, serviceName,
                    e.getResponseBodyAsString()));
        }
    }

    public Object postTemplateNoneServiceInfo(String serviceName, String router, String data, String token){
        String url = getUrl(serviceName) + router;
        HttpHeaders headers = generateHeader(token);
        HttpEntity<String> entity = new HttpEntity<String>(data, headers);
        ResponseEntity<Map> response;
        try{
            logger.debug(String.format(restRequestLog, Instant.now(),
                    serviceName, data));
            response = restTemplate.postForEntity(url, entity, Map.class);
            Map<String, Object> res = response.getBody();
            logger.debug(String.format(restResponseLog, Instant.now(),
                    serviceName, JsonUtils.objectToJsonString(res)));
            return checkResNoneServiceInfo(res, serviceName);
        } catch (HttpStatusCodeException e){
            throw new CustomException(ErrorCode.REMOTE_ERROR, e.getResponseBodyAsString());
        }
    }

    @SuppressWarnings({"unchecked"})
    public Object putTemplate(String service_name, String router,
                                           String parameter, String data, String token) {
        try {
            String url = getUrl(service_name) + router;
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).path(parameter);
            UriComponents components = builder.build(true);
            URI uri = components.toUri();
            logger.debug(String.format(restRequestLog, Instant.now(),
                    service_name, data));
            ResponseEntity<Map> responseEntity = restTemplate.exchange(uri.toString(), HttpMethod.PUT,
                    new HttpEntity<>(data, generateHeader(token)), Map.class);
            Map<String, Object> res = responseEntity.getBody();
            logger.debug(String.format(restResponseLog, Instant.now(),
                    service_name, JsonUtils.objectToJsonString(res)));
            return checkRes(res, service_name);
        } catch (HttpStatusCodeException e) {
            throw new CustomException(ErrorCode.REMOTE_ERROR, String.format(CALL_REMOTE_SERVER_ERROR, service_name,
                    e.getResponseBodyAsString()));
        }
    }

    private String getUrl(String serviceName) {

        String url;
        switch (serviceName) {
            case "auth-service":
                return authService;
            case "quant-service":
                return quantService;
            case "trade-service":
                return tradeService;
            case "market-data-service":
                return marketDataService;
            case "model-service":
                return modelService;
            case "pricing-service":
                return pricingService;
            case "report-service":
                return reportService;
            case "static-data-service":
                return staticDataService;
            case "collateral-service":
                return collateralService;
            case "reference-data-service":
                return referenceDataService;
            case "bct-server":
                return bctServer;
            case "risk-control-service":
                return riskControlService;
            default:
                throw new CustomException(ErrorCode.REMOTE_ERROR, String.format(NO_SERVICEINSTANCE, serviceName));
        }
    }

    private HttpHeaders generateHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add(AUTHORIZATION_DESP, BEARER + token);
        return headers;
    }

    @SuppressWarnings({"unchecked"})
    private Object checkRes(Map<String, Object> res, String serviceName) {
        if (!res.containsKey(RESULT)) {
            if (res.containsKey(ERROR)
                    && (res.get(ERROR) != null)
                    && ((Map<String, Object>)res.get(ERROR)).containsKey(MESSAGE)){
                throw new CustomException(ErrorCode.REMOTE_ERROR, String.format(CALL_REMOTE_SERVER_ERROR, serviceName,
                        ((Map<String, Object>)res.get(ERROR)).get(MESSAGE)));
            } else {
                throw new CustomException(ErrorCode.UNKNOWN, String.format(CALL_REMOTE_SERVER_ERROR, serviceName, null));
            }
        }
        return res.get(RESULT);
    }

    private Object checkResNoneServiceInfo(Map<String, Object> res, String serviceName) {
        if (!res.containsKey(RESULT)) {
            if (res.containsKey(ERROR)
                    && (res.get(ERROR) != null)
                    && ((Map<String, Object>)res.get(ERROR)).containsKey(MESSAGE)){
                throw new CustomException(ErrorCode.REMOTE_ERROR, ((Map<String, Object>)res.get(ERROR)).get(MESSAGE).toString());
            } else {
                throw new CustomException(ErrorCode.UNKNOWN, String.format(CALL_REMOTE_SERVER_ERROR, serviceName, null));
            }
        }
        return res.get(RESULT);
    }
}
