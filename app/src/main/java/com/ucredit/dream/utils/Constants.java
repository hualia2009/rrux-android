package com.ucredit.dream.utils;

public class Constants {

    public static final String CUSTOMER_SERVICE = "400-6909-666";
    public static final String CUSTOMER_EMAIL = "unotice@ucredit.com";
    
    public static final String ID_SCAN_TIP = "图片信息有误，请扫描身份证原件";
    
    //测试环境
//    public static final String PREFIX = "http://10.255.33.106:28431";
//    public static final String PREFIX = "http://10.255.4.49:80";
    //双向认证环境
    public static final String PREFIX = "https://uudream.ucredit.com";
    public static final String PREFIX_FILE = "http://s3.ucredit.com";
    public static final String PREFIX_EX = PREFIX + ":442";
//    public static final String PREFIX_EX = PREFIX;

    // 登陆
    public static final String API_LOGIN = PREFIX + "/v1/user/login";
    // 注册
    public static final String API_REGISTER = PREFIX + "/v1/user/register";
    // 验证手机号是否注册
    public static final String API_IS_VALIDATE = PREFIX
        + "/v1/user/notregistered";
    // 验证码(注册,重置)
    public static final String API_SENDVERIFYCODE_REGISTER = PREFIX
        + "/v1/user/sendverifycode";
    // 校验验证码
    public static final String API_VERIFYCODE_CHECK = PREFIX
            + "/v1/user/checkverifycode";
    // 重置密码
    public static final String API_RESET_PASSWORD = PREFIX
        + "/v1/user/resetpassword";
    // 设置邮箱
    public static final String API_SET_EMAIL = PREFIX + "/v1/user/email";
    // 修改密码
    public static final String API_BIND_ID_INFO = PREFIX
        + "/v1/user/userinfobinding";
    // 活体检测图片上传
    public static final String API_APPLY_UPLOAD_FILE_FACE = PREFIX
        + "/v1/user/uploadFacePhoto";
    // 跳过活体检测
    public static final String API_SKIP_FACE_PLUS = PREFIX
            + "/v1/user/skipfaceplus";
    // 查询联系人
    public static final String API_QUERY_CONTACTS = PREFIX
        + "/v1/application/queryusercontacts";
    // 提交联系人
    public static final String API_SUBMIT_CONTACTS = PREFIX
        + "/v1/application/saveusercontacts";
    // 用户联系人整个提交
    public static final String API_CONTACTS_BOOK = PREFIX + "/v1/user/contacts";

    // 获取AES加密key
    public static final String API_ENCRYPT_KEY = PREFIX
        + "/auth/secret?clientId=";
    // 获取加密Secret
    public static final String API_ENCRYPT_SECRET = PREFIX
        + "/auth/client?clientId=";
    // 获取密码加密盐
    public static final String API_ENCRYPT_SALT = PREFIX + "/auth/datasalt";

    // 查询进件状态
    public static final String API_QUERY_APPLY = PREFIX
        + "/v1/application/queryapply";

    // 请求Code代码
    public static final int REQUEST_CODE_REGIST = 101;

    public static final int REQUEST_CODE_REGIST_BIND = 102;

    public static final int REQUEST_CODE_LOGIN_GESTURE = 103;

    //上传文件f
    public static final String UPLOAD = PREFIX_FILE + "/v2/file/uploadByToken";
    //上传文件f
    public static final String DOWNLOAD = PREFIX_FILE + "/v1/download/";
    
    public static final String PDF =  PREFIX_EX + "/v1/sign/viewagreement/";
    public static final String PAYED_FINISHED =  PREFIX_EX + "/v1/agreement/get/settledcert/";

    // 响应Code代码
    public static final int RESULT_CODE_REGIST = 1001;

    public static final int RESULT_CODE_REGIST_BIND = 1002;

    public static final int RESULT_CODE_LOGIN_GESTURE = 1003;

