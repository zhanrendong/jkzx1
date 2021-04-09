package tech.tongyu.bct.common.api;

import tech.tongyu.bct.common.api.doc.BctField;

import java.util.Collection;
import java.util.Map;

public class BctFieldTestDto {

    @BctField
    private String id;

    @BctField(name = "name")
    private String name;

    @BctField(description = "description")
    private String description;

    @BctField(type = "double")
    private Double doubleField;

    @BctField(name = "integer", description = "should be integer", type = "integer")
    private Integer integerField;

    @BctField(type = "boolean", description = "boolean field")
    private Boolean booleanField;

    @BctField(isCollection = true, componentClass = BctFieldTestNestedDto.class)
    private Collection<BctFieldTestNestedDto> collectionField;

    @BctField(name = "nest dto")
    private BctFieldTestNestedDto nestedDtoField;

    @BctField(possibleComponentClassCollection = {BctFieldTestNestedDto.class})
    private Map<String, Object> mapField;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDoubleField() {
        return doubleField;
    }

    public void setDoubleField(Double doubleField) {
        this.doubleField = doubleField;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public void setIntegerField(Integer integerField) {
        this.integerField = integerField;
    }

    public Boolean getBooleanField() {
        return booleanField;
    }

    public void setBooleanField(Boolean booleanField) {
        this.booleanField = booleanField;
    }

    public Collection<BctFieldTestNestedDto> getCollectionField() {
        return collectionField;
    }

    public void setCollectionField(Collection<BctFieldTestNestedDto> collectionField) {
        this.collectionField = collectionField;
    }

    public BctFieldTestNestedDto getNestedDtoField() {
        return nestedDtoField;
    }

    public Map<String, Object> getMapField() {
        return mapField;
    }

    public void setMapField(Map<String, Object> mapField) {
        this.mapField = mapField;
    }

    public void setNestedDtoField(BctFieldTestNestedDto nestedDtoField) {
        this.nestedDtoField = nestedDtoField;
    }
}
