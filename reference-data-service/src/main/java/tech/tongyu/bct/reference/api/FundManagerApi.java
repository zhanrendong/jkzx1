package tech.tongyu.bct.reference.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.client.dto.AccountDirectionEnum;
import tech.tongyu.bct.client.dto.FundEventRecordDTO;
import tech.tongyu.bct.client.dto.PaymentDirectionEnum;
import tech.tongyu.bct.client.dto.ProcessStatusEnum;
import tech.tongyu.bct.client.service.FundManagerService;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.common.util.ProfilingUtils;
import tech.tongyu.bct.reference.dto.BankAccountDTO;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.BankAccountService;
import tech.tongyu.bct.reference.service.PartyService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class FundManagerApi {

    private PartyService partyService;

    private FundManagerService fundManagerService;

    private BankAccountService bankAccountService;

    @Autowired
    public FundManagerApi(PartyService partyService,
                          FundManagerService fundManagerService,
                          BankAccountService bankAccountService) {
        this.partyService = partyService;
        this.fundManagerService = fundManagerService;
        this.bankAccountService = bankAccountService;
    }

    @BctMethodInfo(
            description = "财务出入金操作",
            retDescription = "财务出入金记录",
            retName = "FundEventRecordDTO",
            returnClass = FundEventRecordDTO.class,
            service = "reference-data-service"
    )
    public FundEventRecordDTO cliFundEventSave(
            @BctMethodArg(name = "uuid", description = "唯一标识", required = false) String uuid,
            @BctMethodArg(name = "clientId", description = "交易对手legalName") String clientId,
            @BctMethodArg(name = "bankAccount", description = "银行账户") String bankAccount,
            @BctMethodArg(name = "paymentAmount", description = "支付金额") Number paymentAmount,
            @BctMethodArg(name = "paymentDate", description = "支付日期") String paymentDate,
            @BctMethodArg(name = "paymentDirection", description = "支付方向") String paymentDirection,
            @BctMethodArg(name = "accountDirection", description = "账户方向") String accountDirection
    ) {
        if (StringUtils.isBlank(clientId)) {
            throw new IllegalArgumentException("请输入交易对手clientId");
        }
        if (StringUtils.isBlank(bankAccount)) {
            throw new IllegalArgumentException("请输入交易银行账号bankAccount");
        }
        if (Objects.isNull(paymentAmount)) {
            throw new IllegalArgumentException("请输入出入金金额paymentAmount");
        }
        if (StringUtils.isBlank(paymentDate)) {
            throw new IllegalArgumentException("请输入出入金日期paymentDate");
        }
        if (StringUtils.isBlank(paymentDirection)) {
            throw new IllegalArgumentException("请输入出入金方向paymentDirection");
        }
        if (StringUtils.isBlank(accountDirection)) {
            throw new IllegalArgumentException("请输入客户类型accountDirection");
        }

        FundEventRecordDTO fundEventRecordDto = new FundEventRecordDTO(
                uuid,
                clientId,
                bankAccount,
                new BigDecimal(paymentAmount.toString()),
                LocalDate.parse(paymentDate),
                PaymentDirectionEnum.valueOf(paymentDirection),
                AccountDirectionEnum.valueOf(accountDirection));

        if (StringUtils.isBlank(uuid)) {
            return fundManagerService.createFundTransRecord(fundEventRecordDto);
        }
        return fundManagerService.updateFundTransRecord(fundEventRecordDto);

    }

    @BctMethodInfo(
            description = "删除财务事件"
    )
    public Boolean cliFundEventDel(
            @BctMethodArg(description = "财务事件唯一标识") String uuid
    ) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("请输入待删除财务事件唯一标识uuid");
        }
        fundManagerService.deleteFundTransRecord(uuid);
        return true;
    }

    @BctMethodInfo(
            description = "获取财务事件列表",
            retDescription = "财务出入金记录列表",
            retName = "List<FundEventRecordDTO>",
            returnClass = FundEventRecordDTO.class
    )
    public List<FundEventRecordDTO> cliFundEvnetListAll() {
        return ProfilingUtils.timed("fetch all funds", () -> fundManagerService.findAllFundTransRecord());
    }

    @BctMethodInfo(
            description = "根据交易对手ID列表获取财务事件列表",
            retDescription = "财务出入金记录列表",
            retName = "List<FundEventRecordDTO>",
            returnClass = FundEventRecordDTO.class
    )
    public List<FundEventRecordDTO> cliFundEventListByClientIds(
            @BctMethodArg(description = "交易对手ID列表") List<String> clientIds
    ) {
        if (CollectionUtils.isEmpty(clientIds)) {
            throw new IllegalArgumentException("请输入交易对手clientId");
        }
        return ProfilingUtils.timed("search fund events by client", () -> {
            List<FundEventRecordDTO> fundEventRecordDtoList = new ArrayList<>();
            clientIds.parallelStream().forEach(clientId -> {
                List<FundEventRecordDTO> queryList = fundManagerService.findByClientId(clientId);
                fundEventRecordDtoList.addAll(queryList);
            });
            return fundEventRecordDtoList;
        });
    }

    @BctMethodInfo(
            description = "根据条件搜索资金操作记录",
            retDescription = "资金操作记录集合",
            retName = "List<FundEventRecordDTO>",
            returnClass = FundEventRecordDTO.class,
            service = "reference-data-service"
    )
    public List<FundEventRecordDTO> cliFundEventSearch(
            @BctMethodArg(name = "startDate", description = "开始日期", required = false) String startDate,
            @BctMethodArg(name = "endDate", description = "结束日期", required = false) String endDate,
            @BctMethodArg(name = "clientId", description = "交易对手legalName", required = false) String clientId,
            @BctMethodArg(name = "bankAccount", description = "银行账户", required = false) String bankAccount,
            @BctMethodArg(name = "processStatus", description = "处理状态", required = false) String processStatus,
            @BctMethodArg(name = "masterAgreementId", description = "主协议编号", required = false) String masterAgreementId
    ) {
        FundEventRecordDTO recordDto = new FundEventRecordDTO();
        recordDto.setClientId(StringUtils.isBlank(clientId) ? null : clientId);
        recordDto.setBankAccount(StringUtils.isBlank(bankAccount) ? null : bankAccount);
        recordDto.setProcessStatus(StringUtils.isBlank(processStatus) ? null : ProcessStatusEnum.valueOf(processStatus));

        if (StringUtils.isNotBlank(masterAgreementId)) {
            PartyDTO party = partyService.getByMasterAgreementId(masterAgreementId);
            if (StringUtils.isNotBlank(clientId)) {
                if (!clientId.equals(party.getLegalName())) {
                    throw new CustomException(String.format("主协议编号:[%s],交易对手:[%s],客户信息不匹配",
                            masterAgreementId, clientId));
                }
            }
            recordDto.setClientId(party.getLegalName());
        }
        return ProfilingUtils.timed("search and enrich fund event", () ->
                fundManagerService.search(recordDto, StringUtils.isBlank(startDate) ? null : LocalDate.parse(startDate),
                        StringUtils.isBlank(endDate) ? null : LocalDate.parse(endDate))
                        .stream()
                        .map(fundEventRecordDTO -> {
                            if (bankAccountService.isBankAccountExistsByLegalNameAndAccount(
                                    fundEventRecordDTO.getClientId(), fundEventRecordDTO.getBankAccount())) {
                                BankAccountDTO bankAccountDTO =
                                        bankAccountService.findBankAccountByLegalNameAndBankAccount(
                                                fundEventRecordDTO.getClientId(), fundEventRecordDTO.getBankAccount());
                                fundEventRecordDTO.setBankName(bankAccountDTO.getBankName());
                                fundEventRecordDTO.setBankAccountName(bankAccountDTO.getBankAccountName());
                            }
                            PartyDTO partyDTO = partyService.getByLegalName(fundEventRecordDTO.getClientId());
                            fundEventRecordDTO.setMasterAgreementId(partyDTO.getMasterAgreementId());
                            return fundEventRecordDTO;
                        }).collect(Collectors.toList()));
    }
}
