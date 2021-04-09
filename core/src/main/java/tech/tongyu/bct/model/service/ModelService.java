package tech.tongyu.bct.model.service;


import com.fasterxml.jackson.databind.JsonNode;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.model.ao.*;
import tech.tongyu.bct.model.dto.ModelDataDTO;
import tech.tongyu.bct.model.dto.ModelDescriptionDTO;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.model.dto.ModelTypeEnum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * 模型服务。模型目前包括波动率曲面，无风险利率曲线和分红/融券曲线({@link ModelTypeEnum})。所有模型都需要quantlib支持建模。
 * 每个模型按照种类，名称，日内/收盘进行分类。波动率曲面和分红/融券曲线需要指明标的物。
 * 无风险利率曲线一般为所有标的物共享。每个模型在每个估值日只能有一个。
 */
public interface ModelService {
    String SCHEMA = "modelService";
    // builder

    /**
     * 创建ATM波动率曲面。该曲面有期限结构，但没有smile/skew。
     * @param atmVolBuilderConfigAO {@link AtmVolBuilderConfigAO}
     * @param save 是否保存创建的波动率曲面？
     * @return 波动率曲面标识
     */
    String createAtmVolCurve(AtmVolBuilderConfigAO atmVolBuilderConfigAO, Boolean save);

    /**
     * 创建在行权价上插值的波动率曲面
     * @param interpVolBuilderConfigAO {@link InterpVolBuilderConfigAO}
     * @param save 是否保存创建的波动率曲面？
     * @return 波动率曲面标识
     */
    String createInterpStrikeVolSurface(InterpVolBuilderConfigAO interpVolBuilderConfigAO, Boolean save);

    /**
     * 创建分红/融券曲线
     * @param dividendCurveBuilderConfigAO {@link DividendCurveBuilderConfigAO}
     * @param save 是否保存创建的创建分红/融券曲线
     * @return 分红/融券曲线标识
     */
    String createDividendCurve(DividendCurveBuilderConfigAO dividendCurveBuilderConfigAO, Boolean save);

    /**
     * 创建无风险利率曲线
     * @param riskFreeCurveBuilderConfigAO {@link RiskFreeCurveBuilderConfigAO}
     * @param save 是否保存创建的无风险利率曲线
     * @return 无风险利率曲线标识
     */
    String createRiskFreeCurve(RiskFreeCurveBuilderConfigAO riskFreeCurveBuilderConfigAO, Boolean save);

    /**
     * 创建用户自定义插值模型: Model_XY
     * @param tradeId 自定义交易结构的交易ID
     * @param valuationDate 估值日
     * @param instance 收盘/日内
     * @param config {@link ModelXYBuilderConfigAO}
     * @param timezone 模型时区
     * @param save 是否保存创建的自定义模型
     * @return 自定义插值模型标识
     */
    String createModelXY(String tradeId,
                         LocalDate valuationDate,
                         InstanceEnum instance,
                         ModelXYBuilderConfigAO config,
                         ZoneId timezone,
                         boolean save);

    // model object

    /**
     * 查询模型数据
     * @param modelType 模型类型{@link ModelTypeEnum}
     * @param name 模型名称
     * @param underlyer 模型标的物
     * @param instance 日内/收盘
     * @param valuationDate 估值日
     * @param modelTimezone 时区
     * @return 模型数据
     */
    ModelDataDTO findModelData(ModelTypeEnum modelType, String name,
                               String underlyer, InstanceEnum instance,
                               LocalDate valuationDate, ZoneId modelTimezone);

    /**
     * 获得模型数据后通过quantlib反序列化
     * @param modelType 模型类型{@link ModelTypeEnum}
     * @param name 模型名称
     * @param underlyer 模型标的物
     * @param instance 日内/收盘
     * @param valuationDate 估值日
     * @param modelTimezone 时区
     * @return 反序列化的模型
     */
    String load(ModelTypeEnum modelType, String name,
                String underlyer, InstanceEnum instance,
                LocalDate valuationDate, ZoneId modelTimezone);

    /**
     * 批量加载模型
     * @param modelLocators 定位模型需要的描述{@link ModelLocator}
     * @return 反序列化的模型
     */
    //List<String> load(List<ModelLocator> modelLocators);

    /**
     * 批量加载模型
     * @param locators 定位模型需要的描述{@link ModelLocator}
     * @return 反序列化的模型
     */
    Map<ModelLocator, String> loadBatch(List<ModelLocator> locators);

    /***
     * Save a model created in quantlib by taking in its uuid and requesting its serialization from quant-service.
     * This API is mostly used in Excel when the user creates the model manually and then saves it into model-service.
     * @param qlObjectId The object uuid in quant lib
     * @param modelType Model type
     * @param modelName Model name
     * @param underlyer Model underlyer (can be null if the model is a risk free curve)
     * @param instance Model instance (intraday/close)
     * @param valuationDate Model valuation date
     * @param timezone Model timezone
     * @param modelInfo A json that records the raw input used to create the model in quant lib. Optional.
     * @return The model's ID in model-service
     */
    String save(String qlObjectId, ModelTypeEnum modelType, String modelName,
                String underlyer, InstanceEnum instance,
                LocalDate valuationDate, ZoneId timezone, JsonNode modelInfo);

    /**
     * 删除模型
     * 警告：删除操作不可逆
     * @param modelType Model type: {@link ModelTypeEnum#RISK_FREE_CURVE}, {@link ModelTypeEnum#DIVIDEND_CURVE}
     *                  and {@link ModelTypeEnum#VOL_SURFACE}
     * @param name Model name
     * @param underlyer Model underlyer (optional, null if the model is {@link ModelTypeEnum#RISK_FREE_CURVE})
     * @param instance Model instance: {@link InstanceEnum#INTRADAY} and {@link InstanceEnum#CLOSE}
     * @param valuationDate Valuation date of the model
     * @param modelTimezone Model timezone
     * @return
     */
    String delete(ModelTypeEnum modelType, String name,
                  String underlyer, InstanceEnum instance,
                  LocalDate valuationDate, ZoneId modelTimezone);

    /**
     * 按类型和标的物罗列模型
     * @param modelType 模型类型{@link ModelTypeEnum}
     * @param underlyer 模型标的物
     * @param instance 日内/收盘
     * @return 模型信息 {@link ModelDescriptionDTO}
     */
    List<ModelDescriptionDTO> list(ModelTypeEnum modelType, String underlyer, InstanceEnum instance);

    List<String> listAllModelNames(ModelTypeEnum modelTypeEnum);

    /**
     * 改变模型估值日，相同期限结果不变
     * @param modelType 模型类型{@link ModelTypeEnum}
     * @param name 模型名称
     * @param undelryer 模型标的物
     * @param instance 日内/收盘
     * @param valuationDate 估值日
     * @param newValuationDate 新估值日
     * @param modelTimeZone 时区
     * @param save 是否保存
     * @return 模型标识
     */
    String shiftModelValuationDate(ModelTypeEnum modelType, String name, String undelryer, InstanceEnum instance,
                                   LocalDate valuationDate, LocalDate newValuationDate, ZoneId modelTimeZone,
                                   boolean save);
}
