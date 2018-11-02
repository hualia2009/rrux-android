/**
 * Copyright(c) 2011-2011 by YouCredit Inc.
 * All Rights Reserved
 */
package com.ucredit.dream.spinnertype;

/**
 * @author ijay
 */
public enum Gender {
    MALE("男"),
    FEMALE("女");

    private final String string;

    private Gender(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return this.string;
    }
    
    public static String fromString(String value) {
        for (Gender iterable : Gender.values()) {
            if (iterable.toString().equals(value)) {
                return iterable.name();
            }
        }
        return null;
    } 
}
