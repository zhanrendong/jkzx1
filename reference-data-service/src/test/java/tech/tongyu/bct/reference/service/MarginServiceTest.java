package tech.tongyu.bct.reference.service;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import tech.tongyu.bct.client.dto.AccountDTO;
import tech.tongyu.bct.client.service.AccountService;
import tech.tongyu.bct.reference.dao.dbo.Margin;
import tech.tongyu.bct.reference.dao.dbo.Party;
import tech.tongyu.bct.reference.dao.repl.intel.MarginRepo;
import tech.tongyu.bct.reference.dao.repl.intel.PartyRepo;
import tech.tongyu.bct.reference.dto.MarginCallStatus;
import tech.tongyu.bct.reference.dto.MarginDTO;
import tech.tongyu.bct.reference.service.impl.MarginServiceImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Matchers.any;

@RunWith(MockitoJUnitRunner.class)
public class MarginServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private PartyRepo partyRepo;

    @Mock
    private MarginRepo marginRepo;

    @Test
    public void getMargin_SuccessUnknown() {
        // MaintenanceMargin = null

        // prepare
        Margin marginDbo = getMarginDbo();
        marginDbo.setMaintenanceMargin(null);
        Mockito.when(marginRepo.findById(any())).thenReturn(Optional.of(marginDbo));

        Party partyDbo = getPartyDbo();
        partyDbo.setMaintenanceMargin(marginDbo);
        Mockito.when(partyRepo.findById(any())).thenReturn(Optional.of(partyDbo));

        AccountDTO accountDto = getAccountDto();
        accountDto.setCreditUsed(BigDecimal.valueOf(0.0));
        Mockito.when(accountService.getAccountByLegalName(any())).thenReturn(accountDto);

        // act
        MarginService service = getServiceInstance();
        MarginDTO result = service.getMargin(UUID.randomUUID()).orElse(null);

        // validate
        Assert.assertEquals(MarginCallStatus.UNKNOWN, result.getStatus());
    }

    @Test
    public void getMargin_SuccessNormalEqual() {
        // MaintenanceMargin = 10
        // margin = 10

        // prepare
        Margin marginDbo = getMarginDbo();
        marginDbo.setMaintenanceMargin(BigDecimal.valueOf(10.0));
        Mockito.when(marginRepo.findById(any())).thenReturn(Optional.of(marginDbo));

        Party partyDbo = getPartyDbo();
        partyDbo.setMaintenanceMargin(marginDbo);
        Mockito.when(partyRepo.findById(any())).thenReturn(Optional.of(partyDbo));

        AccountDTO accountDto = getAccountDto();
        accountDto.setMargin(BigDecimal.valueOf(10.0));
        accountDto.setCreditUsed(BigDecimal.valueOf(0.0));
        Mockito.when(accountService.getAccountByLegalName(any())).thenReturn(accountDto);

        // act
        MarginService service = getServiceInstance();
        MarginDTO result = service.getMargin(UUID.randomUUID()).orElse(null);

        // validate
        Assert.assertEquals(MarginCallStatus.NORMAL, result.getStatus());
    }

    @Test
    public void getMargin_SuccessNormalZero() {
        // MaintenanceMargin = 0
        // margin = 0

        // prepare
        Margin marginDbo = getMarginDbo();
        marginDbo.setMaintenanceMargin(BigDecimal.ZERO);
        Mockito.when(marginRepo.findById(any())).thenReturn(Optional.of(marginDbo));

        Party partyDbo = getPartyDbo();
        partyDbo.setMaintenanceMargin(marginDbo);
        Mockito.when(partyRepo.findById(any())).thenReturn(Optional.of(partyDbo));

        AccountDTO accountDto = getAccountDto();
        accountDto.setMargin(BigDecimal.ZERO);
        accountDto.setCreditUsed(BigDecimal.valueOf(0.0));
        Mockito.when(accountService.getAccountByLegalName(any())).thenReturn(accountDto);

        // act
        MarginService service = getServiceInstance();
        MarginDTO result = service.getMargin(UUID.randomUUID()).orElse(null);

        // validate
        Assert.assertEquals(MarginCallStatus.NORMAL, result.getStatus());
    }

    @Test
    public void getMargin_SuccessNormalGreat() {
        // MaintenanceMargin = 10
        // margin = 11

        // prepare
        Margin marginDbo = getMarginDbo();
        marginDbo.setMaintenanceMargin(BigDecimal.valueOf(10.0));
        Mockito.when(marginRepo.findById(any())).thenReturn(Optional.of(marginDbo));

        Party partyDbo = getPartyDbo();
        partyDbo.setMaintenanceMargin(marginDbo);
        Mockito.when(partyRepo.findById(any())).thenReturn(Optional.of(partyDbo));

        AccountDTO accountDto = getAccountDto();
        accountDto.setMargin(BigDecimal.valueOf(11.0));
        accountDto.setCreditUsed(BigDecimal.valueOf(0.0));
        Mockito.when(accountService.getAccountByLegalName(any())).thenReturn(accountDto);

        // act
        MarginService service = getServiceInstance();
        MarginDTO result = service.getMargin(UUID.randomUUID()).orElse(null);

        // validate
        Assert.assertEquals(MarginCallStatus.NORMAL, result.getStatus());
    }

    @Test
    public void getMargin_SuccessNotEnoughEqual() {
        // MaintenanceMargin = 13
        // margin = 10
        // counterPartyFund = 1
        // counterPartyCreditBalance = 2

        // prepare
        Margin marginDbo = getMarginDbo();
        marginDbo.setMaintenanceMargin(BigDecimal.valueOf(13.0));
        Mockito.when(marginRepo.findById(any())).thenReturn(Optional.of(marginDbo));

        Party partyDbo = getPartyDbo();
        partyDbo.setMaintenanceMargin(marginDbo);
        Mockito.when(partyRepo.findById(any())).thenReturn(Optional.of(partyDbo));

        AccountDTO accountDto = getAccountDto();
        accountDto.setMargin(BigDecimal.valueOf(10.0));
        accountDto.setCash(BigDecimal.valueOf(1.0));
        accountDto.setCredit(BigDecimal.valueOf(2.0));
        accountDto.setCreditUsed(BigDecimal.valueOf(0.0));
        accountDto.setCreditBalance(BigDecimal.valueOf(2.0));
        Mockito.when(accountService.getAccountByLegalName(any())).thenReturn(accountDto);

        // act
        MarginService service = getServiceInstance();
        MarginDTO result = service.getMargin(UUID.randomUUID()).orElse(null);

        // validate
        Assert.assertEquals(MarginCallStatus.NOT_ENOUGH, result.getStatus());
    }

    @Test
    public void getMargin_SuccessNotEnoughLessThan() {
        // MaintenanceMargin = 13
        // margin = 10
        // gap = 3
        // counterPartyFund = 2
        // counterPartyCreditBalance = 2

        // prepare
        Margin marginDbo = getMarginDbo();
        marginDbo.setMaintenanceMargin(BigDecimal.valueOf(13.0));
        Mockito.when(marginRepo.findById(any())).thenReturn(Optional.of(marginDbo));

        Party partyDbo = getPartyDbo();
        partyDbo.setMaintenanceMargin(marginDbo);
        Mockito.when(partyRepo.findById(any())).thenReturn(Optional.of(partyDbo));

        AccountDTO accountDto = getAccountDto();
        accountDto.setMargin(BigDecimal.valueOf(10.0));
        accountDto.setCash(BigDecimal.valueOf(2.0));
        accountDto.setCredit(BigDecimal.valueOf(2.0));
        accountDto.setCreditUsed(BigDecimal.valueOf(0.0));
        accountDto.setCreditBalance(BigDecimal.valueOf(2.0));
        Mockito.when(accountService.getAccountByLegalName(any())).thenReturn(accountDto);

        // act
        MarginService service = getServiceInstance();
        MarginDTO result = service.getMargin(UUID.randomUUID()).orElse(null);

        // validate
        Assert.assertEquals(MarginCallStatus.NOT_ENOUGH, result.getStatus());
    }

    @Test
    public void getMargin_SuccessPendingMarginCall() {
        // MaintenanceMargin = 13
        // margin = 10
        // gap = 3
        // counterPartyFund = 1
        // counterPartyCreditBalance = 1

        // prepare
        Margin marginDbo = getMarginDbo();
        marginDbo.setMaintenanceMargin(BigDecimal.valueOf(13.0));
        Mockito.when(marginRepo.findById(any())).thenReturn(Optional.of(marginDbo));

        Party partyDbo = getPartyDbo();
        partyDbo.setMaintenanceMargin(marginDbo);
        Mockito.when(partyRepo.findById(any())).thenReturn(Optional.of(partyDbo));

        AccountDTO accountDto = getAccountDto();
        accountDto.setMargin(BigDecimal.valueOf(10.0));
        accountDto.setCash(BigDecimal.valueOf(1.0));
        accountDto.setCredit(BigDecimal.valueOf(1.0));
        accountDto.setCreditUsed(BigDecimal.valueOf(0.0));
        accountDto.setCreditBalance(BigDecimal.valueOf(1.0));
        Mockito.when(accountService.getAccountByLegalName(any())).thenReturn(accountDto);

        // act
        MarginService service = getServiceInstance();
        MarginDTO result = service.getMargin(UUID.randomUUID()).orElse(null);

        // validate
        Assert.assertEquals(MarginCallStatus.PENDING_MARGIN_CALL, result.getStatus());
    }

    private Margin getMarginDbo() {
        Margin result = new Margin(UUID.randomUUID(), BigDecimal.valueOf(10.0));
        result.setUuid(UUID.randomUUID());
        result.setCreatedAt(Instant.now());
        result.setUpdatedAt(Instant.now());
        return result;
    }

    private Party getPartyDbo() {
        Party result = new Party();
        result.setUuid(UUID.randomUUID());
        result.setLegalName("Tom");
        // ignore other fields

        result.setMaintenanceMargin(getMarginDbo());

        return result;
    }

    private AccountDTO getAccountDto() {
        AccountDTO result = new AccountDTO();
        result.setAccountId("myAccount");
        result.setLegalName("Tom");
        result.setMargin(BigDecimal.valueOf(10.0));
        result.setCash(BigDecimal.valueOf(10.0));
        result.setCredit(BigDecimal.valueOf(10.0));

        return result;

    }

    private MarginService getServiceInstance() {
        return new MarginServiceImpl(accountService,
                partyRepo,
                marginRepo);
    }
}
