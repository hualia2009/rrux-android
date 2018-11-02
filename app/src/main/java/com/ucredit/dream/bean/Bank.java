package com.ucredit.dream.bean;

import java.io.Serializable;

public class Bank implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String bankId;
    private String bankName;
    private int iconId;

    public Bank() {
        super();
    }

    public Bank(String bankId, String bankName, int iconId) {
        super();
        this.bankId = bankId;
        this.bankName = bankName;
        this.iconId = iconId;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

}
