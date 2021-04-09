package tech.tongyu.bct.quant.api;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.quant.builder.CustomModelBuilder;
import tech.tongyu.bct.quant.library.common.DoubleUtils;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomModelApi {
    @BctMethodInfo(excelType = BctExcelTypeEnum.Handle, tags = {BctApiTagEnum.Excel})
    public String qlModelXYCreate(
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDateTime) List<String> timestamps,
            @BctMethodArg(excelType = BctExcelTypeEnum.ArrayDouble) List<Number> spots,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix) List<List<Number>> prices,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix) List<List<Number>> deltas,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix) List<List<Number>> gammas,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix) List<List<Number>> vegas,
            @BctMethodArg(excelType = BctExcelTypeEnum.Matrix) List<List<Number>> thetas,
            @BctMethodArg(required = false) String id
    ) {
        List<LocalDateTime> ts = timestamps.stream()
                .map(DateTimeUtils::parseToLocalDateTime)
                .collect(Collectors.toList());
        boolean trans = timestamps.size() != prices.size();
        return QuantlibObjectCache.Instance.put(CustomModelBuilder.createModelXY(
                ts,
                Collections.nCopies(ts.size(), DoubleUtils.forceDouble(spots)),
                trans ? DoubleUtils.traspose(DoubleUtils.numberToDoubleMatrix(prices))
                        : DoubleUtils.numberToDoubleMatrix(prices),
                trans ? DoubleUtils.traspose(DoubleUtils.numberToDoubleMatrix(deltas))
                        : DoubleUtils.numberToDoubleMatrix(deltas),
                trans ? DoubleUtils.traspose(DoubleUtils.numberToDoubleMatrix(gammas))
                        : DoubleUtils.numberToDoubleMatrix(gammas),
                trans ? DoubleUtils.traspose(DoubleUtils.numberToDoubleMatrix(vegas))
                        : DoubleUtils.numberToDoubleMatrix(vegas),
                trans ? DoubleUtils.traspose(DoubleUtils.numberToDoubleMatrix(thetas))
                        : DoubleUtils.numberToDoubleMatrix(thetas)), id);
    }
}
