package exocr.exocrengine;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

public final class EXCZKNumResult {
	// recognition data
	public char[] numbers;
	public Rect[] rects;
	public int charCount;
	public String strNumbers;
	public Bitmap bitmap;
	public int nType;
	public int nRate;

	// time
	public long timestart;
	public long timeend;

	public EXCZKNumResult() {
		numbers = new char[64];
		rects = new Rect[64];
		charCount = 0;
		bitmap = null; // bitmap result
	}

	/**
	 * decode from stream return the len of decoded data int the buf
	 */
	public static EXCZKNumResult decode(byte[] pbBuf, int nStrLen) {
		int i;
		int hic, lwc;
		int lft, top, rgt, btm, code;
		int nCharCount, nCharNum;
		int bOk;
		
		EXCZKNumResult result = new EXCZKNumResult();		
		////////////////////////////////////////////////////////////////
		i = 0;
		hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; code = (hic<<8)+lwc; 
		result.nType = code;
		hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; code = (hic<<8)+lwc; 
		result.nRate = code;
		hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; code = (hic<<8)+lwc; 
		nCharNum = code;
		
		//decode the result info
		nCharCount = 0;
		while(i < nStrLen-9){
			hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; code = (hic<<8)+lwc; 
			hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; lft = (hic<<8)+lwc;
			hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; top = (hic<<8)+lwc;
			hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; rgt = (hic<<8)+lwc;
			hic = pbBuf[i++]&0xFF; lwc = pbBuf[i++]&0xFF; btm = (hic<<8)+lwc;
			result.numbers[nCharCount] = (char)code;
			result.rects[nCharCount] = new Rect(lft, top, rgt, btm);
			nCharCount++;
		}
		result.numbers[nCharCount] = 0;
		result.charCount = nCharCount;	
		result.strNumbers = new String(result.numbers, 0, result.charCount);
		
		//is it correct, check it!
		if (result.charCount < 10	|| result.charCount > 64 || nCharCount != nCharNum) {
			return null;
		}
		return result;
	}

	/** @return raw text to show */
	public String getText() {
		long timeescape = timeend - timestart;
		String text = "CardNumber:" + strNumbers;
		text += "\nRecoTime=" + timeescape;
		// text += "\nBeg:[" + timestart + "]";
		// text += "\nEnd:[" + timeend + "]";
		return text;
	}
}
