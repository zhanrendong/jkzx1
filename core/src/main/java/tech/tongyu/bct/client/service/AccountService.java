package tech.tongyu.bct.client.service;

import tech.tongyu.bct.client.dto.AccountDTO;
import tech.tongyu.bct.client.dto.AccountEvent;
import tech.tongyu.bct.client.dto.AccountOpRecordDTO;
import tech.tongyu.bct.reference.dto.PartyDTO;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    String SCHEMA = "ClientService";

    /**
     * 根据交易对手查询账户信息
     * @param legalNames
     * @return
     */
    List<AccountDTO> findAccountsByLegalNames(List<String> legalNames);


    /**
     * 通过accountId查找指定的帐号信息
     * @param accountIds
     * @return
     */
    List<AccountDTO> findAccountsByAccountIds(List<String> accountIds);

    /**
     * 根据交易对手查询账户信息
     * @param legalName
     * @return
     */
    AccountDTO getAccountByLegalName(String legalName);
    /**
     * 新增账户信息
     * @param legalName
     * @return
     */
    AccountDTO saveAccount(String legalName);
    /**
     * 新增账户操作记录
     * @param accountOpRecordDTO
     * @return
     */
    AccountOpRecordDTO saveAccountOpRecord(AccountOpRecordDTO accountOpRecordDTO);
    /**
     * 账户入金操作
     * @param accountId
     * @param amount
     * @param information
     * @return
     */
    AccountOpRecordDTO deposit(String accountId, String amount, String information);
    /**
     * 账户出金操作
     * @param accountId
     * @param amount
     * @param information
     * @return
     */
    AccountOpRecordDTO withdraw(String accountId, String amount, String information);
    /**
     * 调整授信总额
     * @param accountId
     * @param credit
     * @param information
     * @return
     */
    AccountOpRecordDTO updateCredit(String accountId, BigDecimal credit,BigDecimal counterPartyCredit, String information);
    /**
     * 账户新增交易
     * @param accountId
     * @param premium
     * @param information
     * @return
     */
    AccountOpRecordDTO changePremium(String accountId, String tradeId, BigDecimal premium, String information);
    /**
     * 账户结算交易（包括平仓,行权）
     * @param accountId
     * @param amount
     * @param premium
     * @param event
     * @param information
     * @return
     */
    AccountOpRecordDTO settleTrade(String accountId, String tradeId, BigDecimal amount, BigDecimal premium,
                                   AccountEvent event, String information);
    /**
     * 账户现金流操作
     * @param accountId
     * @param tradeId
     * @param cashFlow
     * @param marginFlow
     * @return
     */
    AccountOpRecordDTO tradeCashFlow(String accountId, String tradeId, String cashFlow, String marginFlow);
    /**
     * 账户变更授信操作
     * @param accountId
     * @param amount
     * @param information
     * @return
     */
    AccountOpRecordDTO changeCredit(String accountId, String amount, String information);
    /**
     * 账户开始交易操作
     * @param accountId
     * @param tradeId
     * @param gainOrCost
     * @param marginToBePaid
     * @return
     */
    AccountOpRecordDTO startTrade(String accountId, String tradeId, String gainOrCost, String marginToBePaid);
    /**
     * 账户中止交易操作
     * @param accountId
     * @param tradeId
     * @param startGainOrCost
     * @param endGainOrCost
     * @param marginRelease
     * @return
     */
    AccountOpRecordDTO terminateTrade(String accountId, String tradeId, String startGainOrCost,
                                      String endGainOrCost, String marginRelease);
    /**
     * 账户变更保证金操作
     * @param accountId
     * @param minimalMargin
     * @param initialMargin
     * @param callMargin
     * @return
     */
    AccountOpRecordDTO changeMargin(String accountId, String minimalMargin, String initialMargin, String callMargin);
    /**
     * 获取账户列表信息
     * @return
     */
    List<AccountDTO> listAccounts();
    /**
     * 根据账户编号删除交易信息
     * @param accountId
     */
    void deleteByAccountId(String accountId);

    void deleteByLegalName(String legalName);
    /**
     * 根据交易对手信息搜索账户信息
     *
     * @param party
     * @return
     */
    List<AccountDTO> searchByParty(PartyDTO party);
    /**
     * 搜索账户信息
     * @param accountDto
     * @return
     */
    List<AccountDTO> accountSearch(AccountDTO accountDto);
    /**
     * 获取选定账户操作记录信息
     * @param accountIds
     * @return
     */
    List<AccountOpRecordDTO> listAccountOpRecords(List<String> accountIds);
    /**
     * 搜索账户操作记录
     * @param recordDto
     * @return
     */
    List<AccountOpRecordDTO> accountOpRecordSearch(AccountOpRecordDTO recordDto);

}


