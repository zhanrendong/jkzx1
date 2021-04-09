package tech.tongyu.bct.market.dto;

/**
 * 股指
 * @author Lu Lu
 */
public class EquityIndexInfo extends InstrumentCommonInfo implements InstrumentInfo {

    @Override
    public Integer multiplier() {
        //TODO http://jira.tongyu.tech:8080/browse/OTMS-2245
        return 1;
    }

}
