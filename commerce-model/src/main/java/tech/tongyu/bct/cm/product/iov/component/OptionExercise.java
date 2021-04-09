package tech.tongyu.bct.cm.product.iov.component;

import tech.tongyu.bct.cm.core.AbsoluteDate;
import tech.tongyu.bct.cm.core.AdjustedDate;
import tech.tongyu.bct.cm.product.iov.ExerciseTypeEnum;

import java.time.LocalDate;
import java.time.LocalTime;

public interface OptionExercise {
    Boolean automaticExercise();

    AdjustedDate expirationDate();

    ExerciseTypeEnum exerciseType();

    default AbsoluteDate absoluteExpirationDate() {
        AdjustedDate adjustedDate = expirationDate();
        if (adjustedDate instanceof AbsoluteDate) {
            return (AbsoluteDate) adjustedDate;
        } else {
            throw new IllegalStateException(String.format("%s is not an AbsoluteDate", adjustedDate));
        }
    }
}
