package com.ucredit.dream.utils;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ucredit.crypt.CryptUtils;

import android.util.Base64;
import android.util.Log;

public class AESUtils {

    public static final String KEY_ALGORITHM = "AES";
    public static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final String CIPHER_ALGORITHM_CBC = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 32;

    private static SecretKeySpec getKey(String secretkey) throws Exception {
        if (secretkey == null || secretkey.isEmpty()
            || secretkey.length() != AESUtils.KEY_SIZE) {
            Log.e("AESUtils", "secretkey is not valid!");
            throw new Exception();
        }

        final int keyLength = 256;
        final byte[] keyBytes = new byte[keyLength / 8];
        Arrays.fill(keyBytes, (byte) 0x0);
        final byte[] passwordBytes = secretkey.getBytes("UTF-8");
        final int length = passwordBytes.length < keyBytes.length ? passwordBytes.length
            : keyBytes.length;
        System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
        final SecretKeySpec key = new SecretKeySpec(keyBytes,
            AESUtils.KEY_ALGORITHM);
        return key;
    }

    /**
     * 加密方法
     * 
     * @param content
     * @param uid
     * @return 返回base64编码的加密字符串
     * @throws AesException
     */
    public static String doEncrypt(String content, String secretkey) {
        try {
            final Key key = AESUtils.getKey(secretkey);
            return Base64.encodeToString(encrypt(content, key), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AESUtils", "doEncrypt failure!");
            return "";
        }
    }

    /**
     * 加密方法
     * 
     * @param content
     * @param uid
     * @return 返回base64编码的加密字符串
     * @throws AesException
     */
    public static String doEncryptCBC(String content, String secretkey) {
        try {
            final Key key = AESUtils.getKey(secretkey);
            return Base64.encodeToString(encryptCBC(content, key),
                Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AESUtils", "doEncrypt failure!");
            return "";
        }
    }

    /**
     * 加密
     * 
     * @param content
     *        需要加密的内容
     * @param key
     *        加密key
     * @return
     */
    public static byte[] encrypt(String content, Key key) throws Exception {
        final Cipher cipher = Cipher.getInstance(AESUtils.CIPHER_ALGORITHM);
        final byte[] byteContent = content.getBytes("utf-8");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final byte[] result = cipher.doFinal(byteContent);
        return result;
    }

    /**
     * 加密
     * 
     * @param content
     *        需要加密的内容
     * @param key
     *        加密key
     * @return
     * @throws Exception
     */
    public static byte[] encryptCBC(String content, Key key) throws Exception {
        final Cipher cipher = Cipher.getInstance(AESUtils.CIPHER_ALGORITHM_CBC);
        final byte[] byteContent = content.getBytes("utf-8");
        IvParameterSpec ivs = new IvParameterSpec(new CryptUtils().getIV().getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, key, ivs);
        final byte[] result = cipher.doFinal(byteContent);
        return result;
    }

    /**
     * 解密方法
     * 
     * @param content
     * @param uid
     * @return 返回原文本
     * @throws AesException
     */
    public static String doDecrypt(String content, String secretkey) {
        try {
            final Key key = AESUtils.getKey(secretkey);
            return new String(decrypt(content, key));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AESUtils", "doDecrypt failure!");
            return "";
        }
    }

    /**
     * 解密方法
     * 
     * @param content
     * @param uid
     * @return 返回原文本
     * @throws AesException
     */
    public static String doDecryptCBC(String content, String secretkey) {
        try {
            final Key key = AESUtils.getKey(secretkey);
            return new String(decryptCBC(content, key));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("AESUtils", "doDecrypt failure!");
            return "";
        }
    }

    /**
     * 解密
     * 
     * @param encodedBase64String
     *        待解密内容
     * @param key
     *        解密密钥
     * @return
     */
    public static byte[] decrypt(String encodedBase64String, Key key)
            throws Exception {
        final Cipher cipher = Cipher.getInstance(AESUtils.CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        final byte[] encryptedContent = Base64.decode(encodedBase64String,
            Base64.DEFAULT);
        final byte[] result = cipher.doFinal(encryptedContent);
        return result;
    }

    /**
     * 解密
     * 
     * @param content
     *        待解密内容
     * @param key
     *        解密密钥
     * @return
     * @throws Exception
     */
    public static byte[] decryptCBC(String content, Key key) throws Exception {
        final Cipher cipher = Cipher.getInstance(AESUtils.CIPHER_ALGORITHM_CBC);
        IvParameterSpec ivs = new IvParameterSpec(new CryptUtils().getIV().getBytes());
        cipher.init(Cipher.DECRYPT_MODE, key, ivs);
        final byte[] encryptedContent = Base64.decode(content, Base64.DEFAULT);
        final byte[] result = cipher.doFinal(encryptedContent);
        return result;

    }

}
