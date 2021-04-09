package tech.tongyu.bct.reference.service;

import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.dto.SalesDTO;

import java.util.List;

public interface PartyService {
    String SECHEMA = "referenceDataService";

    void deleteByLegalName(String legalName);

    PartyDTO createParty(PartyDTO partyDTO);

    PartyDTO deleteParty(String partyId);

    PartyDTO updateParty(PartyDTO partyDTO);

    PartyDTO getByLegalName(String legalName);

    PartyDTO getByMasterAgreementId(String masterAgreementId);


    SalesDTO getSalesByLegalName(String legalName);

    List<PartyDTO> listParty(PartyDTO partyDTO);

    Boolean isPartyExistsByLegalName(String legalName);

    List<String> listBySimilarLegalName(String similarLegalName, boolean listAll);

    List<String> searchMasterAgreementIdByKey(String key);

    List<PartyDTO> listAllParties();
}
