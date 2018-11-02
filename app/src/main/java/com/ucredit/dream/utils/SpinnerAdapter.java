package com.ucredit.dream.utils;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ucredit.dream.R;

public class SpinnerAdapter extends BaseAdapter {

    private ArrayList<String> arrayList;
    
    private Context context;
    
    public SpinnerAdapter(ArrayList<String> arrayList, Context context) {
        super();
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public String getItem(int arg0) {
        return arrayList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context,
                R.layout.spinner_item, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.text.setText(getItem(position));
        return convertView;        
    }

    class ViewHolder {
        TextView text;
        public ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.text);
            view.setTag(this);
        }
    }
    
}
