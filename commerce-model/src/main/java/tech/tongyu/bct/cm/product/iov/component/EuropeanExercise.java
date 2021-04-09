package tech.tongyu.bct.cm.product.iov.component;

import tech.tongyu.bct.cm.core.BusinessCenterTime;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;

public interface EuropeanExercise extends OptionExercise {
    BusinessCenterTime expirationTime();

    BusinessCenterTimeTypeEnum expirationTimeType();

    @Override
    default Boolean automaticExercise() {
        return false;
    }

    default ExerciseTypeEnum exerciseType() {
        return ExerciseTypeEnum.EUROPEAN;
    }
}
