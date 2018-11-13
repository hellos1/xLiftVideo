package com.anjie.lift.parse;

import java.io.ByteArrayInputStream;

import org.xmlpull.v1.XmlPullParser;

import com.anjie.common.util.NumUtils;
import com.anjie.lift.server.response.VersionResponse;

import android.text.TextUtils;
import android.util.Xml;

/**
 * 版本解析
 */
public final class VersionParser
{
    /**
     * 解析服务器的结果
     *
     * @param xmlText
     * @return
     * @throws Exception
     */
    public static VersionResponse parserServerVersion(String xmlText) throws Exception
    {
        if (TextUtils.isEmpty(xmlText))
        {
            return null;
        }
        VersionResponse response = null;
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(xmlText.getBytes("UTF-8"));
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        // 设置输入流 并指明编码方式
        xpp.setInput(byteInputStream, "UTF-8");
        // 产生第一个事件
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            switch (eventType)
            {
                // 判断当前事件是否为文档开始事件
                case XmlPullParser.START_DOCUMENT:
                {
                    response = new VersionResponse();
                    break;
                }
                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:
                {
                    if ("code".equals(xpp.getName()))
                    {
                        String codeText = xpp.nextText();
                        int code = NumUtils.parseSafeInt(codeText, -1);
                        int resultCode = code == 0 ? VersionResponse.Success : VersionResponse.Failed;
                        response.setResultCode(resultCode);
                    }
                    else if ("item".equals(xpp.getName()))
                    {
                        String version = xpp.getAttributeValue("", "version");
                        response.setResultCode(NumUtils.parseSafeInt(version, 0));
                        String path = xpp.getAttributeValue("", "path");
                        response.setPath(path);
                    }
                }
                case XmlPullParser.END_TAG:
                {

                }
            }
            // 进入下一个元素并触发相应事件
            eventType = xpp.next();
        }
        return response;
    }
}
