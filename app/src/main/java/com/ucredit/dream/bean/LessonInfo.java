package com.ucredit.dream.bean;

import java.io.Serializable;
import java.util.ArrayList;

public class LessonInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String amount;
    private String name;
    private String id;
    private String discription;
    private ArrayList<ProductInfo> arrayList;
    
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
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
    public String getDiscription() {
        return discription;
    }
    public void setDiscription(String discription) {
        this.discription = discription;
    }
    public ArrayList<ProductInfo> getArrayList() {
        return arrayList;
    }
    public void setArrayList(ArrayList<ProductInfo> arrayList) {
        this.arrayList = arrayList;
    }

}
