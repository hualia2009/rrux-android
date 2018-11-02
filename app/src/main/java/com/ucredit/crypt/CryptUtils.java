package com.ucredit.crypt;

public class CryptUtils {
    
    public native String getIV();
    
    public native String getEncryptStr(String...strings);
}
