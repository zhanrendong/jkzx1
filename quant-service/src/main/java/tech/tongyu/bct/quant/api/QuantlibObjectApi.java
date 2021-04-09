package tech.tongyu.bct.quant.api;

import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.quant.library.common.QuantlibSerializableObject;
import tech.tongyu.bct.quant.library.market.vol.VolCalendar;
import tech.tongyu.bct.quant.library.market.vol.VolCalendarCache;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

@Service
public class QuantlibObjectApi {
    @BctMethodInfo(excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.Excel})
    public QuantlibSerializableObject qlQuantlibObjectInfo(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String object
    ) {
        return QuantlibObjectCache.Instance.getMayThrow(object);
    }

    @BctMethodInfo(excelType = BctExcelTypeEnum.DataDict, tags = {BctApiTagEnum.Excel})
    public VolCalendar qlVolCalendarInfo(
            @BctMethodArg(excelType = BctExcelTypeEnum.Handle) String name
    ) {
        return VolCalendarCache.getMayThrow(name);
    }
}
