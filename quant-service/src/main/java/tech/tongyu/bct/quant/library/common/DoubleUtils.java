package tech.tongyu.bct.quant.library.common;


import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DoubleUtils {
    public static double SMALL_NUMBER = 1e-14;

    public static boolean smallEnough(double x, double threshold) {
        return FastMath.abs(x) <= FastMath.abs(threshold);
    }

    public static boolean nonZero(double x, double threshold) {
        return FastMath.abs(x) > threshold;
    }

    public static List<Double> forceDouble(List<Number> in) {
        return in.stream().map(Number::doubleValue).collect(Collectors.toList());
    }

    public static List<List<Double>> numberToDoubleMatrix(List<List<Number>> in) {
        return in.stream().map(DoubleUtils::forceDouble).collect(Collectors.toList());
    }

    public static List<List<Double>> traspose(List<List<Double>> in) {
        List<List<Double>> ret = new ArrayList<>();
        int nRows = in.size();
        int nCols = in.get(0).size();
        for (int icol = 0; icol < nCols; ++icol) {
            List<Double> cols = new ArrayList<>();
            for (int irow = 0; irow < nRows; ++irow) {
                cols.add(in.get(irow).get(icol));
            }
            ret.add(cols);
        }
        return ret;
    }

    public static List<Double> scaleList(List<Double> orig, double factor) {
        return orig.stream().map(x -> x * factor).collect(Collectors.toList());
    }

    public static List<List<Double>> scaleMatrix(List<List<Double>> orig, double factor) {
        return orig.stream().map(row -> scaleList(row, factor)).collect(Collectors.toList());
    }

    public static List<Double> sumList(List<Double> l1, List<Double> l2) {
        return IntStream.range(0, l1.size()).mapToObj(i -> l1.get(i) + l2.get(i)).collect(Collectors.toList());
    }

    public static List<List<Double>> sumMatrix(List<List<Double>> m1, List<List<Double>> m2) {
        return IntStream.range(0, m1.size()).mapToObj(i -> sumList(m1.get(i), m2.get(i))).collect(Collectors.toList());
    }
}
