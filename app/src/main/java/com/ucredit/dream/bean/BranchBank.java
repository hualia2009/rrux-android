package com.ucredit.dream.bean;

import java.io.Serializable;

public class BranchBank implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private String id;
    public String getName() {
        return name;
    }
    public BranchBank(String name) {
        super();
        this.name = name;
    }
    public BranchBank() {
        super();
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
}
