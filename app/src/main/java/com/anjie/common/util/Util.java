package com.anjie.common.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Util
{

    private static final String TAG = "Util";

    /**
     * <getMD5Str> MD5加密
     * 
     * @param str
     *            明文
     * @return MD5加密后的密文
     **/
    public static String getMD5Str(String str)
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
        }
        catch (UnsupportedEncodingException e)
        {
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++)
        {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
            {
                md5StrBuff.append("0")
                        .append(Integer.toHexString(0xFF & byteArray[i]));
            }
            else
            {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString();
    }

    public static String URLEncoding(String str)
    {
        String temp = str;
        try
        {
            temp = URLEncoder.encode(str, "utf-8");
        }
        catch (UnsupportedEncodingException e)
        {

        }
        return temp;
    }

    /**
     * <parseDate> 解析时间数据
     * 
     * @param dateStr
     *            yyyyMMddHHmmss
     * @return
     **/
    @SuppressWarnings("unused")
    private static Date parseDate(String dateStr)
    {
        Calendar calendar = Calendar.getInstance();
        try
        {
            if (dateStr.length() > 0)
            {
                String year = dateStr.substring(0, 4);
                calendar.set(Calendar.YEAR, Integer.parseInt(year));
            }
            if (dateStr.length() > 4)
            {
                String month = dateStr.substring(4, 6);
                calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
            }
            if (dateStr.length() > 6)
            {
                String day = dateStr.substring(6, 8);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
            }

            if (dateStr.length() > 8)
            {
                String hour = dateStr.substring(8, 10);
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
            }
            if (dateStr.length() > 10)
            {
                String min = dateStr.substring(10, 12);
                calendar.set(Calendar.MINUTE, Integer.parseInt(min));
            }
            if (dateStr.length() > 12)
            {
                String second = dateStr.substring(12, 14);
                calendar.set(Calendar.SECOND, Integer.parseInt(second));
            }
        }
        catch (Exception e)
        {
        }
        return calendar.getTime();
    }

    /**
     * <formatDate> 格式化时间
     * 
     * @param str
     * date
     * @return
     * dateString
     **/
    public static String formatDate(String str)
    {
        if (str == null)
        {
            return "";
        }
        // Date date = parseDate(str);
        Date date = null;
        try
        {
            String temple = "yyyyMMddHHmmss";
            if (str.length() <= 14)
            {
                temple = temple.substring(0, str.length());
            }
            SimpleDateFormat sdf = new SimpleDateFormat(temple,
                    Locale.getDefault());
            date = sdf.parse(str);
        }
        catch (Exception e)
        {
        }
        if (date == null)
        {
            date = Calendar.getInstance().getTime();
        }
        SimpleDateFormat dateformat = new SimpleDateFormat("MM月dd日 HH:mm",
                Locale.getDefault());
        return dateformat.format(date);
    }

    /**
     * <formatDate> 格式化时间
     * 
     * @param str
     * date
     * @return
     * date string
     **/
    public static String formatBirthday(String str)
    {
        if (str == null)
        {
            return "";
        }
        // Date date = parseDate(str);
        Date date = null;
        try
        {
            String temple = "yyyyMMdd";
            if (str.length() <= 8)
            {
                temple = temple.substring(0, str.length());
            }
            SimpleDateFormat sdf = new SimpleDateFormat(temple,
                    Locale.getDefault());
            date = sdf.parse(str);
        }
        catch (Exception e)
        {
        }
        if (date == null)
        {
            date = Calendar.getInstance().getTime();
        }
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy年MM月dd日",
                Locale.getDefault());
        return dateformat.format(date);
    }

    /**
     * <formatDate> 格式化日期
     * 
     * @return
     **/
    public static String formatCurrentDate()
    {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyyMMddHHmmss",
                Locale.getDefault());
        String date = dateformat.format(Calendar.getInstance().getTime());
        return date;
    }

    public static String buildGetParams(int num, String[] key, Object[] value)
    {
        StringBuffer sb = new StringBuffer();
        int len = num;
        sb.append("?");
        for (int i = 0; i < len; i++)
        {
            sb.append(key[i]);
            sb.append("=");
            sb.append(value[i]);
            // if (i != len - 1) {
            sb.append("&");
            // }
        }
        sb.append("token=");
        // if (Macro.token != null)
        // {
        // sb.append(Macro.token);
        // }
        return sb.toString();
    }

    public static String buildPostParams(int num, String[] key, Object[] value)
    {
        StringBuffer sb = new StringBuffer();
        int len = num;
        for (int i = 0; i < len; i++)
        {
            sb.append(key[i]);
            sb.append("=");
            sb.append(value[i]);
            // if (i != len - 1) {
            sb.append("&");
            // }
        }
        sb.append("token=");
        // if (Macro.token != null)
        // {
        // sb.append(Macro.token);
        // }
        return sb.toString();
    }

    /**
     * 返回当前时间
     * 
     * @return "yyyy-MM-dd HH:mm:ss"格式的时间字符串
     */
    @SuppressLint("SimpleDateFormat")
    public static String getTime()
    {
        // 使用默认时区和语言环境获得一个日历
        Calendar cale = Calendar.getInstance();
        // 将Calendar类型转换成Date类型
        Date tasktime = cale.getTime();
        // 设置日期输出的格式
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        // 格式化输出
        return df.format(tasktime);
    }

    /**
     * 字符串变成JSON对象
     * 
     * @param str
     * json
     * JSON格式的字符串
     * @return JSON对象
     */
    public static JSONObject strToJson(String str)
    {
        if (str == null)
        {
            return null;
        }
        JSONObject json = null;
        try
        {
            json = new JSONObject(str);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 重设图片的大小
     * 
     * @param bitmap
     *            图片
     * @param w
     *            宽
     * @param h
     *            高
     * @return 重设过的图片
     */
    public static Bitmap resizeImage(Bitmap bitmap, int w, int h)
    {
        if (bitmap == null)
        {
            return null;
        }
        if (w <= 0 || h <= 0)
        {
            return null;
        }
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        if (width <= 0 || height <= 0)
        {
            return null;
        }
        int newWidth = w;
        int newHeight = h;
        // calculate the scale
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        // make a Drawable from Bitmap to allow to set the Bitmap
        // to the ImageView, ImageButton or what ever
        // return new BitmapDrawable(resizedBitmap);
        return resizedBitmap;

    }

    /**
     * Drawable对象转化Bitmap对象
     * 
     * @param drawable
     *            Drawable对象
     * @return Bitmap对象
     */
    public static Bitmap drawableToBitmap(Drawable drawable)
    {

        Bitmap bitmap = null;
        try
        {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE
                            ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            // canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }
        catch (OutOfMemoryError e)
        {
            Log.e(TAG, "Util.java drawableToBitmap OutOfMemoryError.");
        }
        catch (Exception e)
        {
            Log.e(TAG, "Util.java drawableToBitmap Exception.");
        }
        return bitmap;
    }

    /**
     * Bitmap对象转化byte数组
     * 
     * @param bitmap
     *            Bitmap对象
     * @return byte数组
     */
    public static byte[] bitmapToBytes(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Byte数组转化为Bitmap对象
     * 
     * @param data
     *            byte数组
     * @return Bitmap对象
     */
    public static Bitmap bytesToBimap(byte[] data)
    {
        if (data != null && data.length != 0)
        {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        else
        {
            return null;
        }
    }

    /**
     * 将字符串str按子字符串separatorChars 分割成数组
     * 
     * @param str
     *            要拆分的字符串
     * @param separatorChars
     *            用来拆分的分割字符
     * @return 拆分后的字符串
     */
    public static String[] split2(String str, String separatorChars)
    {
        return splitWorker(str, separatorChars, -1, false);
    }

    /**
     * 拆分字符串
     * 
     * @param str
     *            要拆分的字符串
     * @param separatorChars
     *            用来拆分的分割字符
     * @param max
     *            要拆分字符串的最大长度
     * @param preserveAllTokens
     * @return 拆分后的字符串
     */
    private static String[] splitWorker(String str, String separatorChars,
            int max, boolean preserveAllTokens)
    {
        if (str == null)
        {
            return null;
        }
        int len = str.length();
        if (len == 0)
        {
            return new String[] {"" };
        }
        Vector<String> vector = new Vector<String>();
        int sizePlus1 = 1;
        int i = 0;
        int start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null)
        {
            while (i < len)
            {
                if (str.charAt(i) == '\r' || str.charAt(i) == '\n'
                        || str.charAt(i) == '\t')
                {
                    if (match || preserveAllTokens)
                    {
                        lastMatch = true;
                        if (sizePlus1++ == max)
                        {
                            i = len;
                            lastMatch = false;
                        }
                        vector.addElement(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                }
                else
                {
                    lastMatch = false;
                    match = true;
                    i++;
                }
            }
        }
        else if (separatorChars.length() == 1)
        {
            char sep = separatorChars.charAt(0);
            while (i < len)
            {
                if (str.charAt(i) == sep)
                {
                    if (match || preserveAllTokens)
                    {
                        lastMatch = true;
                        if (sizePlus1++ == max)
                        {
                            i = len;
                            lastMatch = false;
                        }
                        vector.addElement(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                }
                else
                {
                    lastMatch = false;
                    match = true;
                    i++;
                }
            }
        }
        else
        {
            while (i < len)
            {
                int id = i + separatorChars.length() < len
                        ? i + separatorChars.length() : len;
                if (separatorChars.indexOf(str.charAt(i)) >= 0
                        && separatorChars.equals(str.substring(i, id)))
                {
                    if (match || preserveAllTokens)
                    {
                        lastMatch = true;
                        if (sizePlus1++ == max)
                        {
                            i = len;
                            lastMatch = false;
                        }
                        vector.addElement(str.substring(start, i));
                        match = false;
                    }
                    i += separatorChars.length();
                    start = i;
                }
                else
                {
                    lastMatch = false;
                    match = true;
                    i++;
                }
            }
        }

        if (match || preserveAllTokens && lastMatch)
        {
            vector.addElement(str.substring(start, i));
        }
        String[] ret = new String[vector.size()];
        vector.copyInto(ret);
        return ret;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
        {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++)
        {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
