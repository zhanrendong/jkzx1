package tech.tongyu.bct.reference.service.impl;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.TimeUtils;
import tech.tongyu.bct.reference.dao.dbo.Authorizer;
import tech.tongyu.bct.reference.dao.dbo.Margin;
import tech.tongyu.bct.reference.dao.dbo.Party;
import tech.tongyu.bct.reference.dao.repl.intel.AuthorizerRepo;
import tech.tongyu.bct.reference.dao.repl.intel.MarginRepo;
import tech.tongyu.bct.reference.dao.repl.intel.PartyRepo;
import tech.tongyu.bct.reference.dao.repl.intel.SalesRepo;
import tech.tongyu.bct.reference.dto.AuthorizerDTO;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.dto.PartyStatusEnum;
import tech.tongyu.bct.reference.dto.SalesDTO;
import tech.tongyu.bct.reference.service.PartyService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PartyServiceImpl implements PartyService {

    private SalesRepo salesRepo;
    private PartyRepo partyRepo;
    private MarginRepo marginRepo;
    private AuthorizerRepo authorizerRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    public PartyServiceImpl(SalesRepo salesRepo,
                            PartyRepo partyRepo,
                            MarginRepo marginRepo,
                            AuthorizerRepo authorizerRepo) {
        this.salesRepo = salesRepo;
        this.partyRepo = partyRepo;
        this.marginRepo = marginRepo;
        this.authorizerRepo = authorizerRepo;
    }

    @Override
    @Transactional
    public void deleteByLegalName(String legalName) {
        if (StringUtils.isBlank(legalName)) {
            throw new CustomException("请输入待删除交易对手legalName");
        }
        Party party = partyRepo.findByLegalName(legalName)
                .orElseThrow(() -> new CustomException(String.format("交易对手[%s]数据不存在", legalName)));
        accountService.deleteByLegalName(legalName);
        marginRepo.deleteByPartyId(party.getUuid());
        partyRepo.deleteByLegalName(legalName);
    }

    @Override
    @Transactional
    public PartyDTO createParty(PartyDTO partyDTO) {
        if (partyRepo.existsByLegalName(partyDTO.getLegalName())) {
            throw new CustomException(String.format("已经存在开户名称为[%s]的数据记录", partyDTO.getLegalName()));
        }
        if (partyRepo.existsByMasterAgreementId(partyDTO.getMasterAgreementId())) {
            throw new CustomException(String.format("已经存在主协议编号为[%s]的数据记录", partyDTO.getMasterAgreementId()));
        }
        if (!salesRepo.existsBySalesName(partyDTO.getSalesName())) {
            throw new CustomException(String.format("找不到销售名称为[%s]的数据记录", partyDTO.getSalesName()));
        }

        accountService.saveAccount(partyDTO.getLegalName());

        authorizerRepo.saveAll(getAuthorizer(partyDTO));
        Party party = convert(partyDTO);
        party = this.partyRepo.save(party);
        Margin margin = new Margin(party.getUuid(), BigDecimal.ZERO);
        margin = this.marginRepo.save(margin);
        party.setMaintenanceMargin(margin);
        PartyDTO partyDto = convert(party);
        partyDto.setAuthorizers(getAuthorizerDTO(party.getLegalName()));
        return partyDto;
    }

    @Override
    @Transactional
    public PartyDTO deleteParty(String partyId) {
        if (StringUtils.isBlank(partyId)) {
            throw new CustomException("请输入待删除交易对手唯一标识uuid");
        }
        UUID uuid = UUID.fromString(partyId);
        Party party = partyRepo.findById(uuid).orElseThrow(() ->
                new CustomException(ErrorCode.MISSING_ENTITY, String.format("交易对手Id[%s],记录不存在", partyId)));
        accountService.deleteByLegalName(party.getLegalName());
        marginRepo.deleteByPartyId(uuid);
        partyRepo.delete(party);
        return convert(party);
    }

    @Override
    @Transactional
    public PartyDTO updateParty(PartyDTO partyDTO) {
        if (StringUtils.isBlank(partyDTO.getUuid())) {
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "partId为空，请检查");
        }

        Party oldParty = partyRepo.findById(UUID.fromString(partyDTO.getUuid()))
                .orElseThrow(() -> new CustomException(ErrorCode.MISSING_ENTITY,
                        String.format("找不到Id为[%s]的记录", partyDTO.getUuid())));

        authorizerRepo.deleteAllByPartyLegalName(partyDTO.getLegalName());
        authorizerRepo.saveAll(getAuthorizer(partyDTO));

        Party newParty = convert(partyDTO);
        newParty.setMaintenanceMargin(oldParty.getMaintenanceMargin());

        Party saved = this.partyRepo.save(newParty);
        PartyDTO partyDto = convert(saved);
        partyDto.setAuthorizers(getAuthorizerDTO(saved.getLegalName()));
        return partyDto;
    }

    @Override
    public PartyDTO getByLegalName(String legalName) {
        if (StringUtils.isBlank(legalName)) {
            throw new CustomException("请输入交易对手legalName");
        }
        PartyDTO partyDTO = partyRepo.findByLegalName(legalName)
                .map(this::convert)
                .orElseThrow(() -> new CustomException(String.format("交易对手:[%s],信息查询不存在", legalName)));
        partyDTO.setAuthorizers(getAuthorizerDTO(legalName));
        return partyDTO;
    }

    @Override
    public PartyDTO getByMasterAgreementId(String masterAgreementId) {
        if (StringUtils.isBlank(masterAgreementId)) {
            throw new CustomException("请输入主协议编号masterAgreementId");
        }
        return partyRepo.findByMasterAgreementId(masterAgreementId)
                .map(this::convert)
                .orElseThrow(() -> new CustomException(String.format("主协议编号:[%s],信息查询不存在", masterAgreementId)));
    }

    @Override
    public SalesDTO getSalesByLegalName(String legalName) {
        if (StringUtils.isBlank(legalName)) {
            throw new CustomException("请输入交易对手legalName");
        }
        return partyRepo.findByLegalName(legalName)
                .map(party -> {
                    SalesDTO salesDto = new SalesDTO();
                    salesDto.setBranchName(party.getBranchName());
                    salesDto.setSalesName(party.getSalesName());
                    salesDto.setSubsidiaryName(party.getSubsidiaryName());
                    return salesDto;

                }).orElseThrow(() -> new CustomException(String.format("交易对手:[%s],信息查询不存在", legalName)));
    }

    @Override
    public List<PartyDTO> listParty(PartyDTO partyDTO) {
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();

        List<Party> partys = this.partyRepo.findAll(Example.of(convert(partyDTO), exampleMatcher));
        return partys.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Boolean isPartyExistsByLegalName(String legalName) {
        if (StringUtils.isBlank(legalName)) {
            throw new CustomException("请输入交易对手legalName");
        }
        return partyRepo.existsByLegalName(legalName);
    }

    @Override
    // list all is true: list all parties which meet the search criteria
    // list all is false: list all parties which meet the search criteria and party status is normal
    public List<String> listBySimilarLegalName(String similarLegalName, boolean listAll) {
        return partyRepo.findAllByLegalNameContaining(similarLegalName)
                .stream()
                .filter(party -> listAll || party.getPartyStatus() == PartyStatusEnum.NORMAL)
                .map(party -> new Pair<>(party.getLegalName().equals(similarLegalName) ? -1 : party.getLegalName().indexOf(similarLegalName), party.getLegalName()))
                .sorted(Comparator.comparing(Pair::getKey))
                .map(Pair::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> searchMasterAgreementIdByKey(String key) {
        return partyRepo.findAllByMasterAgreementIdContaining(key).stream().map(Party::getMasterAgreementId).collect(Collectors.toList());
    }

    @Override
    public List<PartyDTO> listAllParties() {
        return partyRepo.findAll().stream().map(this::convert).collect(Collectors.toList());
    }

    private Party convert(PartyDTO partyDTO) {
        Party party = new Party();
        String uuid = partyDTO.getUuid();
        party.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));
        BeanUtils.copyProperties(partyDTO, party);
        return party;
    }

    private PartyDTO convert(Party party) {
        UUID uuid = party.getUuid();
        PartyDTO partyDto = new PartyDTO();
        BeanUtils.copyProperties(party, partyDto);
        partyDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());
        partyDto.setCreatedAt(Objects.isNull(party.getCreatedAt()) ? null :
                TimeUtils.instantTransToLocalDateTime(party.getCreatedAt()));
        partyDto.setUpdatedAt(Objects.isNull(party.getUpdatedAt()) ? null :
                TimeUtils.instantTransToLocalDateTime(party.getUpdatedAt()));
        partyDto.setPartyStatus(Objects.isNull(party.getPartyStatus()) ? PartyStatusEnum.NORMAL :
                party.getPartyStatus());

        return partyDto;
    }

    private Collection<Authorizer> getAuthorizer(PartyDTO partyDTO) {
        String partyLegalName = partyDTO.getLegalName();
        return partyDTO.getAuthorizers()
                .stream()
                .map(p -> AuthorizerDTOToAuthorizer(p, partyLegalName))
                .collect(Collectors.toSet());
    }

    private Collection<AuthorizerDTO> getAuthorizerDTO(String partyLegalName) {
        return authorizerRepo.findAllByPartyLegalName(partyLegalName)
                .stream()
                .map(a -> AuthorizerToAuthorizerDTO(a))
                .collect(Collectors.toSet());
    }

    private Authorizer AuthorizerDTOToAuthorizer(AuthorizerDTO authorizerDTO, String partyLegalName) {
        return new Authorizer(
                authorizerDTO.getTradeAuthorizerName(),
                authorizerDTO.getTradeAuthorizerIdNumber(),
                authorizerDTO.getTradeAuthorizerIdExpiryDate(),
                authorizerDTO.getTradeAuthorizerPhone(),
                partyLegalName
        );
    }

    private AuthorizerDTO AuthorizerToAuthorizerDTO(Authorizer authorizer) {
        return new AuthorizerDTO(
                authorizer.getTradeAuthorizerName(),
                authorizer.getTradeAuthorizerIdNumber(),
                authorizer.getTradeAuthorizerIdExpiryDate(),
                authorizer.getTradeAuthorizerPhone()
        );
    }
}
