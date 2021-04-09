package tech.tongyu.bct.reference.service;

import tech.tongyu.bct.reference.dto.CompanyTypeInfoDTO;

import java.util.List;

public interface CompanyTypeInfoService {
    String SCHEMA = "referenceDataService";

    void deleteAll();

    void batchUpdateAll(List<CompanyTypeInfoDTO> infos);

    List<CompanyTypeInfoDTO> getAllByLevelTwoType(String leveltwoType);
}
