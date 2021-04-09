package tech.tongyu.bct.trade.service.impl.lcm;

import io.vavr.Tuple;
import io.vavr.Tuple6;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class EagleExerciseProcessorTest {

    @Test
    public void testCalEagleUnitPayment(){
        Tuple6<BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal> data1 = Tuple.of(
                new BigDecimal("90"),
                new BigDecimal("100"),
                new BigDecimal("110"),
                new BigDecimal("120"),
                new BigDecimal("1"),
                new BigDecimal("1")
        );

        BigDecimal spot1 = new BigDecimal("95");
        BigDecimal result1 = new BigDecimal("5");

        Assert.assertEquals(result1, EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot1));

        BigDecimal spot2 = new BigDecimal("106");
        BigDecimal result2 = new BigDecimal("10");

        Assert.assertEquals(result2, EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot2));

        BigDecimal spot3 = new BigDecimal("117");
        BigDecimal result3 = new BigDecimal("3");

        Assert.assertEquals(result3, EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot3));

        BigDecimal spot4 = new BigDecimal("129");
        BigDecimal result4 = new BigDecimal("0");

        Assert.assertEquals(result4, EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot4));

        BigDecimal spot5 = new BigDecimal("87");
        BigDecimal result5 = new BigDecimal("0");

        Assert.assertEquals(result5, EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot5));

        BigDecimal spot6 = new BigDecimal("100");
        BigDecimal result6 = new BigDecimal("10");

        Assert.assertEquals(result6, EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot6));

        BigDecimal spot7 = new BigDecimal("110");
        BigDecimal result7 = new BigDecimal("10");

        Assert.assertEquals(result7, EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot7));
    }

    @Test
    public void testCalEagleUnitPayment2(){
        Tuple6<BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal> data1 = Tuple.of(
                new BigDecimal("90"),
                new BigDecimal("95"),
                new BigDecimal("110"),
                new BigDecimal("130"),
                new BigDecimal("2"),
                new BigDecimal("0.5")
        );

        BigDecimal spot1 = new BigDecimal("92");
        BigDecimal result1 = new BigDecimal("4");

        Assert.assertTrue(result1.compareTo(EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot1)) == 0);

        BigDecimal spot2 = new BigDecimal("120");
        BigDecimal result2 = new BigDecimal("5");

        Assert.assertTrue(result2.compareTo(EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot2)) == 0);

        BigDecimal spot3 = new BigDecimal("100");
        BigDecimal result3 = new BigDecimal("10");

        Assert.assertTrue(result3.compareTo(EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot3)) == 0);

        BigDecimal spot4 = new BigDecimal("131");
        BigDecimal result4 = new BigDecimal("0");

        Assert.assertTrue(result4.compareTo(EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot4)) == 0);

        BigDecimal spot5 = new BigDecimal("87");
        BigDecimal result5 = new BigDecimal("0");

        Assert.assertTrue(result5.compareTo(EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot5)) == 0);

        BigDecimal spot6 = new BigDecimal("95");
        BigDecimal result6 = new BigDecimal("10");

        Assert.assertTrue(result6.compareTo(EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot6)) == 0);

        BigDecimal spot7 = new BigDecimal("110");
        BigDecimal result7 = new BigDecimal("10");

        Assert.assertTrue(result7.compareTo(EagleExerciseProcessor.calEagleUnitPayment(
                data1._1, data1._2, data1._3, data1._4
                , data1._5, data1._6, spot7)) == 0);
    }

}
