package tech.tongyu.bct.trade.service.impl.lcm;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class DoubleSharkExerciseProcessorTest {

    @Test
    public void testHasKnockedOut() {
        BigDecimal lowBarrier = new BigDecimal("100");
        BigDecimal highBarrier = new BigDecimal("120");
        BigDecimal spot1 = new BigDecimal("90");
        BigDecimal spot2 = new BigDecimal("130");
        BigDecimal spot3 = new BigDecimal("110");

        Assert.assertTrue(DoubleSharkExerciseProcessor.hasKnockedOut(lowBarrier, highBarrier, spot1));
        Assert.assertTrue(DoubleSharkExerciseProcessor.hasKnockedOut(lowBarrier, highBarrier, spot2));

        Assert.assertFalse(DoubleSharkExerciseProcessor.hasKnockedOut(lowBarrier, highBarrier, spot3));

        Assert.assertFalse(DoubleSharkExerciseProcessor.hasKnockedOut(lowBarrier, highBarrier, lowBarrier));
        Assert.assertFalse(DoubleSharkExerciseProcessor.hasKnockedOut(lowBarrier, highBarrier, highBarrier));
    }

    @Test
    public void testCalDoubleSharkUnitPayment(){
        BigDecimal lowStrike = new BigDecimal("100");
        BigDecimal highStrike = new BigDecimal("120");
        BigDecimal lowParticipationRate = new BigDecimal("1");
        BigDecimal highParticipationRate = new BigDecimal("1");

        BigDecimal spot1 = new BigDecimal("90");
        BigDecimal result1 = new BigDecimal("10");
        assertEquals(result1, DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrike, highStrike, spot1, lowParticipationRate, highParticipationRate));

        BigDecimal spot2 = new BigDecimal("125");
        BigDecimal result2 = new BigDecimal("5");
        assertEquals(result2, DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrike, highStrike, spot2, lowParticipationRate, highParticipationRate));

        BigDecimal spot3 = new BigDecimal("105");
        BigDecimal result3 = BigDecimal.ZERO;
        assertEquals(result3, DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrike, highStrike, spot3, lowParticipationRate, highParticipationRate));

        BigDecimal lowStrikeCross = new BigDecimal("120");
        BigDecimal highStrikeCross = new BigDecimal("100");

        BigDecimal spotCross1 = new BigDecimal("95");
        BigDecimal resultCross1 = new BigDecimal("25");
        assertEquals(resultCross1, DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrikeCross, highStrikeCross, spotCross1, lowParticipationRate, highParticipationRate));

        BigDecimal spotCross2 = new BigDecimal("105");
        BigDecimal resultCross2 = new BigDecimal("20");
        assertEquals(resultCross2, DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrikeCross, highStrikeCross, spotCross2, lowParticipationRate, highParticipationRate));

        BigDecimal spotCross3 = new BigDecimal("130");
        BigDecimal resultCross3 = new BigDecimal("30");
        assertEquals(resultCross3, DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrikeCross, highStrikeCross, spotCross3, lowParticipationRate, highParticipationRate));
    }

    @Test
    public void testCalDoubleSharkUnitPayment2(){
        BigDecimal lowStrike = new BigDecimal("100");
        BigDecimal highStrike = new BigDecimal("120");
        BigDecimal lowParticipationRate = new BigDecimal("0.5");
        BigDecimal highParticipationRate = new BigDecimal("2");

        BigDecimal spot1 = new BigDecimal("90");
        BigDecimal result1 = new BigDecimal("5");
        Assert.assertTrue(result1.compareTo(DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrike, highStrike, spot1, lowParticipationRate, highParticipationRate)) == 0);

        BigDecimal spot2 = new BigDecimal("125");
        BigDecimal result2 = new BigDecimal("10");
        Assert.assertTrue(result2.compareTo(DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrike, highStrike, spot2, lowParticipationRate, highParticipationRate)) == 0);

        BigDecimal spot3 = new BigDecimal("105");
        BigDecimal result3 = BigDecimal.ZERO;
        Assert.assertTrue(result3.compareTo(DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrike, highStrike, spot3, lowParticipationRate, highParticipationRate)) == 0);

        BigDecimal lowStrikeCross = new BigDecimal("120");
        BigDecimal highStrikeCross = new BigDecimal("100");

        BigDecimal spotCross1 = new BigDecimal("95");
        BigDecimal resultCross1 = new BigDecimal("12.5");
        Assert.assertTrue(resultCross1.compareTo(DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrikeCross, highStrikeCross, spotCross1, lowParticipationRate, highParticipationRate)) == 0);

        BigDecimal spotCross2 = new BigDecimal("105");
        BigDecimal resultCross2 = new BigDecimal("17.5");
        Assert.assertTrue(resultCross2.compareTo(DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrikeCross, highStrikeCross, spotCross2, lowParticipationRate, highParticipationRate)) == 0);

        BigDecimal spotCross3 = new BigDecimal("130");
        BigDecimal resultCross3 = new BigDecimal("60");
        Assert.assertTrue(resultCross3.compareTo(DoubleSharkExerciseProcessor.calDoubleSharkUnitPayment(lowStrikeCross, highStrikeCross, spotCross3, lowParticipationRate, highParticipationRate)) == 0);
    }
}