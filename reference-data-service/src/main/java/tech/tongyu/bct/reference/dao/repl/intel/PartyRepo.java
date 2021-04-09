package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.Party;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PartyRepo extends JpaRepository<Party, UUID> {

    void deleteByLegalName(String legalName);

    Optional<Party> findByLegalName(String legalName);

    Optional<Party> findByMasterAgreementId(String masterAgreementId);

    boolean existsByLegalName(String legalName);

    boolean existsByMasterAgreementId(String masterAgreementId);

    List<Party> findAllByLegalNameContaining(String similarLegalName);

    List<Party> findAllByLegalNameIn(List<String> legalNames);

    List<Party> findAllByMasterAgreementIdContaining(String masterAgreementId);
}
