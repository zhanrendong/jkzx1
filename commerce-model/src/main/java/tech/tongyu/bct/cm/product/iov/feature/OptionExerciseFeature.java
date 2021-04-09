package tech.tongyu.bct.cm.product.iov.feature;

import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.core.AdjustedDate;
import tech.tongyu.bct.cm.core.BusinessCenterTimeTypeEnum;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;

import java.time.LocalDate;
import java.time.LocalTime;

public interface OptionExerciseFeature extends UnderlyerFeature {

    BusinessCenterTimeTypeEnum specifiedPrice();

    AdjustedDate expirationDate();

    LocalTime expirationTime();

    ExerciseTypeEnum exerciseType();

    void rollLCMEvent(LocalDate expirationDate);

    default LocalDate absoluteExpirationDate() {
        AdjustedDate adjustedDate = expirationDate();
        if (adjustedDate instanceof AbsoluteDate) {
            return ((AbsoluteDate) adjustedDate).unadjustedDate;
        } else {
            throw new IllegalStateException(String.format("%s is not an AbsoluteDate", adjustedDate));
        }
    }

    default LocalTime absoluteExpirationTime() {
        if (specifiedPrice().equals(BusinessCenterTimeTypeEnum.SPECIFIC_TIME)) {
            return expirationTime();
        } else {
            return LocalTime.of(15, 0, 0);
        }
    }
}
