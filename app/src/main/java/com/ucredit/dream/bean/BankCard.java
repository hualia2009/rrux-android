package com.ucredit.dream.bean;

import java.io.Serializable;

public class BankCard implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bankId;
    private String bankName;
    private String id;
    private String storablePan;
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
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getStorablePan() {
        return storablePan;
    }
    public void setStorablePan(String storablePan) {
        this.storablePan = storablePan;
    }
    
}
