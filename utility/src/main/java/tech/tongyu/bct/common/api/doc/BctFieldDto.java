package tech.tongyu.bct.common.api.doc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import tech.tongyu.bct.common.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class BctFieldDto {

    private String name;
    private String description;
    private String type;
    private Boolean isCollection;
    private Boolean ignore;
    private int order;
    private List<BctFieldDto> subClassFieldList;
    private Map<String, Collection<BctFieldDto>> possibleSubClassFieldCollectionMap;

    public BctFieldDto(Field field, BctField bctField){
        if(Objects.isNull(bctField)){
            name = field.getName();
            description = field.getName();
            type = field.getType().getSimpleName();
            if(BctFieldUtils.needRecursiveDetection(field.getType())){
                if(Map.class.isAssignableFrom(field.getType())
                        || Enum.class.isAssignableFrom(field.getType())
                        || Collection.class.isAssignableFrom(field.getType())){
                    subClassFieldList = Lists.newArrayList();
                } else {
                    subClassFieldList = BctFieldUtils.getFieldsDecorationFromDto(field.getType());
                }
            }
            order = 1;
            ignore = false;
            isCollection = Collection.class.isAssignableFrom(field.getType());
            possibleSubClassFieldCollectionMap = Maps.newHashMap();
        }
        else {
            name = StringUtils.isBlank(bctField.name()) ? field.getName() : bctField.name();
            description = StringUtils.isBlank(bctField.description()) ? field.getName() : bctField.description();
            type = StringUtils.isBlank(bctField.type()) ? field.getType().getSimpleName() : bctField.type();
            order = bctField.order();
            isCollection = bctField.isCollection() || Collection.class.isAssignableFrom(field.getType());
            ignore = bctField.ignore();
            if(BctFieldUtils.needRecursiveDetection(field.getType())){
                if(isCollection){
                    if(Objects.equals(bctField.componentClass(), Object.class)){
                        subClassFieldList = BctFieldUtils.getFieldsDecorationFromDto(field.getType());
                    }else {
                        subClassFieldList = BctFieldUtils.getFieldsDecorationFromDto(bctField.componentClass());
                    }
                }
                else if (Map.class.isAssignableFrom(field.getType())){
                    subClassFieldList = BctFieldUtils.getFieldsDecorationFromDto(bctField.componentClass());
                }
                else if (Enum.class.isAssignableFrom(field.getType())){
                    subClassFieldList = BctFieldUtils.getFieldsDecorationFromDto(bctField.componentClass());
                }
                else {
                    subClassFieldList = BctFieldUtils.getFieldsDecorationFromDto(field.getType());
                }
            }
            if(CollectionUtils.isNotEmpty(bctField.possibleComponentClassCollection())){
                possibleSubClassFieldCollectionMap = Arrays.stream(bctField.possibleComponentClassCollection())
                        .collect(Collectors.toMap(Class::getSimpleName, BctFieldUtils::getFieldsDecorationFromDto));
            }
        }
    }

    public BctFieldDto(String name, String description, String type
            , Boolean isCollection, Boolean ignore, int order) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.isCollection = isCollection;
        this.ignore = ignore;
        this.order = order;
    }

    public Map<String, Collection<BctFieldDto>> getPossibleSubClassFieldCollectionMap() {
        return possibleSubClassFieldCollectionMap;
    }

    public void setPossibleSubClassFieldCollectionMap(Map<String, Collection<BctFieldDto>> possibleSubClassFieldCollectionMap) {
        this.possibleSubClassFieldCollectionMap = possibleSubClassFieldCollectionMap;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getCollection() {
        return isCollection;
    }

    public void setCollection(Boolean collection) {
        isCollection = collection;
    }

    public Boolean getIgnore() {
        return ignore;
    }

    public void setIgnore(Boolean ignore) {
        this.ignore = ignore;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public List<BctFieldDto> getSubClassFieldList() {
        return subClassFieldList;
    }

    public void setSubClassFieldList(List<BctFieldDto> subClassFieldList) {
        this.subClassFieldList = subClassFieldList;
    }
}
