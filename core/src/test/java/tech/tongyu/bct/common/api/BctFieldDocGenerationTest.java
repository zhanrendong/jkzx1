package tech.tongyu.bct.common.api;

import org.junit.Assert;
import org.junit.Test;
import tech.tongyu.bct.common.api.doc.BctFieldDto;
import tech.tongyu.bct.common.api.doc.BctFieldUtils;

import java.util.List;

public class BctFieldDocGenerationTest {

    private List<BctFieldDto> testBctFieldAnnotation(Class<?> clazz){
         return BctFieldUtils.getFieldsDecorationFromDto(clazz);
    }

    @Test
    public void testBctFieldTestDtoAnnotation(){
        List<BctFieldDto> bctFieldDtoList = testBctFieldAnnotation(BctFieldTestDto.class);
        Assert.assertFalse("not empty!", bctFieldDtoList.isEmpty());
    }
}
