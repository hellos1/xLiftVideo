package com.anjie.lift.parse;

import android.text.TextUtils;
import android.util.Xml;

import com.anjie.common.log.LogX;
import com.anjie.common.util.NumUtils;
import com.anjie.lift.download.MediaDownloadTask;
import com.anjie.lift.player.PlayerElement;
import com.anjie.lift.player.PlayerElement.ElementType;
import com.anjie.lift.server.response.PlayListResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 播放列表解析器
 */
public final class PlayerListParser
{

    /**
     * 日志标签
     */
    private static final String TAG = "PlayerListParser";

    /**
     * 解析播放列表
     * 
     * @param input
     * @return
     * @throws Exception
     */
    public static List<PlayerElement> parse(InputStream input) throws Exception
    {
        List<PlayerElement> mList = null;

        PlayerElement element = null;

        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        // 设置输入流 并指明编码方式
        xpp.setInput(input, "UTF-8");
        // 产生第一个事件
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            switch (eventType)
            {
                // 判断当前事件是否为文档开始事件
                case XmlPullParser.START_DOCUMENT:
                {
                    mList = new ArrayList<PlayerElement>(); // 初始化PlayerElement集合
                    break;
                }
                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:
                {
                    if (xpp.getName().equals("item"))
                    {
                        // 获取item的属性type
                        String type = xpp.getAttributeValue("", "type");
                        if ("image".equalsIgnoreCase(type))
                        {
                            String strNum = xpp.getAttributeValue("", "time");
                            element = new PlayerElement(ElementType.image);
                            if (!TextUtils.isEmpty(strNum))
                            {
                                element.setImageShowTime(NumUtils.parseSafeInt(strNum, 3));
                            }
                        }
                        else if ("video".equalsIgnoreCase(type))
                        {
                            element = new PlayerElement(ElementType.video);
                        }
                        else if ("audio".equalsIgnoreCase(type))//Liuchun
                        {
                            element = new PlayerElement(ElementType.audio);
                        }
                        else if ("url".equalsIgnoreCase(type))
                        {
                            element = new PlayerElement(ElementType.url);
                        }
                    }
                    else if (xpp.getName().equals("path"))
                    {
                        eventType = xpp.next();// 让解析器指向path属性的值
                        if (element != null)
                        {
                            element.setFilePath(xpp.getText());
                        }
                    }
                    break;
                }
                // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                {
                    if (xpp.getName().equals("item"))
                    { // 判断结束标签元素是否是item
                        if (element != null)
                        {
                            mList.add(element); // 将item添加到PlayList集合
                            element = null;
                        }
                    }
                    break;
                }
                default:
                {
                    break;
                }
            }
            // 进入下一个元素并触发相应事件
            eventType = xpp.next();
        }
        return mList;
    }

    /**
     * 解析从服务器获取到的播放列表
     * 
     * @param jsonText
     *            JSON数据
     * @return
     * @throws Exception
     */
    public static PlayListResponse parserServerPlayList(String jsonText)
    {
        if (TextUtils.isEmpty(jsonText))
        {
            return null;
        }
        PlayListResponse response = null;
        MediaDownloadTask itemTask = null;
        try
        {
            JSONArray jsonArray = new JSONArray(jsonText);
            int length = jsonArray.length();
            LogX.d(TAG, "response total size():" + length);
            if (length > 0)
            {
                response = new PlayListResponse();
                response.setResultCode(PlayListResponse.Success);
                JSONObject jsonObj;
                String path;
                String name;
                String type;
                for (int i = 0; i < length; i++)
                {
                    jsonObj = (JSONObject) jsonArray.get(i);
                    path = jsonObj.optString("URL", null);
                    name = jsonObj.optString("CNAME", null);
                    type = jsonObj.optString("PTYPLE", null);
                    itemTask = new MediaDownloadTask(path, name, type);
                    response.addMediaDownloadTask(itemTask);
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse server playlist response failed.", e);
            response = null;
        }
        return response;
    }
}
