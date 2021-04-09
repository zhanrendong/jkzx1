package tech.tongyu.bct.cm.product.iov;

import tech.tongyu.bct.cm.core.CMEnumeration;

public enum KnockEnum implements CMEnumeration {
    /**
     * The option is knocked out if the spot goes up and crosses the barrier
     */
    UP_AND_OUT("向上敲出"),
    /**
     * The option is knocked out if the spot goes down and crosses the barrier
     */
    DOWN_AND_OUT("向下敲出"),
    /**
     * The option is knocked in if the spot goes up and crosses the barrier
     */
    UP_AND_IN("向上敲入"),
    /**
     * The option is knocked in if the spot goes down and crosses the barrier
     */
    DOWN_AND_IN("向下敲入");


    private String description;

    KnockEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
    }
