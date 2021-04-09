package tech.tongyu.bct.rpc.json.http.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.tongyu.bct.common.api.request.JsonRpcRequest;
import tech.tongyu.bct.common.api.response.ExcelApiInfoResponse;
import tech.tongyu.bct.common.api.response.JsonRpcResponse;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.rpc.json.http.server.service.BctJsonRpcHttpService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
public class BctJsonRpcHttpController {
    private BctJsonRpcHttpService bctJsonRpcHttpService;

    @Autowired
    public BctJsonRpcHttpController(BctJsonRpcHttpService bctJsonRpcHttpService) {
        this.bctJsonRpcHttpService = bctJsonRpcHttpService;
    }

    @PostMapping(value = "/api/upload/rpc", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> uploadRpc(
            @RequestParam("method") String method,
            @RequestParam("params") String params,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> stringObjectMap = JsonUtils.mapFromJsonString(params);
        Map<String, Object> newParams = new HashMap<>(stringObjectMap);
        newParams.put("file", file);
        JsonRpcRequest req = new JsonRpcRequest(method, newParams);
        return new ResponseEntity<>(bctJsonRpcHttpService.rpc(req), HttpStatus.OK);
    }

    @PostMapping(value = "/api/rpc", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postRpc(@RequestBody JsonRpcRequest req) {
        String json;
        json = JsonUtils.objectToJsonString(bctJsonRpcHttpService.rpc(req));
        ResponseEntity responseEntity = new ResponseEntity<>(json, HttpStatus.OK);
        return responseEntity;
    }

    @GetMapping(value = "/api/list", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getList() {
        return new ResponseEntity<>(bctJsonRpcHttpService.list(), HttpStatus.OK);
    }

    @GetMapping(value = "/api/method/list", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> methodInfoList() {
        return new ResponseEntity<>(bctJsonRpcHttpService.listMethod(), HttpStatus.OK);
    }


    @GetMapping(value = "/api/info/{method}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> info(@PathVariable String method) {
        return new ResponseEntity<>(bctJsonRpcHttpService.info(method), HttpStatus.OK);
    }

    // excel specific below
    @GetMapping(value = "/excel/list", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getListExcel() {
        return new ResponseEntity<>(bctJsonRpcHttpService.listExcel(), HttpStatus.OK);
    }

    @GetMapping(value = "/excel/info/{method}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ExcelApiInfoResponse> infoExcel(@PathVariable String method) {
        return new ResponseEntity<>(bctJsonRpcHttpService.infoExcel(method), HttpStatus.OK);
    }

    @PostMapping(value = "/excel/rpc", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> excelRpc(@RequestBody JsonRpcRequest req) {
        return new ResponseEntity<>(bctJsonRpcHttpService.rpc(req), HttpStatus.OK);
    }

    @PostMapping(value = "/excel/parallel", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonRpcResponse> excelParallel(@RequestBody List<JsonRpcRequest> req) {
        return new ResponseEntity<>(bctJsonRpcHttpService.parallel(req), HttpStatus.OK);
    }
}