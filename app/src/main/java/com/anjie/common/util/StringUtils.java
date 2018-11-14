package com.anjie.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Base64;

import com.anjie.common.log.LogX;

public class StringUtils
{
    /**
     * 获取MD5加密
     * 
     * @param str
     * @return
     */
    public static String getMD5encode(String str)
    {
        MessageDigest messageDigest = null;
        try
        {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        }
        catch (NoSuchAlgorithmException e)
        {
            LogX.d("", "MD5 NoSuchAlgorithmException Exception!");
        }
        catch (UnsupportedEncodingException e)
        {
            LogX.d("", "MD5 UnsupportedEncodingException Exception!");
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++)
        {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
            {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            }
            else
            {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString();
    }

    /**
     * URL编码
     * 
     * @param str
     * @return
     */
    public static String URLEncoding(String str)
    {
        if (str == null)
        {
            return str;
        }
        String temp = str;
        try
        {
            temp = URLEncoder.encode(str, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            LogX.d("", "URL encode meet Exception!");
        }
        return temp;
    }

    /**
     * BASE64加密
     * 
     * @param srcInfo
     * @return
     */
    public static String Base64Encode(byte[] srcInfo)
    {
        String secText = null;
        if (srcInfo == null || srcInfo.length == 0)
        {
            return secText;
        }
        secText = Base64.encodeToString(srcInfo, Base64.DEFAULT);
        return secText;
    }

    /**
     * BASE64加密
     * 
     * @param strText
     * @return
     */
    public static String Base64Encode(String strText)
    {
        if (TextUtils.isEmpty(strText))
        {
            return null;
        }
        byte[] resultBuf = getUTF8Bytes(strText);
        return Base64Encode(resultBuf);
    }

    /**
     * Base64解密
     * 
     * @param strText
     * @return
     */
    public static byte[] Base64Decode(String strText)
    {
        if (TextUtils.isEmpty(strText))
        {
            return null;
        }
        return Base64.decode(strText, Base64.DEFAULT);
    }

    /**
     * 字符串转JSON
     * 
     * @param jsonStr
     * @return
     */
    public static JSONObject parseJson(String jsonStr)
    {
        if (TextUtils.isEmpty(jsonStr))
        {
            return null;
        }
        JSONObject jsonObj = null;
        try
        {
            jsonObj = new JSONObject(jsonStr);
        }
        catch (JSONException e)
        {
            LogX.d("", "parse JSONObject meet Exception!");
        }

        return jsonObj;
    }

    /**
     * 获取UTF-8
     * 
     * @param text
     * @return
     */
    public static byte[] getUTF8Bytes(String text)
    {
        byte[] result = null;
        try
        {
            result = text.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            LogX.d("", "Text to UTF-8 byte meet UnsupportedEncodingException!");
        }
        return result;
    }

    /**
     * 比较字符串是否相等
     * 
     * @param str1
     * @param str2
     * @return
     */
    public static boolean isSameStr(String str1, String str2)
    {
        if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2))
        {
            return false;
        }
        return str1.equals(str2);
    }
}
