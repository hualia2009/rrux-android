package com.ucredit.dream.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Province implements Serializable{

    private static final long serialVersionUID = -8976973762243915520L;
    
    private String name;
    
    private int code;
    
    private List<City> citys;

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

	public List<City> getCitys() {
		if(citys==null)citys = new ArrayList<City>();
		return citys;
	}

	public void setCitys(List<City> citys) {
		this.citys = citys;
	}

}
