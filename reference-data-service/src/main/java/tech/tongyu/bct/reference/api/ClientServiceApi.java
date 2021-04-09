package tech.tongyu.bct.reference.api;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.tongyu.bct.auth.enums.ResourcePermissionTypeEnum;
import tech.tongyu.bct.auth.enums.ResourceTypeEnum;
import tech.tongyu.bct.auth.service.ResourcePermissionService;
import tech.tongyu.bct.client.dto.AccountDTO;
import tech.tongyu.bct.client.dto.AccountEvent;
import tech.tongyu.bct.client.dto.AccountOpRecordDTO;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.exception.ErrorCode;
import tech.tongyu.bct.common.util.JsonUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.PartyService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ClientServiceApi {

    private PartyService partyService;
    private AccountService accountService;
    private ResourcePermissionService resourcePermissionService;

    @Autowired
    public ClientServiceApi(PartyService partyService,
                            AccountService accountService,
                            ResourcePermissionService resourcePermissionService){
        this.partyService = partyService;
        this.accountService = accountService;
        this.resourcePermissionService = resourcePermissionService;
    }

    @BctMethodInfo(
            description = "获取交易对手相关的账户信息",
            retDescription = "账户信息",
            retName = "AccountDTO", returnClass = AccountDTO.class,
            service = "reference-data-service"
    )
    public AccountDTO clientAccountGetByLegalName(
            @BctMethodArg(name = "legalName", description = "交易对手") String legalName
    ){
        if (StringUtils.isBlank(legalName)){
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        return accountService.getAccountByLegalName(legalName);
    }

    @BctMethodInfo(
            description = "新增账户信息",
            retDescription = "账户信息",
            retName = "AccountDTO",
            returnClass = AccountDTO.class,
            service = "reference-data-service"
    )
    public AccountDTO clientSaveAccount(
            @BctMethodArg(name = "legalName", description = "交易对手") String legalName
    ){
        if (!hasPermission(ResourcePermissionTypeEnum.CREATE_CLIENT)) {
            throw new CustomException("没有创建客户的权限");
        }

        if (StringUtils.isBlank(legalName)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入交易对手标识(legalName)");
        }
        return accountService.saveAccount(legalName);
    }

    @BctMethodInfo(
            description = "根据账户编号删除",
            service = "reference-data-service"
    )
    public Boolean clientAccountDel(
            @BctMethodArg(name = "accountId", description = "账户编号") String accountId
    ){
        if (!hasPermission(ResourcePermissionTypeEnum.DELETE_CLIENT)) {
            throw new CustomException("没有删除客户的权限");
        }

        if (StringUtils.isBlank(accountId)){
            throw new CustomException("请输入待删除账户编号accountId");
        }
        accountService.deleteByAccountId(accountId);
        return true;
    }

    @BctMethodInfo(
            description = "新增账户操作记录",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class,
            service = "reference-data-service"
    )
    public AccountOpRecordDTO clientSaveAccountOpRecord(
            @BctMethodArg(description = "账户操作搜索条件", argClass = AccountOpRecordDTO.class) Map<String, Object> accountOpRecord
    ){
        AccountOpRecordDTO accountOpRecordDTO = JsonUtils.mapper.convertValue(accountOpRecord, AccountOpRecordDTO.class);
        initAccountOpRecord(accountOpRecordDTO);
        return accountService.saveAccountOpRecord(accountOpRecordDTO);
    }

    @BctMethodInfo(
            description = "变更账户授信额度",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class,
            service = "reference-data-service"
    )
    public AccountOpRecordDTO clientUpdateCredit(
            @BctMethodArg(name = "accountId", description = "账户编号") String accountId,
            @BctMethodArg(name = "credit", description = "客户授信总额") Number credit,
            @BctMethodArg(name = "counterPartyCredit", description = "我方授信总额") Number counterPartyCredit,
            @BctMethodArg(name = "infromation", description = "备注信息", required = false) String information
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (Objects.isNull(credit)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入授信总额(credit)");
        }

        return accountService.updateCredit(accountId, new BigDecimal(credit.toString()),
                new BigDecimal(counterPartyCredit.toString()), information);
    }

    @BctMethodInfo(
            description = "期权费变动引起的账户变动",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class,
            service = "reference-data-service"
    )
    public AccountOpRecordDTO clientChangePremium(
            @BctMethodArg(name = "accountId", description = "账户编号") String accountId,
            @BctMethodArg(name = "tradeId", description = "交易编号") String tradeId,
            @BctMethodArg(name = "premium", description = "期权费变动") Number premium,
            @BctMethodArg(name = "infromation", description = "备注信息", required = false) String information
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (Objects.isNull(premium)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入期权费(premium)");
        }

        return accountService.changePremium(accountId, tradeId, new BigDecimal(premium.toString()), information);
    }

    @BctMethodInfo(
            description = "结算交易引起的账户变动,包括平仓,行权等",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class,
            service = "reference-data-service"
    )
    public AccountOpRecordDTO clientSettleTrade(
            @BctMethodArg(name = "accountId", description = "账户编号") String accountId,
            @BctMethodArg(name = "tradeId", description = "交易编号") String tradeId,
            @BctMethodArg(name = "amount", description = "结算金额") Number amount,
            @BctMethodArg(name = "premium", description = "权力金金额") Number premium,
            @BctMethodArg(name = "accountEvent", description = "事件类型") String accountEvent,
            @BctMethodArg(name = "information", description = "备注信息",required = false) String information
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (Objects.isNull(amount)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入结算金额(amount)");
        }
        if (Objects.isNull(premium)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入期权费(premium)");
        }
        if (StringUtils.isBlank(accountEvent)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户事件(accountEvent)");
        }

        return accountService.settleTrade(accountId, tradeId, new BigDecimal(amount.toString()),
                new BigDecimal(premium.toString()), AccountEvent.valueOf(accountEvent), information);
    }

    @BctMethodInfo(
            description = "查询客户存款",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class
    )
    public AccountOpRecordDTO clientDeposit(
            @BctMethodArg(description = "账户编号") String accountId,
            @BctMethodArg(description = "结算金额") String amount,
            @BctMethodArg(description = "备注信息") String information
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (StringUtils.isBlank(amount)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入入金金额(amount)");
        }

        return accountService.deposit(accountId, amount, information);
    }

    @BctMethodInfo(
            description = "查询客户提款",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class
    )
    public AccountOpRecordDTO clientWithdraw(
            @BctMethodArg(description = "账户编号") String accountId,
            @BctMethodArg(description = "结算金额") String amount,
            @BctMethodArg(description = "备注信息") String information
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (StringUtils.isBlank(amount)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入出金金额(amount)");
        }
        return accountService.withdraw(accountId, amount, information);
    }

    @BctMethodInfo(
            description = "账户现金流操作",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class
    )
    public AccountOpRecordDTO clientTradeCashFlow(
            @BctMethodArg(description = "账户编号") String accountId,
            @BctMethodArg(required = false, description = "交易编号") String tradeId,
            @BctMethodArg(description = "现金流金额") String cashFlow,
            @BctMethodArg(description = "保证金流金额") String marginFlow
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (StringUtils.isBlank(cashFlow)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入现金流金额(cashFlow)");
        }
        if (StringUtils.isBlank(marginFlow)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入保证金流金额(marginFlow)");
        }

        return accountService.tradeCashFlow(accountId, tradeId, cashFlow, marginFlow);

    }

    @BctMethodInfo(
            description = "账户变更授信操作",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class
    )
    public AccountOpRecordDTO clientChangeCredit(
            @BctMethodArg(description = "账户编号") String accountId,
            @BctMethodArg(description = "授信变更金额") String amount,
            @BctMethodArg(description = "备注信息") String information
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (StringUtils.isBlank(amount)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入授信变更金额(amount)");
        }
        return accountService.changeCredit(accountId, amount, information);
    }

    @BctMethodInfo(
            description = "账户开始交易操作",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class
    )
    public AccountOpRecordDTO clientStartTrade(
            @BctMethodArg(description = "账户编号") String accountId,
            @BctMethodArg(description = "交易标识") String tradeId,
            @BctMethodArg(description = "支出或收入") String gainOrCost,
            @BctMethodArg(description = "保证金额度") String marginToBePaid
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (StringUtils.isBlank(tradeId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入交易标识(tradeId)");
        }
        if (StringUtils.isBlank(gainOrCost)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入支出或收入(gainOrCost)");
        }
        if (StringUtils.isBlank(marginToBePaid)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入保证金额度(marginToBePaid)");
        }
        return accountService.startTrade(accountId, tradeId, gainOrCost, marginToBePaid);
    }

    @BctMethodInfo(
            description = "账户中止交易操作",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class
    )
    public AccountOpRecordDTO clientTerminateTrade(
            @BctMethodArg(description = "账户编号") String accountId,
            @BctMethodArg(description = "交易标识") String tradeId,
            @BctMethodArg(description = "开始支出或收入") String startGainOrCost,
            @BctMethodArg(description = "结束支出或收入") String endGainOrCost,
            @BctMethodArg(description = "保证金释放金额") String marginRelease
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)" );
        }
        if (StringUtils.isBlank(tradeId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入交易标识(tradeId)");
        }
        if (StringUtils.isBlank(startGainOrCost)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入开始支出或收入(startGainOrCost)");
        }
        if (StringUtils.isBlank(endGainOrCost)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入结束支出或收入(endGainOrCost)");
        }
        if (StringUtils.isBlank(marginRelease)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入保证金释放金额(marginRelease)");
        }

        return accountService.terminateTrade(accountId, tradeId, startGainOrCost, endGainOrCost, marginRelease);
    }

    @BctMethodInfo(
            description = "账户变更保证金操作",
            retDescription = "账户操作记录信息",
            retName = "AccountOpRecordDTO",
            returnClass = AccountOpRecordDTO.class
    )
    public AccountOpRecordDTO clientChangeMargin(
            @BctMethodArg(description = "账户编号") String accountId,
            @BctMethodArg(description = "最小保证金") String minimalMargin,
            @BctMethodArg(description = "最大保证金") String maximalMargin,
            @BctMethodArg(description = "追加保证金") String callMargin
    ){
        if (StringUtils.isBlank(accountId)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户标识(accountId)");
        }
        if (StringUtils.isBlank(minimalMargin)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入最小保证金(minimalMargin)");
        }
        if (StringUtils.isBlank(maximalMargin)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入最大保证金(maximalMargin)");
        }
        if (StringUtils.isBlank(callMargin)){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入追加保证金(callMargin)");
        }
        return accountService.changeMargin(accountId, minimalMargin, maximalMargin, callMargin);
    }

    @BctMethodInfo(
            description = "获取账户列表信息",
            retDescription = "账户信息集合",
            retName = "List<AccountDTO>",
            returnClass = AccountDTO.class,
            service = "reference-data-service"
    )
    public List<AccountDTO> clientAccountList(){
        return accountService.listAccounts();
    }

    @BctMethodInfo(
            description = "根据交易对手,账户状态,主协议编号获取账户列表",
            retDescription = "账户信息集合",
            retName = "List<AccountDTO>",
            returnClass = AccountDTO.class,
            service = "reference-data-service"
    )
    public List<AccountDTO> clientAccountSearch(
            @BctMethodArg(name = "legalName", description = "交易对手", required = false) String legalName,
            @BctMethodArg(name = "normalStatus", description = "状态", required = false) Boolean normalStatus,
            @BctMethodArg(name = "masterAgreementId", description = "主协议编号", required = false) String masterAgreementId
    ){
        if (!hasPermission(ResourcePermissionTypeEnum.READ_CLIENT)) {
            throw new CustomException("没有读取客户的权限");
        }

        AccountDTO accountDto = new AccountDTO();
        accountDto.setNormalStatus(normalStatus);
        accountDto.setLegalName(StringUtils.isBlank(legalName) ? null : legalName);
        if (StringUtils.isNotBlank(masterAgreementId)){
            PartyDTO party = partyService.getByMasterAgreementId(masterAgreementId);
            if (StringUtils.isNotBlank(legalName)){
                if (!legalName.equals(party.getLegalName())){
                    throw new CustomException(String.format("主协议编号:[%s],交易对手:[%s],客户信息不匹配",
                            masterAgreementId, legalName));
                }
            }
            accountDto.setLegalName(party.getLegalName());
        }

        return ProfilingUtils.timed("search account", ()-> accountService.accountSearch(accountDto));

    }

    @BctMethodInfo(
            description = "根据交易对手信息获取账户信息",
            retDescription = "账户信息集合",
            retName = "List<AccountDTO>",
            returnClass = AccountDTO.class,
            service = "reference-data-service"
    )
    public List<AccountDTO> cliAccountListByLegalNames(
            @BctMethodArg(name = "legalNames", description = "交易对手名称列表") List<String> legalNames
    ){
        if (CollectionUtils.isEmpty(legalNames)){
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        return accountService.findAccountsByLegalNames(legalNames);
    }

    @BctMethodInfo(
            description = "获取选定账户操作记录信息",
            retDescription = "账户操作记录集合",
            retName = "List<AccountOpRecordDTO>",
            returnClass = AccountOpRecordDTO.class,
            service = "reference-data-service"
    )
    public List<AccountOpRecordDTO> clientAccountOpRecordList(
            @BctMethodArg(name = "accountIds", description = "账户编号列表") List<String> accountIds
    ){
        if (CollectionUtils.isEmpty(accountIds)){
            throw new CustomException(ErrorCode.MISSING_ENTITY, "请至少选择一个账户信息");
        }
        return accountService.listAccountOpRecords(accountIds);
    }

    @BctMethodInfo(
            description = "搜索账户操作记录信息",
            retDescription = "账户操作记录集合",
            retName = "List<AccountOpRecordDTO>",
            returnClass = AccountOpRecordDTO.class,
            service = "reference-data-service"
    )
    public List<AccountOpRecordDTO> clientAccountOpRecordSearch(
            @BctMethodArg(name = "event", description = "事件类型", required = false) String event,
            @BctMethodArg(name = "tradeId", description = "交易编号", required = false) String tradeId,
            @BctMethodArg(name = "legalName", description = "交易对手", required = false) String legalName,
            @BctMethodArg(name = "masterAgreementId", description = "主协议编号", required = false) String masterAgreementId
    ){
        AccountOpRecordDTO accountOpRecordDto = new AccountOpRecordDTO();
        accountOpRecordDto.setEvent(StringUtils.isBlank(event) ? null : event);
        accountOpRecordDto.setTradeId(StringUtils.isBlank(tradeId) ? null : tradeId);
        accountOpRecordDto.setLegalName(StringUtils.isBlank(legalName) ? null : legalName);
        if (StringUtils.isNotBlank(masterAgreementId)){
            PartyDTO party = partyService.getByMasterAgreementId(masterAgreementId);
            if (StringUtils.isNotBlank(legalName)){
                if (!legalName.equals(party.getLegalName())){
                    throw new CustomException(String.format("主协议编号:[%s],交易对手:[%s],客户信息不匹配",
                            masterAgreementId, legalName));
                }
            }
            accountOpRecordDto.setLegalName(party.getLegalName());
        }
        return accountService.accountOpRecordSearch(accountOpRecordDto)
                .stream()
                .map(accountOpRecordDTO -> {
                    PartyDTO partyDTO = partyService.getByLegalName(accountOpRecordDTO.getLegalName());
                    accountOpRecordDTO.setMasterAgreementId(partyDTO.getMasterAgreementId());
                    return accountOpRecordDTO;
                }).collect(Collectors.toList());

    }

    private String killBlank(String s) {
        return StringUtils.isBlank(s) ? null : s;
    }

    /**
     * 校验账户操作记录传入参数
     * @param recordDto
     */
    private void initAccountOpRecord(AccountOpRecordDTO recordDto){
        if (Objects.isNull(recordDto)){
            throw new CustomException(ErrorCode.MISSING_ENTITY, "请输入账户操作记录信息");
        }
        if (StringUtils.isBlank(recordDto.getAccountId())){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请输入账户编号accountId");
        }
        if (StringUtils.isBlank(recordDto.getEvent())){
            throw new CustomException(ErrorCode.INPUT_NOT_VALID, "请选择账户操作事件accountEvent");
        }
        if (Objects.isNull(recordDto.getCashChange())){
            recordDto.setCashChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getDebtChange())){
            recordDto.setDebtChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getMarginChange())){
            recordDto.setMarginChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getCreditChange())){
            recordDto.setCreditChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getPremiumChange())){
            recordDto.setPremiumChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getCreditUsedChange())){
            recordDto.setCreditUsedChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getNetDepositChange())){
            recordDto.setNetDepositChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getRealizedPnLChange())){
            recordDto.setRealizedPnLChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getCreditBalanceChange())){
            recordDto.setCreditBalanceChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getCounterPartyFundChange())){
            recordDto.setCounterPartyFundChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getCounterPartyMarginChange())){
            recordDto.setCounterPartyMarginChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getCounterPartyCreditChange())){
            recordDto.setCounterPartyCreditChange(BigDecimal.ZERO);
        }
        if (Objects.isNull(recordDto.getCounterPartyCreditBalanceChange())){
            recordDto.setCounterPartyCreditBalanceChange(BigDecimal.ZERO);
        }
    }

    private Boolean hasPermission(ResourcePermissionTypeEnum resourcePermissionTypeEnum) {
        return resourcePermissionService.authCan(
                ResourceTypeEnum.CLIENT_INFO.toString(),
                Lists.newArrayList("客户信息"),
                resourcePermissionTypeEnum.toString()
        ).get(0);
    }

}
