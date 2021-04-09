package tech.tongyu.bct.model.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.common.api.annotation.BctApiTagEnum;
import tech.tongyu.bct.common.api.annotation.BctExcelTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.DateTimeUtils;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.market.dto.QuoteFieldEnum;
import tech.tongyu.bct.model.ao.*;
import tech.tongyu.bct.model.dto.ModelDataDTO;
import tech.tongyu.bct.model.dto.ModelDescriptionDTO;
import tech.tongyu.bct.model.dto.ModelTypeEnum;
import tech.tongyu.bct.model.service.ModelService;


import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ModelApis {
    private ModelService modelService;

    @Autowired
    public ModelApis(ModelService modelService) {
        this.modelService = modelService;
    }

    @BctMethodInfo(
            description = "获取所有模型名称",
            retName = "modelNames",
            retDescription = "所有模型名称",
            service = "model-service"
    )
    public List<String> mdlGetAllModelNames(
            @BctMethodArg(description = "Model type: VOL_SURFACE, DIVIDEND_CURVE, RISK_FREE_CURVE") String modelType
    ) {
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        return modelService.listAllModelNames(modelTypeEnum);
    }

    @BctMethodInfo(
            description = "删除一个序列化模型",
            retName = "status",
            retDescription = "模型是否被删除",
            service = "model-service"
    )
    public String mdlDelete(@BctMethodArg(description = "Model type: VOL_SURFACE, DIVIDEND_CURVE, RISK_FREE_CURVE") String modelType,
                            @BctMethodArg(description = "The model name") String modelName,
                            @BctMethodArg(description = "The instance of the model data: \\'close\\' or \\'intraday\\'") String instance,
                            @BctMethodArg(
                                    description = "Valuation date",
                                    excelType = BctExcelTypeEnum.DateTime) String valuationDate,
                            @BctMethodArg(
                                    description = "Model underlyer (not needed if it is a risk free curve)",
                                    required = false) String underlyer,
                            @BctMethodArg(
                                    description = "Timezone of the model timestamp",
                                    required = false) String modelTimezone) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        return modelService.delete(modelTypeEnum, modelName, underlyer, instanceEnum, t.toLocalDate(), t.getZone());
    }

    @BctMethodInfo(
            description = "从数据加载并且反序列化模型",
            retName = "objectId",
            retDescription = "加载的模型ID",
            tags = {BctApiTagEnum.Excel},
            service = "model-service"
    )
    public String mdlLoad(
            @BctMethodArg(description = "模型类型: VOL_SURFACE, DIVIDEND_CURVE, RISK_FREE_CURVE") String modelType,
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "收盘/日内: \\'close\\' or \\'intraday\\'") String instance,
            @BctMethodArg(
                    description = "模型估值日",
                    excelType = BctExcelTypeEnum.DateTime) String valuationDate,
            @BctMethodArg(
                    description = "模型标的物（若为无风险利率曲线，标的物为空）",
                    required = false) String underlyer,
            @BctMethodArg(description = "Timezone of the model timestamp", required = false) String modelTimezone
    ) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        return modelService.load(modelTypeEnum, modelName, underlyer, instanceEnum, t.toLocalDate(), t.getZone());
    }

    @BctMethodInfo(
            description = "保存模型",
            retName = "status",
            retDescription = "是否保存",
            tags = {BctApiTagEnum.Excel},
            service = "model-service"
    )
    public String mdlSave(@BctMethodArg(description = "The quant model id to be saved") String qlObjectId,
                          @BctMethodArg(
                                  description = "Model type: VOL_SURFACE, DIVIDEND_CURVE, RISK_FREE_CURVE, MODEL_XY"
                          ) String modelType,
                          @BctMethodArg(description = "Model name") String modelName,
                          @BctMethodArg(description = "Model instance: close or intraday") String instance,
                          @BctMethodArg(
                                  description = "Valuation date",
                                  excelType = BctExcelTypeEnum.DateTime
                          ) String valuationDate,
                          @BctMethodArg(
                                  description = "Model underlyer (not needed if it is a risk free curve)",
                                  required = false
                          ) String underlyer,
                          @BctMethodArg(
                                  description = "Timezone of the model timestamp",
                                  required = false
                          ) String modelTimezone,
                          @BctMethodArg(description = "Model builder config", required = false) JsonNode modelBuildInfo
    ) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        return modelService.save(qlObjectId, modelTypeEnum, modelName,
                underlyer, instanceEnum, t.toLocalDate(), t.getZone(), modelBuildInfo);
    }

    @BctMethodInfo(
            description = "创建ATM波动率曲线",
            retName = "objectId",
            retDescription = "创建的模型Id",
            service = "model-service"
    )
    public String mdlVolSurfaceAtmCreate(
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "模型估值日", required = false) String valuationDate,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "波动率标的物") Map<String, Object> underlyer,
            @BctMethodArg(description = "波动率（按期限/到期日）") List<Map<String, Object>> instruments,
            @BctMethodArg(description = "时区", required = false) String modelTimezone,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save
    ) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        Boolean toSave = Objects.isNull(save) ? false : save;
        List<AtmVolInstrumentAO> atmVolInstrumentAOList = instruments.stream()
                .map(i -> JsonUtils.mapper.convertValue(i, AtmVolInstrumentAO.class))
                .collect(Collectors.toList());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        AtmVolBuilderConfigAO atmVolBuilderConfigAO = new AtmVolBuilderConfigAO(modelName, t.toLocalDate(), t.getZone(),
                instanceEnum, JsonUtils.mapper.convertValue(underlyer, VolUnderlyerAO.class),
                atmVolInstrumentAOList);
        return modelService.createAtmVolCurve(atmVolBuilderConfigAO, toSave);
    }

    @BctMethodInfo(
            description = "创建ATM波动率曲线",
            retName = "objectId",
            retDescription = "创建的模型Id",
            service = "model-service"
    )
    public String mdlVolSurfaceInterpolatedStrikeCreate(
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "模型估值日", required = false) String valuationDate,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "波动率标的物") Map<String, Object> underlyer,
            @BctMethodArg(description = "波动率（按期限/到期日）") List<Map<String, Object>> instruments,
            @BctMethodArg(description = "一年的天数") double daysInYear,
            @BctMethodArg(description = "是否使用交易日历计算到期日？", required = false) Boolean useCalendarForTenor,
            @BctMethodArg(description = "交易日历", required = false) List<String> calendars,
            @BctMethodArg(description = "是否使用波动率日历?", required = false) Boolean useVolCalendar,
            @BctMethodArg(description = "波动率日历", required = false) String volCalendar,
            @BctMethodArg(description = "时区", required = false) String modelTimezone,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save
    ) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        Boolean toSave = Objects.isNull(save) ? false : save;
        List<InterpVolInstrumentRowAO> atmVolInstrumentAOList = instruments.stream()
                .map(i -> JsonUtils.mapper.convertValue(i, InterpVolInstrumentRowAO.class))
                .collect(Collectors.toList());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        boolean useCalendarForTau = Objects.isNull(useCalendarForTenor) ? false : useCalendarForTenor;
        boolean useVolCalendarFlag = Objects.isNull(useVolCalendar) ? false : useVolCalendar;
        InterpVolBuilderConfigAO interpVolBuilderConfigAO = new InterpVolBuilderConfigAO(modelName,
                t.toLocalDate(), t.getZone(),
                instanceEnum, JsonUtils.mapper.convertValue(underlyer, VolUnderlyerAO.class),
                atmVolInstrumentAOList, daysInYear,
                useCalendarForTau, calendars,
                useVolCalendarFlag, volCalendar);
        return modelService.createInterpStrikeVolSurface(interpVolBuilderConfigAO, toSave);
    }

    @BctMethodInfo(
            description = "创建波动率曲面",
            retName = "objectId",
            retDescription = "创建的模型Id",
            tags = {BctApiTagEnum.Excel},
            service = "model-service"
    )
    public String mdlVolSurfaceInterpolatedCreate(
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "模型估值日", excelType = BctExcelTypeEnum.DateTime, required = false)
                    String valuationDate,
            @BctMethodArg(description = "波动率标的物") String underlyerInstrumentId,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "标的物价格类型(close/last)") String quoteField,
            @BctMethodArg(description = "标的物价格", excelType = BctExcelTypeEnum.Double) Number underlyerPrice,
            @BctMethodArg(description = "期限", excelType = BctExcelTypeEnum.ArrayString) List<String> tenors,
            @BctMethodArg(description = "行权价百分比", excelType = BctExcelTypeEnum.ArrayDouble) List<Number> percents,
            @BctMethodArg(description = "行权价标签", excelType = BctExcelTypeEnum.ArrayString, required = false)
                    List<String> labels,
            @BctMethodArg(description = "波动率", excelType = BctExcelTypeEnum.Matrix) List<List<Number>> vols,
            @BctMethodArg(description = "一年的天数", excelType = BctExcelTypeEnum.Double) Number daysInYear,
            @BctMethodArg(description = "是否使用交易日历计算到期日？", required = false) Boolean useCalendarForTenor,
            @BctMethodArg(description = "交易日历", excelType = BctExcelTypeEnum.ArrayString, required = false)
                    List<String> calendars,
            @BctMethodArg(description = "是否使用波动率日历?", required = false) Boolean useVolCalendar,
            @BctMethodArg(description = "波动率日历", required = false) String volCalendar,
            @BctMethodArg(description = "时区", required = false) String modelTimezone,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save
    ) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        Boolean toSave = Objects.isNull(save) ? false : save;
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        boolean useCalendarForTau = Objects.isNull(useCalendarForTenor) ? false : useCalendarForTenor;
        boolean useVolCalendarFlag = Objects.isNull(useVolCalendar) ? false : useVolCalendar;
        QuoteFieldEnum quoteFieldEnum = QuoteFieldEnum.valueOf(quoteField.toUpperCase());
        VolUnderlyerAO underlyerAO = new VolUnderlyerAO(underlyerInstrumentId, underlyerPrice.doubleValue(),
                instanceEnum, quoteFieldEnum);
        List<InterpVolInstrumentRowAO> atmVolInstrumentAOList = new ArrayList<>();
        for (int i = 0; i < tenors.size(); i++) {
            List<InterpVolInstrumentRowAO.VolInstrument> volInstruments = new ArrayList<>();
            List<Number> volRow = vols.get(i);
            for (int j = 0; j < percents.size(); j++) {
                volInstruments.add(new InterpVolInstrumentRowAO.VolInstrument(
                        volRow.get(j).doubleValue(), null, percents.get(j).doubleValue(), labels.get(j)));
            }
            atmVolInstrumentAOList.add(new InterpVolInstrumentRowAO(tenors.get(i), null, volInstruments));
        }
        InterpVolBuilderConfigAO interpVolBuilderConfigAO = new InterpVolBuilderConfigAO(modelName,
                t.toLocalDate(), t.getZone(), instanceEnum, underlyerAO, atmVolInstrumentAOList,
                daysInYear.doubleValue(), useCalendarForTau, calendars, useVolCalendarFlag, volCalendar);
        return modelService.createInterpStrikeVolSurface(interpVolBuilderConfigAO, toSave);
    }

    @BctMethodInfo(
            description = "创建分红/融券曲线",
            retName = "objectId",
            retDescription = "创建的模型Id",
            service = "model-service"
    )
    public String mdlCurveDividendCreate(
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "模型估值日", excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDate,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "曲线标的物") String underlyer,
            @BctMethodArg(description = "利率（按期限/到期日）", excelType = BctExcelTypeEnum.Table) List<Map<String, Object>> instruments,
            @BctMethodArg(description = "时区", required = false) String modelTimezone,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save){
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        Boolean toSave = Objects.isNull(save) ? false : save;
        List<CurveInstrumentAO> curveInstrumentAOList = instruments.stream()
                .map(i -> JsonUtils.mapper.convertValue(i, CurveInstrumentAO.class))
                .collect(Collectors.toList());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        DividendCurveBuilderConfigAO config = new DividendCurveBuilderConfigAO(modelName, t.toLocalDate(), t.getZone(),
                instanceEnum, underlyer, curveInstrumentAOList);
        return modelService.createDividendCurve(config, toSave);
    }

    @BctMethodInfo(
            description = "根据期限创建分红/融券曲线",
            retName = "objectId",
            retDescription = "创建的模型Id",
            tags = {BctApiTagEnum.Excel},
            service = "model-service"
    )
    public String mdlCurveDividendByTenorCreate(
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "模型估值日", excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDate,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "曲线标的物") String underlyer,
            @BctMethodArg(description = "时区", required = false) String modelTimezone,
            @BctMethodArg(description = "期限", excelType = BctExcelTypeEnum.ArrayString) List<String> tenors,
            @BctMethodArg(description = "利率", excelType = BctExcelTypeEnum.ArrayDouble) List<Number> quote,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save){
       InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
       Boolean toSave = Objects.isNull(save) ? false: save;
       List<CurveInstrumentAO> curveInstrumentAOList = new ArrayList<>();
       for (int i = 0; i < tenors.size(); i++){
           curveInstrumentAOList.add(new CurveInstrumentAO(tenors.get(i),null, quote.get(i).doubleValue(), true));
       }
       ZonedDateTime t = DateTimeUtils.parse(valuationDate,modelTimezone);
       DividendCurveBuilderConfigAO config = new DividendCurveBuilderConfigAO(modelName, t.toLocalDate(), t.getZone(),
               instanceEnum, underlyer, curveInstrumentAOList);
       return modelService. createDividendCurve(config, toSave);
    }

    @BctMethodInfo(
            description = "创建无风险利率曲线",
            retName = "objectId",
            retDescription = "创建的模型Id",
            service = "model-service"
    )
    public String mdlCurveRiskFreeCreate(
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "模型估值日", required = false) String valuationDate,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "利率（按期限/到期日）") List<Map<String, Object>> instruments,
            @BctMethodArg(description = "时区", required = false) String modelTimezone,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save
    ) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        Boolean toSave = Objects.isNull(save) ? false : save;
        List<CurveInstrumentAO> curveInstrumentAOList = instruments.stream()
                .map(i -> JsonUtils.mapper.convertValue(i, CurveInstrumentAO.class))
                .collect(Collectors.toList());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        RiskFreeCurveBuilderConfigAO config = new RiskFreeCurveBuilderConfigAO(modelName, t.toLocalDate(), t.getZone(),
                instanceEnum, curveInstrumentAOList);
        return modelService.createRiskFreeCurve(config, toSave);
    }

    @BctMethodInfo(
            description = "根据期限创建无风险利率曲线",
            retName = "objectId",
            retDescription = "创建的模型Id",
            tags = {BctApiTagEnum.Excel},
            service = "model-service"
    )
    public String mdlRiskFreeByTenorCreate(
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "模型估值日", excelType = BctExcelTypeEnum.DateTime, required = false) String valuationDate,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg(description = "时区", required = false) String modelTimezone,
            @BctMethodArg(description = "期限", excelType = BctExcelTypeEnum.ArrayString) List<String> tenors,
            @BctMethodArg(description = "利率", excelType = BctExcelTypeEnum.ArrayDouble) List<Number> quote,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save){
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        Boolean toSave = Objects.isNull(save) ? false: save;
        List<CurveInstrumentAO> curveInstrumentAOList = new ArrayList<>();
        for (int i = 0; i < tenors.size(); i++){
        curveInstrumentAOList.add(new CurveInstrumentAO(tenors.get(i), null, quote.get(i).doubleValue(), true));}
        ZonedDateTime t = DateTimeUtils.parse(valuationDate,modelTimezone);
        RiskFreeCurveBuilderConfigAO config = new RiskFreeCurveBuilderConfigAO(modelName, t.toLocalDate(), t.getZone(),
                instanceEnum, curveInstrumentAOList);
        return modelService. createRiskFreeCurve(config, toSave);
    }

    @BctMethodInfo(
            description = "创建自定义模型",
            retName = "objectId",
            retDescription = "创建的模型Id",
            service = "model-service"
    )
    public String mdlModelXYCreate(
            @BctMethodArg(description = "模型名称/分组") String tradeId,
            @BctMethodArg(description = "模型估值日", required = false) String valuationDate,
            @BctMethodArg(description = "收盘/日内") String instance,
            @BctMethodArg List<Map<String, Object>> data,
            @BctMethodArg(required = false) String modelTimezone,
            @BctMethodArg(required = false) Boolean save
    ) {
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        boolean toSave = Objects.isNull(save) ? false : save;
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        List<ModelXYBuilderConfigAO.Slice> slices = data.stream()
                .map(m -> JsonUtils.mapper.convertValue(m, ModelXYBuilderConfigAO.Slice.class))
                .collect(Collectors.toList());
        String modelName = ModelTypeEnum.MODEL_XY + "_" + tradeId;
        ModelXYBuilderConfigAO config = new ModelXYBuilderConfigAO(modelName,
                t.toLocalDate(), t.getZone(), instanceEnum, slices);
        return modelService.createModelXY(tradeId, t.toLocalDate(), instanceEnum,
                config,
                t.getZone(), toSave);
    }

    @BctMethodInfo(
            description = "加载波动率曲面数据",
            retDescription = "波动率曲面数据",
            retName = "VolSurfaceDate",
            returnClass = ModelDataDTO.class,
            service = "model-service"
    )
    public ModelDataDTO mdlModelDataGet(
            @BctMethodArg(description = "Model type") String modelType,
            @BctMethodArg(description = "Modle name") String modelName,
            @BctMethodArg(description = "Underlyer", required = false) String underlyer,
            @BctMethodArg(description = "Instance") String instance,
            @BctMethodArg(description = "Valuation date", required = false) String valuationDate,
            @BctMethodArg(description = "Timezone", required = false) String modelTimezone

    ) {
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        return modelService.findModelData(modelTypeEnum, modelName,
                underlyer, instanceEnum, t.toLocalDate(), t.getZone());
    }

    @BctMethodInfo(
            description = "罗列已保存模型",
            retDescription = "罗列模型描述",
            retName = "ModelList",
            returnClass = ModelDescriptionDTO.class,
            service = "model-service"
    )
    public List<ModelDescriptionDTO> mdlModelList(
            @BctMethodArg(description = "模型类型") String modelType,
            @BctMethodArg(description = "模型名称/分组", required = false) String underlyer,
            @BctMethodArg(description = "收盘/日内") String instance

    ) {
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        if ((modelTypeEnum == ModelTypeEnum.RISK_FREE_CURVE
                || modelTypeEnum == ModelTypeEnum.MODEL_XY)
                && !Objects.isNull(underlyer))
            throw new CustomException("无风险利率曲线/MODEL_XY 不应有标的物");
        if ((modelTypeEnum != ModelTypeEnum.RISK_FREE_CURVE && modelTypeEnum != ModelTypeEnum.MODEL_XY)
                && Objects.isNull(underlyer))
            throw new CustomException("波动率曲面/分红融券曲线 必须有标的物");
        return modelService.list(modelTypeEnum, underlyer, instanceEnum);
    }

    @BctMethodInfo(
            description = "改变按期限定义的模型的估值起始日并返回",
            retName = "objectId",
            retDescription = "改动后的模型Id",
            tags = {BctApiTagEnum.Excel},
            service = "model-service"
    )
    public String mdlModelShiftValuationDate(
            @BctMethodArg(description = "模型类型: VOL_SURFACE, DIVIDEND_CURVE, RISK_FREE_CURVE") String modelType,
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "收盘/日内: \\'close\\' or \\'intraday\\'") String instance,
            @BctMethodArg(description = "模型标的物（若为无风险利率曲线，标的物为空）", required = false) String underlyer,
            @BctMethodArg(description = "模型估值日", excelType = BctExcelTypeEnum.DateTime) String valuationDate,
            @BctMethodArg(description = "新估值日", excelType = BctExcelTypeEnum.DateTime) String newValuationDate,
            @BctMethodArg(description = "Timezone of the model timestamp", required = false) String modelTimezone,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save
    ) {
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        ZonedDateTime newT = DateTimeUtils.parse(newValuationDate, modelTimezone);
        Boolean toSave = Objects.isNull(save) ? false : save;
        return modelService.shiftModelValuationDate(modelTypeEnum, modelName, underlyer, instanceEnum,
                t.toLocalDate(), newT.toLocalDate(), t.getZone(), toSave);
    }

    @BctMethodInfo(
            description = "批量改变按期限定义的模型的估值起始日并返回",
            retName = "objectId",
            retDescription = "改动后的模型Id",
            tags = {BctApiTagEnum.Excel},
            service = "model-service"
    )
    public List<String> mdlModelShiftValuationDateBatch(
            @BctMethodArg(description = "模型类型: VOL_SURFACE, DIVIDEND_CURVE") String modelType,
            @BctMethodArg(description = "模型名称/分组") String modelName,
            @BctMethodArg(description = "收盘/日内: \\'close\\' or \\'intraday\\'") String instance,
            @BctMethodArg(description = "模型标的物", excelType = BctExcelTypeEnum.ArrayString) List<String> underlyers,
            @BctMethodArg(description = "模型估值日", excelType = BctExcelTypeEnum.DateTime) String valuationDate,
            @BctMethodArg(description = "新估值日", excelType = BctExcelTypeEnum.DateTime) String newValuationDate,
            @BctMethodArg(description = "Timezone of the model timestamp", required = false) String modelTimezone,
            @BctMethodArg(description = "是否保存?", required = false) Boolean save
    ) {
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.valueOf(modelType.toUpperCase());
        InstanceEnum instanceEnum = InstanceEnum.valueOf(instance.toUpperCase());
        ZonedDateTime t = DateTimeUtils.parse(valuationDate, modelTimezone);
        ZonedDateTime newT = DateTimeUtils.parse(newValuationDate, modelTimezone);
        Boolean toSave = Objects.isNull(save) ? false : save;
        List<String> modelIds = underlyers.stream()
                .map(u -> {
                    try {
                        return modelService.shiftModelValuationDate(modelTypeEnum, modelName, u, instanceEnum,
                                t.toLocalDate(), newT.toLocalDate(), t.getZone(), toSave);
                    } catch (CustomException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return modelIds;
    }
}
