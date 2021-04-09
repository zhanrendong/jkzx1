package tech.tongyu.bct.workflow.dto.process;

import java.util.Map;

public interface ProcessData {

    Map<String, Object> getBusinessProcessData();

    Map<String, Object> getCtlProcessData();

    void setProcessCtlData(String key, Object Object);

    void setBusinessProcessData(String key, Object Object);

    Map<String, Object> getAllData();

    String getProcessSequenceNum();

    String getSubject();
}
