package com.ucredit.dream.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.ucredit.dream.R;
import com.ucredit.dream.bean.Bank;

public class BankLogoUtil {

    private HashMap<String, Integer> map = new HashMap<String, Integer>();
    
    private ArrayList<Bank> list = new ArrayList<Bank>();
    
    private static BankLogoUtil util;

    public ArrayList<Bank> getList() {
        return list;
    }

    private BankLogoUtil() {
        super();
        map.put("ICBC", R.drawable.bank_gongshang);
        map.put("ABC", R.drawable.bank_nongye);
        map.put("BOC", R.drawable.bank_zhongguo);
        map.put("CCB", R.drawable.bank_jianshe);
        map.put("BOCM", R.drawable.bank_jiaotong);
        map.put("CITIC", R.drawable.bank_zhongxin);
        map.put("CEB", R.drawable.bank_guangda);
        map.put("HXB", R.drawable.bank_huaxia);
        map.put("CMBC", R.drawable.bank_minsheng);
        map.put("GDB", R.drawable.bank_guangfa);
        map.put("PAB", R.drawable.bank_pingan);
        map.put("CMB", R.drawable.bank_zhaoshang);
        map.put("CIB", R.drawable.bank_xingye);
        map.put("SPDB", R.drawable.bank_pufa);
        map.put("SJB", R.drawable.bank_shengjing);
        map.put("JSB", R.drawable.bank_jiangsu);
        map.put("HZB", R.drawable.bank_hangzhou);
        map.put("ZJTLCB", R.drawable.bank_tailong);
        map.put("QLB", R.drawable.bank_qilu);
        map.put("QSRCB", R.drawable.bank_qishang);
        map.put("WFB", R.drawable.bank_weifang);
        map.put("DZB", R.drawable.bank_dezhou);
        map.put("HKB", R.drawable.bank_hankou);
        map.put("GXBBWB", R.drawable.bank_guangxi);
        map.put("HSB", R.drawable.bank_huishang);
        map.put("SHB", R.drawable.bank_shanghai);
        map.put("BJRCB", R.drawable.bank_nongshang);
        map.put("NBCB", R.drawable.bank_ningbo);
        map.put("WZB", R.drawable.bank_wenzhou);
        map.put("GZCB", R.drawable.bank_guangzhou);
        map.put("DLB", R.drawable.bank_dalian);
        map.put("NJCB", R.drawable.bank_nanjing);
        map.put("DGB", R.drawable.bank_dongguan);
        map.put("BOCD", R.drawable.bank_chengdu);
        map.put("HRBB", R.drawable.bank_haerbin);
        map.put("NCB", R.drawable.bank_nanchang);
        map.put("QDRCB", R.drawable.bank_qingdao);
        map.put("RZRCB", R.drawable.bank_rizhao);
        map.put("CSB", R.drawable.bank_changsha);
        map.put("WHCCB", R.drawable.bank_weihai);
        map.put("CSRCB", R.drawable.bank_changshu);
        map.put("CQRCB", R.drawable.bank_chongqing);
        Bank bank1 = new Bank("ICBC", "中国工商银行", R.drawable.bank_gongshang);
        Bank bank2 = new Bank("ABC", "中国农业银行", R.drawable.bank_nongye);
        Bank bank3 = new Bank("BOC", "中国银行", R.drawable.bank_zhongguo);
        Bank bank4 = new Bank("CCB", "中国建设银行", R.drawable.bank_jianshe);
        Bank bank5 = new Bank("BCOM", "交通银行", R.drawable.bank_jiaotong);
        Bank bank6 = new Bank("CITIC", "中信银行", R.drawable.bank_zhongxin);
        Bank bank7 = new Bank("CEB", "光大银行", R.drawable.bank_guangda);
        Bank bank8 = new Bank("HXB", "华夏银行", R.drawable.bank_huaxia);
        Bank bank9 = new Bank("CMBC", "民生银行", R.drawable.bank_minsheng);
        Bank bank10 = new Bank("GDB", "广发银行", R.drawable.bank_guangfa);
        Bank bank11 = new Bank("PAB", "平安银行", R.drawable.bank_pingan);
        Bank bank12 = new Bank("CMB", "招商银行", R.drawable.bank_zhaoshang);
        Bank bank13 = new Bank("CIB", "兴业银行", R.drawable.bank_xingye);
        Bank bank14 = new Bank("SPDB", "浦发银行", R.drawable.bank_pufa);
        Bank bank15 = new Bank("SJB", "盛京银行", R.drawable.bank_shengjing);
        Bank bank16 = new Bank("JSB", "江苏银行", R.drawable.bank_jiangsu);
        Bank bank17 = new Bank("HZB", "杭州银行", R.drawable.bank_hangzhou);
        Bank bank18 = new Bank("ZJTLCB", "泰隆银行", R.drawable.bank_tailong);
        Bank bank19 = new Bank("QLB", "齐鲁银行", R.drawable.bank_qilu);
        Bank bank20 = new Bank("QSRCB", "齐商银行", R.drawable.bank_qishang);
        Bank bank21 = new Bank("WFB", "潍坊银行", R.drawable.bank_weifang);
        Bank bank22 = new Bank("DZB", "德州银行", R.drawable.bank_dezhou);
        Bank bank23 = new Bank("HKB", "汉口银行", R.drawable.bank_hankou);
        Bank bank24 = new Bank("GXBBWB", "广西北部湾银行", R.drawable.bank_guangxi);
        Bank bank25 = new Bank("HSB", "徽商银行", R.drawable.bank_huishang);
        Bank bank26 = new Bank("SHB", "上海银行", R.drawable.bank_shanghai);
        Bank bank27 = new Bank("BJRCB", "北京农商银行", R.drawable.bank_nongshang);
        Bank bank28 = new Bank("NBCB", "宁波银行", R.drawable.bank_ningbo);
        Bank bank29 = new Bank("WZB", "温州银行", R.drawable.bank_wenzhou);
        Bank bank30 = new Bank("GZCB", "广州银行", R.drawable.bank_guangzhou);
        Bank bank31 = new Bank("DLB", "大连银行", R.drawable.bank_dalian);
        Bank bank32 = new Bank("NJCB", "南京银行", R.drawable.bank_nanjing);
        Bank bank33 = new Bank("DGB", "东莞银行", R.drawable.bank_dongguan);
        Bank bank34 = new Bank("BOCD", "成都银行", R.drawable.bank_chengdu);
        Bank bank35 = new Bank("HRBB", "哈尔滨银行", R.drawable.bank_haerbin);
        Bank bank36 = new Bank("NCB", "南昌银行", R.drawable.bank_nanchang);
        Bank bank37 = new Bank("QDRCB", "青岛银行", R.drawable.bank_qingdao);
        Bank bank38 = new Bank("RZRCB", "日照银行", R.drawable.bank_rizhao);
        Bank bank39 = new Bank("CSB", "长沙银行", R.drawable.bank_changsha);
        Bank bank40 = new Bank("WHCCB", "威海市商业银行", R.drawable.bank_weihai);
        Bank bank41 = new Bank("CSRCB", "常熟农商行", R.drawable.bank_changshu);
        Bank bank42 = new Bank("CQRCB", "重庆农商行", R.drawable.bank_chongqing);
        list.add(bank1);
        list.add(bank2);
        list.add(bank3);
        list.add(bank4);
        list.add(bank5);
        list.add(bank6);
        list.add(bank7);
        list.add(bank8);
        list.add(bank9);
        list.add(bank10);
        list.add(bank11);
        list.add(bank12);
        list.add(bank13);
        list.add(bank14);
        list.add(bank15);
        list.add(bank16);
        list.add(bank17);
        list.add(bank18);
        list.add(bank19);
        list.add(bank20);
        list.add(bank21);
        list.add(bank22);
        list.add(bank23);
        list.add(bank24);
        list.add(bank25);
        list.add(bank26);
        list.add(bank27);
        list.add(bank28);
        list.add(bank29);
        list.add(bank30);
        list.add(bank31);
        list.add(bank32);
        list.add(bank33);
        list.add(bank34);
        list.add(bank35);
        list.add(bank36);
        list.add(bank37);
        list.add(bank38);
        list.add(bank39);
        list.add(bank40);
        list.add(bank41);
        list.add(bank42);
    }

    public static BankLogoUtil getInstance() {
        if (util == null) {
            util = new BankLogoUtil();
        }
        return util;
    }

    public Integer getLogoId(String bankId) {
        return map.get(bankId);
    }

}
