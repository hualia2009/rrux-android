package com.ucredit.dream.ui.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.ucredit.dream.R;
import com.ucredit.dream.UcreditDreamApplication;
import com.ucredit.dream.bean.CitySortModel;
import com.ucredit.dream.ui.activity.main.BaseActivity;
import com.ucredit.dream.utils.PinyinComparator;
import com.ucredit.dream.utils.Utils;
import com.ucredit.dream.view.ClearEditText;
import com.ucredit.dream.view.SideBar;

public class CityListActivity extends BaseActivity {

    @ViewInject(R.id.citylist)
    private ListView sortListView;
    @ViewInject(R.id.filter_edit)
    private ClearEditText editText;
    @ViewInject(R.id.sidebar)
    private SideBar sideBar;
    private SortAdapter adapter;
    private List<CitySortModel> SourceDateList;
    
    @OnClick({R.id.cancle})
    private void onclick(View view){
        switch (view.getId()) {
            case R.id.cancle:
                finish();
                break;
            default:
                break;
        }
    }
    
    @Override
    protected boolean hasTitle() {
        return false;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return null;
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_citylist;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    sortListView.setSelection(position);
                }

            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        SourceDateList = filledData(UcreditDreamApplication.cityNameList);
        Collections.sort(SourceDateList, new PinyinComparator());
        adapter = new SortAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);
    }

    private void filterData(String filterStr){
        List<CitySortModel> filterDateList = new ArrayList<CitySortModel>();
        if(TextUtils.isEmpty(filterStr)){
            filterDateList = SourceDateList;
        }else{
            filterDateList.clear();
            for(CitySortModel sortModel : SourceDateList){
                String name = sortModel.getName();
                if(name.indexOf(filterStr.toString()) != -1 || Utils.getPingYin(name).startsWith(filterStr.toString())){
                    filterDateList.add(sortModel);
                }
            }
        }
        Collections.sort(filterDateList, new PinyinComparator());
        adapter.updateListView(filterDateList);
    }    
    
    private List<CitySortModel> filledData(ArrayList<String> date) {
        List<CitySortModel> mSortList = new ArrayList<CitySortModel>();
        ArrayList<String> indexString = new ArrayList<String>();

        for (int i = 0; i < date.size(); i++) {
            CitySortModel sortModel = new CitySortModel();
            sortModel.setName(date.get(i));
            String pinyin = Utils.getPingYin(date.get(i));
            String sortString = pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {

                //对重庆多音字做特殊处理
                if (pinyin.startsWith("zhongqing")) {
                    sortString = "C";
                    sortModel.setSortLetters("C");
                } else {
                    sortModel.setSortLetters(sortString.toUpperCase());
                }

                if (!indexString.contains(sortString)) {
                    indexString.add(sortString);
                }
            }

            mSortList.add(sortModel);
        }
        Collections.sort(indexString);
        sideBar.setIndexText(indexString);
        return mSortList;

    }
    
    public class SortAdapter extends BaseAdapter implements SectionIndexer {
        private List<CitySortModel> list = null;
        private Context mContext;

        public SortAdapter(Context mContext, List<CitySortModel> list) {
            this.mContext = mContext;
            this.list = list;
        }

        public int getCount() {
            return this.list.size();
        }

        public Object getItem(int position) {
            return list.get(position);
        }

        public void updateListView(List<CitySortModel> list){
            this.list = list;
            notifyDataSetChanged();
        }
        
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View view, ViewGroup arg2) {
            ViewHolder viewHolder = null;
            final CitySortModel mContent = list.get(position);
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(mContext).inflate(R.layout.item_select_city, null);
                viewHolder.tvTitle = (TextView) view.findViewById(R.id.tv_city_name);
                viewHolder.tvLetter = (TextView) view.findViewById(R.id.tv_catagory);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            int section = getSectionForPosition(position);

            if (position == getPositionForSection(section)) {
                viewHolder.tvLetter.setVisibility(View.VISIBLE);
                viewHolder.tvLetter.setText(mContent.getSortLetters());
            } else {
                viewHolder.tvLetter.setVisibility(View.GONE);
            }

            viewHolder.tvTitle.setText(this.list.get(position).getName());
            viewHolder.tvTitle.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
                    Intent data=new Intent();
                    data.putExtra("city", SortAdapter.this.list.get(position).getName());
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
            });
            return view;

        }


         class ViewHolder {
            TextView tvLetter;
            TextView tvTitle;
        }


        public int getSectionForPosition(int position) {
            return list.get(position).getSortLetters().charAt(0);
        }


        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = list.get(i).getSortLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }

            return -1;
        }


        @Override
        public Object[] getSections() {
            return null;
        }
    }
    
}
