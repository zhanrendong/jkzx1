package tech.tongyu.bct.cm.product.iov.component;

import tech.tongyu.bct.cm.core.AdjustedDate;
import tech.tongyu.bct.cm.core.BusinessCenterTime;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;

public interface AmericanExercise extends OptionExercise {
    BusinessCenterTime expirationTime();

    BusinessCenterTimeTypeEnum expirationTimeType();

    AdjustedDate commencementDate();

    AdjustedDate noticeDate();

    default ExerciseTypeEnum exerciseType() {
        return ExerciseTypeEnum.AMERICAN;
    }
}
