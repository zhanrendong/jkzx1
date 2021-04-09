package tech.tongyu.bct.model.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.model.ao.*;
import tech.tongyu.bct.model.dao.dbo.ModelData;
import tech.tongyu.bct.model.dao.repl.intel.ModelDataRepo;
import tech.tongyu.bct.model.dto.ModelDataDTO;
import tech.tongyu.bct.model.dto.ModelDescriptionDTO;
import tech.tongyu.bct.model.dto.ModelLocator;
import tech.tongyu.bct.model.dto.ModelTypeEnum;
import tech.tongyu.bct.model.service.ModelService;
import tech.tongyu.bct.quant.builder.CurveBuilder;
import tech.tongyu.bct.quant.builder.CustomModelBuilder;
import tech.tongyu.bct.quant.builder.VolSurfaceBuilder;
import tech.tongyu.bct.quant.service.cache.QuantlibObjectCache;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;

@Service
public class ModelServiceImpl implements ModelService {
    private ModelDataRepo modelDataRepo;

    @Autowired
    public ModelServiceImpl(ModelDataRepo modelDataRepo) {
        this.modelDataRepo = modelDataRepo;
    }

    private LocalDate getExpiry(LocalDate val, @Nullable LocalDate expiry, @Nullable String tenor) {
        if (Objects.isNull(expiry) && !Objects.isNull(tenor)) {
            return val.plus(Period.parse("P" + tenor.toUpperCase()));
        } else if (!Objects.isNull(expiry)) {
            return expiry;
        } else {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "期限和到期日必须指定其中之一");
        }
    }

    //builder
    @Override
    @Transactional
    public String createAtmVolCurve(AtmVolBuilderConfigAO atmVolBuilderConfigAO, Boolean save) {
        LocalDate val = atmVolBuilderConfigAO.getValuationDate();
        String handle = QuantlibObjectCache.Instance.put(VolSurfaceBuilder.createAtmPwcVolSurface(val.atStartOfDay(),
                atmVolBuilderConfigAO.getUnderlyer().getQuote(),
                atmVolBuilderConfigAO.getInstruments().stream()
                        .map(i -> getExpiry(val, i.getExpiry(), i.getTenor())).collect(Collectors.toList()),
                atmVolBuilderConfigAO.getInstruments().stream()
                        .filter(AtmVolInstrumentAO::getUse)
                        .map(AtmVolInstrumentAO::getQuote).collect(Collectors.toList())));
        if (!save)
            return handle;
        JsonNode qlData = JsonUtils.mapper.valueToTree(QuantlibObjectCache.Instance.get(handle));
        String modelId = atmVolBuilderConfigAO.getModelId();
        ModelData modelData = new ModelData(atmVolBuilderConfigAO.getModelName(),
                atmVolBuilderConfigAO.getInstance(), qlData,
                atmVolBuilderConfigAO.getValuationDate(), atmVolBuilderConfigAO.getTimezone(),
                atmVolBuilderConfigAO.getUnderlyer().getInstrumentId(),
                JsonUtils.mapper.valueToTree(atmVolBuilderConfigAO),
                modelId, ModelTypeEnum.VOL_SURFACE);
        Optional<ModelData> existed = modelDataRepo.findByModelId(modelId);
        existed.ifPresent(m -> modelData.setUuid(m.getUuid()));
        modelDataRepo.save(modelData);
        return modelId;
    }

    @Override
    @Transactional
    public String createDividendCurve(DividendCurveBuilderConfigAO config, Boolean save) {
        LocalDate val = config.getValuationDate();
        String handle = QuantlibObjectCache.Instance.put(CurveBuilder.createCurveFromRates(val.atStartOfDay(),
                config.getInstruments().stream()
                        .filter(CurveInstrumentAO::getUse)
                        .map(i -> getExpiry(val, i.getExpiry(), i.getTenor()))
                        .collect(Collectors.toList()),
                config.getInstruments().stream()
                        .filter(CurveInstrumentAO::getUse)
                        .map(CurveInstrumentAO::getQuote).collect(Collectors.toList())));
        if (!save)
            return handle;
        JsonNode qlData = JsonUtils.mapper.valueToTree(QuantlibObjectCache.Instance.get(handle));
        String modelId = config.getModelId();
        ModelData modelData = new ModelData(config.getModelName(),
                config.getInstance(), qlData,
                config.getValuationDate(), config.getTimezone(),
                config.getUnderlyer(),
                JsonUtils.mapper.valueToTree(config),
                modelId, ModelTypeEnum.DIVIDEND_CURVE);
        Optional<ModelData> existed = modelDataRepo.findByModelId(modelId);
        existed.ifPresent(m -> modelData.setUuid(m.getUuid()));
        modelDataRepo.save(modelData);
        return modelId;
    }

    @Override
    @Transactional
    public String createRiskFreeCurve(RiskFreeCurveBuilderConfigAO config, Boolean save) {
        LocalDate val = config.getValuationDate();
        String handle = QuantlibObjectCache.Instance.put(CurveBuilder.createCurveFromRates(val.atStartOfDay(),
                config.getInstruments().stream()
                        .filter(CurveInstrumentAO::getUse)
                        .map(i -> getExpiry(val, i.getExpiry(), i.getTenor()))
                        .collect(Collectors.toList()),
                config.getInstruments().stream()
                        .filter(CurveInstrumentAO::getUse)
                        .map(CurveInstrumentAO::getQuote).collect(Collectors.toList())));
        if (!save)
            return handle;
        JsonNode qlData = JsonUtils.mapper.valueToTree(QuantlibObjectCache.Instance.get(handle));
        String modelId = config.getModelId();
        ModelData modelData = new ModelData(config.getModelName(),
                config.getInstance(), qlData,
                config.getValuationDate(), config.getTimezone(),
                null,
                JsonUtils.mapper.valueToTree(config),
                modelId, ModelTypeEnum.RISK_FREE_CURVE);
        Optional<ModelData> existed = modelDataRepo.findByModelId(modelId);
        existed.ifPresent(m -> modelData.setUuid(m.getUuid()));
        modelDataRepo.save(modelData);
        return modelId;
    }

    @Override
    @Transactional
    public String createInterpStrikeVolSurface(InterpVolBuilderConfigAO config, Boolean save) {
        LocalDate val = config.getValuationDate();
        double spot = config.getUnderlyer().getQuote();
        List<InterpVolInstrumentRowAO> volGrid = config.getVolGrid().stream()
                .map(r -> {
                    if (Objects.isNull(r.getExpiry())) {
                        LocalDate exp = val.plus(Period.parse("P" + r.getTenor()));
                        return new InterpVolInstrumentRowAO(r.getTenor(), exp, r.getVols());
                    } else {
                        return r;
                    }
                })
                .sorted(Comparator.comparing(InterpVolInstrumentRowAO::getExpiry))
                .collect(Collectors.toList());
        List<LocalDate> expiries = volGrid.stream()
                .map(InterpVolInstrumentRowAO::getExpiry)
                .collect(Collectors.toList());
        List<Double> strikes = volGrid.get(0).getVols().stream()
                .map(i -> {
                    if (Objects.isNull(i.getStrike())) {
                        return i.getPercent() * config.getUnderlyer().getQuote();
                    } else {
                        return i.getStrike();
                    }
                })
                .sorted()
                .collect(Collectors.toList());
        List<List<Double>> vols = volGrid.stream()
                .map(row -> row.getVols().stream()
                        .sorted(Comparator.comparing(i -> {
                            if (Objects.isNull(i.getStrike())) {
                                return i.getPercent() * config.getUnderlyer().getQuote();
                            } else {
                                return i.getStrike();
                            }
                        }))
                        .map(InterpVolInstrumentRowAO.VolInstrument::getQuote).collect(Collectors.toList())
                )
                .collect(Collectors.toList());
        double daysInYear = config.getDaysInYear();
        VolSurfaceBuilder.InterpolatedVolSurfaceConfig volSurfaceConfig =
                new VolSurfaceBuilder.InterpolatedVolSurfaceConfig();

        volSurfaceConfig.daysInYear = daysInYear;
        volSurfaceConfig.useVolCalendar = config.isUseVolCalendar();
        volSurfaceConfig.volCalendar = config.getVolCalendar();
        volSurfaceConfig.calendars = config.getCalendars();

        String handle = QuantlibObjectCache.Instance.put(VolSurfaceBuilder.createInterpolatedStrikeSurface(
                val.atStartOfDay(), spot, expiries, strikes, vols, volSurfaceConfig));
        if (!save)
            return handle;
        JsonNode qlData = JsonUtils.mapper.valueToTree(QuantlibObjectCache.Instance.get(handle));
        String modelId = config.getModelId();
        ModelData modelData = new ModelData(config.getModelName(),
                config.getInstance(), qlData,
                config.getValuationDate(), config.getTimezone(),
                config.getUnderlyer().getInstrumentId(),
                JsonUtils.mapper.valueToTree(config),
                modelId, ModelTypeEnum.VOL_SURFACE);
        Optional<ModelData> existed = modelDataRepo.findByModelId(modelId);
        existed.ifPresent(m -> modelData.setUuid(m.getUuid()));
        ModelData saved = modelDataRepo.save(modelData);
        return modelId;
    }

    @Override
    public String createModelXY(String tradeId,
                                LocalDate valuationDate,
                                InstanceEnum instance,
                                ModelXYBuilderConfigAO config,
                                ZoneId timezone,
                                boolean save) {
        String modelName = ModelTypeEnum.MODEL_XY.name() + "_" + tradeId;
        String modelId = modelName + "|" + instance + "|"
                + valuationDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "|"
                + timezone;
        List<ModelXYBuilderConfigAO.Slice> sorted = config.getSlices().stream()
                .sorted(Comparator.comparing(ModelXYBuilderConfigAO.Slice::getTimestamp))
                .collect(Collectors.toList());
        List<List<Double>> spots = sorted.stream()
                .map(ModelXYBuilderConfigAO.Slice::getSpots)
                .collect(Collectors.toList());
        List<List<Double>> prices = sorted.stream()
                .map(ModelXYBuilderConfigAO.Slice::getPrices)
                .collect(Collectors.toList());
        List<List<Double>> deltas = sorted.stream()
                .map(ModelXYBuilderConfigAO.Slice::getDeltas)
                .collect(Collectors.toList());
        List<List<Double>> gammas = sorted.stream()
                .map(ModelXYBuilderConfigAO.Slice::getGammas)
                .collect(Collectors.toList());
        List<List<Double>> vegas = sorted.stream()
                .map(ModelXYBuilderConfigAO.Slice::getVegas)
                .collect(Collectors.toList());
        List<List<Double>> thetas = sorted.stream()
                .map(ModelXYBuilderConfigAO.Slice::getThetas)
                .collect(Collectors.toList());
        // at least 3 spots to interpolate
        for (List<Double> s : spots) {
            if (s.size() < 3) {
                throw new CustomException("自定义模型标的物价格数量少于3.");
            }
        }

        String handle = QuantlibObjectCache.Instance.put(
                CustomModelBuilder.createModelXY(sorted.stream()
                        .map(ModelXYBuilderConfigAO.Slice::getTimestamp)
                        .collect(Collectors.toList()), spots, prices, deltas, gammas, vegas, thetas));
        if (!save) {
            return handle;
        }
        JsonNode qlData = JsonUtils.mapper.valueToTree(QuantlibObjectCache.Instance.get(handle));
        ModelData modelData = new ModelData(modelName,
                instance, qlData,
                valuationDate, timezone,
                null,
                JsonUtils.mapper.valueToTree(config),
                modelId, ModelTypeEnum.MODEL_XY);
        Optional<ModelData> existed = modelDataRepo.findByModelId(modelId);
        existed.ifPresent(m -> modelData.setUuid(m.getUuid()));
        ModelData saved = modelDataRepo.save(modelData);
        return modelId;
    }

    // model object
    private ModelData find(ModelTypeEnum modelType, String name,
                           String underlyer, InstanceEnum instance,
                           LocalDate valuationDate, ZoneId modelTimezone) {
        List<ModelData> volDataList;
        if (modelType != ModelTypeEnum.RISK_FREE_CURVE && modelType != ModelTypeEnum.MODEL_XY){
            volDataList = modelDataRepo.findModelData(modelType.name(), name, underlyer,
                    instance.name(), valuationDate, modelTimezone);
        }else {
            /*if (!Objects.isNull(underlyer))
                throw new CustomException("a risk free curve cannot have an underlyer");*/
            volDataList = modelDataRepo.findModelDataWithoutUnderlyer(
                    modelType.name(), name, instance.name(), valuationDate, modelTimezone);
        }
        if (volDataList.size() == 0)
            throw new CustomException(String.format("failed to find model data for %s, %s", name, underlyer));
        return volDataList.get(0);
    }

    @Override
    public ModelDataDTO findModelData(ModelTypeEnum modelType, String name,
                                      String underlyer, InstanceEnum instance,
                                      LocalDate valuationDate, ZoneId modelTimezone) {

        ModelData m = find(modelType, name, underlyer, instance, valuationDate, modelTimezone);
        return new ModelDataDTO(m.getModelName(), m.getUnderlyer(),
                m.getInstance(), m.getValuationDate(),
                m.getModelTimezone(), m.getModelData(), m.getModelInfo(), m.getModelId(), m.getModelType());
    }

    @Override
    public String load(ModelTypeEnum modelType, String name,
                       String underlyer, InstanceEnum instance,
                       LocalDate valuationDate, ZoneId modelTimezone) {
        ModelDataDTO data = findModelData(modelType, name, underlyer, instance,
                valuationDate, modelTimezone);
        QuantlibObjectCache.Instance.create(data.getModelId(), data.getModelData());
        return data.getModelId();
    }

    /*@Override
    public List<String> load(List<ModelLocator> modelLocators) {
        List<String> results = new ArrayList<>();
        for (ModelLocator locator : modelLocators) {
            try {
                String handle = load(locator.getModelType(), locator.getModelName(),
                        locator.getUnderlyer(), locator.getInstance(),
                        locator.getValuationDate(), locator.getModelTimezone());
                results.add(handle);
            } catch (Exception e) {
                //results.add(null);
            }
        }
        return results;
    }*/

    @Override
    public Map<ModelLocator, String> loadBatch(List<ModelLocator> locators) {
        return locators.parallelStream().map(locator -> {
            String handle = load(locator.getModelType(), locator.getModelName(),
                    locator.getUnderlyer(), locator.getInstance(),
                    locator.getValuationDate(), locator.getModelTimezone());
            return new ImmutablePair<ModelLocator, String>(locator, handle);
        }).collect(Collectors.toMap(key -> key.getLeft(), key -> key.getRight()));
    }

    @Override
    public String save(String qlObjectId, ModelTypeEnum modelType, String modelName,
                       String underlyer, InstanceEnum instance,
                       LocalDate valuationDate, ZoneId modelTimezone,
                       JsonNode modelInfo) {
        if (modelType == ModelTypeEnum.RISK_FREE_CURVE && !Objects.isNull(underlyer))
            throw new CustomException("risk free curve cannot have underlyer");
        JsonNode modelData = JsonUtils.mapper.valueToTree(QuantlibObjectCache.Instance.getMayThrow(qlObjectId));
        String modelId = modelName + "|" + instance.toString()
                + "|" + valuationDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "|"
                + modelTimezone;
        ModelData data = new ModelData(modelName, instance,
                modelData, valuationDate, modelTimezone,
                underlyer, modelInfo, modelId, modelType);
        modelDataRepo.findByModelId(modelId).ifPresent(m -> data.setUuid(m.getUuid()));
        modelDataRepo.save(data);
        return modelId;
    }

    @Override
    @Transactional
    public String delete(ModelTypeEnum modelType, String name,
                         String underlyer, InstanceEnum instance,
                         LocalDate valuationDate, ZoneId modelTimezone) {
        if (modelType == ModelTypeEnum.RISK_FREE_CURVE && !Objects.isNull(underlyer))
            throw new CustomException("risk free curve cannot have underlyer");
        ModelData m = find(modelType, name, underlyer, instance, valuationDate, modelTimezone);
        modelDataRepo.delete(m);
        return m.getModelId();
    }

    @Override
    public List<ModelDescriptionDTO> list(ModelTypeEnum modelType, String underlyer, InstanceEnum instance) {
        switch (modelType) {
            case VOL_SURFACE:
            case DIVIDEND_CURVE:
            case MODEL_XY:
                return modelDataRepo
                        .findByModelTypeAndUnderlyerAndInstance(modelType, underlyer, instance)
                        .stream()
                        .sorted(comparing(ModelData::getModelName)
                                .thenComparing(ModelData::getValuationDate, reverseOrder())
                        )
                        .map(m -> new ModelDescriptionDTO(m.getModelName(), m.getUnderlyer(),
                                m.getInstance(), m.getValuationDate(),
                                m.getModelTimezone(), m.getModelId(), m.getModelType()))
                        .collect(Collectors.toList());
            case RISK_FREE_CURVE:
                return modelDataRepo
                        .findByModelTypeAndInstance(ModelTypeEnum.RISK_FREE_CURVE, instance)
                        .stream()
                        .sorted(comparing(ModelData::getModelName)
                                .thenComparing(ModelData::getValuationDate, reverseOrder())
                        )
                        .map(m -> new ModelDescriptionDTO(m.getModelName(), null,
                                m.getInstance(), m.getValuationDate(),
                                m.getModelTimezone(), m.getModelId(), ModelTypeEnum.RISK_FREE_CURVE))
                        .collect(Collectors.toList());
            default:
                throw new CustomException("unknown model type");
        }
    }

    @Override
    public List<String> listAllModelNames(ModelTypeEnum modelTypeEnum) {
        return modelDataRepo.findModelNames(modelTypeEnum);
    }

    @Override
    @Transactional
    public String shiftModelValuationDate(ModelTypeEnum modelType, String name, String undelryer, InstanceEnum instance,
                                          LocalDate valuationDate, LocalDate newValuationDate, ZoneId modelTimeZone,
                                          boolean save) {
        ModelDataDTO modelDataDTO = findModelData(modelType, name, undelryer, instance, valuationDate, modelTimeZone);
        JsonNode modelInfo = modelDataDTO.getModelInfo();
        ModelServiceImpl modelService = (ModelServiceImpl) AopContext.currentProxy();
        if (modelInfo instanceof NullNode) {
            throw new CustomException("Model info of " + name +
                    (modelType == ModelTypeEnum.RISK_FREE_CURVE ? "" : "|" + undelryer) + " is null.");
        }
        if (modelType == ModelTypeEnum.RISK_FREE_CURVE) {
            List<Map<String, Object>> instrumentsData =
                    JsonUtils.mapper.convertValue(modelInfo.get("instruments"), List.class);
            List<CurveInstrumentAO> instruments = instrumentsData.stream()
                    .map(i -> JsonUtils.mapper.convertValue(i, CurveInstrumentAO.class))
                    .collect(Collectors.toList());
            RiskFreeCurveBuilderConfigAO config = new RiskFreeCurveBuilderConfigAO(name, newValuationDate,
                    modelTimeZone, instance, instruments);
            return modelService.createRiskFreeCurve(config, save);
        } else if (modelType == ModelTypeEnum.DIVIDEND_CURVE) {
            List<Map<String, Object>> instrumentsData =
                    JsonUtils.mapper.convertValue(modelInfo.get("instruments"), List.class);
            List<CurveInstrumentAO> instruments = instrumentsData.stream()
                    .map(i -> JsonUtils.mapper.convertValue(i, CurveInstrumentAO.class))
                    .collect(Collectors.toList());
            DividendCurveBuilderConfigAO config = new DividendCurveBuilderConfigAO(name, newValuationDate,
                    modelTimeZone, instance, undelryer, instruments);
            return modelService.createDividendCurve(config, save);
        } else if (modelType == ModelTypeEnum.VOL_SURFACE) {
            List<Map<String, Object>> volGridData = JsonUtils.mapper.convertValue(modelInfo.get("volGrid"), List.class);
            List<InterpVolInstrumentRowAO> volGrid = volGridData.stream()
                    .map(i -> JsonUtils.mapper.convertValue(i, InterpVolInstrumentRowAO.class))
                    .collect(Collectors.toList());
            List<String> calendars = JsonUtils.mapper.convertValue(modelInfo.get("calendars"), List.class);
            InterpVolBuilderConfigAO config = new InterpVolBuilderConfigAO(name, newValuationDate, modelTimeZone,
                    instance, JsonUtils.mapper.convertValue(modelInfo.get("underlyer"), VolUnderlyerAO.class),
                    volGrid, modelInfo.get("daysInYear").asDouble(), modelInfo.get("useCalendarForTau").asBoolean(),
                    calendars, modelInfo.get("useVolCalendar").asBoolean(), modelInfo.get("volCalendar").asText());
            return modelService.createInterpStrikeVolSurface(config, save);
        } else {
            throw new CustomException("Cannot shift valuation date of model type " + modelType.toString() + ".");
        }
    }
}
