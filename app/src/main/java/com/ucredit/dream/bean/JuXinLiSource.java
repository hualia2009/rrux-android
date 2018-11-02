package com.ucredit.dream.bean;

import java.io.Serializable;

public class JuXinLiSource implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String nameChinese;
    private String name;
    private String category;

    public String getNameChinese() {
        return nameChinese;
    }

    public void setNameChinese(String nameChinese) {
        this.nameChinese = nameChinese;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

}
