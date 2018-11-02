package com.ucredit.dream.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ucredit.dream.R;

public class CircleTextView extends TextView {

	private Paint mBgPaint = new Paint();; 
	private int circleColor=Color.BLACK;
	
	
	public CircleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
            attrs, R.styleable.CircleTextView, 0, 0);
        for (int i = 0; i < typedArray.getIndexCount(); i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.CircleTextView_circleColor:
                    circleColor = typedArray.getColor(attr, Color.BLACK);
                    break;

            }

        }
        typedArray.recycle();
        mBgPaint.setColor(circleColor);
        mBgPaint.setAntiAlias(true);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int measuredWidth = getMeasuredWidth();  
        int measuredHeight = getMeasuredHeight();  
        int max = Math.max(measuredWidth, measuredHeight);  
        setMeasuredDimension(max, max);  
	}

    public void setCircleColor(int circleColor) {
        this.circleColor = circleColor;
        mBgPaint.setColor(circleColor);
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawCircle(getWidth()/2, getHeight()/2, Math.max(getWidth(), getHeight())/2, mBgPaint); 
		super.onDraw(canvas);
	}

	
	
}
