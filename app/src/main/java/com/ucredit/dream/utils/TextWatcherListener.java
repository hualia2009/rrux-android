package com.ucredit.dream.utils;

import android.text.Editable;
import android.text.TextWatcher;

public class TextWatcherListener implements TextWatcher{
    
    private TextWatcherCallBack callBack;
    
    public TextWatcherListener(TextWatcherCallBack callBack){
        this.callBack = callBack;
    }

    @Override
    public void afterTextChanged(Editable arg0) {
        callBack.check();
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
            int arg3) {
        
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        
    }

}
