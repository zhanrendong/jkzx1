package tech.tongyu.bct.market.service;

import tech.tongyu.bct.market.dto.CorrelationDTO;

import java.util.List;
import java.util.Optional;


public interface CorrelationService {
    Optional<CorrelationDTO> correlation(String instrumentId1, String instrumentId2);
    List<List<Double>> correlationMatrix(List<String> instrumentIds);
    CorrelationDTO save(String instrumentId1, String instrumentId2, double correlation);
    List<CorrelationDTO> delete(String instrumentId1, String instrumentId2);
}
