package com.ucredit.dream.bean;

public class StudentAidRecord {

    private String status;
    private String lesson;
    private String date;
    private String amount;
    private String lendId;
    
    private String abacusLoanId;
    private String applyAmount;
    
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getLesson() {
        return lesson;
    }
    public void setLesson(String lesson) {
        this.lesson = lesson;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getLendId() {
        return lendId;
    }
    public void setLendId(String lendId) {
        this.lendId = lendId;
    }
    public String getAbacusLoanId() {
        return abacusLoanId;
    }
    public void setAbacusLoanId(String abacusLoanId) {
        this.abacusLoanId = abacusLoanId;
    }
    public String getApplyAmount() {
        return applyAmount;
    }
    public void setApplyAmount(String applyAmount) {
        this.applyAmount = applyAmount;
    }
    
}
