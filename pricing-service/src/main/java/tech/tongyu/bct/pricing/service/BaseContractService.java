package tech.tongyu.bct.pricing.service;

import tech.tongyu.bct.pricing.dto.BaseContractDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BaseContractService {
    /*BaseContractDTO save(String positionId, String baseContractId,
                         String hedingContractId, LocalDate baseContractValidStart);*/
    Optional<BaseContractDTO> find(String positionId, LocalDate valuationDate);
    List<BaseContractDTO> list(LocalDate valuationDate);
    Map<String, BaseContractDTO> buildLookupTable(List<String> positionIds, LocalDate valuationDate);
    List<BaseContractDTO> deleteByPositionId(String positionId);
    List<BaseContractDTO> createOrUpdate(List<BaseContractDTO> baseContractDTOS, LocalDate valuationDate);
}
