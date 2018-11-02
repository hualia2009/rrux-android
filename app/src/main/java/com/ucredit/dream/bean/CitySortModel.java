package com.ucredit.dream.bean;

import java.io.Serializable;

public class CitySortModel implements Serializable{

    private static final long serialVersionUID = 1L;
    private String name;
    private String sortLetters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }
}
