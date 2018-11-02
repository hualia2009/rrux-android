package com.ucredit.dream.bean;

public class RepayPlan {

    private String term;
    private String date;
    private String money;
    
    public RepayPlan() {
        super();
    }
    public RepayPlan(String term, String date, String money) {
        super();
        this.term = term;
        this.date = date;
        this.money = money;
    }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getMoney() {
        return money;
    }
    public void setMoney(String money) {
        this.money = money;
    }
    
}
