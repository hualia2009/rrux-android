package com.ucredit.dream.bean;


import java.io.Serializable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ucredit.dream.utils.Logger;
import com.ucredit.dream.utils.Utils;

public class UserState implements Serializable{
    private static final long serialVersionUID = -2686404045198471592L;

    //扫描前
    public static final int STATE_BEFORE_SCAN = 100;
    
    //扫描后 || 提交申请资料
    public static final int STATE_AFTER_SCAN = 101;
    
    //提交资料，审核中 || 提交征信
    public static final int STATE_USER_INFO_CHECK = 102;
    
    //资料审核通过
    public static final int STATE_USER_INFO_PASS = 103;
    
    //资料拒绝
    public static final int STATE_USER_INFO_REJECT = 104;
    
    //征信审核中
    public static final int STATE_CREDIT_CHECK = 105;
    
    //征信拒绝
    public static final int STATE_CREDIT_REJECT = 106;
    
    //征信审核通过 || 在线签约
    public static final int STATE_CREDIT_PASS = 107;
    
    //在线签约审核中
    public static final int STATE_SIGN_PROTOCOL_CHECK = 108;
    
    //在线签约拒绝
    public static final int STATE_SIGN_PROTOCOL_REJECT = 109;
    
    //在线签约审核通过 || 还款中
    public static final int STATE_SIGN_PROTOCOL_PASS = 110;
    
    //还清
    public static final int STATE_REPAY_OVER = 111;

    private boolean signMapCredit; //是否完成信用协议
    private boolean signMapLend; //是否完成借款协议
    private boolean signMapTransfer; //是否完成划扣协议
    
    private boolean userinfoAudit; //是否等待授信审核
    private boolean userInfo; //是否完成授信资料
    private boolean userInfoPass; //是否授信审核通过
    private boolean userInfoMapBankcard; //是否完成授权银行卡
    private boolean userInfoMapContact; //是否完成联络人信息
    private boolean userInfoMapEducation; //是否完成学籍信息
    private boolean userInfoMapJxl; //是否完成手机验证
    
    private boolean creaditInfo; //是否提交征信资料
    private boolean creaditInfoAudit; //是否征信审核中
    private boolean creaditInfoPass; //是否征信通过
    
    private boolean sign; //是否完成在线签约
    private String message; //信息提示
    private boolean over; //是否结清
    private boolean exist; //是否存在未完成进件
    
    private boolean faceplus; //是否验证通过face++
    private boolean email; //是否验证通过EMAIL
    private boolean identify;//是否验证通过身份证
    private boolean lock;//是否账号锁定
    private boolean idcard;//是否上传身份证
    private boolean special;//特殊账号是否通过,false不通过
    private boolean over18;
    
    private boolean uploadCard; //身份证重复上传开关
    private boolean skipPbc; //是否跳过征信
    
    private boolean canRecharge;//能否还款
    private int state;
    
    public boolean isCanRecharge() {
		return canRecharge;
	}
	public void setCanRecharge(boolean canRecharge) {
		this.canRecharge = canRecharge;
	}
	private boolean validate;
    
    //拒贷期
    private boolean inRefusalPeriod; //是否在拒贷期
    private String refusalMsg; //拒贷期提示信息
    
