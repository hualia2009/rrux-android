/**
 * Copyright(c) 2011-2011 by YouCredit Inc.
 * All Rights Reserved
 */
package com.ucredit.dream.spinnertype;

/**
 * 婚姻状态（添加再婚）
 * 
 * @author ijay
 */
public enum Marriage {
    SINGLE("未婚"),
    MARRIED("已婚"),
    REMARRY("再婚"),
    DIVORCED("离异"),
    WIDOWED("丧偶"),
    OTHER("其他");

    private final String string;

    private Marriage(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return this.string;
    }
    
    public static String fromString(String value) {
        for (Marriage iterable : Marriage.values()) {
            if (iterable.toString().equals(value)) {
                return iterable.name();
            }
        }
        return null;
    }     
    
}
