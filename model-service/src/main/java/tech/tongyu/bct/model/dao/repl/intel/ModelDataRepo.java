package tech.tongyu.bct.model.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.tongyu.bct.market.dto.InstanceEnum;
import tech.tongyu.bct.model.dao.dbo.ModelData;
import tech.tongyu.bct.model.dto.ModelTypeEnum;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ModelDataRepo extends JpaRepository<ModelData, UUID> {
    @Query("select distinct m.modelName from ModelData m where m.modelType=:modelType")
    List<String> findModelNames(ModelTypeEnum modelType);

    @Query(value = "select m.* from model_service.model_data m  " +
            "join (SELECT max(valuation_date) as valuation_date from model_service.model_data " +
            "where model_type=:modelType and model_name=:modelName and underlyer=:underlyer and instance=:instance " +
            "and model_timezone=:modelTimezone and valuation_date<=:valuationDate ) t  " +
            "on  m.model_type=:modelType and m.model_name=:modelName and m.underlyer=:underlyer and m.instance=:instance " +
            "and m.model_timezone=:modelTimezone  AND m.valuation_date = t.valuation_date", nativeQuery = true)
    List<ModelData> findModelData(String modelType, String modelName,
                                  String underlyer, String instance,
                                  LocalDate valuationDate, ZoneId modelTimezone);

    @Query(value = "select m.* from model_service.model_data m  " +
            "join (SELECT max(valuation_date) as valuation_date from model_service.model_data " +
            "where model_type=:modelType and model_name=:modelName and instance=:instance " +
            "and model_timezone=:modelTimezone and valuation_date<=:valuationDate ) t  " +
            "on  m.model_type=:modelType and m.model_name=:modelName and m.instance=:instance " +
            "and m.model_timezone=:modelTimezone  AND m.valuation_date = t.valuation_date", nativeQuery = true)
    List<ModelData> findModelDataWithoutUnderlyer(String modelType, String modelName, String instance,
                                                  LocalDate valuationDate, ZoneId modelTimezone);

    List<ModelData> findByModelTypeAndUnderlyerAndInstance(ModelTypeEnum modelType,
                                                           String underlyer, InstanceEnum instance);

    List<ModelData> findByModelTypeAndInstance(ModelTypeEnum modelType, InstanceEnum instance);

    Optional<ModelData> findByModelId(String modelId);
}
