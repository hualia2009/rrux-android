package com.ucredit.dream.bean;

import java.io.Serializable;

/**
 * 城市
 * @author Li Huang
 *
 */
public class City implements Serializable {

    private static final long serialVersionUID = 591333748584847874L;
    
    private String name;
    
    private int code;
    
    private Province province;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }
}
