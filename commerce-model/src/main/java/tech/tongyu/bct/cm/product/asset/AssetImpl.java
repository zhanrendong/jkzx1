package tech.tongyu.bct.cm.product.asset;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValue;
import tech.tongyu.bct.cm.product.iov.InstrumentOfValuePartyRoleTypeEnum;
import tech.tongyu.bct.cm.product.iov.impl.PartyRole;
import tech.tongyu.bct.cm.reference.elemental.Party;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class AssetImpl implements Asset<InstrumentOfValue> {
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public InstrumentOfValue instrument;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    public List<LegalPartyRole> partyRoles;

    public SortedMap<LocalDate, BigDecimal> quantityFactors;

    public AssetImpl() {
    }

    public AssetImpl(InstrumentOfValue instrument, List<LegalPartyRole> partyRoles,
                     SortedMap<LocalDate, BigDecimal> quantityFactors) {
        this.instrument = instrument;
        this.partyRoles = partyRoles;
        this.quantityFactors = quantityFactors;
    }

    public AssetImpl(InstrumentOfValue instrument,
                     Party party,
                     InstrumentOfValuePartyRoleTypeEnum partyRole,
                     Party counterparty,
                     InstrumentOfValuePartyRoleTypeEnum counterpartyRole) {
        this.instrument = instrument;
        this.partyRoles = Arrays.asList(
                new PartyRole(party, partyRole),
                new PartyRole(counterparty, counterpartyRole));
        this.quantityFactors = new TreeMap<>();
    }


    @Override
    public InstrumentOfValue instrumentOfValue() {
        return instrument;
    }

    @Override
    public List<LegalPartyRole> legalPartyRoles() {
        return partyRoles;
    }

    @Override
    public SortedMap<LocalDate, BigDecimal> quantityFactors() {
        return quantityFactors;
    }

    @Override
    public List<InstrumentOfValueLegalPartyRole<InstrumentOfValue>> instrumentOfValueLegalPartyRoles() {
        return legalPartyRoles()
                .stream()
                .map(pr -> new tech.tongyu.bct.cm.product.iov.impl.InstrumentOfValueLegalPartyRole<InstrumentOfValue>(
                        instrumentOfValue(), pr.party(), pr.roleType(), pr.account().orElse(null)))
                .collect(Collectors.toList());
    }
}
