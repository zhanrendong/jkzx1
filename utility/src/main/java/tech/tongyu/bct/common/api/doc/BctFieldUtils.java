package tech.tongyu.bct.common.api.doc;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author david.yang
 *  - mailto: yangyiwei@tongyu.tech
 */
public class BctFieldUtils {

    /**
     * get collection of fields' decorations by dto's class
     * @param dtoClass class of dto
     * @return collection of @BctField
     */
    public static List<BctFieldDto> getFieldsDecorationFromDto(@NotNull Class<?> dtoClass){
        if(Objects.isNull(dtoClass)) {
            throw new NullPointerException("param dto Class can't be null");
        }
        List<BctFieldDto> bctFieldList = new LinkedList<>();
        for(Field field : dtoClass.getDeclaredFields()){
            bctFieldList.add(new BctFieldDto(field, field.getAnnotation(BctField.class)));
        }

        bctFieldList = bctFieldList.stream()
                .sorted((bctFieldDto1, bctFieldDto2) -> {
                    if(Objects.equals(bctFieldDto1.getOrder(), bctFieldDto2.getOrder())){
                        return StringUtils.compare(bctFieldDto1.getName(), bctFieldDto2.getName());
                    }
                    else {
                        return Comparator.comparingInt(BctFieldDto::getOrder).compare(bctFieldDto1, bctFieldDto2);
                    }
                })
                .collect(Collectors.toList());

        return bctFieldList;
    }

    public static Boolean needRecursiveDetection(Class<?> type){
        return !ClassUtils.isPrimitiveOrWrapper(type)
                && !String.class.isAssignableFrom(type)
                && !UUID.class.isAssignableFrom(type)
                && !BigDecimal.class.isAssignableFrom(type)
                && !LocalDate.class.isAssignableFrom(type)
                && !Instant.class.isAssignableFrom(type)
                && !Timestamp.class.isAssignableFrom(type)
                && !LocalDateTime.class.isAssignableFrom(type)
                && !LocalTime.class.isAssignableFrom(type)
                && !ZoneId.class.isAssignableFrom(type);
    }
}