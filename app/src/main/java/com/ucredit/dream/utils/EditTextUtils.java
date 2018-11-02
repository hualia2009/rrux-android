package com.ucredit.dream.utils;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import com.custom.widgt.CustomEditText;
import com.custom.widgt.CustomEditText.DrawableClickListener;
import com.ucredit.dream.R;

public class EditTextUtils {
    
    public boolean isVisible = false;
    public CustomEditText editText;
    public Context context;
    
    public EditTextUtils(CustomEditText editText,Context context){
        this.editText = editText;
        this.context = context;
    }
    
    public void setPassword(){
        editText.setDrawableClicklistener(new DrawableClickListener() {
            
            @Override
            public void onClick() {
                Logger.e("isVi", ""+isVisible);
                if(!isVisible){
                    setEditTextVisible();
                }else{
                    editText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(null, null, 
                        context.getResources().getDrawable(R.drawable.edittext_eye_open), null);
                    isVisible = false;
                }
                editText.setSelection(editText.getText().length());//光标移到最后
            }

        });
        editText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence string, int arg1, int arg2, int arg3) {
                if(Utils.isNotEmptyString(string.toString())){
                    if(isVisible){
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, 
                            context.getResources().getDrawable(R.drawable.edittext_eye_close), null);
                    }else{
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, 
                            context.getResources().getDrawable(R.drawable.edittext_eye_open), null);
                    }
                }else{
                    editText.setCompoundDrawablesWithIntrinsicBounds(null, null, 
                        null, null); 
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                    int arg3) {
                
            }
            
            @Override
            public void afterTextChanged(Editable arg0) {
                
            }
        });
    }
    
    public void setEditTextVisible() {
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, 
            context.getResources().getDrawable(R.drawable.edittext_eye_close), null);
        isVisible = true;
    }

}
