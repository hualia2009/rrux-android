package com.ucredit.dream.spinnertype;


/**
 * 进件状态
 *
 * @author 罗学勇（luoxueyong@126.com）
 */
public enum ApplicationStateEnum {

    // 还款方式
    NO(0, "否", "derate_abacus_state,derate_thread_state"),
    YES(1, "是", "derate_abacus_state,derate_thread_state"),
    // 还款方式
    REPAY_WAY_DEDUCTIONS(1, "划扣", "repay_way"),
    REPAY_WAY_REMITTANCE(2, "汇款", "repay_way"),
    // 减免标示
    DERATEING(1, "减免审核中", "derate_state"),
    DERATE_FAILURE(2, "减免审核失败", "derate_state"),
    DERATE_SUCCESS(3, "减免审核成功", "derate_state"),
    // 删除标示
    DELETE_NO(0, "正常", "is_deleted"),
    DELETE_YES(1, "删除", "is_deleted"),
    // 是否特殊申请标示
    SPECIAL_NO(0, "正常申请", "special"),
    SPECIAL_YES(1, "特殊申请", "special"),
    // 是否通过标示
    APPROVED_UNKOWN(0, "未知通过状态", "approved"),
    APPROVED_NO(1, "为通过", "approved"),
    APPROVED_YES(2, "通过", "approved"),
    // 以下是进件流转状态
    DELETE(-1, "无效状态", "state"),
    CREATE(0, "创建", "state"), //扫卡上传材料 ，然后提交 进入到

    SALE_WITHDRAW(9, "销售放弃", "state"),
    OASISING(10, "信审审核中", "state"),
    OASIS_FAILURE(12, "信审失败", "state"),
    OASIS_SUCCESS(13, "信审成功", "state"), //征信审核前

    CREDITING(30, "征信审核中", "state"),
    CREDIT_FAILURE(31, "征信失败", "state"),
    CREDIT_SUCCESS(32, "征信成功", "state"), //签约前
    SIGNING(50, "签约审核中", "state"), //签约确认

    SIGN_FAILURE(51, "签约失败/放弃", "state"),
    SIGN_EXPIRE(56, "过期自动放弃", "state"),
    SIGN_SUCCESS(59, "签约成功", "state"),

    LENDING(60, "放款审核中", "state"),
    ABACUS_LENDING(61, "放款审核中-账务系统", "state"),
    LEND_FAILURE(71, "放款失败/放弃", "state"),
    LEND_SUCCESS(72, "放款成功", "state"),
    SUCCESS(90, "结清", "state");

    public int code;
    public String note;
    public String group;

    ApplicationStateEnum(int code, String note, String group) {
        this.code = code;
        this.note = note;
        this.group = group;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public static String fromState(int state) {
        for (ApplicationStateEnum iterable : ApplicationStateEnum.values()) {
            if (iterable.getCode()==state) {
                return iterable.getNote();
            }
        }
        return null;
    } 
    
}
