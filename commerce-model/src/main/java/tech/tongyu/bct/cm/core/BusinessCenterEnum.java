package tech.tongyu.bct.cm.core;

/**
 * Mirros FpML "BUSINESS Center". FpML Definition: "A business day calendar
 * location" Further info: "The codes are based on the business calendar
 * location of some of which based on the ISO contry code or exchange code,
 * or some other codes" Additional business day calendar location codes
 * could be built according to the following rules. The first two characters
 * represent the ISO country code, the next two characters represent a) if
 * the location name consists of at least two letters of the location b) if
 * the location name consists of at least two words, the first letter of the
 * first word followed by the first letter of the second word. see
 * http://www.fpml.org/coding-scheme/business-center for the full data set
 */
public enum BusinessCenterEnum implements CMEnumeration {

    SSE("上交所"),
    SZSE("深交所"),
    SGE("金交所"),
    SHFE("上期所"),
    DCE("大商所"),
    CZCE("郑商所"),
    CFFEX("中金所");

    BusinessCenterEnum(String description) {
        this.description = description;
    }

    private String description;

    @Override
    public String description() {
        return description;
    }
}
