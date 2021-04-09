package tech.tongyu.bct.document.service;

import tech.tongyu.bct.document.dto.EmailConfigInfoDTO;

public interface EmailService {

    EmailConfigInfoDTO findCurrentEmailConfigInfo();

    void emlSaveOrUpdateEmailConfigInfo(String emailAddress, String emailPassword, String emailServerHost, String emailServerPort, String createdBy);

    void emlSendValuationReport(String tos, String ccs, String bccs, String valuationReportId) throws Exception;

    void emlSendSettleReport(String tos, String tradeId, String positionId, String partyName) throws Exception;

    void emlSendSupplementaryAgreementReport(String tos, String tradeId, String partyName, String marketInterruptionMessage, String earlyTerminationMessage) throws Exception;
}
