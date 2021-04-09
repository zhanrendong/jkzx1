package tech.tongyu.bct.quant.builder;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.quant.library.market.custom.ModelXY;
import tech.tongyu.bct.quant.library.numerics.interp.ExtrapTypeEnum;
import tech.tongyu.bct.quant.library.numerics.interp.Interpolator1DCubicSpline;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CustomModelBuilder {
    public static ModelXY createModelXY(List<LocalDateTime> timestamps,
                                        List<List<Double>> spots,
                                        List<List<Double>> prices,
                                        List<List<Double>> deltas,
                                        List<List<Double>> gammas,
                                        List<List<Double>> vegas,
                                        List<List<Double>> thetas) {
        int nTimestamps = timestamps.size();
        if (spots.size() != nTimestamps ||prices.size() != nTimestamps || deltas.size() != nTimestamps
                || gammas.size() != nTimestamps || vegas.size() != nTimestamps || thetas.size() != nTimestamps) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                    String.format("quantlib: ModelXY: 模型时间戳数目 %d 不等于spot/price/delta/gamma/vega/theta数目",
                            nTimestamps));
        }

        return new ModelXY(timestamps, buildInterps(spots, prices), buildInterps(spots, deltas), buildInterps(spots, gammas),
                buildInterps(spots, vegas), buildInterps(spots, thetas));
    }

    private static List<Interpolator1DCubicSpline> buildInterps(List<List<Double>> xss, List<List<Double>> yss) {
        return IntStream.range(0, xss.size()).mapToObj(i ->
                new Interpolator1DCubicSpline(
                        xss.get(i).stream().mapToDouble(j -> j).toArray(),
                        yss.get(i).stream().mapToDouble(k -> k).toArray(),
                        ExtrapTypeEnum.EXTRAP_1D_LINEAR,
                        ExtrapTypeEnum.EXTRAP_1D_LINEAR))
                .collect(Collectors.toList());
    }
}
