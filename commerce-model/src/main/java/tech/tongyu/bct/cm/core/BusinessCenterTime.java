package tech.tongyu.bct.cm.core;

import java.time.LocalTime;

public class BusinessCenterTime {

    public LocalTime time;

    public BusinessCenterEnum businessCenter;

    public BusinessCenterTime() {
    }

    public BusinessCenterTime(LocalTime time, BusinessCenterEnum businessCenter) {
        this.time = time;
        this.businessCenter = businessCenter;
    }

    public LocalTime time() {
        return time;
    }

    public BusinessCenterEnum businessCenter() {
        return businessCenter;
    }
}
