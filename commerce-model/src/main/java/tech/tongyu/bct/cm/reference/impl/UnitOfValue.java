package tech.tongyu.bct.cm.reference.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.reference.elemental.Unit;

public class UnitOfValue<V> implements tech.tongyu.bct.cm.reference.elemental.UnitOfValue<V> {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public Unit unit;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public V value;

    public UnitOfValue() {
    }

    public UnitOfValue(Unit unit, V value) {
        this.unit = unit;
        this.value = value;
    }

    @Override
    public V value() {
        return value;
    }

    @Override
    public Unit unit() {
        return unit;
    }
}
