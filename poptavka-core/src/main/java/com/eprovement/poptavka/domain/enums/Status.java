package com.eprovement.poptavka.domain.enums;

/**
 * @author Juraj Martinka
 *         Date: 29.1.11
 */
public enum Status {

    ACTIVE("a"),
    INACTIVE("n");
    private String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
