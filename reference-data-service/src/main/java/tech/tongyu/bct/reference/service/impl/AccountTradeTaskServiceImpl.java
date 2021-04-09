package tech.tongyu.bct.reference.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.tongyu.bct.client.dto.AccountDTO;
import tech.tongyu.bct.client.dto.AccountTradeTaskDTO;
import tech.tongyu.bct.client.dto.ProcessStatusEnum;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.client.service.AccountTradeTaskService;
import tech.tongyu.bct.cm.trade.LCMEventTypeEnum;
import tech.tongyu.bct.cm.trade.impl.BctTradePosition;
import tech.tongyu.bct.common.exception.CustomException;
import tech.tongyu.bct.common.util.CollectionUtils;
import tech.tongyu.bct.reference.dao.dbo.AccountTradeTask;
import tech.tongyu.bct.reference.dao.repl.intel.AccountTradeTaskRepo;
import tech.tongyu.bct.trade.dto.event.LCMEventDTO;
import tech.tongyu.bct.trade.service.PositionService;
import tech.tongyu.bct.trade.service.TradeLCMService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountTradeTaskServiceImpl implements AccountTradeTaskService {

    private AccountService accountService;
    private TradeLCMService tradeLCMService;
    private PositionService positionService;
    private AccountTradeTaskRepo accountTradeTaskRepo;

    private static Logger logger = LoggerFactory.getLogger(AccountTradeTaskServiceImpl.class);

    @Autowired
    public AccountTradeTaskServiceImpl(AccountService accountService,
                                       TradeLCMService tradeLCMService,
                                       PositionService positionService,
                                       AccountTradeTaskRepo accountTradeTaskRepo) {
        this.accountService = accountService;
        this.tradeLCMService = tradeLCMService;
        this.positionService = positionService;
        this.accountTradeTaskRepo = accountTradeTaskRepo;
    }

    @Override
    public List<AccountTradeTaskDTO> search(AccountTradeTaskDTO taskDto) {
        AccountTradeTask tradeTask = new AccountTradeTask();
        BeanUtils.copyProperties(taskDto, tradeTask);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();

        return accountTradeTaskRepo.findAll(Example.of(tradeTask, exampleMatcher))
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AccountTradeTaskDTO> generateTasksByTradeId(String tradeId, String legalName) {
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        if (StringUtils.isBlank(legalName)) {
            throw new CustomException("请输入交易对手legalName");
        }
        AccountTradeTaskServiceImpl aopTradeTaskService = (AccountTradeTaskServiceImpl) AopContext.currentProxy();
        AccountDTO account = accountService.getAccountByLegalName(legalName);
        return tradeLCMService.listCashFlowsByTradeId(tradeId)
                .stream()
                .filter(lcmEvent -> BigDecimal.ZERO.compareTo(lcmEvent.getPremium()) != 0
                        || BigDecimal.ZERO.compareTo(lcmEvent.getCashFlow()) != 0)
                .filter(lcmEvent -> !accountTradeTaskRepo.existsByLcmUUID(lcmEvent.getUuid()))
                .map(lcmEvent -> {
                    AccountTradeTaskDTO accountTradeTaskDto = new AccountTradeTaskDTO();
                    BeanUtils.copyProperties(lcmEvent, accountTradeTaskDto);
                    accountTradeTaskDto.setLegalName(legalName);
                    accountTradeTaskDto.setLcmUUID(lcmEvent.getUuid());
                    accountTradeTaskDto.setAccountId(account.getAccountId());
                    accountTradeTaskDto.setPremium(Objects.isNull(lcmEvent.getPremium()) ? null : lcmEvent.getPremium().negate());
                    accountTradeTaskDto.setCashFlow(Objects.isNull(lcmEvent.getCashFlow()) ? null : lcmEvent.getCashFlow().negate());

                    BctTradePosition bctPosition = positionService.getBctPositionById(lcmEvent.getPositionId());
                    accountTradeTaskDto.setDirection(bctPosition.partyRole());
                    logger.info("开始创建交易:[{}],产生的资金任务:[{}]", lcmEvent.getTradeId(), lcmEvent.getUuid());
                    return aopTradeTaskService.createAccountTradeTask(accountTradeTaskDto);
                }).filter(task -> Objects.nonNull(task)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountTradeTaskDTO createAccountTradeTask(AccountTradeTaskDTO taskDto) {
        String tradeId = taskDto.getTradeId();
        if (StringUtils.isBlank(tradeId)) {
            throw new CustomException("请输入交易编号tradeId");
        }
        String legalName = taskDto.getLegalName();
        if (StringUtils.isBlank(legalName)) {
            throw new CustomException("请输入交易对手legalName");
        }
        if (Objects.isNull(taskDto.getCashFlow())) {
            throw new CustomException("请输入现金流cashFlow");
        }
        LCMEventTypeEnum lcmEventType = taskDto.getLcmEventType();
        if (Objects.isNull(taskDto.getLcmEventType())) {
            throw new CustomException("请输入交易生命周期事件lcmEvent");
        }
        if (StringUtils.isBlank(taskDto.getAccountId())) {
            AccountDTO account = accountService.getAccountByLegalName(legalName);
            taskDto.setAccountId(account.getAccountId());
        }
        if (StringUtils.isBlank(taskDto.getLcmUUID())) {
            LCMEventDTO lcmEventDTO = tradeLCMService.listCashFlowsByTradeId(tradeId)
                    .stream()
                    .filter(lcmEvent ->
                            !accountTradeTaskRepo.existsByLcmUUID(lcmEvent.getUuid())
                                    && lcmEventType.equals(lcmEvent.getLcmEventType())
                    ).findFirst()
                    .orElseThrow(() -> new CustomException(""));
            taskDto.setLcmUUID(lcmEventDTO.getUuid());
            BctTradePosition bctPosition = positionService.getBctPositionById(lcmEventDTO.getPositionId());
            taskDto.setDirection(bctPosition.partyRole());
        }
        String lcmUUID = taskDto.getLcmUUID();
        if (!tradeLCMService.lcmEventExistByUUID(UUID.fromString(lcmUUID))) {
            logger.info(String.format("交易编号:[{}],lcmEvent:[{}]事件不存在,检查错误信息", taskDto.getTradeId(), lcmUUID));
            return null;
        }

        taskDto.setProcessStatus(ProcessStatusEnum.UN_PROCESSED);
        AccountTradeTask tradeTask = transToDbo(taskDto);
        return transToDto(accountTradeTaskRepo.save(tradeTask));
    }

    @Override
    public AccountTradeTaskDTO updateAccountTradeTask(AccountTradeTaskDTO tradeTaskDto) {
        String uuid = tradeTaskDto.getUuid();
        if (StringUtils.isBlank(uuid)) {
            throw new CustomException("请输入账户交易任务唯一标识uuid");
        }
        if (!accountTradeTaskRepo.existsById(UUID.fromString(uuid))) {
            throw new CustomException(String.format("查询唯一标识:[%s],数据记录不存在", uuid));
        }
        AccountTradeTask tradeTask = transToDbo(tradeTaskDto);
        return transToDto(accountTradeTaskRepo.save(tradeTask));
    }


    @Override
    public List<AccountTradeTaskDTO> findTasksByExample(AccountTradeTaskDTO tradeTaskDto) {
        AccountTradeTask tradeTask = transToDbo(tradeTaskDto);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withIgnoreCase();
        return accountTradeTaskRepo.findAll(Example.of(tradeTask, exampleMatcher))
                .stream()
                .map(this::transToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AccountTradeTaskDTO> markTradeTaskProcessed(List<String> uuidList) {
        if (CollectionUtils.isEmpty(uuidList)) {
            throw new CustomException("请输入待标记");
        }
        return uuidList.stream()
                .map(uuid -> {
                    AccountTradeTask tradeTask = accountTradeTaskRepo.findById(UUID.fromString(uuid))
                            .orElseThrow(() -> new CustomException(String.format("查询唯一标识:[%s],数据记录不存在", uuid)));
                    tradeTask.setProcessStatus(ProcessStatusEnum.PROCESSED);
                    return transToDto(accountTradeTaskRepo.save(tradeTask));
                }).collect(Collectors.toList());
    }

    private AccountTradeTaskDTO transToDto(AccountTradeTask tradeTask) {
        AccountTradeTaskDTO tradeTaskDto = new AccountTradeTaskDTO();
        UUID uuid = tradeTask.getUuid();
        BeanUtils.copyProperties(tradeTask, tradeTaskDto);
        tradeTaskDto.setUuid(Objects.isNull(uuid) ? null : uuid.toString());

        return tradeTaskDto;
    }

    private AccountTradeTask transToDbo(AccountTradeTaskDTO tradeTaskDto) {
        AccountTradeTask tradeTask = new AccountTradeTask();
        String uuid = tradeTaskDto.getUuid();
        BeanUtils.copyProperties(tradeTaskDto, tradeTask);
        tradeTask.setUuid(StringUtils.isBlank(uuid) ? null : UUID.fromString(uuid));

        return tradeTask;
    }
}
