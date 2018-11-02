/*
 *  Copyright 2011 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kankan.wheel.widget.adapters;

import java.util.List;

import android.content.Context;

import com.ucredit.dream.bean.City;

/**
 * 返回省份列表
 * @author miukoo
 *
 */
public class CityWheelAdapter extends AbstractWheelTextAdapter {
    private List<City> items;

    /**
     * Constructor
     * @param context the current context
     * @param items the items
     */
    public CityWheelAdapter(Context context, List<City> items) {
        super(context);
        this.items = items;
    }
    
    @Override
    public CharSequence getItemText(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index).getName();
        }
        return null;
    }

    @Override
    public int getItemsCount() {
        return items.size();
    }
    
    public City getCurrentCity(int index){
    	return items.get(index);
    }
    
    public int whereCity(int code){
    	int count = 0;
    	for(City p : items){
    		if(p.getCode()==code)return count;
    		count++;
    	}
    	return 0;
    }
    
    public int whereCity(String city){
        int count = 0;
        for(City p : items){
            if(p.getName().contains(city))return count;
            count++;
        }
        return 0;
    }
    
}
