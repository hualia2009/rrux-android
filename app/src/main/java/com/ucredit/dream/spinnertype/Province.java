package com.ucredit.dream.spinnertype;

import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;



public class Province{
    String privinceData = "["+
    "{\"code\" : \"2\", \"name\" : \"北京\" },"+
    "{\"code\" : \"3\", \"name\" : \"天津\" },"+
    "{ \"code\" : \"4\", \"name\" : \"河北\" },"+
    "{ \"code\" : \"5\", \"name\" : \"山西\" },"+
    "{ \"code\" : \"6\", \"name\" : \"内蒙古\" },"+
    "{ \"code\" : \"7\", \"name\" : \"辽宁\" },"+
    "{ \"code\" : \"8\", \"name\" : \"吉林\" },"+
    "{ \"code\" : \"9\", \"name\" : \"黑龙江\" },"+
    "{ \"code\" : \"10\", \"name\" : \"上海\" },"+
    "{ \"code\" : \"11\", \"name\" : \"江苏\" },"+
    "{ \"code\" : \"12\", \"name\" : \"浙江\" },"+
    "{ \"code\" : \"13\", \"name\" : \"安徽\" },"+
    "{ \"code\" : \"14\", \"name\" : \"福建\" },"+
    "{ \"code\" : \"15\", \"name\" : \"江西\" },"+
    "{ \"code\" : \"16\", \"name\" : \"山东\" },"+
    "{ \"code\" : \"17\", \"name\" : \"河南\" },"+
    "{ \"code\" : \"18\", \"name\" : \"湖北\" },"+
    "{ \"code\" : \"19\", \"name\" : \"湖南\" },"+
    "{ \"code\" : \"20\", \"name\" : \"广东\" },"+
    "{ \"code\" : \"21\", \"name\" : \"广西\" },"+
    "{ \"code\" : \"22\", \"name\" : \"海南\" },"+
    "{ \"code\" : \"23\", \"name\" : \"重庆\" },"+
    "{ \"code\" : \"24\", \"name\" : \"四川\" },"+
    "{ \"code\" : \"25\", \"name\" : \"贵州\" },"+
    "{ \"code\" : \"26\", \"name\" : \"云南\" },"+
    "{ \"code\" : \"27\", \"name\" : \"西藏\" },"+
    "{ \"code\" : \"28\", \"name\" : \"陕西\" },"+
    "{ \"code\" : \"29\", \"name\" : \"甘肃\" },"+
    "{ \"code\" : \"30\", \"name\" : \"青海\" },"+
    "{ \"code\" : \"31\", \"name\" : \"宁夏\" },"+
    "{ \"code\" : \"32\", \"name\" : \"新疆\" }"+
    "]";
    
    private static Province province;
    
    private ArrayList<String> names;
    
    private int[] codes=new int[31];

    private Province() {
        super();
        names=new ArrayList<String>();
        JSONArray array=JSONArray.parseArray(privinceData);
        for (int i = 0; i < array.size(); i++) {
            JSONObject object=array.getJSONObject(i);
            names.add(object.getString("name"));
            codes[i]=object.getIntValue("code");
        }
    }

    public static Province getInstance() {
        if (province==null) {
            province = new Province();
        }
        return province;
    }

    public ArrayList<String> getNames() {
        return names;
    }

    public int getCode(String name){
        return codes[names.indexOf(name)];
    }
    
    public String getProvince(int code){
        for (int i = 0; i < codes.length; i++) {
            if (codes[i]==code) {
                return names.get(i);
            }
        }
        return null;
    }
    
}
