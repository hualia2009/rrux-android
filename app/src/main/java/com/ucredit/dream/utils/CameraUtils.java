package com.ucredit.dream.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.DisplayMetrics;

public class CameraUtils {
    
    public static Size getProperSize(List<Size> sizeList, float displayRatio)
    {
        //先对传进来的size列表进行排序
        Collections.sort(sizeList, new SizeComparator());
        
        Size result = null;
        for(Size size: sizeList)
        {
            float curRatio =  ((float)size.width) / size.height;
            if(curRatio - displayRatio == 0)
            {
                result = size;
            }
        }
        if(null == result)
        {
            for(Size size: sizeList)
            {
                float curRatio =  ((float)size.width) / size.height;
                if(curRatio == 4f/3)
                {
                    result = size;
                }
            }
        }
        return result;
    }
    
    protected DisplayMetrics getScreenWH(Context context) {
        DisplayMetrics dMetrics = new DisplayMetrics();
        dMetrics = context.getResources().getDisplayMetrics();
        return dMetrics;
     }
    
    private Camera.Size findBestPreviewSize(Camera camera,Context context) {
        Camera.Parameters parameters=camera.getParameters();
                // 系统支持的所有预览分辨率
                String previewSizeValueString = null;
                previewSizeValueString = parameters.get("preview-size-values");
        
                 if (previewSizeValueString == null) {
                    previewSizeValueString = parameters.get("preview-size-value");
                }
         
                if (previewSizeValueString == null) { // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
                     return camera.new Size(getScreenWH(context).widthPixels,
                             getScreenWH(context).heightPixels);
               }
                 float bestX = 0;
               float bestY = 0;
       
                float tmpRadio = 0;
               float viewRadio = 0;
        
//              if (viewWidth != 0 && viewHeight != 0) {
//                   viewRadio = Math.min((float) viewWidth, (float) viewHeight)
//                            / Math.max((float) viewWidth, (float) viewHeight);
//                }
        
                String[] COMMA_PATTERN = previewSizeValueString.split(",");
                for (String prewsizeString : COMMA_PATTERN) {
                     prewsizeString = prewsizeString.trim();
         
                    int dimPosition = prewsizeString.indexOf('x');
                    if (dimPosition == -1) {
                       continue;
                    }
         
                     float newX = 0;
                    float newY = 0;
         
                   try {
                         newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
                        newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
                    } catch (NumberFormatException e) {
                        continue;
                   }
         
                   float radio = Math.min(newX, newY) / Math.max(newX, newY);
                    if (tmpRadio == 0) {
                         tmpRadio = radio;
                      bestX = newX;
                         bestY = newY;
                   } else if (tmpRadio != 0 && (Math.abs(radio - viewRadio)) < (Math.abs(tmpRadio - viewRadio))) {
                       tmpRadio = radio;
                       bestX = newX;
                         bestY = newY;
                    }
                 }
         
                if (bestX > 0 && bestY > 0) {
                     return camera.new Size((int) bestX, (int) bestY);
                }
                return null;
             }
    
    static class SizeComparator implements Comparator<Size>
    {

        @Override
        public int compare(Size lhs, Size rhs) {
            // TODO Auto-generated method stub
            Size size1 = lhs;
            Size size2 = rhs;
            if(size1.width < size2.width 
                    || size1.width == size2.width && size1.height < size2.height)
            {
                return -1;
            }
            else if(!(size1.width == size2.width && size1.height == size2.height))
            {
                return 1;
            }
            return 0;
        }
        
    }
}
