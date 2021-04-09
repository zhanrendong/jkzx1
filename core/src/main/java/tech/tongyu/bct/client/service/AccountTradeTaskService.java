package tech.tongyu.bct.client.service;

import tech.tongyu.bct.client.dto.AccountTradeTaskDTO;

import java.util.List;

public interface AccountTradeTaskService {

    List<AccountTradeTaskDTO> generateTasksByTradeId(String tradeId, String legalName);

    AccountTradeTaskDTO createAccountTradeTask(AccountTradeTaskDTO accountTradeTaskDto);

    AccountTradeTaskDTO updateAccountTradeTask(AccountTradeTaskDTO accountTradeTaskDto);

    List<AccountTradeTaskDTO> findTasksByExample(AccountTradeTaskDTO accountTradeTaskDto);

    List<AccountTradeTaskDTO> markTradeTaskProcessed(List<String> uuidList);

    List<AccountTradeTaskDTO> search(AccountTradeTaskDTO taskDto);
}