    public static final int TIME_BACK_GROUND = 5 * 60 * 1000;
    // public static final int TIME_BACK_GROUND = 10*1000;
    public static final String API_ENCRYPT_TOKEN = PREFIX + "/oauth/token";
    // 聚信立数据源
    public static final String API_JUXINLI_SOURCE = PREFIX
        + "/v1/jxl/getdatasources";
    // 聚信立提交申请
    public static final String API_JUXINLI_APPLY = PREFIX + "/v1/jxl/apply";
    // 聚信立采集
    public static final String API_JUXINLI_COLLECT = PREFIX + "/v1/jxl/collect";
    // 聚信立重置密码
    public static final String API_JUXINLI_RESET = PREFIX
        + "/v1/jxl/resetpassword";
    // 聚信立完成接口
    // public static final String API_JUXINLI_FINISH = PREFIX
    // + "/v1/apply/updatejuxinlistatus";
    // 聚信立跳过电商接口
    public static final String API_JUXINLI_SKIPBUSINESS = PREFIX
        + "/v1/jxl/skipbusiness";
    // 助学纪录f
    public static final String STUDENT_AID_RECORD = PREFIX
        + "/v1/application/loanhistory";
    // 还款计划f
    public static final String REPAY_PLAN = PREFIX
        + "/v1/application/repayschedule";
    // 课程信息f
    public static final String LESSON_INFO = PREFIX + "/v1/course/get";
    // 提交申请f
    public static final String LESSON_SUBMIT = PREFIX
        + "/v1/application/createapplication";
    // 还款方式
    public static final String REPAY_WAY = PREFIX + "/v1/product/queryproduct";
    // 分期试算f
    public static final String LESSON_CALCULATE = PREFIX
        + "/v1/application/calculator";
    // 获取支行f
    public static final String BRANCH_LIST = PREFIX
        + "/v1/application/bankBranch";
    //提交地理位置
    public static final String LOCATION_SUBMIT = PREFIX
            + "/v1/user/location";
    // 授权银行卡
    public static final String BANKCARD_AUTHORIZE = PREFIX
        + "/v1/user/bankcard";
    // 更换银行卡
    public static final String BANKCARD_CHANGE = PREFIX
            + "/v1/user/chgbankcard";
    // 授权银行卡
    public static final String BANKCARD_AUTHORIZE_VERIFY = PREFIX
            + "/v1/user/bankcardauth";
    //授权银行卡鉴权认证
    public static final String BANKCARD_AUTHORIZE_CERTIFY = PREFIX
            + "/v1/user/bankcard/verify";
    // 查询开户行f
    public static final String BANK_LIST = PREFIX + "/v1/application/bank";
    // 助学明细f
    public static final String STUDENT_AID_DETAIL = PREFIX
        + "/v1/application/assistancedetail";
    // 上传身份证照片
    public static final String UPLOAD_IDCARD_PHOTO = PREFIX
        + "/v1/user/idcard/upload";
    // 付款f
    public static final String PAY = PREFIX + "/v1/pay";
    // 付款验证码f
    public static final String PAY_VERIFY = PREFIX + "/v1/pay/verifyCode";
    // 绑银行卡验证码f
    public static final String BIND_VERIFY = PREFIX + "/v1/pay/indauth";
    // 支付支持的银行卡
    public static final String SUPPORTED_BANKS = PREFIX + "/v1/pay/supportbanks";
    // 获取绑定的银行卡f
    public static final String BINDED_BANKLIST = PREFIX + "/v1/pay/bankcard";
    // 删除银行卡f
    public static final String DELETE_BANK = PREFIX + "/v1/pay/delbankcard";
    // 绑定银行卡f
    public static final String BIND_BANK = PREFIX + "/v1/pay/indauthverify";
    // 当期账单f
    public static final String BILL = PREFIX + "/v1/pay/bill";
    // 聚信立完成接口
    public static final String JUXINLI_FINISH = PREFIX + "/v1/jxl/status";
    // 居住地址-提交
    public static final String APPLICATION_ADDRESS_SUBMIT = PREFIX
        + "/v1/application/createresideaddress";
    // 居住地址-查询
    public static final String APPLICATION_ADDRESS_QUERY = PREFIX
        + "/v1/application/queryresideaddress";
    // 学籍信息保存接口 
    public static final String APPLICATION_STUDY_SUBMIT = PREFIX
        + "/v1/user/chsi";
    // 学籍信息验证码刷新
    public static final String APPLICATION_STUDY_REFRESH_VERIFY = PREFIX
            + "/v1/user/chsi/captcha";
    // 注册学信网 
    public static final String APPLICATION_STUDY_REGIST = "https://account.chsi.com.cn/account/preregister.action";
    // 学信网找回密码 
    public static final String APPLICATION_STUDY_RETRIVE = "https://account.chsi.com.cn/account/password!retrive";
    // 完成资料保存按钮
    public static final String APPLICATION_LEND_SUBMIT = PREFIX
        + "/v1/application/lendapplication";
    //划扣协议
    public static final String API_SIGN_HUAKOU_PROTOCOL = PREFIX
        + "/v1/sign/transferagreement";
    //图片地址
    public static final String API_IMAGE_PREFIX = PREFIX + "/v1/file/view/";
    //在线签约
    public static final String API_SIGN_NOTICE = PREFIX
        + "/v1/sign/loanwithdrawcreate";
    //检查是否可以抽奖
    public static final String API_PRIZE_CHECK = PREFIX
            + "/v1/prize/check";
    //抽奖详细页面
    public static final String API_PRIZE_DETAIL = "https://marketing.ucredit.com/index.html";
    //确认签约
    public static final String API_SIGN_CONFIRM = PREFIX
        + "/v1/sign/signconfirm";

    //信用服务,借款，划扣授权书
    public static final String API_SIGN_PROTOCOL = PREFIX_EX
        + "/v1/agreement/get";
    //助学明细 借款协议，还款协议
    public static final String API_VIEW_PROTOCOL = PREFIX_EX
        + "/v1/sign/viewagreement/";
    //黑卡
    public static final String HEIKA_UNION_PAY = PREFIX_EX + "/pbc/index.html";
    //黑卡
    public static final String HEIKA_INDEX = PREFIX_EX + "/pbc/index.html";
    //版本信息
    public static final String URL_APP_VERSION = PREFIX_EX + "/v1/appVersion";
    // 查询用户状态
    public static final String API_USER_STATE = PREFIX + "/v1/userstate/get";
    // H5静态协议地址
    public static final String H5_PROTOCOL = PREFIX_EX + "/h5/protocol/";
    
    //意见反馈QQ
    public static final String FEED_BACK_QQ = "4001015012";
    //意见反馈
    public static final String FEED_BACK_URL = "http://wpd.b.qq.com/page/webchat.html?nameAccount=4001015012";
}