    public boolean isValidate() {
        return validate;
    }
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public boolean isSignMapCredit() {
        return signMapCredit;
    }
    public void setSignMapCredit(boolean signMapCredit) {
        this.signMapCredit = signMapCredit;
    }
    public boolean isSignMapLend() {
        return signMapLend;
    }
    public void setSignMapLend(boolean signMapLend) {
        this.signMapLend = signMapLend;
    }
    public boolean isSignMapTransfer() {
        return signMapTransfer;
    }
    public void setSignMapTransfer(boolean signMapTransfer) {
        this.signMapTransfer = signMapTransfer;
    }
    public boolean isUserinfoAudit() {
        return userinfoAudit;
    }
    public void setUserinfoAudit(boolean userinfoAudit) {
        this.userinfoAudit = userinfoAudit;
    }
    public boolean isUserInfo() {
        return userInfo;
    }
    public void setUserInfo(boolean userInfo) {
        this.userInfo = userInfo;
    }
    public boolean isUserInfoPass() {
        return userInfoPass;
    }
    public void setUserInfoPass(boolean userInfoPass) {
        this.userInfoPass = userInfoPass;
    }
    public boolean isUserInfoMapBankcard() {
        return userInfoMapBankcard;
    }
    public void setUserInfoMapBankcard(boolean userInfoMapBankcard) {
        this.userInfoMapBankcard = userInfoMapBankcard;
    }
    public boolean isUserInfoMapContact() {
        return userInfoMapContact;
    }
    public void setUserInfoMapContact(boolean userInfoMapContact) {
        this.userInfoMapContact = userInfoMapContact;
    }
    public boolean isUserInfoMapEducation() {
        return userInfoMapEducation;
    }
    public void setUserInfoMapEducation(boolean userInfoMapEducation) {
        this.userInfoMapEducation = userInfoMapEducation;
    }
    public boolean isUserInfoMapJxl() {
        return userInfoMapJxl;
    }
    public void setUserInfoMapJxl(boolean userInfoMapJxl) {
        this.userInfoMapJxl = userInfoMapJxl;
    }
    public boolean isCreaditInfo() {
        return creaditInfo;
    }
    public void setCreaditInfo(boolean creaditInfo) {
        this.creaditInfo = creaditInfo;
    }
    public boolean isCreaditInfoAudit() {
        return creaditInfoAudit;
    }
    public void setCreaditInfoAudit(boolean creaditInfoAudit) {
        this.creaditInfoAudit = creaditInfoAudit;
    }
    public boolean isCreaditInfoPass() {
        return creaditInfoPass;
    }
    public void setCreaditInfoPass(boolean creaditInfoPass) {
        this.creaditInfoPass = creaditInfoPass;
    }
    public boolean isSign() {
        return sign;
    }
    public void setSign(boolean sign) {
        this.sign = sign;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isOver() {
        return over;
    }
    public void setOver(boolean over) {
        this.over = over;
    }
    public boolean isExist() {
        return exist;
    }
    public void setExist(boolean exist) {
        this.exist = exist;
    }
    public boolean isFaceplus() {
        return faceplus;
    }
    public void setFaceplus(boolean faceplus) {
        this.faceplus = faceplus;
    }
    public boolean isEmail() {
        return email;
    }
    public void setEmail(boolean email) {
        this.email = email;
    }
    public boolean isIdentify() {
        return identify;
    }
    public void setIdentify(boolean identify) {
        this.identify = identify;
    }
    public boolean isLock() {
        return lock;
    }
    public void setLock(boolean lock) {
        this.lock = lock;
    }
    public boolean isIdcard() {
        return idcard;
    }
    public void setIdcard(boolean idcard) {
        this.idcard = idcard;
    }
    public boolean isSpecial() {
        return special;
    }
    public void setSpecial(boolean special) {
        this.special = special;
    }
    public boolean isOver18() {
        return over18;
    }
    public void setOver18(boolean over18) {
        this.over18 = over18;
    }
    
    public boolean isUploadCard() {
        return uploadCard;
    }
    public void setUploadCard(boolean uploadCard) {
        this.uploadCard = uploadCard;
    }
    
    public boolean isSkipPbc() {
        return skipPbc;
    }
    public void setSkipPbc(boolean skipPbc) {
        this.skipPbc = skipPbc;
    }
    
    public boolean isInRefusalPeriod() {
        return inRefusalPeriod;
    }
    public void setInRefusalPeriod(boolean inRefusalPeriod) {
        this.inRefusalPeriod = inRefusalPeriod;
    }
    public String getRefusalMsg() {
        return refusalMsg;
    }
    public void setRefusalMsg(String refusalMsg) {
        this.refusalMsg = refusalMsg;
    }
    /**
     * {
    "success": "true",
    "error": null,
    "result": {
        "apply": {
            "sign_map_credit": "false",
            "userinfo_audit": "false",
            "creaditinfo": "false",
            "sign_map_lend": "false",
            "userinfo_map_bankcard": "false",
            "userinfo_map_education": "false",
            "userinfo": "false",
            "creaditinfo_audit": "false",
            "sign": "false",
            "message": "可咨询培训机构，扫描二维码申请贷款",
            "over": "false",
            "creaditinfo_pass": "false",
            "userinfo_map_jxl": "false",
            "userinfo_pass": "false",
            "sign_map_transfer": "false",
            "userinfo_map_contact": "false",
            "exist": "false"
        },
        "user": {
            "faceplus": "false",
            "email": "false",
            "identify": "false",
            "lock": "false",
            "idcard": "false",
            "special": "false",
            "over18": "false"
        }
    }
}
     */
    
    public static UserState parseUserState(String jsonStr){
        Logger.e("applyState", jsonStr);
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }
        UserState userState = new UserState();
        JSONObject response = JSON.parseObject(jsonStr);
        if(response == null || response.isEmpty()){
            return null;
        }
        
        JSONObject result = response.getJSONObject("result");
        if(result == null || result.isEmpty()){
            return null;
        }
        JSONObject apply = result.getJSONObject("apply");
        if(apply == null || apply.isEmpty()){
            return null;
        }
        userState.setSignMapCredit(apply.getBooleanValue("sign_map_credit"));
        userState.setSignMapLend(apply.getBooleanValue("sign_map_lend"));
        userState.setSignMapTransfer(apply.getBooleanValue("sign_map_transfer"));
        
        userState.setUserinfoAudit(apply.getBooleanValue("userinfo_audit"));
        userState.setUserInfo(apply.getBooleanValue("userinfo"));
        userState.setUserInfoPass(apply.getBooleanValue("userinfo_pass"));
        userState.setUserInfoMapBankcard(apply.getBooleanValue("userinfo_map_bankcard"));
        userState.setUserInfoMapContact(apply.getBooleanValue("userinfo_map_contact"));
        userState.setUserInfoMapEducation(apply.getBooleanValue("userinfo_map_education"));
        userState.setUserInfoMapJxl(apply.getBooleanValue("userinfo_map_jxl"));
        
        userState.setCreaditInfo(apply.getBooleanValue("creaditinfo"));
        userState.setCreaditInfoAudit(apply.getBooleanValue("creaditinfo_audit"));
        userState.setCreaditInfoPass(apply.getBooleanValue("creaditinfo_pass"));
        
        userState.setSign(apply.getBooleanValue("sign"));
        userState.setMessage(apply.getString("message"));
        userState.setOver(apply.getBooleanValue("over"));
        userState.setExist(apply.getBooleanValue("exist"));
        userState.setUploadCard(apply.getBooleanValue("upload_card"));
        userState.setCanRecharge(apply.getBooleanValue("canRecharge"));
        JSONObject user = result.getJSONObject("user");
        if(user == null || user.isEmpty()){
            return null;
        }
        userState.setFaceplus(user.getBooleanValue("faceplus"));
        userState.setEmail(user.getBooleanValue("email"));
        userState.setIdentify(user.getBooleanValue("identify"));
        userState.setLock(user.getBooleanValue("lock"));
        userState.setIdcard(user.getBooleanValue("idcard"));
        userState.setSpecial(user.getBooleanValue("special"));
        userState.setOver18(user.getBooleanValue("over18"));
        userState.setSkipPbc(user.getBooleanValue("skipPbc"));
        userState.setInRefusalPeriod(user.getBooleanValue("inRefusalPeriod"));
        userState.setRefusalMsg(user.getString("refusalMsg"));
        
        doState(userState);
        
        doValidate(userState);
        
        return userState;
        
    }
    
