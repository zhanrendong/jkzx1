package tech.tongyu.bct.reference.api;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.tongyu.bct.client.dto.AccountTradeTaskDTO;
import tech.tongyu.bct.client.dto.ProcessStatusEnum;
import tech.tongyu.bct.client.service.AccountTradeTaskService;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.common.api.annotation.BctMethodArg;
import tech.tongyu.bct.common.api.annotation.BctMethodInfo;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.reference.dto.PartyDTO;
import tech.tongyu.bct.reference.service.PartyService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
public class AccountTradeTaskApi {

    private PartyService partyService;

    private AccountTradeTaskService accountTradeTaskService;

    @Autowired
    public AccountTradeTaskApi(PartyService partyService,
                               AccountTradeTaskService accountTradeTaskService) {
        this.partyService = partyService;
        this.accountTradeTaskService = accountTradeTaskService;
    }


    @BctMethodInfo(
            description = "根据交易编号和交易对手生成待处理台帐任务",
            retDescription = "生成的待处理台帐任务",
            retName = "List<AccountTradeTaskDTO>",
            returnClass = AccountTradeTaskDTO.class,
            service = "reference-data-service"
    )
    public List<AccountTradeTaskDTO> cliTasksGenerateByTradeId(@BctMethodArg(name = "tradeId", description = "交易编号") String tradeId,
                                                               @BctMethodArg(name = "legalName", description = "交易对手") String legalName){
        if (StringUtils.isBlank(tradeId)){
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(legalName)){
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        return accountTradeTaskService.generateTasksByTradeId(tradeId, legalName);
    }

    @BctMethodInfo(
            description = "保存交易关联的待台帐任务",
            retDescription = "保存的待处理台帐任务",
            retName = "AccountTradeTaskDTO",
            returnClass = AccountTradeTaskDTO.class,
            service = "reference-data-service"
    )
    public AccountTradeTaskDTO cliTradeTaskSave(@BctMethodArg(required = false, description = "台账ID") String uuid,
                                                @BctMethodArg(description = "交易编号") String tradeId,
                                                @BctMethodArg(description = "交易对手") String legalName,
                                                @BctMethodArg(description = "现金流") Number cashFlow,
                                                @BctMethodArg(description = "生命周期事件", argClass = LCMEventTypeEnum.class) String lcmEventType){
        if (StringUtils.isBlank(tradeId)){
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(legalName)){
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        if (Objects.isNull(cashFlow)){
            throw new IllegalArgumentException("请输入现金流cashFlow");
        }
        if (StringUtils.isBlank(lcmEventType)){
            throw new IllegalArgumentException("请输入生命周期事件lcmEventType");
        }
        AccountTradeTaskDTO tradeTaskDto = new AccountTradeTaskDTO(
                uuid,
                tradeId,
                legalName,
                new BigDecimal(cashFlow.toString()),
                LCMEventTypeEnum.valueOf(lcmEventType));
        if (StringUtils.isBlank(uuid)){
            return accountTradeTaskService.createAccountTradeTask(tradeTaskDto);
        }
        return accountTradeTaskService.updateAccountTradeTask(tradeTaskDto);
    }

    @BctMethodInfo(
            description = "获取交易相关的未处理台帐任务",
            retDescription = "交易相关的未处理台帐任务",
            retName = "List<AccountTradeTaskDTO>",
            returnClass = AccountTradeTaskDTO.class,
            service = "reference-data-service"
    )
    public List<AccountTradeTaskDTO> cliUnProcessedTradeTaskListByTradeId(
            @BctMethodArg(name = "tradeId", description = "交易编号") String tradeId){
        if (StringUtils.isBlank(tradeId)){
            throw new IllegalArgumentException("请输入交易编号tradeId");
        }
        AccountTradeTaskDTO tradeTaskDto = new AccountTradeTaskDTO();
        tradeTaskDto.setTradeId(tradeId);
        tradeTaskDto.setProcessStatus(ProcessStatusEnum.UN_PROCESSED);

        return accountTradeTaskService.findTasksByExample(tradeTaskDto);
    }

    @BctMethodInfo(
            description = "获取交易对手待处理台帐任务",
            retDescription = "交易对手相关的未处理台帐任务",
            retName = "List<AccountTradeTaskDTO>",
            returnClass = AccountTradeTaskDTO.class,
            service = "reference-data-service"
    )
    public List<AccountTradeTaskDTO> cliTradeTaskListByLegalNames(
            @BctMethodArg(name = "legalNames", description = "交易对手列表") List<String> legalNames){
        if (CollectionUtils.isEmpty(legalNames)){
            throw new IllegalArgumentException("请输入交易对手legalName");
        }
        List<AccountTradeTaskDTO> tradeTaskDtoList = new ArrayList<>();
        legalNames.forEach(legalName ->{
            AccountTradeTaskDTO tradeTaskDto = new AccountTradeTaskDTO();
            tradeTaskDto.setLegalName(legalName);
            tradeTaskDto.setProcessStatus(ProcessStatusEnum.UN_PROCESSED);

            List<AccountTradeTaskDTO> queryList = accountTradeTaskService.findTasksByExample(tradeTaskDto);
            tradeTaskDtoList.addAll(queryList);
        });

        return tradeTaskDtoList;
    }

    @BctMethodInfo(
            description = "根据交易,交易对手,生命周期事件,任务状态,主协议编号搜索交易台帐任务",
            retDescription = "交易资金台帐任务",
            retName = "List<AccountTradeTaskDTO>",
            returnClass = AccountTradeTaskDTO.class,
            service = "reference-data-service"
    )
    public List<AccountTradeTaskDTO> cliTradeTaskSearch(
            @BctMethodArg(name = "tradeId", description = "交易编号", required = false) String tradeId,
            @BctMethodArg(name = "legalName", description = "交易对手", required = false) String legalName,
            @BctMethodArg(name = "lcmEventType", description = "生命周期事件", argClass = LCMEventTypeEnum.class, required = false) String lcmEventType,
            @BctMethodArg(name = "processStatus", description = "处理状态", argClass = ProcessStatusEnum.class, required = false) String processStatus,
            @BctMethodArg(required = false) String masterAgreementId){
        AccountTradeTaskDTO tradeTaskDto = new AccountTradeTaskDTO();
        tradeTaskDto.setTradeId(StringUtils.isBlank(tradeId) ? null : tradeId);
        tradeTaskDto.setLegalName(StringUtils.isBlank(legalName) ? null : legalName);
        tradeTaskDto.setLcmEventType(StringUtils.isBlank(lcmEventType) ? null : LCMEventTypeEnum.valueOf(lcmEventType));
        tradeTaskDto.setProcessStatus(StringUtils.isBlank(processStatus) ? null : ProcessStatusEnum.valueOf(processStatus));
        if (StringUtils.isNotBlank(masterAgreementId)){
            PartyDTO party = partyService.getByMasterAgreementId(masterAgreementId);
            if (StringUtils.isNotBlank(legalName)){
                if (!legalName.equals(party.getLegalName())){
                    throw new CustomException(String.format("主协议编号:[%s],交易对手:[%s],客户信息不匹配",
                            masterAgreementId, legalName));
                }
            }
            tradeTaskDto.setLegalName(party.getLegalName());
        }
        return accountTradeTaskService.search(tradeTaskDto);

    }

    @BctMethodInfo(
            description = "标记资金任务已经被处理",
            retDescription = "被标记资金任务信息",
            retName = "List<AccountTradeTaskDTO>",
            returnClass = AccountTradeTaskDTO.class,
            service = "reference-data-service"
    )
    public List<AccountTradeTaskDTO> cliMmarkTradeTaskProcessed(
            @BctMethodArg(name = "uuidList", description = "待处理任务唯一标识列表") List<String> uuidList){
        if (CollectionUtils.isEmpty(uuidList)){
            throw new IllegalArgumentException("请输入待标记唯一标识");
        }

        return accountTradeTaskService.markTradeTaskProcessed(uuidList);
    }

}
