package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.Authorizer;

import java.util.Collection;
import java.util.UUID;

public interface AuthorizerRepo extends JpaRepository<Authorizer, UUID> {

    Collection<Authorizer> findAllByPartyLegalName(String partyLegalName);

    void deleteAllByPartyLegalName(String partyLegalName);
}