    private static void doValidate(UserState userState) {
        if(userState.isIdentify() && userState.isIdcard()){
            userState.setValidate(true);
        }else{
            userState.setValidate(false);
        }
    }
    private static void doState(UserState userState) {
        if(!userState.isExist()){
            userState.setState(STATE_BEFORE_SCAN);//扫描前,扫描申请
        }else if(!userState.userInfo){ 
            userState.setState(STATE_AFTER_SCAN);//扫描后，提交申请资料
        }else if(!userState.userinfoAudit){
            userState.setState(STATE_USER_INFO_CHECK);//客观别急
        }else if(!userState.userInfoPass){
            userState.setState(STATE_USER_INFO_REJECT);//很遗憾
        }else if(!userState.sign){
            userState.setState(STATE_USER_INFO_PASS);//授信通过
        }else if(!userState.creaditInfo){
            userState.setState(STATE_SIGN_PROTOCOL_PASS);//签约完成
        }else if(!userState.creaditInfoAudit){
            userState.setState(STATE_CREDIT_CHECK);//征信审核中
        }else if(!userState.creaditInfoPass){
            userState.setState(STATE_CREDIT_REJECT);//很遗憾
        }else if(!userState.over){
            userState.setState(STATE_CREDIT_PASS);//征信通过
        }else{
            userState.setState(STATE_REPAY_OVER);//已结清
        }
    }
}
