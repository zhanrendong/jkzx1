package tech.tongyu.bct.market.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.market.dao.dbo.InstrumentWhitelist;
import tech.tongyu.bct.market.dao.repo.intel.InstrumentWhitelistRepo;
import tech.tongyu.bct.market.dto.InstrumentWhitelistDTO;
import tech.tongyu.bct.market.service.InstrumentWhitelistService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InstrumentWhitelistServiceImpl implements InstrumentWhitelistService {
    private static Logger logger = LoggerFactory.getLogger(InstrumentWhitelistServiceImpl.class);

    InstrumentWhitelistRepo instrumentWhitelistRepo;

    @Autowired
    public InstrumentWhitelistServiceImpl(InstrumentWhitelistRepo instrumentWhitelistRepo) {
        this.instrumentWhitelistRepo = instrumentWhitelistRepo;
    }

    @Override
    public Optional<InstrumentWhitelistDTO> getInstrumentWhitelist(String instrumentId) {
        return instrumentWhitelistRepo.findByInstrumentId(instrumentId).map(this::toDTO);
    }

    @Override
    public List<InstrumentWhitelistDTO> listInstrumentWhitelist() {
        return instrumentWhitelistRepo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Optional<InstrumentWhitelistDTO> deleteInstrumentWhitelist(String instrumentId) {
        return instrumentWhitelistRepo.findByInstrumentId(instrumentId).map(i -> {
            logger.debug(String.format("删除标的物%s的白名单记录", instrumentId));
            InstrumentWhitelistDTO dto = toDTO(i);
            instrumentWhitelistRepo.delete(i);
            return dto;
        });
    }

    @Override
    public Boolean deleteAllInstrumentWhitelist() {
        instrumentWhitelistRepo.deleteAll();
        return true;
    }

    @Override
    public InstrumentWhitelistDTO saveInstrumentWhitelist(String venueCode, String instrumentId, Double notionalLimit) {
        return instrumentWhitelistRepo.findByInstrumentId(instrumentId)
                .map(i -> {
                    logger.debug(String.format("标的物%s白名单已存在，将更新其名义本金上限至%s", instrumentId, notionalLimit));
                    if (venueCode != null) i.setVenueCode(venueCode);
                    if (notionalLimit != null) i.setNotionalLimit(notionalLimit);
                    InstrumentWhitelist newInstrumentWhitelist = instrumentWhitelistRepo.save(i);
                    return toDTO(newInstrumentWhitelist);
                })
                .orElseGet(() -> {
                    logger.debug(String.format("标的物%s白名单不存在，将新建一条白名单记录，其名义本金上限为%s", instrumentId, notionalLimit));
                    InstrumentWhitelist newInstrumentWhitelist = new InstrumentWhitelist(venueCode, instrumentId, notionalLimit);
                    newInstrumentWhitelist = instrumentWhitelistRepo.save(newInstrumentWhitelist);
                    return toDTO(newInstrumentWhitelist);
                });
    }

    @Override
    public List<InstrumentWhitelistDTO> listInstrumentWhitelistPaged(Pageable page) {
        return instrumentWhitelistRepo.findAllByOrderByInstrumentIdAsc(page)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Long countInstrumentWhitelist() {
        return instrumentWhitelistRepo.count();
    }

    @Override
    public List<InstrumentWhitelistDTO> listInstrumentWhitelistPaged(List<String> instrumentIds, Pageable page) {
        return instrumentWhitelistRepo.findByInstrumentIdInOrderByInstrumentIdAsc(instrumentIds, page)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public Long countInstrumentWhitelist(List<String> instrumentIds) {
        return instrumentWhitelistRepo.countByInstrumentIdIn(instrumentIds);
    }

    @Override
    public List<InstrumentWhitelistDTO> searchInstruments(String instrumentIdPart) {
        return instrumentWhitelistRepo
                .findByInstrumentIdStartingWith(instrumentIdPart)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    InstrumentWhitelistDTO toDTO(InstrumentWhitelist instrumentWhitelist) {
        return new InstrumentWhitelistDTO(instrumentWhitelist.getVenueCode(),
                instrumentWhitelist.getInstrumentId(), instrumentWhitelist.getNotionalLimit());
    }
}
