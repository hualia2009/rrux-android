package com.ucredit.dream.utils;

import java.security.MessageDigest;

/**
 * SHA字符加密串
 * 密码等安全信息存入数据库时，转换成MD5加密形式
 *
 * @author
 */
public class SHAUtil {
    public static final String KEY_SHA = "SHA";

    /**
     * 加密字符串
     *
     * @param data
     * @return
     */
    public static String encryptSHA(String data) {
        StringBuffer s = new StringBuffer();

        try {
            MessageDigest md = MessageDigest.getInstance(SHAUtil.KEY_SHA);
            byte[] dat = data.getBytes();
            md.update(dat);

            byte[] digest = md.digest();
            for (byte element : digest) {
                int d = element;

                if (d < 0) {        // byte 128-255
                    d += 256;
                }
                if (d < 16) {       // 0-15 16
                    s.append("0");
                }
                s.append(Integer.toString(d, 16));
            }
        } catch (Exception e) {
        }

        return s.toString();
    }

}