package tech.tongyu.bct.service.quantlib.server.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.tongyu.bct.service.quantlib.common.utils.JsonMapper;
import tech.tongyu.bct.service.quantlib.server.services.Batch;
import tech.tongyu.bct.service.quantlib.server.services.Converter;
import tech.tongyu.bct.service.quantlib.server.services.FunctionCache;
import tech.tongyu.bct.service.quantlib.server.services.ObjectCache;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class QuantController  {

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    private static final Logger logger = LoggerFactory.getLogger(QuantController.class);

    private static Map<String, Object> defaultErrHandler(Exception e){
        logger.error("RPC call failed", e);
        Map<String, Object> ret = new HashMap<>();
        String msg;
        if (e.getCause() != null){
            msg = e.getCause().getMessage();
        }else {
            msg = e.getMessage();
        }
        Map<String, Object> err = new HashMap<>();
        err.put("message", msg);
        err.put("code", -32063);
        ret.put("error", err);
        return ret;
    }

    @RequestMapping(value = "/api/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String listApis() {
        logger.info("List all API names");
        List<String> funcs = FunctionCache.Instance.listAllAPI();
        return JsonMapper.toJson(funcs);
    }

    @RequestMapping(value = "/api/info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String infoApis(){
        logger.info("GET all API info");
        List<Map<String, Object>> allInfo = new ArrayList<>();
        List<String> methods = FunctionCache.Instance.listAllAPI();
        for(String m : methods){
            Map<String, Object> info = FunctionCache.Instance.getAPIInfo(m);
            allInfo.add(info);
        }
        return JsonMapper.toJson(allInfo);
    }

    @RequestMapping(value = "/api/info/excel/{method}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> infoApi(@PathVariable String method){
        Map<String, Object> ret = new HashMap<>();
        try{
            method = new String(method.getBytes("ISO-8859-1"), "UTF-8");
            ret = FunctionCache.Instance.getAPIInfo(method);
        }catch (Exception e){
            ret = defaultErrHandler(e);
        }finally {
            return ret;
        }
    }

    @RequestMapping(value = "/api/rpc", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> rpc(@RequestBody Map<String, Object> request){
        String functionName = (String)request.get("method");
        logger.info("RPC call: " + functionName);
        String id = (String)request.get("id");
        Map<String, Object> ret = new HashMap<>();
        ret.put("jsonrpc", "2.0");
        ret.put("id", id);
        try{
            String retType = FunctionCache.Instance.getReturnType(functionName);
            Map<String, Object> params = (Map<String, Object>)request.get("params");
            String objectId = (String) params.get("id");
            Object o = FunctionCache.Instance.invoke(functionName, params);
            ret.put("result", Converter.toOutput(retType, o, objectId));
        }catch (Exception e){
            ret = defaultErrHandler(e);
        }finally {
            return ret;
        }
    }

    @RequestMapping(value = "/api/rpc/parallel", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> parallel(@RequestBody List<Map<String, Object>> request){
        logger.info("Json RPC parallel call");
        List<Object> results = new Batch().parallel(request);
        Map<String, Object> ret = new HashMap<>();
        ret.put("jsonrpc", "2.0");
        ret.put("id", "0");
        ret.put("result", results);
        return ret;
    }

    @RequestMapping(value = "/api/rpc/batch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> batch(@RequestBody List<Map<String, Object>> request){
        logger.info("Json RPC batch");
        Map<String, Object> ret = new HashMap<>();
        ret.put("jsonrpc", "2.0");
        ret.put("id", "0");
        try{
            Object o = new Batch().execute(request);
            ret.put("result", o);
        }catch (Exception e){
            ret = defaultErrHandler(e);
        }finally {
            return ret;
        }
    }

    @RequestMapping(value = "/api/object/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getObject(@PathVariable String id){
        logger.info("Installing GET object handler");
        Map<String, Object> ret = new HashMap<>();
        ret.put("jsonrpc", "2.0");
        ret.put("id", "0");
        try{
            id = new String(id.getBytes("ISO-8859-1"), "UTF-8");
            Object o = ObjectCache.Instance.get(id);
            if (o != null){
                String json = JsonMapper.toJson(o);
                ret.put("result", json);
                return new ResponseEntity<>(JsonMapper.toJson(ret), HttpStatus.OK);
            } else {
                ret.put("result", null);
                return new ResponseEntity<>(JsonMapper.toJson(ret), HttpStatus.NOT_FOUND);
            }
        }catch (Exception e){
            return new ResponseEntity<>(JsonMapper.toJson(defaultErrHandler(e)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/api/object/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> putObject(@PathVariable String id, @RequestBody String body){
        logger.info("Installing PUT object handler");
        Map<String, Object> ret = new HashMap<>();
        ret.put("jsonrpc", "2.0");
        ret.put("id", "0");
        try{
            //id = new String(id.getBytes("ISO-8859-1"), "UTF-8");
            id = URLDecoder.decode(id, "UTF-8");
            logger.debug("/api/object/{id} with id {} and body {}", id, body);
            if (ObjectCache.Instance.get(id) != null){
                logger.debug("Object with id {} already exists ", id);
                ret.put("result", id);
                return new ResponseEntity<>(JsonMapper.toJson(ret), HttpStatus.OK);
            }
            ObjectCache.Instance.create(id, body);
            logger.info("Generated object with given id {}", id);
            ret.put("result", id);
            return new ResponseEntity<>(JsonMapper.toJson(ret), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(JsonMapper.toJson(defaultErrHandler(e)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/api/object", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> postObjects(@RequestBody List<Map<String, Object>> request) {
        List<Object> results = new ArrayList<>();
        for (int i = 0; i < request.size(); ++i) {
            Map<String, Object> model = request.get(i);
            String id = (String)model.get("model_id");
            if (ObjectCache.Instance.get(id) != null){
                logger.debug("Object with id {} already exists ", id);
                results.add(id);
            } else {
                try {
                    ObjectCache.Instance.create(id, (Map<String, Object>)model.get("model_data"));
                    logger.info("Generated object with given id {}", id);
                    results.add(id);
                } catch (Exception e) {
                    results.add(null);
                }
            }
        }
        Map<String, Object> ret = new HashMap<>();
        ret.put("jsonrpc", "2.0");
        ret.put("id", "0");
        ret.put("result", results);
        return ret;
    }

    @RequestMapping(value = "/api/object/{id:.+}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteObject(@PathVariable String id){
        logger.info("Installing DELETE object handler");
        Map<String, Object> ret = new HashMap<>();
        ret.put("jsonrpc", "2.0");
        ret.put("id", "0");
        try{
            id = new String(id.getBytes("ISO-8859-1"), "UTF-8");
            logger.debug("Deleting /api/object/:id with id {}", id);
            ObjectCache.Instance.delete(id);
            ret.put("result", id);
            return new ResponseEntity<>(JsonMapper.toJson(ret), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(JsonMapper.toJson(defaultErrHandler(e)), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}