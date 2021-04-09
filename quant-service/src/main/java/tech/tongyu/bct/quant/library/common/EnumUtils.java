package tech.tongyu.bct.quant.library.common;

import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnumUtils {
    // https://stackoverflow.com/a/1723239
    public static <T extends Enum<T>> T fromString(String name, Class<T> c) {
        if( c != null && name != null ) {
            try {
                return Enum.valueOf(c, name.trim().toUpperCase());
            } catch(IllegalArgumentException ex) {
                throw new CustomException(ErrorCode.INPUT_NOT_VALID,
                        String.format("quantlib: 无法将 %s 转换为 enum", name));
            }
        }
        throw new CustomException(ErrorCode.INPUT_NOT_VALID, String.format("quantlib: 无法将 %s 转换为 enum", name));
    }

    public static List<CalcTypeEnum> getPricingRequests(List<String> requests) {
        List<CalcTypeEnum> reqs = new ArrayList<>();
        if (Objects.isNull(requests) || requests.size() == 0) {
            reqs.add(CalcTypeEnum.PRICE);
            reqs.add(CalcTypeEnum.DELTA);
            reqs.add(CalcTypeEnum.GAMMA);
            reqs.add(CalcTypeEnum.VEGA);
            reqs.add(CalcTypeEnum.THETA);
            reqs.add(CalcTypeEnum.RHO_R);
            reqs.add(CalcTypeEnum.RHO_Q);
        } else {
            requests.stream()
                    .map(String::toUpperCase)
                    .distinct()
                    .forEach(r -> {
                        try {
                            reqs.add(CalcTypeEnum.valueOf(r.toUpperCase()));
                        } catch (Exception e) {
                        }
                    });
        }
        return reqs;
    }

    public static List<CalcTypeEnum> getIrPricingRequests(List<String> requests) {
        List<CalcTypeEnum> reqs = new ArrayList<>();
        if (Objects.isNull(requests) || requests.size() == 0) {
            reqs.add(CalcTypeEnum.NPV);
            reqs.add(CalcTypeEnum.DV01);
        } else {
            requests.stream()
                    .map(String::toUpperCase)
                    .distinct()
                    .filter(r -> r.equals(CalcTypeEnum.NPV.name()) || r.equals(CalcTypeEnum.DV01.name()))
                    .forEach(r -> {
                        try {
                            reqs.add(CalcTypeEnum.valueOf(r.toUpperCase()));
                        } catch (Exception e) {
                        }
                    });
        }
        return reqs;
    }
}
