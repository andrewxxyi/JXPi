package cn.ijingxi.common.util;

import java.security.MessageDigest;

/**
 * Created by andrew on 15-11-3.
 */
public class MD5 {

    private static MessageDigest md5 = null;

    public static String getMD5(String msg) throws Exception {
        utils.Check(msg==null,"MD5摘要的字符串不能为空");
        if(md5==null)
            md5 = MessageDigest.getInstance("MD5");
        char[] charArray = msg.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }

    public static String getHMAC_MD5(String msg,String key) throws Exception {
        utils.Check(msg==null,"MD5摘要的字符串不能为空");
        if(md5==null)
            md5 = MessageDigest.getInstance("MD5");
        char[] msgArray = msg.toCharArray();
        char[] keyArray = key.toCharArray();
        byte[] keybyteArray1 = new byte[keyArray.length];
        byte[] keybyteArray2 = new byte[keyArray.length];
        for (int i = 0; i < keyArray.length; i++){
            keybyteArray1[i] = (byte) ((byte) keyArray[i] ^ 0x36);
            keybyteArray2[i] = (byte) ((byte) keyArray[i] ^ 0x5C);
        }
        byte[] msgbyteArray = new byte[msgArray.length+keybyteArray1.length];
        for (int i = 0; i < keybyteArray1.length; i++)
            msgbyteArray[i] = (byte) keybyteArray1[i];
        for (int i = 0; i < msgArray.length; i++)
            msgbyteArray[keybyteArray1.length+i] = (byte) msgArray[i];

        byte[] md5Bytes1 = md5.digest(msgbyteArray);

        byte[] msgbyteArray1 = new byte[md5Bytes1.length+keybyteArray2.length];
        for (int i = 0; i < keybyteArray2.length; i++)
            msgbyteArray1[i] = (byte) keybyteArray2[i];
        for (int i = 0; i < md5Bytes1.length; i++)
            msgbyteArray1[keybyteArray2.length+i] = (byte) md5Bytes1[i];

        byte[] md5Bytes2= md5.digest(msgbyteArray1);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes2.length; i++){
            int val = ((int) md5Bytes2[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }


}
