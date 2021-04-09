package tech.tongyu.bct.document.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TransfromUtil {

    private static Map<Object, String> map = new HashMap<>();

    static {
        map.put("BUYER", "买");
        map.put("SELLER", "卖");
        map.put("CALL", "看涨");
        map.put("PUT", "看跌");
        map.put("UP", "向上敲出");
        map.put("DOWN", "向下敲出");
        map.put("CLOSE", "收盘价");
        map.put("TWAP", "平均价");
        map.put("PERCENT", "百分比");
        map.put("CNY", "人民币");
        map.put("LOT", "手");

        map.put("VANILLA_EUROPEAN", "香草欧式");
        map.put("VANILLA_AMERICAN", "香草美式");
        map.put("VERTICAL_SPREAD", "价差");
        map.put("AUTOCALL", "AutoCall");
        map.put("AUTOCALL_PHOENIX", "凤凰式AutoCall");
        map.put("DIGITAL", "二元");
        map.put("BARRIER", "障碍");
        map.put("DOUBLE_SHARK_FIN", "双鲨");
        map.put("EAGLE", "鹰式");
        map.put("DOUBLE_TOUCH", "双触碰");
        map.put("DOUBLE_NO_TOUCH", "双不触碰");
        map.put("CONCAVA", "二元凹式");
        map.put("CONVEX", "二元凸式");
        map.put("DOUBLE_DIGITAL", "三层阶梯");
        map.put("TRIPLE_DIGITAL", "四层阶梯");
        map.put("RANGE_ACCRUALS", "区间累积");
        map.put("STRADDLE", "跨式");
        map.put("ASIAN", "亚式");
        map.put("MODEL_XY", "自定义产品");

        map.put("PAY_WHEN_HIT", "立即支付");
        map.put("PAY_NONE", "不支付");
        map.put("PAY_AT_EXPIRY", "到期支付");
        map.put("TERMINAL", "到期观察");
        map.put("CONTINUOUS", "连续观察");
        map.put("DAILY", "每日观察");
        map.put("DISCRETE", "离散观察");
        map.put("null", "空");
        map.put("FORWARD", "远期");
    }

    public static String transfrom(String filed) {
        if (map.get(filed).isEmpty()) {
            throw new RuntimeException("字典翻译失败，请检查改字段:" + filed);
        }
        return map.get(filed);
    }

    public static Object transfromPOI(String filed) {
        if (Objects.isNull(map.get(filed))) {
            return filed;
        }
        return map.get(filed);
    }
}
