package com.zwping.a;


import com.zwping.alibx.Util;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Des {

    // 密钥
    public final static String partKey = "e12ef31041c8a49a";
    // 算法名称
    public static final String KEY_ALGORITHM = "DES";
    // 算法名称/加密模式/填充方式
    public static final String CIPHER_ALGORITHM_ECB = "DES/ECB/PKCS5Padding";
    public static final String CIPHER_ALGORITHM_CBC = "DES/CBC/PKCS5Padding";

    public static void test() {
        /*
         * 使用 ECB mode 密钥生成器 生成密钥 ECB mode cannot use IV
         */
        String key = partKey;
        try {
            byte[] c = encodeECB(key, "刷卡机达康书记的卡开始就恐龙当家爱上了肯定就拉伸到家啦科技四路打开就撒了肯定");

            String imageString = new String(Base64.encode(c));
            Util.logd("-->imageString==>"+c+":"+imageString);

            String e = decodeECB(key, Base64.decode(imageString));
            Util.logd("-->"+e);

            String d = decodeECB(key , c);
            Util.logd("-->"+d);

            byte[] a = encodeCBC("123456789","刷卡机达康书记的卡开始就恐龙当家爱上了肯定就拉伸到家啦科技四路打开就撒了肯定");
            Util.logd("-->"+a);
            String b = decodeCBC("123456789", a);
            Util.logd("-->"+b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成密钥
     *
     * @return
     * @throws Exception
     */
    private static byte[] generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(56); // des 必须是56, 此初始方法不必须调用
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }

    /**
     * 加密 CBC mode
     * 使用密钥工厂生成密钥，加密 解密
     * iv: DES in CBC mode and RSA ciphers with OAEP encoding operation.
     */
    public static byte[] encodeCBC(String key, String content) {
        try {
            Key k = toKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, k, new IvParameterSpec(getIV()));
            byte[] enc = cipher.doFinal(content.getBytes()); // 加密
            return enc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解密 CBC mode
     * 使用密钥工厂生成密钥，加密 解密
     * iv: DES in CBC mode and RSA ciphers with OAEP encoding operation.
     */
    public static String decodeCBC(String key, byte[] content) {
        try {
            Key k = toKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC);
            cipher.init(Cipher.DECRYPT_MODE, k, new IvParameterSpec(getIV()));
            byte[] dec = cipher.doFinal(content); // 解密
            return new String(dec);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] getIV() {
        String iv = "asdfivh7"; // IV length: must be 8 bytes long
        return iv.getBytes();
    }

    /**
     * 加密 ECB
     *
     * @param data 原文
     * @param key
     * @return 密文
     * @throws Exception
     */
    public static byte[] encodeECB(String key, String data) throws Exception {
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
        cipher.init(Cipher.ENCRYPT_MODE, k, new SecureRandom());
        return cipher.doFinal(data.getBytes());
    }

    /**
     * 解密 ECB
     *
     * @param data 密文
     * @param key
     * @return 明文、原文
     * @throws Exception
     */
    public static String decodeECB(String key, byte[] data) throws Exception {
        Key k = toKey(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
        cipher.init(Cipher.DECRYPT_MODE, k, new SecureRandom());
        return new String(cipher.doFinal(data));
    }

    /**
     * 还原密钥
     *
     * @param key
     * @return
     * @throws Exception
     */
    private static Key toKey(String key) throws Exception {
        String newKey = Md5.encode(key);
        Util.logd("Des newKey:"+newKey);
        DESKeySpec des = new DESKeySpec(newKey.substring(0,8).getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(des);
        return secretKey;
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string
     * @return
     */
    public static String string2MD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    /**
     * 解密试题试卷
     * @param secretKey
     * @param encryptText
     * @return
     */
    public static String decodeForPapaer(String secretKey, String encryptText) {
        String newKey = Md5.encode(Md5.encode(partKey + secretKey));
        if (newKey.length() < 8) {
            Util.logd("new key length < 8");
            // com.hjq.toast.ToastUtils.show(GlobalApplication.getInstance(), "获取题目出错，请稍后再试");
            return "";
        }
        try {
            //使用指定密钥构造IV
            IvParameterSpec zeroIv = new IvParameterSpec(Md5.encode(newKey).getBytes());
            //根据给定的字节数组和指定算法构造一个密钥。
            SecretKeySpec key = new SecretKeySpec(newKey.substring(0, 8).getBytes(), "DES");
            //返回实现指定转换的 Cipher 对象
            Cipher cipher = Cipher.getInstance("DES");
            //解密初始化
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            byte[] decryptData = cipher.doFinal(Base64.decode(encryptText));
            return new String(decryptData);
        } catch (Exception e) {
            e.printStackTrace();
            Util.logd("decodeForPapaer error : "+e.getMessage());
        }
        return "";
    }

}
