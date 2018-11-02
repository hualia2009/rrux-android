package com.ucredit.dream.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {

    private int mVideoWidth;
    private int mVideoHeight;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //下面的代码是让视频的播放的长宽是根据你设置的参数来决定

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(500, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    public int getmVideoWidth() {
        return mVideoWidth;
    }

    public void setmVideoWidth(int mVideoWidth) {
        this.mVideoWidth = mVideoWidth;
    }

    public int getmVideoHeight() {
        return mVideoHeight;
    }

    public void setmVideoHeight(int mVideoHeight) {
        this.mVideoHeight = mVideoHeight;
    }

}
