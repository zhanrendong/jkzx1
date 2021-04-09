package tech.tongyu.bct.reference.dao.repl.intel;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.tongyu.bct.reference.dao.dbo.Margin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarginRepo extends JpaRepository<Margin, UUID> {

    Long deleteByPartyId(UUID partyId);

    Optional<Margin> findByPartyId(UUID partyId);
}
