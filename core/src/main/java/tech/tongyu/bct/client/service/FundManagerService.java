package tech.tongyu.bct.client.service;

import tech.tongyu.bct.client.dto.FundEventRecordDTO;

import java.time.LocalDate;
import java.util.List;

public interface FundManagerService {

    FundEventRecordDTO createFundTransRecord(FundEventRecordDTO fundEventRecordDto);

    FundEventRecordDTO updateFundTransRecord(FundEventRecordDTO fundEventRecordDto);

    void deleteFundTransRecord(String uuid);

    List<FundEventRecordDTO> findAllFundTransRecord();

    List<FundEventRecordDTO> findByClientId(String clientId);

    List<FundEventRecordDTO> search(FundEventRecordDTO recordDto, LocalDate startDate, LocalDate endDate);
}
