package tech.tongyu.bct.reference.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.client.dto.AccountDTO;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.reference.dao.dbo.Margin;
import tech.tongyu.bct.reference.dao.dbo.Party;
import tech.tongyu.bct.reference.dao.repl.intel.MarginRepo;
import tech.tongyu.bct.reference.dao.repl.intel.PartyRepo;
import tech.tongyu.bct.reference.dto.MarginCallStatus;
import tech.tongyu.bct.reference.dto.MarginDTO;
import tech.tongyu.bct.reference.service.MarginService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 保证金服务
 * @author hangzhi
 */
@Service
public class MarginServiceImpl implements MarginService {

    private final AccountService accountService;

    private final PartyRepo partyRepo;

    private final MarginRepo marginRepo;

    @Autowired
    public MarginServiceImpl(AccountService accountService,
                              PartyRepo partyRepo,
                              MarginRepo marginRepo) {
        this.accountService = accountService;
        this.partyRepo = partyRepo;
        this.marginRepo = marginRepo;
    }

    /**
     * 获取指定的保证金．
     *
     * @param marginId
     * @return
     */
    @Override
    public Optional<MarginDTO> getMargin(UUID marginId) {
        if (null == marginId) {
            throw new CustomException("marginId cannot be null.");
        }
        Margin margin = marginRepo.findById(marginId)
                .orElseThrow(() -> new CustomException(String.format("[%s]invalid marginId", marginId.toString())));
        Party party = partyRepo.findById(margin.getPartyId())
                .orElseThrow(() -> new CustomException(String.format("[%s]invalid partyId", margin.getPartyId().toString())));
        AccountDTO account = accountService.getAccountByLegalName(party.getLegalName());
        MarginDTO result = getMarginDto(party, account, margin);
        return Optional.of(result);
    }

