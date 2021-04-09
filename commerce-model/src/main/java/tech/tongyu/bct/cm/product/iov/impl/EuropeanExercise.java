package tech.tongyu.bct.cm.product.iov.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.core.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class EuropeanExercise implements tech.tongyu.bct.cm.product.iov.component.EuropeanExercise {

    public BusinessCenterTime expirationTime;

    public BusinessCenterTimeTypeEnum expirationTimeType;

    public EuropeanExercise() {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public AdjustedDate expirationDate;

    public EuropeanExercise(BusinessCenterTime expirationTime, BusinessCenterTimeTypeEnum expirationTimeType, AdjustedDate expirationDate) {
        this.expirationTime = expirationTime;
        this.expirationTimeType = expirationTimeType;
        this.expirationDate = expirationDate;
    }

    public EuropeanExercise(LocalDate expirationDate, LocalTime expirationTime, BusinessCenterEnum businessCenter,
                            BusinessCenterTimeTypeEnum expirationTimeType) {
        this.expirationTime = new BusinessCenterTime(expirationTime, businessCenter);
        this.expirationTimeType = expirationTimeType;
        this.expirationDate = new AbsoluteDate(expirationDate);
    }

    @Override
    public BusinessCenterTime expirationTime() {
        return expirationTime;
    }

    @Override
    public BusinessCenterTimeTypeEnum expirationTimeType() {
        return expirationTimeType;
    }

    @Override
    public AdjustedDate expirationDate() {
        return expirationDate;
    }
}
