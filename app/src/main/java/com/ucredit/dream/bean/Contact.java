package com.ucredit.dream.bean;

import java.io.Serializable;

/**
 * 联系人
 * @author Li Huang
 *
 */
public class Contact implements Serializable{

    private static final long serialVersionUID = -8250315799365062442L;
    
    private String id;

    private String name;

    private int relation;

    private String cellphone;
    
    private String relative;

    public String getName() {
        return name;
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

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getRelative() {
        return relative;
    }

    public void setRelative(String relative) {
        this.relative = relative;
    }


}
