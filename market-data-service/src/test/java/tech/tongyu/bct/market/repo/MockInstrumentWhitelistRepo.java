package tech.tongyu.bct.market.repo;

import org.springframework.data.domain.Pageable;
import tech.tongyu.bct.common.jpa.MockJpaRepository;
import tech.tongyu.bct.market.dao.dbo.InstrumentWhitelist;
import tech.tongyu.bct.market.dao.repo.intel.InstrumentWhitelistRepo;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MockInstrumentWhitelistRepo extends MockJpaRepository<InstrumentWhitelist> implements InstrumentWhitelistRepo {

    public MockInstrumentWhitelistRepo() {
        super(new LinkedList<>());
    }

    @Override
    public Optional<InstrumentWhitelist> findByInstrumentId(String instrumentId) {
        return data.stream().filter(i -> i.getInstrumentId().equals(instrumentId)).findAny();
    }

    @Override
    public List<InstrumentWhitelist> findAllByOrderByInstrumentIdAsc(Pageable p) {
        return null;
    }

    @Override
    public List<InstrumentWhitelist> findByInstrumentIdInOrderByInstrumentIdAsc(List<String> instrumentIds, Pageable p) {
        return null;
    }

    @Override
    public Long countByInstrumentIdIn(List<String> instrumentIds) {
        return null;
    }

    @Override
    public List<InstrumentWhitelist> findByInstrumentIdStartingWith(String idParts) {
        return null;
    }
}
