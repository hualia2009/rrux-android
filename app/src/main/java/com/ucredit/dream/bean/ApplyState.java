package com.ucredit.dream.bean;

import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.Utils;

public class ApplyState implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1427830204863885002L;

    private String abacusCreditId; //账务系统信审流水号

    private String abacusLoanId; //账务系统贷款ID

    private String abacusWithdrawId; //账务系统不可撤销申请ID
    
    private String acquiredAmount;//到手金额

    private String actualAmount; //实际金额， 小于等于批复金额

    private String applyAmount;  //申请金额@mock=11
    
    private String approveAmount;   //批复金额@mock=12
    
    private String auditTime; //审批完成时间@mock=1448609474000
    
    private String campusId;  //校区ID@mock=99999
    
    private String campusName; //校区名称@mock=校区名字
    
    private String courseBindTime; //完成课程绑定时间@mock=1448609471000
    
    private String courseId; //课程ID@mock=11
    
    private String courseName; //课程名称@mock=Java
    
    private String createdTime; //number  创建时间及申请时间@mock=1449201344000
    
    private String creditAgreement; //信用服务协议url@mock=
    
    private String discountAmount; //贴息金额，来源账务@mock=0
    
    private String id; //进件号@mock=123

    private boolean isBankcardBound;  // 是否完成银行卡绑定（还款信息）@mock=false
    
    private boolean isContactAdded;     // 是否完成联系人信息@mock=true
    
    private boolean isCreditPassed;      // 银行征信是否通过@mock=true
    
    private boolean isEducationVerified;     // 是否获取学籍信息（数仓）@mock=true
    
    private boolean isIdcardBound;       // 是否完成身份绑定@mock=true
    
    private boolean isIdcardVerified;        // 是否完成身份证明@mock=true
    
    private boolean isJxlVerified;       // 是否完成数据鉴权（聚信立）@mock=true

    private String lendAgreement;// string  借款协议url@mock=1
    
    private String lendDate;// string  放款日期yyyy-MM-dd
    
    private String orgId;//number  机构ID@mock=5
    
    private String orgName;//string  机构名称@mock=达达
    
    private String period;//number  期限@mock=1
    
    private String productId;//number  产品ID@mock=1
    
    private String reason;//number  原因类型
    
    private String remark;// string  备注
    
    private String signAmount;//number  签约金额，来源账务@mock=0
    
    private String signContractDate;// string  签约日期
    
    private String state;//number  进件状态@mock=72
    
    private String transferAgreement;//string  划扣协议@mock=
    
    private String updatedTime;//number  更新时间@mock=1449565811000
    
    private String updatedUserId;//number  记录更新人id@mock=16
    
    private String updatedUserName;// string  记录更新人名称@mock=adminUser
    
    private String userId;//number  用户ID@mock=1124
    
    private String logUrl; //logo图标
    
    private String desc; //课程描述:6+6

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLogUrl() {
        return logUrl;
    }

    public void setLogUrl(String logUrl) {
        this.logUrl = logUrl;
    }

    public String getAbacusCreditId() {
        return abacusCreditId;
    }

    public void setAbacusCreditId(String abacusCreditId) {
        this.abacusCreditId = abacusCreditId;
    }

    public String getAbacusLoanId() {
        return abacusLoanId;
    }

    public void setAbacusLoanId(String abacusLoanId) {
        this.abacusLoanId = abacusLoanId;
    }

    public String getAbacusWithdrawId() {
        return abacusWithdrawId;
    }

    public void setAbacusWithdrawId(String abacusWithdrawId) {
        this.abacusWithdrawId = abacusWithdrawId;
    }

    public String getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(String actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(String applyAmount) {
        this.applyAmount = applyAmount;
    }

    public String getApproveAmount() {
        return approveAmount;
    }

    public void setApproveAmount(String approveAmount) {
        this.approveAmount = approveAmount;
    }

    public String getAcquiredAmount() {
        return acquiredAmount;
    }

    public void setAcquiredAmount(String acquiredAmount) {
        this.acquiredAmount = acquiredAmount;
    }

    public String getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(String auditTime) {
        this.auditTime = auditTime;
    }

    public String getCampusId() {
        return campusId;
    }

    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getCourseBindTime() {
        return courseBindTime;
    }

    public void setCourseBindTime(String courseBindTime) {
        this.courseBindTime = courseBindTime;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreditAgreement() {
        return creditAgreement;
    }

    public void setCreditAgreement(String creditAgreement) {
        this.creditAgreement = creditAgreement;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isBankcardBound() {
        return isBankcardBound;
    }

    public void setBankcardBound(boolean isBankcardBound) {
        this.isBankcardBound = isBankcardBound;
    }

    public boolean isContactAdded() {
        return isContactAdded;
    }

    public void setContactAdded(boolean isContactAdded) {
        this.isContactAdded = isContactAdded;
    }

    public boolean isCreditPassed() {
        return isCreditPassed;
    }

    public void setCreditPassed(boolean isCreditPassed) {
        this.isCreditPassed = isCreditPassed;
    }

    public boolean isEducationVerified() {
        return isEducationVerified;
    }

    public void setEducationVerified(boolean isEducationVerified) {
        this.isEducationVerified = isEducationVerified;
    }

    public boolean isIdcardBound() {
        return isIdcardBound;
    }

    public void setIdcardBound(boolean isIdcardBound) {
        this.isIdcardBound = isIdcardBound;
    }

    public boolean isIdcardVerified() {
        return isIdcardVerified;
    }

    public void setIdcardVerified(boolean isIdcardVerified) {
        this.isIdcardVerified = isIdcardVerified;
    }

    public boolean isJxlVerified() {
        return isJxlVerified;
    }

    public void setJxlVerified(boolean isJxlVerified) {
        this.isJxlVerified = isJxlVerified;
    }

    public String getLendAgreement() {
        return lendAgreement;
    }

    public void setLendAgreement(String lendAgreement) {
        this.lendAgreement = lendAgreement;
    }

    public String getLendDate() {
        return lendDate;
    }

    public void setLendDate(String lendDate) {
        this.lendDate = lendDate;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getSignAmount() {
        return signAmount;
    }

    public void setSignAmount(String signAmount) {
        this.signAmount = signAmount;
    }

    public String getSignContractDate() {
        return signContractDate;
    }

    public void setSignContractDate(String signContractDate) {
        this.signContractDate = signContractDate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTransferAgreement() {
        return transferAgreement;
    }

    public void setTransferAgreement(String transferAgreement) {
        this.transferAgreement = transferAgreement;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedUserId() {
        return updatedUserId;
    }

    public void setUpdatedUserId(String updatedUserId) {
        this.updatedUserId = updatedUserId;
    }

    public String getUpdatedUserName() {
        return updatedUserName;
    }

    public void setUpdatedUserName(String updatedUserName) {
        this.updatedUserName = updatedUserName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    /**
     *  "result": {
        "abacusCreditId": "测试内容705p",
        "abacusLoanId": "测试内容z4uu",
        "abacusWithdrawId": "测试内容97vc",
        "acquiredAmount": 86422,
        "actualAmount": 0,
        "applyAmount": 11,
        "approveAmount": 12,
        
        "auditTime": 1448609474000,
        "campusId": 99999,
        "campusName": "校区名字",
        "courseBindTime": 1448609471000,
        "courseId": 11,
        "courseName": "Java",
        "createdTime": 1449201344000,
        
        "creditAgreement": "",
        "discountAmount": 0,
        "id": "123",
        "isBankcardBound": false,
        "isContactAdded": true,
        "isCreditPassed": true,
        "isEducationVerified": true,
        
        "isIdcardBound": true,
        "isIdcardVerified": true,
        "isJxlVerified": true,
        "lendAgreement": "1",
        "lendDate": "测试内容8v53",
        "orgId": 5,
        "orgName": "达达",
        
        "period": 1,
        "productId": 1,
        "reason": 28400,
        "remark": "测试内容p52c",
        "signAmount": 0,
        "signContractDate": "测试内容1j4e",
        
        "state": 72,
        "transferAgreement": "",
        "updatedTime": 1449565811000,
        "updatedUserId": 16,
        "updatedUserName": "adminUser",
        "userId": 1124
     * @param jsonStr
     * @return
     */
    public static ApplyState parseApplyState(String jsonStr){
        Logger.e("parseApplyState", ""+jsonStr);
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }
        ApplyState applyState = new ApplyState();
        JSONObject response = JSON.parseObject(jsonStr);
        if(response == null || response.isEmpty()){
            return null;
        }
        
        JSONObject result = response.getJSONObject("result");
        if(result == null || result.isEmpty()){
            return null;
        }
        
        applyState.setAbacusCreditId(result.getString("abacusCreditId"));
        applyState.setAbacusLoanId(result.getString("abacusLoanId"));
        applyState.setAbacusWithdrawId(result.getString("abacusWithdrawId"));
        applyState.setAcquiredAmount(result.getString("acquiredAmount"));
        applyState.setActualAmount(result.getString("actualAmount"));
        applyState.setApplyAmount(result.getString("applyAmount"));
        applyState.setApproveAmount(result.getString("approveAmount"));
        
        applyState.setAuditTime(result.getString("auditTime"));
        applyState.setCampusId(result.getString("campusId"));
        applyState.setCampusName(result.getString("campusName"));
        applyState.setCourseBindTime(result.getString("courseBindTime"));
        applyState.setCourseId(result.getString("courseId"));
        applyState.setCourseName(result.getString("courseName"));
        applyState.setCreatedTime(result.getString("createdTime"));
        
        applyState.setCreditAgreement(result.getString("creditAgreement"));
        applyState.setDiscountAmount(result.getString("discountAmount"));
        applyState.setId(result.getString("id"));
        applyState.setBankcardBound(result.getBooleanValue("isBankcardBound"));
        applyState.setContactAdded(result.getBooleanValue("isContactAdded"));
        applyState.setCreditPassed(result.getBooleanValue("isCreditPassed"));
        applyState.setEducationVerified(result.getBooleanValue("isEducationVerified"));
        
        applyState.setIdcardBound(result.getBooleanValue("isIdcardBound"));
        applyState.setIdcardVerified(result.getBooleanValue("isIdcardVerified"));
        applyState.setJxlVerified(result.getBooleanValue("isJxlVerified"));
        applyState.setLendAgreement(result.getString("lendAgreement"));
        applyState.setLendDate(result.getString("lendDate"));
        applyState.setOrgId(result.getString("orgId"));
        applyState.setOrgName(result.getString("orgName"));
        
        applyState.setPeriod(result.getString("period"));
        applyState.setProductId(result.getString("productId"));
        applyState.setReason(result.getString("reason"));
        applyState.setRemark(result.getString("remark"));
        applyState.setSignAmount(result.getString("signAmount"));
        applyState.setSignContractDate(result.getString("signContractDate"));
        
        applyState.setState(result.getString("state"));
        applyState.setTransferAgreement(result.getString("transferAgreement"));
        applyState.setUpdatedTime(result.getString("updatedTime"));
        applyState.setUpdatedUserId(result.getString("updatedUserId"));
        applyState.setUpdatedUserName(result.getString("updatedUserName"));
        applyState.setUserId(result.getString("userId"));
        applyState.setLogUrl(result.getString("logoUrl"));
        applyState.setDesc(result.getString("desc"));
        
        return applyState;
        
    }
    
    
}
