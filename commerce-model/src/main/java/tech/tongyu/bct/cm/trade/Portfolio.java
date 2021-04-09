package tech.tongyu.bct.cm.trade;

import java.util.List;

/**
 * An ad-hoc grouping of POSITIONS (whether prospective or executed, firm or
 * client). Any one POSITION can be in zero, one or more different
 * PORTFOLIOs. A PORTFOLIO can contain POSITIONs from ultiple legal entities
 */
public interface Portfolio extends PositionSet {
    String name();

}
