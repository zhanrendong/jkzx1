package tech.tongyu.bct.service.quantlib.server.services;

import com.google.common.collect.Maps;
import tech.tongyu.bct.service.quantlib.common.utils.CustomException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/***
 * Batch processing engine
 */
public class Batch {
    private Map<String, Object> heap;
    //private Map<String, String> functions;

    public Batch() {
        this.heap = Collections.synchronizedMap(new HashMap<>());
        //this.functions = new HashMap<>();
    }

    /***
     * Execute one statement of the form
     * {
     *     op: operator,
     *     args: {
     *         ...
     *     }
     *     assign_to: var
     * }
     * If an assign_to variable is given, the result of the statement is assigned to var on the heap
     * and the function returns null.
     * Otherwise the function returns the statement result.
     * Below are the accepted operators and their corresponding args:
     *
     * op: call
     * args: {
     *     method: funcName,
     *     params: funcParams
     * }
     *
     * op: parallel
     * args: [op calls] (a list of call operators defined above
     *
     * If op is missing the statement is defaulted to 'op: call'
     *
     * @param statement
     */
    private Object executeOne(Map<String, Object> statement) throws Exception{
        String assignTo = (String)statement.get("assign_to");
        if (!statement.containsKey("op")) {
            // {method, params}
            return opCall(statement, assignTo);
        }
        if (statement.get("op").equals("call")) {
            // {op: call, args: {method, params}}
            return opCall((Map<String, Object>)statement.get("args"), assignTo);
        }
        if (statement.get("op").equals("parallel")) {
            // {op: parallel, args: [...]}
            return opParallel((List<Map<String, Object>>)statement.get("args"), assignTo);
        }
        if (statement.get("op").equals("return")) {
            // {op: return, args: var}, {op: return, args: [var]
            Object args = statement.get("args");
            if (args instanceof List) {
                return ((List<Object>)args).stream().map(t->this.heap.get(t)).collect(Collectors.toList());
            } else {
                return this.heap.get(args);
            }
        }
        throw new Exception("Unrecognized op: " + statement.get("op"));
    }
    /***
     * A single function call. The returned result is assigned to assignTo on the heap
     * {
     *     op: call,
     *     args: {
     *         method:
     *         params
     *     }
     *     assign_to:
     * }
     * @param commands {method, params}
     * @param assignTo The variable on the heap to assign the returned value to.
     * @throws Exception
     */
    private Object opCall(Map<String, Object> commands, String assignTo) throws Exception {
        String method = (String)commands.get("method");
        Map<String, Object> params = (Map<String, Object>)commands.get("params");
        // check if the input is already on the heap
        params.forEach((k, v)->{
            if (this.heap.containsKey(k)) {
                params.replace(k, this.heap.get(k));
            }
        });
        Object o = FunctionCache.Instance.invoke(method, params);
        Object rhs = null;
        // if the return type is Handle, we will generate an id and store the handle in the cache
        // only the object id will be returned
        if (FunctionCache.Instance.getReturnType(method).equals("Handle")) {
            String objectId = Utils.genID(o);
            ObjectCache.Instance.put(objectId, o);
            rhs = objectId;
        } else {
            rhs = o;
        }
        if (assignTo == null || assignTo.length() == 0)
            return rhs;
        else {
            this.heap.put(assignTo, rhs);
            return null;
        }
    }

    /***
     * Parallel execution
     * @param jobs A list of statements to be executed in parallel
     * @param assignTo
     * @return The variable on the heap to assign the returned value to.
     */
    private List<Object> opParallel(List<Map<String, Object>> jobs, String assignTo) {
        List<CompletableFuture<Object>> futures = jobs.stream()
                .map(t->CompletableFuture.supplyAsync(() -> {
                    Object o;
                    try {
                        o = executeOne(t);
                    } catch (Exception e) {
                        o = null;
                    }
                    return o;
                }))
                .collect(Collectors.toList());
        List<Object> results =
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList());
        if (assignTo==null || assignTo.length() == 0)
            return results;
        else {
            this.heap.put(assignTo, results);
            return null;
        }
    }

    /***
     * Get the value of a heap variable
     * @param var Name of the variable
     * @return If the last statement is op: return, a specific variable is returned. Otherwise the last
     * statement result is returned
     */
    private Object opReturn(String var) {
        return this.heap.get(var);
    }

    public List<Object> parallel(List<Map<String, Object>> jobs) {
        return opParallel(jobs, null);
    }

    public Object execute(List<Map<String, Object>> jobs) throws Exception {
        Object o = null;
        for (int i =0; i < jobs.size(); ++i) {
            try {
                o = executeOne(jobs.get(i));
            } catch (Exception e) {
                throw new Exception("Failed to execute " + i + "th statement", e);
            }
        }
        return o;
    }

    /**
     * new parallel. return exception information
     * @param jobs
     * @return
     */
    public Map<String, List<Object>> submitJobs(List<Map<String, Object>> jobs) {
        List<Object> diagnostics = Collections.synchronizedList(new ArrayList<>());
        List<Object> futures = jobs.stream().map(t -> CompletableFuture.supplyAsync( () -> {
            Object o = null;
            try {
                o = executeOne(t);
            } catch (Exception e) {
                throw new CustomException(e.getMessage());
            }
            return o;
        }).handle( (r, e) -> {
            if (e != null) {
                Map<String, Object> p = Maps.newConcurrentMap();
                p.put("error", e.getMessage());
                p.put("params", t);
                diagnostics.add(p);
            }
            return r;
        }).join()).collect(Collectors.toList());
        Map<String, List<Object>> results = Maps.newConcurrentMap();
        results.put("results", futures);
        results.put("diagnostics", diagnostics);
        return results;
    }
}