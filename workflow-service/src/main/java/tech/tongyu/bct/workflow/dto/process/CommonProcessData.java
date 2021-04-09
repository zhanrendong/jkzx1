package tech.tongyu.bct.workflow.dto.process;

import com.google.common.collect.Maps;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.workflow.process.ProcessConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommonProcessData extends HashMap implements ProcessData{

    public static final String BUSINESS = "_business_payload";
    public static final String CTL = "_ctl_payload";

    private CommonProcessData(){
        super();
    }

    public CommonProcessData(Map<String, Object> map) {
        super(map);
        if(!map.containsKey(CTL))
            put(CTL, Maps.newHashMap());
        if(!this.containsKey(BUSINESS))
            put(BUSINESS, Maps.newHashMap());
    }

    public static CommonProcessData of(Map<String, Object> ctlProcessData, Map<String, Object> businessProcessData){
        CommonProcessData commonProcessData = new CommonProcessData();
        commonProcessData.put(CTL, ctlProcessData);
        commonProcessData.put(BUSINESS, businessProcessData);
        return commonProcessData;
    }

    public static CommonProcessData ofBusinessProcessData(Map<String, Object> map){
        CommonProcessData result = new CommonProcessData();
        result.put(BUSINESS, map);
        result.put(CTL, Maps.newHashMap());
        return result;
    }

    public static CommonProcessData ofCtlProcessData(Map<String, Object> map){
        CommonProcessData result = new CommonProcessData();
        result.put(CTL, map);
        result.put(BUSINESS, Maps.newHashMap());
        return result;
    }

    @Override
    public Map<String, Object> getBusinessProcessData() {
        return (Map<String, Object>) this.get(BUSINESS);
    }

    @Override
    public Map<String, Object> getCtlProcessData() {
        return (Map<String, Object>) (this.get(CTL));
    }

    @Override
    public void setProcessCtlData(String key, Object object) {
        getCtlProcessData().put(key, object);
    }

    @Override
    public void setBusinessProcessData(String key, Object object) {
        getBusinessProcessData().put(key, object);
    }

    @Override
    public Map<String, Object> getAllData() {
        return this;
    }

    @Override
    public String getProcessSequenceNum() {
        Object processSequenceNumObj = getCtlProcessData().get(ProcessConstants.PROCESS_SEQUENCE_NUM);
        return Objects.isNull(processSequenceNumObj)
                ? ""
                : processSequenceNumObj.toString();
    }

    @Override
    public String getSubject() {
        Object subjectObj = getCtlProcessData().get(ProcessConstants.SUBJECT);
        return Objects.isNull(subjectObj)
                ? ""
                : subjectObj.toString();
    }


    public Map<String, Object> toActStorage(){
        Map<String, Object> data = JsonUtils.fromJson(JsonUtils.toJson(this));
        data.remove(BUSINESS);
        data.remove(CTL);
        data.put(BUSINESS, JsonUtils.toJson(getBusinessProcessData()));
        data.put(CTL, JsonUtils.toJson(getCtlProcessData()));
        return data;
    }

    public static CommonProcessData fromActStorage(Map<String, Object> map){
        Map<String, Object> data = Maps.newHashMap(map);
        String bus = (String) map.get(BUSINESS);
        String ctl = (String) map.get(CTL);
        if(map.containsKey(BUSINESS)){
            data.replace(BUSINESS, JsonUtils.fromJson(bus));
        }
        if(map.containsKey(CTL)){
            data.replace(CTL, JsonUtils.fromJson(ctl));
        }

        return new CommonProcessData(data);
    }
}
