package com.ucredit.dream.bean;

import java.io.Serializable;

public class User implements Serializable{

    private static final long serialVersionUID = -7818749406379694643L;
    //111
    private String name;
    //name
    private String phone;
    //用户id？token
    private String userId;
    //客户id
    private String customerId;
    //身份证号码
    private String identityNo;
    //密码状态(0：未设置；1：正常；2：重置待修改)
    private int cipherStatus;
    //邮箱
    private String email;
    
    private UserState userState;
    
    
//    //是否通过国政通校验 0：未验证 1: 验证通过 2：验证失败
//    private int validate;
//    //是否活体检测（0-为活体检测；1-已活体检测）
//    private int facePlusPlus;
//    //是否上传身份证照片(0未上传，1已上传);
//    private int idCardUploaded;
    
//    public int getIdCardUploaded() {
//        return idCardUploaded;
//    }
//
//    public void setIdCardUploaded(int idCardUploaded) {
//        this.idCardUploaded = idCardUploaded;
//    }
//
//    public int getFacePlusPlus() {
//        return facePlusPlus;
//    }
//
//    public void setFacePlusPlus(int facePlusPlus) {
//        this.facePlusPlus = facePlusPlus;
//    }

    public UserState getUserState() {
        return userState;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }

    //密码
    private String password;

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    //员工号
    private String employeeNumber;
    //员工类型
    private String type;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public int getCipherStatus() {
        return cipherStatus;
    }

    public void setCipherStatus(int cipherStatus) {
        this.cipherStatus = cipherStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

//    public int getValidate() {
//        return validate;
//    }
//
//    public void setValidate(int validate) {
//        this.validate = validate;
//    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
