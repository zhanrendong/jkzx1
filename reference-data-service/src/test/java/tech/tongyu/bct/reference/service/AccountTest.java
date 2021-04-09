package tech.tongyu.bct.reference.service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.beans.BeanUtils;
import tech.tongyu.bct.client.dto.AccountEvent;
import tech.tongyu.bct.client.dto.AccountOpRecordStatus;
import tech.tongyu.bct.reference.dao.dbo.Account;
import tech.tongyu.bct.reference.dao.dbo.AccountOpRecord;
import tech.tongyu.bct.reference.service.impl.AccountLocker;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class AccountTest {

    private Account normalAccount;
    private Account debtAccount;
    private Account invalidAccount;
    private Account normalAccount2;

    @Test
    public void CreateAccount() {
        Account account = new Account();
        account.initDefaltValue();
        assertTrue(account.getNormalStatus());
    }

    @Test
    @Ignore("http://jira.tongyu.tech/browse/OTMS-2299")
    public void accountLockerTest() throws InterruptedException {
        Account account = new Account();
        account.initDefaltValue();
        account.setAccountId("test");
        for (int i = 0; i < 10000; i++) {
            new Thread(() -> {
                final BigDecimal[] cash = new BigDecimal[1];
                Account accountAfter = AccountLocker.getInstance().lockAndExecute(() -> {
                    cash[0] = account.getCash();
                    account.setCash(account.getCash().add(BigDecimal.TEN));
                    Account accountResult = new Account();
                    BeanUtils.copyProperties(account, accountResult);
                    return accountResult;
                }, "test");
                assertEquals(accountAfter.getCash(), cash[0].add(BigDecimal.TEN));
            }).start();
        }
        Thread.sleep(5000L);
        assertEquals("100000", account.getCash().toPlainString());
    }

    @Test
    public void CreateValidAccountWithParameter() {
        Account account = new Account("1",
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(13),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertTrue(account.getNormalStatus());
    }

    @Test
    public void CreateInvalidAccountWithParameter() {
        Account account = new Account("1",
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(-10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(2),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("现金信息无效."));

        account = new Account("1",
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(-2),
                BigDecimal.valueOf(14),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("负债信息无效."));

        account = new Account("1",
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(3),
                BigDecimal.valueOf(9),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("负债和可用资金都不为0."));

        account = new Account("1",
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(-10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(33),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("已用授信额度无效."));

        account = new Account("1",
                BigDecimal.valueOf(15),
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(13),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("已用授信额度超出最大授信额度."));

        account = new Account("1",
                BigDecimal.valueOf(-1),
                BigDecimal.valueOf(27),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(13),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("保证金信息无效."));

        account = new Account("1",
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(-1),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("出入金总额信息无效."));

        account = new Account("1",
                BigDecimal.valueOf(14),
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(13),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        assertFalse(account.getNormalStatus());
        assertTrue(account.getAccountInformation().contains("资金无法配平"));
    }
    
    @Before
    public void initAccounts() {
        normalAccount = new Account("normal",
                BigDecimal.valueOf(15.2),
                BigDecimal.valueOf(10.3),
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(9.8),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(13.0),
                BigDecimal.valueOf(8.2),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        debtAccount = new Account("debt",
                BigDecimal.valueOf(15.2),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(4.4),
                BigDecimal.valueOf(4.0),
                BigDecimal.valueOf(-7.7),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        invalidAccount = new Account("invalid",
                BigDecimal.valueOf(16.0),
                BigDecimal.valueOf(10.3),
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(9.8),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(13.0),
                BigDecimal.valueOf(8.2),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
        normalAccount2 = new Account("normal2",
                BigDecimal.valueOf(15.2),
                BigDecimal.valueOf(5.3),
                BigDecimal.valueOf(5.5),
                BigDecimal.valueOf(9.8),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(8.2),
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0));
    }

    @Test
    @Ignore
    public void startTrade() {
//        AccountOpRecord record = normalAccount.tryInitializeTrade(
//                "out trade", new BigDecimal("2.1"), new BigDecimal("8.8"));
//        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.START_TRADE,
//                "8.8", "-6.7", "2.1", "0", "0", "0", "0", "0", "out trade", null));
//        accountCompare(normalAccount, accountFromString("normal", "24", "3.6", "-3.4", "9.8", "13", "8.2", "20", "0"));
//
//        initAccounts();
//        record = normalAccount.tryInitializeTrade("in trade", new BigDecimal("13.3"), new BigDecimal(0));
//        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.START_TRADE,
//                "0", "3.5", "13.3", "-9.8", "0", "0", "0", "0", "in trade", null));
//        accountCompare(normalAccount, accountFromString("normal", "15.2", "13.8", "7.8", "0", "13", "8.2", "20", "0"));
//
//        initAccounts();
//        record = normalAccount.tryInitializeTrade("out trade use credit",
//                new BigDecimal("0.5"), new BigDecimal("18.1"));
//        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.START_TRADE,
//                "18.1", "-10.3", "0.5", "7.3", "0", "0", "0", "0", "out trade use credit", null));
//        accountCompare(normalAccount, accountFromString(
//                "normal", "33.3", "0", "-5", "17.1", "13", "8.2", "20", "0"));
//
//        initAccounts();
//        record = normalAccount.tryInitializeTrade("reject trade", new BigDecimal(-1), new BigDecimal("20"));
//        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
//        assertEquals("Inconsistent event.", AccountEvent.START_TRADE, record.getEvent());
//        assertEquals("Unexpected message.", "没有足够的资金, 21 required, 20.5 available.", record.getInformation());
//        accountCompare(normalAccount, accountFromString(
//                "normal", "15.2", "10.3", "-5.5", "9.8", "13", "8.2", "20", "0"));
//
//        initAccounts();
//        record = debtAccount.tryInitializeTrade("in trade with debt", new BigDecimal(5), new BigDecimal(0));
//        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.START_TRADE,
//                "0", "0", "5", "-0.6", "0", "0", "0", "-4.4", "in trade with debt", null));
//        accountCompare(debtAccount, accountFromString("debt", "15.2", "0", "-0.5", "19.4", "4", "-7.7", "20", "0"));
//
//        initAccounts();
//        record = debtAccount.tryInitializeTrade("reject trade with debt",
//                new BigDecimal(2), new BigDecimal("1.8"));
//        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
//        assertEquals("Inconsistent event.", AccountEvent.START_TRADE, record.getEvent());
//        assertEquals("Unexpected message.", "没有足够的资金, -0.2 required, -4.4 available.", record.getInformation());
//        accountCompare(debtAccount, accountFromString("debt", "15.2", "0", "-5.5", "20", "4", "-7.7", "20", "4.4"));
//
//        initAccounts();
//        record = invalidAccount.tryInitializeTrade("invalid account trade", new BigDecimal(3), new BigDecimal("6.4"));
//        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
//        assertEquals("Inconsistent event.", AccountEvent.START_TRADE, record.getEvent());
//        assertEquals("Unexpected message.", invalidAccount.getAccountInformation(), record.getInformation());
//        accountCompare(invalidAccount, accountFromString(
//                "invalid", "16.0", "10.3", "-5.5", "9.8", "13.0", "8.2", "20", "0"));
//
//        initAccounts();
//        record = normalAccount.tryInitializeTrade("neg. margin trade", new BigDecimal("1.3"), new BigDecimal("-1.5"));
//        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
//        assertEquals("Inconsistent event.", AccountEvent.START_TRADE, record.getEvent());
//        assertEquals("Unexpected message.", "支付的保证金无效.", record.getInformation());
//        accountCompare(normalAccount, accountFromString(
//                "normal", "15.2", "10.3", "-5.5", "9.8", "13", "8.2", "20", "0"));
    }

    @Test
    @Ignore
    public void terminateTrade() {
//        AccountOpRecord record = normalAccount.terminateTrade("in trade",
//                new BigDecimal("-0.5"), new BigDecimal("15.3"), new BigDecimal("2.2"));
//        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.TERMINATE_TRADE,
//                "-2.2", "7.7", "0.5", "-9.8", "0", "14.8", "0", "0", "in trade", null));
//        accountCompare(normalAccount, accountFromString("normal", "13", "18", "-5", "0", "13", "23", "20", "0"));
//
//        initAccounts();
//        record = normalAccount.terminateTrade("out trade",
//                new BigDecimal("1.2"), new BigDecimal("-16"), new BigDecimal("1"));
//        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.TERMINATE_TRADE,
//                "-1", "-10.3", "-1.2", "4.7", "0", "-14.8", "0", "0", "out trade", null));
//        accountCompare(normalAccount, accountFromString(
//                "normal", "14.2", "0", "-6.7", "14.5", "13", "-6.6", "20", "0"));
//
//        initAccounts();
//        record = normalAccount.terminateTrade("end trade cause debt",
//                new BigDecimal("2"), new BigDecimal("-26.7"), new BigDecimal("2"));
//        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.TERMINATE_TRADE,
//                "-2", "-10.3", "-2", "10.2", "0", "-24.7", "0", "4.2", "end trade cause debt", null));
//        accountCompare(normalAccount, accountFromString(
//                "normal", "13.2", "0", "-7.5", "20", "13", "-16.5", "20", "4.2"));
//
//        initAccounts();
//        record = normalAccount.terminateTrade("too big margin release",
//                new BigDecimal("0.5"), new BigDecimal("-6.6"), new BigDecimal(20));
//        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.TERMINATE_TRADE,
//                "-15.2", "0", "-0.5", "-8.6", "0", "-6.1", "0", "0", "too big margin release", null));
//        accountCompare(normalAccount, accountFromString("normal", "0", "10.3", "-6", "1.2", "13", "2.1", "20", "0"));
//
//        initAccounts();
//        record = debtAccount.terminateTrade("in trade with debt",
//                new BigDecimal("-2.5"), new BigDecimal("25.2"), new BigDecimal("3"));
//        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.TERMINATE_TRADE,
//                "-3", "3.8", "2.5", "-20", "0", "22.7", "0", "-4.4", "in trade with debt", null));
//        accountCompare(debtAccount, accountFromString("debt", "12.2", "3.8", "-3", "0", "4", "15", "20", "0"));
//
//        initAccounts();
//        record = debtAccount.terminateTrade("out trade with debt",
//                new BigDecimal("0.8"), new BigDecimal("-2.1"), new BigDecimal("0.8"));
//        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.TERMINATE_TRADE,
//                "-0.8", "0", "-0.8", "0", "0", "-1.3", "0", "1.3", "out trade with debt", null));
//        accountCompare(debtAccount, accountFromString("debt", "14.4", "0", "-6.3", "20", "4", "-9", "20", "5.7"));
//
//        initAccounts();
//        record = normalAccount.terminateTrade("neg. margin trade",
//                new BigDecimal("-0.5"), new BigDecimal("-2.2"), new BigDecimal("-1.8"));
//        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
//        assertEquals("Inconsistent event.", AccountEvent.TERMINATE_TRADE, record.getEvent());
//        assertEquals("Unexpected massage.", "保证金释放无效.", record.getInformation());
//        accountCompare(normalAccount, accountFromString(
//                "normal", "15.2", "10.3", "-5.5", "9.8", "13", "8.2", "20", "0"));
//
//        initAccounts();
//        record = invalidAccount.terminateTrade("invalid account trade",
//                new BigDecimal("-0.5"), new BigDecimal("15.3"), new BigDecimal("2.2"));
//        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
//        assertEquals("Inconsistent event.", AccountEvent.TERMINATE_TRADE, record.getEvent());
//        assertEquals("Unexpected message.", invalidAccount.getAccountInformation(), record.getInformation());
//        accountCompare(invalidAccount, accountFromString(
//                "invalid", "16.0", "10.3", "-5.5", "9.8", "13.0", "8.2", "20", "0"));
    }

    @Test
    @Ignore
    public void changeMargin() {
//         margin between minimum and call margin
        AccountOpRecord record = normalAccount.marginChange(BigDecimal.valueOf(1), BigDecimal.valueOf(20), BigDecimal.valueOf(16));
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.REEVALUATE_MARGIN, record.getEvent());
        assertEquals("Unexpected message.", "保证金没有变化.", record.getInformation());
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.3", "5.5", "9.8", "13", "8.2", "20", "0"));

        // margin between call and initial margin
        initAccounts();
        record = normalAccount.marginChange(BigDecimal.valueOf(1), BigDecimal.valueOf(20), BigDecimal.valueOf(14));
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.REEVALUATE_MARGIN, record.getEvent());
        assertEquals("Unexpected message.", "保证金没有变化.", record.getInformation());
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.3", "5.5", "9.8", "13", "8.2", "20", "0"));

        // release margin to credit
        initAccounts();
        record = normalAccount.marginChange(BigDecimal.valueOf(1), BigDecimal.valueOf(8.3), BigDecimal.valueOf(5));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.REEVALUATE_MARGIN, "-6.9", "0", "0", "-6.9", "0", "0", "0", "0", null, null));
        accountCompare(normalAccount, accountFromString(
                "normal", "8.3", "10.3", "5.5", "2.9", "13", "8.2", "20", "0"));

        // release margin to credit and cash
        initAccounts();
        record = normalAccount.marginChange(BigDecimal.valueOf(1), BigDecimal.valueOf(3.6), BigDecimal.valueOf(2));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.REEVALUATE_MARGIN, "-11.6", "1.8", "0", "-9.8", "0", "0", "0", "0", null, null));
        accountCompare(normalAccount, accountFromString("normal", "3.6", "12.1", "5.5", "0", "13", "8.2", "20", "0"));

        // call margin using cash
        initAccounts();
        record = normalAccount.marginChange(BigDecimal.valueOf(16), BigDecimal.valueOf(28), BigDecimal.valueOf(22));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.REEVALUATE_MARGIN, "6.8", "-6.8", "0", "0", "0", "0", "0", "0", null, null));
        accountCompare(normalAccount, accountFromString("normal", "22", "3.5", "5.5", "9.8", "13", "8.2", "20", "0"));

        // call margin using cash and credit
        initAccounts();
        record = normalAccount.marginChange(BigDecimal.valueOf(18), BigDecimal.valueOf(32.3), BigDecimal.valueOf(27));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.REEVALUATE_MARGIN, "11.8", "-10.3", "0", "1.5", "0", "0", "0", "0", null, null));
        accountCompare(normalAccount, accountFromString("normal", "27", "0", "5.5", "11.3", "13", "8.2", "20", "0"));

        // call margin and generate debt
        initAccounts();
        record = normalAccount.marginChange(BigDecimal.valueOf(18), BigDecimal.valueOf(45), BigDecimal.valueOf(39));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.REEVALUATE_MARGIN, "23.8", "-10.3", "0", "10.2", "0", "0", "0", "3.3", null, null));
        accountCompare(normalAccount, accountFromString("normal", "39", "0", "5.5", "20", "13", "8.2", "20", "3.3"));

        // release margin and pay debt
        initAccounts();
        record = debtAccount.marginChange(BigDecimal.valueOf(5), BigDecimal.valueOf(13.5), BigDecimal.valueOf(10));
        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.REEVALUATE_MARGIN,
                "-1.7", "0", "0", "0", "0", "0", "0", "-1.7", null, null));
        accountCompare(debtAccount, accountFromString("debt", "13.5", "0", "5.5", "20", "4", "-7.7", "20", "2.7"));

        // release margin and pay all debt
        initAccounts();
        record = debtAccount.marginChange(BigDecimal.valueOf(2), BigDecimal.valueOf(8), BigDecimal.valueOf(5.5));
        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.REEVALUATE_MARGIN,
                "-7.2", "0", "0", "-2.8", "0", "0", "0", "-4.4", null, null));
        accountCompare(debtAccount, accountFromString("debt", "8", "0", "5.5", "17.2", "4", "-7.7", "20", "0"));

        // invalid account
        initAccounts();
        record = invalidAccount.marginChange(BigDecimal.valueOf(4), BigDecimal.valueOf(10), BigDecimal.valueOf(8));
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.REEVALUATE_MARGIN, record.getEvent());
        assertEquals("Unexpected message.", invalidAccount.getAccountInformation(), record.getInformation());
        accountCompare(invalidAccount, accountFromString(
                "invalid", "16.0", "10.3", "5.5", "9.8", "13.0", "8.2", "20", "0"));
    }
    
    @Test
    @Ignore
    public void deposit() {
        AccountOpRecord record = normalAccount.deposit(BigDecimal.valueOf(7), "small deposit");
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.DEPOSIT,
                "0", "0", "0", "-7", "7", "0", "0", "0", null, "small deposit"));
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.3", "5.5", "2.8", "20", "8.2", "20", "0"));

        initAccounts();
        record = normalAccount.deposit(BigDecimal.valueOf(12.5), "big deposit");
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.DEPOSIT,
                "0", "2.7", "0", "-9.8", "12.5", "0", "0", "0", null, "big deposit"));
        accountCompare(normalAccount, accountFromString("normal", "15.2", "13", "5.5", "0", "25.5", "8.2", "20", "0"));

        initAccounts();
        record = debtAccount.deposit(BigDecimal.valueOf(32), "deposit pay debt");
        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.DEPOSIT,
                "0", "7.6", "0", "-20", "32", "0", "0", "-4.4", null, "deposit pay debt"));
        accountCompare(debtAccount, accountFromString("debt", "15.2", "7.6", "5.5", "0", "36", "-7.7", "20", "0"));

        initAccounts();
        record = normalAccount.deposit(BigDecimal.valueOf(-3), "negative deposit");
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.DEPOSIT, record.getEvent());
        assertEquals("Unexpected message.", "入金金额无效, -3.", record.getInformation());
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.3", "5.5", "9.8", "13", "8.2", "20", "0"));

        initAccounts();
        record = invalidAccount.deposit(BigDecimal.valueOf(10), "invalid account");
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.DEPOSIT, record.getEvent());
        assertEquals("Unexpected message.", invalidAccount.getAccountInformation(), record.getInformation());
        accountCompare(invalidAccount, accountFromString(
                "invalid", "16.0", "10.3", "5.5", "9.8", "13.0", "8.2", "20", "0"));
    }

    @Test
    @Ignore
    public void withdraw() {
        AccountOpRecord record = normalAccount.tryWithdraw(BigDecimal.valueOf(0.2), "small withdraw");
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.WITHDRAW,
                "0", "-0.2", "0", "0", "-0.2", "0", "0", "0", null, "small withdraw"));
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.1", "5.5", "9.8", "12.8", "8.2", "20", "0"));

        initAccounts();
        record = normalAccount.tryWithdraw(BigDecimal.valueOf(0.5), "withdraw all");
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.WITHDRAW,
                "0", "-0.5","0", "0", "-0.5", "0", "0", "0", null, "withdraw all"));
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "9.8", "5.5", "9.8", "12.5", "8.2", "20", "0"));

        initAccounts();
        record = normalAccount.tryWithdraw(BigDecimal.valueOf(0.501), "reject withdraw");
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.WITHDRAW, record.getEvent());
        assertEquals("Unexpected message.", "没有足够现金提取, 0.501 wanted, 0.5 available.",
                record.getInformation());
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.3", "5.5", "9.8", "13", "8.2", "20", "0"));

        initAccounts();
        record = debtAccount.tryWithdraw(BigDecimal.valueOf(0.01), "withdraw debt account");
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.WITHDRAW, record.getEvent());
        assertEquals("Unexpected message.", "没有足够现金提取, 0.01 wanted, 0 available.",
                record.getInformation());
        accountCompare(debtAccount, accountFromString("debt", "15.2", "0", "5.5", "20", "4", "-7.7", "20", "4.4"));

        initAccounts();
        record = invalidAccount.tryWithdraw(BigDecimal.valueOf(0.2), "withdraw invalid account");
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.WITHDRAW, record.getEvent());
        assertEquals("Unexpected message.", invalidAccount.getAccountInformation(), record.getInformation());
        accountCompare(invalidAccount, accountFromString(
                "invalid", "16.0", "10.3", "5.5", "9.8", "13.0", "8.2", "20", "0"));
    }

    @Test
    @Ignore
    public void cashflow() {
        AccountOpRecord record = normalAccount.tradeCashFlowEvent("flow in", BigDecimal.valueOf(13.8), BigDecimal.valueOf(1));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.TRADE_CASH_FLOW, "1", "3", "0", "-9.8", "0", "13.8", "0", "0", "flow in", null));
        accountCompare(normalAccount, accountFromString("normal", "16.2", "13.3", "5.5", "0", "13.0", "22.0", "20", "0"));

        initAccounts();
        record = normalAccount.tradeCashFlowEvent("flow out", BigDecimal.valueOf(-7.2), BigDecimal.valueOf(-1));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.TRADE_CASH_FLOW, "-1.0", "-7.2", "0", "-1.0", "0", "-7.2", "0", "0", "flow out", null));
        accountCompare(normalAccount, accountFromString("normal", "14.2", "3.1", "5.5", "8.8", "13.0", "1.0", "20", "0"));

        initAccounts();
        record = normalAccount.tradeCashFlowEvent("flow out use debt", BigDecimal.valueOf(-28.1), BigDecimal.valueOf(-1.1));
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL,
                AccountEvent.TRADE_CASH_FLOW, "-1.1", "-10.3", "0", "10.2", "0", "-28.1", "0", "6.5",
                "flow out use debt", null));
        accountCompare(normalAccount, accountFromString(
                "normal", "14.1", "0", "5.5", "20", "13.0", "-19.9", "20", "6.5"));

        initAccounts();
        record = debtAccount.tradeCashFlowEvent("flow in pay debt", BigDecimal.valueOf(7.1), BigDecimal.valueOf(1));
        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.TRADE_CASH_FLOW,
                "1", "0", "0", "-1.7", "0", "7.1", "0", "-4.4", "flow in pay debt", null));
        accountCompare(debtAccount, accountFromString("debt", "16.2", "0", "5.5", "18.3", "4.0", "-0.6", "20", "0"));

        initAccounts();
        record = invalidAccount.tradeCashFlowEvent("invalid account cashflow", BigDecimal.valueOf(-1), BigDecimal.valueOf(-4));
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.TRADE_CASH_FLOW, record.getEvent());
        assertEquals("Unexpected message.", invalidAccount.getAccountInformation(), record.getInformation());
        accountCompare(invalidAccount, accountFromString(
                "invalid", "16.0", "10.3", "5.5", "9.8", "13.0", "8.2", "20", "0"));
    }

    @Test
    @Ignore
    public void changeCredit() {
        AccountOpRecord record = normalAccount.changeCredit(BigDecimal.valueOf(9.8), "reduce credit");
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.CHANGE_CREDIT,
                "0", "0", "0", "0", "0", "0", "-10.2", "0", null, "reduce credit"));
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.3", "5.5", "9.8", "13", "8.2", "9.8", "0"));

        initAccounts();
        record = normalAccount.changeCredit(BigDecimal.valueOf(5), "reduce credit and cash");
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.CHANGE_CREDIT,
                "0", "-4.8", "0", "-4.8", "0", "0", "-15", "0", null, "reduce credit and cash"));
        accountCompare(normalAccount, accountFromString("normal", "15.2", "5.5", "5.5", "5", "13", "8.2", "5", "0"));

        initAccounts();
        record = normalAccount2.changeCredit(BigDecimal.valueOf(0), "reject credit change");
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.CHANGE_CREDIT, record.getEvent());
        assertEquals("Unexpected message.", "Not enough fund to change credit, 20 required, 15.5 available.",
                record.getInformation());
        accountCompare(normalAccount2, accountFromString(
                "normal2", "15.2", "5.3", "5.5", "9.8", "8", "8.2", "20", "0"));

        initAccounts();
        record = normalAccount.changeCredit(BigDecimal.valueOf(30), "increase credit");
        recordCompare(record, recordFromString(normalAccount, AccountOpRecordStatus.NORMAL, AccountEvent.CHANGE_CREDIT,
                "0", "0", "0", "0", "0", "0", "10", "0", null , "increase credit"));
        accountCompare(normalAccount, accountFromString(
                "normal", "15.2", "10.3", "5.5", "9.8", "13", "8.2", "30", "0"));

        initAccounts();
        record = debtAccount.changeCredit(BigDecimal.valueOf(30), "raise debt account credit");
        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.CHANGE_CREDIT,
                "0", "0", "0", "4.4", "0", "0", "10", "-4.4", null, "raise debt account credit"));
        accountCompare(debtAccount, accountFromString("debt", "15.2", "0", "5.5", "24.4", "4", "-7.7", "30", "0"));

        initAccounts();
        record = debtAccount.changeCredit(BigDecimal.valueOf(22), "raise debt credit small");
        recordCompare(record, recordFromString(debtAccount, AccountOpRecordStatus.NORMAL, AccountEvent.CHANGE_CREDIT,
                "0", "0", "0", "2", "0", "0", "2", "-2", null, "raise debt credit small"));
        accountCompare(debtAccount, accountFromString("debt", "15.2", "0", "5.5", "22", "4", "-7.7", "22", "2.4"));

        initAccounts();
        record = invalidAccount.changeCredit(BigDecimal.valueOf(0), "invalid account credit change");
        assertEquals("Invalid record passes.", AccountOpRecordStatus.INVALID, record.getStatus());
        assertEquals("Inconsistent event.", AccountEvent.CHANGE_CREDIT, record.getEvent());
        assertEquals("Unexpected message.", invalidAccount.getAccountInformation(), record.getInformation());
        accountCompare(invalidAccount, accountFromString(
                "invalid", "16.0", "10.3", "5.5", "9.8", "13.0", "8.2", "20", "0"));
    }

    private AccountOpRecord recordFromString(Account account, AccountOpRecordStatus status, AccountEvent event, String margin,
                                             String cash, String premium, String creditUsed, String deposit, String pnl,
                                             String credit, String debt, String trade, String information) {
        AccountOpRecord record =  new AccountOpRecord(trade, account.getAccountId(), event, ZonedDateTime.now(), status,
                new BigDecimal(margin), new BigDecimal(cash), new BigDecimal(premium), new BigDecimal(creditUsed),
                new BigDecimal(debt), new BigDecimal(deposit), new BigDecimal(pnl), new BigDecimal(credit),
                BigDecimal.valueOf(0),BigDecimal.valueOf(0),BigDecimal.valueOf(0),BigDecimal.valueOf(0));
        record.setStatus(status);
        record.setInformation(information);
        record.setAccountInformation(account);
        return record;
    }

    private Account accountFromString(String accountId, String margin, String cash, String premium, String creditUsed,
                                      String deposit, String pnl, String credit, String debt) {
        return new Account(accountId, new BigDecimal(margin), new BigDecimal(cash),
                new BigDecimal(premium), new BigDecimal(creditUsed),new BigDecimal(debt), new BigDecimal(deposit), new BigDecimal(pnl),
                new BigDecimal(credit), BigDecimal.valueOf(0),BigDecimal.valueOf(0),BigDecimal.valueOf(0),BigDecimal.valueOf(0));
    }

    private void recordCompare(AccountOpRecord left, AccountOpRecord right) {
        assertEquals("Inconsistent account.", right.getAccountId(), left.getAccountId());
        assertEquals("Inconsistent event.", right.getEvent(), left.getEvent());
        assertEquals("Inconsistent status.", right.getStatus(), left.getStatus());
        assertEquals("Inconsistent information.", right.getInformation(), left.getInformation());
        assertEquals("Inconsistent trade.", right.getTradeId(), left.getTradeId());
        assertEquals("Inconsistent margin change.", 0, right.getMarginChange().compareTo(left.getMarginChange()));
        assertEquals("Inconsistent cash change.", 0, right.getCashChange().compareTo(left.getCashChange()));
        assertEquals("Inconsistent premium change.", 0, right.getPremiumChange().compareTo(left.getPremiumChange()));
        assertEquals("Inconsistent used credit change.", 0,
                right.getCreditUsedChange().compareTo(left.getCreditUsedChange()));
        assertEquals("Inconsistent debt change.", 0, right.getDebtChange().compareTo(left.getDebtChange()));
        assertEquals("Inconsistent deposit change.", 0,
                right.getNetDepositChange().compareTo(left.getNetDepositChange()));
        assertEquals("Inconsistent PnL change.", 0,
                right.getRealizedPnLChange().compareTo(left.getRealizedPnLChange()));
        assertEquals("Inconsistent credit change.", 0, right.getCreditChange().compareTo(left.getCreditChange()));
        assertEquals("Inconsistent margin.", 0, right.getMargin().compareTo(left.getMargin()));
        assertEquals("Inconsistent cash.", 0, right.getCash().compareTo(left.getCash()));
        assertEquals("Inconsistent premium.", 0, right.getPremium().compareTo(left.getPremium()));
        assertEquals("Inconsistent used credit.", 0, right.getCreditUsed().compareTo(left.getCreditUsed()));
        assertEquals("Inconsistent debt.", 0, right.getDebt().compareTo(left.getDebt()));
        assertEquals("Inconsistent deposit.", 0, right.getNetDeposit().compareTo(left.getNetDeposit()));
        assertEquals("Inconsistent PnL.", 0, right.getRealizedPnL().compareTo(left.getRealizedPnL()));
        assertEquals("Inconsistent credit.", 0, right.getCredit().compareTo(left.getCredit()));
    }

    private void accountCompare(Account left, Account right) {
        assertEquals("Account not match.", right.getAccountId(), left.getAccountId());
        assertEquals("Account status not match.", right.getNormalStatus(), left.getNormalStatus());
        assertEquals("Account information not match.", right.getAccountInformation(), left.getAccountInformation());
        assertEquals("Margin not match.", 0, right.getMargin().compareTo(left.getMargin()));
        assertEquals("Cash not match.", 0, right.getCash().compareTo(left.getCash()));
        assertEquals("Premium not match.", 0, right.getPremium().compareTo(left.getPremium()));
        assertEquals("Used credit not match.", 0, right.getCreditUsed().compareTo(left.getCreditUsed()));
        assertEquals("Debt not match.", 0, right.getDebt().compareTo(left.getDebt()));
        assertEquals("Deposit not match.", 0, right.getNetDeposit().compareTo(left.getNetDeposit()));
        assertEquals("PnL not match.", 0, right.getRealizedPnL().compareTo(left.getRealizedPnL()));
        assertEquals("Credit not match.", 0, right.getCredit().compareTo(left.getCredit()));
    }

}