    @Override
    public List<MarginDTO> marginSearch(String legalName) {
        if (StringUtils.isNotBlank(legalName)){
            Party party = partyRepo.findByLegalName(legalName)
                    .orElseThrow(() -> new CustomException(String.format("[%s]partyInfo not exist", legalName)));
            Margin margin = marginRepo.findByPartyId(party.getUuid())
                    .orElseThrow(() -> new CustomException(String.format("[%s]party margin not exist", party.getUuid().toString())));
            AccountDTO account = accountService.getAccountByLegalName(legalName);
            return Arrays.asList(getMarginDto(party, account, margin));
        }
        return marginRepo.findAll()
                .stream()
                .map(m -> {
                    Optional<Party> partyOptional = partyRepo.findById(m.getPartyId());
                    if (!partyOptional.isPresent()){
                        return null;
                    }
                    Party party = partyOptional.get();
                    AccountDTO account = accountService.getAccountByLegalName(party.getLegalName());
                    MarginDTO marginDto = getMarginDto(party, account, m);
                    if (Objects.nonNull(marginDto)){
                        marginDto.setMasterAgreementId(party.getMasterAgreementId());
                    }
                    return marginDto;
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 通过accountId检索搜索保证金信息．
     *
     * @param accountIds
     * @return
     */
    @Override
    public List<MarginDTO> getMargins(List<String> accountIds) {
        if (CollectionUtils.isEmpty(accountIds)){
            return new ArrayList<>();
        }
        List<AccountDTO>  accounts = accountService.findAccountsByAccountIds(accountIds);
        if (CollectionUtils.isEmpty(accounts)){
            return new ArrayList<>();
        }
        Map<String, Party> parties = partyRepo.findAllByLegalNameIn(accounts.stream()
                .map(AccountDTO::getLegalName)
                .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Party::getLegalName, p -> p));
        return accounts.stream()
                .map(a -> {
                    String legalName = a.getLegalName();
                    if (!parties.containsKey(legalName)){
                        return null;
                    }
                    Party party = parties.get(legalName);
                    Margin margin = marginRepo.findByPartyId(party.getUuid()).orElse(null);
                    if (Objects.nonNull(margin)){
                        return null;
                    }
                    return getMarginDto(party, a, margin);
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 更新维持保证金
     *
     * @param marginId
     * @param maintenanceMargin
     * @return
     */
    @Override
    public MarginDTO updateMaintenanceMargin(UUID marginId, BigDecimal maintenanceMargin) {
        if (null == marginId || null == maintenanceMargin) {
            throw new CustomException("invalid marginId or maintenanceMargin.");
        }
        Margin margin = marginRepo.findById(marginId).orElseThrow(() -> new CustomException("invalid marginId"));
        margin.setMaintenanceMargin(maintenanceMargin);
        margin = marginRepo.saveAndFlush(margin);

        Party party = partyRepo.findById(margin.getPartyId()).orElseThrow(() -> new CustomException("invalid partyId"));
        AccountDTO account = accountService.getAccountByLegalName(party.getLegalName());
        return getMarginDto(party, account, margin);
    }

    /**
     * 创建维持保证金。
     * 注：该API主要用于弥补历史遗留数据中， 维持保证金缺失． 如果保证金信息已经存在，那么就跳过．
     * @param partyId　交易对手的ID
     * @param maintenanceMargin
     * @return
     */
    @Override
    public MarginDTO createMargin(UUID partyId, BigDecimal maintenanceMargin) {
        if (null == partyId) {
            throw new CustomException("Invalid party Id");
        }
        if (null == maintenanceMargin) {
            maintenanceMargin = BigDecimal.ZERO;
        }
        Party party = partyRepo.findById(partyId).orElseThrow(() -> new CustomException("invalid partyId"));
        Margin margin = party.getMaintenanceMargin();
        if (null == margin) {
            margin = new Margin(party.getUuid(), maintenanceMargin);
            party.setMaintenanceMargin(margin);
            margin = this.marginRepo.save(margin);
            party.setMaintenanceMargin(margin);
        }
        AccountDTO account = accountService.getAccountByLegalName(party.getLegalName());
        return getMarginDto(party, account, margin);
    }


    /**
     * 批量更新维持保证金。
     *
     * @param margins map (LegalName, MaintenanceMargin)
     * @return
     */
    @Override
    public List<MarginDTO> updateMaintenanceMargins(Map<String, BigDecimal> margins) {
        if (CollectionUtils.isEmpty(margins)){
            return new ArrayList<>();
        }
        List<String> legalNames =  new ArrayList<>(margins.keySet());
        Map<String, Party> parties = partyRepo.findAllByLegalNameIn(legalNames)
                .stream().collect(Collectors.toMap(Party::getLegalName, p -> p));
        if (CollectionUtils.isEmpty(parties)) {
            throw new CustomException("交易对手未找到");
        }
        Map<UUID, Margin> marginMap = parties.values().stream()
                .map(p -> {
                    BigDecimal marginValue = margins.get(p.getLegalName());
                    if (Objects.isNull(marginValue)) {
                        return null;
                    }
                    Margin margin = marginRepo.findByPartyId(p.getUuid()).orElse(null);
                    if (Objects.isNull(margin)) {
                        return null;
                    }
                    margin.setMaintenanceMargin(marginValue);
                    return margin;
                }).filter(Objects::nonNull)
                .collect(Collectors.toMap(Margin::getPartyId, m -> m));
        marginRepo.saveAll(marginMap.values());
        Map<String, AccountDTO> accounts = accountService.findAccountsByLegalNames(legalNames)
                .stream().collect(Collectors.toMap(a -> a.getLegalName(), a -> a));

        return parties.keySet().stream()
                .map(legalName -> {
                    Party party = parties.get(legalName);
                    AccountDTO account = accounts.get(legalName);
                    Margin margin = marginMap.get(party.getUuid());
                    return getMarginDto(party, account, margin);
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private MarginDTO getMarginDto(Party party, AccountDTO account, Margin margin) {
        if (Objects.isNull(party) || Objects.isNull(account) || Objects.isNull(margin)){
            return null;
        }
        MarginCallStatus status = calculateStatus(margin.getMaintenanceMargin(),
                account.getMargin(),
                account.getCash(),
                account.getCreditBalance());

        MarginDTO result = toDto(party, account, margin, status);
        return result;
    }

    private MarginCallStatus calculateStatus(BigDecimal maintenanceMargin,
                                                                 BigDecimal margin,
                                                                 BigDecimal counterPartyFund,
                                                                 BigDecimal counterPartyCreditBalance
                                                                 ) {
        if (null == maintenanceMargin || null == margin || null == counterPartyFund) {
            return MarginCallStatus.UNKNOWN;
        }

        if (margin.compareTo(maintenanceMargin) >= 0) {
            return MarginCallStatus.NORMAL;
        }

        BigDecimal gap = maintenanceMargin.subtract(margin);

        if (gap.compareTo(counterPartyFund.add(counterPartyCreditBalance)) <= 0) {
            return MarginCallStatus.NOT_ENOUGH;
        }

        return MarginCallStatus.PENDING_MARGIN_CALL;
    } // end calculateStatus

    private MarginDTO toDto(Party party, AccountDTO account,Margin margin, MarginCallStatus status) {
        assert null != party;
        assert null != account;
        assert null != margin;

        MarginDTO result = new MarginDTO();
        result.setLegalName(party.getLegalName());
        result.setUuid(margin.getUuid());
        result.setPartyId(margin.getPartyId());
        result.setCreatedAt(margin.getCreatedAt());
        result.setUpdatedAt(margin.getUpdatedAt());
        result.setMaintenanceMargin(margin.getMaintenanceMargin());
        result.setMasterAgreementId(party.getMasterAgreementId());

        result.setAccountId(account.getAccountId());
        result.setCash(account.getCash());
        result.setMargin(account.getMargin());
        result.setCredit(account.getCredit().subtract(account.getCreditUsed()));

        result.setStatus(status);
        return result;
    } // end toDto
}
