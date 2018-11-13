package com.anjie.lift.parse;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Xml;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.common.util.NumUtils;
import com.anjie.lift.server.response.ViewResponse;
import com.anjie.lift.utils.ColorUtils;
import com.anjie.lift.view.ADTextView;
import com.anjie.lift.view.ArrowsView;
import com.anjie.lift.view.BGView;
import com.anjie.lift.view.BaseView;
import com.anjie.lift.view.CTextView;
import com.anjie.lift.view.CusImageView;
import com.anjie.lift.view.CusTextView;
import com.anjie.lift.view.DateView;
import com.anjie.lift.view.FloorView;
import com.anjie.lift.view.PlayerView;
import com.anjie.lift.view.Position;
import com.anjie.lift.view.StatusView;
import com.anjie.lift.view.TimerView;
import com.anjie.lift.view.TitleView;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 视图布局解析器
 */
public final class ViewParser
{
    /**
     * 视图解析模块
     */
    private static final String TAG = "ViewParser";

    /**
     * 解析页面布局
     * 
     * @param input
     * @return
     * @throws Exception
     */
    public static List<BaseView> parse(InputStream input, Bundle bundle) throws Exception
    {
        List<BaseView> mList = null;

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
                    mList = new ArrayList<BaseView>(); // 初始化BaseView集合
                    break;
                }
                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:
                {
                    if ("view".equals(xpp.getName()))
                    {
                        if (bundle != null)
                        {
                            // 取当前版本号
                            int version = NumUtils.parseSafeInt(xpp.getAttributeValue("", "version"), 0);
                            bundle.putInt("viewVersion", version);
                        }
                    }
                    else if ("control".equals(xpp.getName()))
                    {
                        // 获取item的属性type
                        String name = xpp.getAttributeValue("", "name");
                        if ("background".equals(name))
                        {
                            BGView bgView = new BGView();
                            bgView.setPosition(parsePosition(xpp));
                            parseBackGroundTag(xpp, bgView);
                            mList.add(bgView);
                        }
                        else if ("player".equals(name))
                        {
                            PlayerView playerView = new PlayerView();
                            playerView.setPosition(parsePosition(xpp));
                            mList.add(playerView);
                        }
                        else if ("timer".equals(name))
                        {
                            TimerView timerView = new TimerView();
                            timerView.setPosition(parsePosition(xpp));
                            parseTimerTag(xpp, timerView);
                            mList.add(timerView);
                        }
                        else if ("date".equals(name))
                        {
                            DateView dateView = new DateView();
                            dateView.setPosition(parsePosition(xpp));
                            parseDateTag(xpp, dateView);
                            mList.add(dateView);
                        }
                        else if ("arrows".equals(name))
                        {
                            ArrowsView arrowsView = new ArrowsView();
                            arrowsView.setPosition(parsePosition(xpp));
                            parseArrowsTag(xpp, arrowsView);
                            mList.add(arrowsView);
                        }
                        else if ("status".equals(name))
                        {
                            StatusView statusView = new StatusView();
                            statusView.setPosition(parsePosition(xpp));
                            parseImageTypeTag(xpp, statusView);
                            mList.add(statusView);
                        }
                        else if ("title".equals(name))
                        {
                            TitleView titleView = new TitleView();
                            titleView.setPosition(parsePosition(xpp));
                            parseTextTypeTag(xpp, titleView);
                            mList.add(titleView);
                        }
                        else if ("floor".equals(name))
                        {
                            FloorView floorView = new FloorView();
                            floorView.setPosition(parsePosition(xpp));
                            parseTextTypeTag(xpp, floorView);
                            mList.add(floorView);
                        }
                        else if ("custom".equals(name))
                        {
                            String type = xpp.getAttributeValue("", "type");
                            if ("text".equals(type))
                            {
                                CusTextView cusTextView = new CusTextView();
                                cusTextView.setPosition(parsePosition(xpp));
                                parseTextTypeTag(xpp, cusTextView);
                                mList.add(cusTextView);
                            }
                            else if ("image".equals(type))
                            {
                                CusImageView cusImageView = new CusImageView();
                                cusImageView.setPosition(parsePosition(xpp));
                                parseImageTypeTag(xpp, cusImageView);
                                mList.add(cusImageView);
                            }
                        }
                        else if ("adtext".endsWith(name))
                        {
                            // 广告文字
                            ADTextView adTextView = new ADTextView();
                            adTextView.setPosition(parsePosition(xpp));
                            parseTextTypeTag(xpp, adTextView);
                            mList.add(adTextView);
                        }
                    }
                    break;
                }
                // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                {
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
     * 解析背景视图
     *
     * @param xpp
     * @param bgView
     */
    private static void parseBackGroundTag(XmlPullParser xpp, BGView bgView)
    {
        final int initialDepth = xpp.getDepth();
        try
        {
            outerloop: while (true)
            {
                int eventType = xpp.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = xpp.getName();
                        if ("image".equalsIgnoreCase(elementName))
                        {
                            bgView.setBackground(xpp.nextText());
                        }
                        else if ("color".equalsIgnoreCase(elementName))
                        {
                            bgView.setBackgroundColor(ColorUtils.rgbToColor(xpp.nextText()));
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (xpp.getDepth() == initialDepth)
                        {
                            break outerloop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG,"",e);
        }
    }

    /**
     * 解析时间视图
     *
     * @param xpp
     * @param timerView
     */
    private static void parseTimerTag(XmlPullParser xpp, TimerView timerView)
    {
        final int initialDepth = xpp.getDepth();
        try
        {
            outerloop: while (true)
            {
                int eventType = xpp.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = xpp.getName();
                        if ("format".equalsIgnoreCase(elementName))
                        {
                            timerView.setFormat(xpp.nextText());
                        }
                        else if ("align".equalsIgnoreCase(elementName))
                        {
                            timerView.setTextAlign(xpp.nextText());
                        }
                        else if("size".equalsIgnoreCase(elementName))
                        {
                            int size = NumUtils.parseSafeInt(xpp.nextText(), -1);
                            timerView.setTextSize(size);
                        }
                        else if ("color".equalsIgnoreCase(elementName))
                        {
                            timerView.setTextColor(ColorUtils.rgbToColor(xpp.nextText()));
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (xpp.getDepth() == initialDepth)
                        {
                            break outerloop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse view time tag meet exception", e);
        }
    }

    /**
     * 解析时间视图
     *
     * @param xpp
     * @param dateView
     */
    private static void parseDateTag(XmlPullParser xpp, DateView dateView)
    {
        final int initialDepth = xpp.getDepth();
        try
        {
            outerloop: while (true)
            {
                int eventType = xpp.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = xpp.getName();
                        if ("format".equalsIgnoreCase(elementName))
                        {
                            dateView.setFormat(xpp.nextText());
                        }
                        else if ("align".equalsIgnoreCase(elementName))
                        {
                            dateView.setTextAlign(xpp.nextText());
                        }
                        else if("size".equalsIgnoreCase(elementName))
                        {
                            int size = NumUtils.parseSafeInt(xpp.nextText(), -1);
                            dateView.setTextSize(size);
                        }
                        else if ("color".equalsIgnoreCase(elementName))
                        {
                            dateView.setTextColor(ColorUtils.rgbToColor(xpp.nextText()));
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (xpp.getDepth() == initialDepth)
                        {
                            break outerloop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse view time tag meet exception", e);
        }
    }

    /**
     * 解析电梯箭头视图
     *
     * @param xpp
     * @param arrowsView
     */
    private static void parseArrowsTag(XmlPullParser xpp, ArrowsView arrowsView)
    {
        final int initialDepth = xpp.getDepth();
        try
        {
            outerloop: while (true)
            {
                int eventType = xpp.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = xpp.getName();
                        if ("image".equalsIgnoreCase(elementName))
                        {
                            arrowsView.setImagePath(xpp.nextText());
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (xpp.getDepth() == initialDepth)
                        {
                            break outerloop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse view arrows tag meet exception", e);
        }
    }

    /**
     * 解析图片类型的视图
     *
     * @param xpp
     * @param arrowsView
     */
    private static void parseImageTypeTag(XmlPullParser xpp, CusImageView arrowsView)
    {
        final int initialDepth = xpp.getDepth();
        try
        {
            outerloop: while (true)
            {
                int eventType = xpp.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = xpp.getName();
                        if ("image".equalsIgnoreCase(elementName))
                        {
                            arrowsView.setImagePath(xpp.nextText());
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (xpp.getDepth() == initialDepth)
                        {
                            break outerloop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse view image type tag meet exception", e);
        }
    }

    /**
     * 解析文本类型的视图
     *
     * @param xpp
     * @param cTextView
     */
    private static void parseTextTypeTag(XmlPullParser xpp, CTextView cTextView)
    {
        final int initialDepth = xpp.getDepth();
        try
        {
            outerloop: while (true)
            {
                int eventType = xpp.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = xpp.getName();
                        if ("size".equalsIgnoreCase(elementName))
                        {
                            cTextView.setTextSize(NumUtils.parseSafeInt(xpp.nextText(), -1));
                        }
                        else if("color".equalsIgnoreCase(elementName))
                        {
                            cTextView.setTextColor(ColorUtils.rgbToColor(xpp.nextText()));
                        }
                        else if("align".equalsIgnoreCase(elementName))
                        {
                            cTextView.setTextAlign(xpp.nextText());
                        }
                        else if("maxLine".equalsIgnoreCase(elementName))
                        {
                            cTextView.setMaxLine(NumUtils.parseSafeInt(xpp.nextText(), -1));
                        }
                        else if("text".equalsIgnoreCase(elementName))
                        {
                            cTextView.setText(xpp.nextText());
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (xpp.getDepth() == initialDepth)
                        {
                            break outerloop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse view text type tag meet exception", e);
        }
    }

    /**
     * 解析坐标属性
     * 
     * @param xpp
     * @return
     */
    private static Position parsePosition(XmlPullParser xpp)
    {
        int x = NumUtils.parseSafeInt(xpp.getAttributeValue("", "x"), 0);
        int y = NumUtils.parseSafeInt(xpp.getAttributeValue("", "y"), 0);
        int width = NumUtils.parseSafeInt(xpp.getAttributeValue("", "width"), 0);
        int height = NumUtils.parseSafeInt(xpp.getAttributeValue("", "height"), 0);
        return new Position(x, y, width, height);
    }

    /**
     * 解析服务器下发的视图请求
     * 
     * @param xmlText
     * @return
     * @throws Exception
     */
    public static ViewResponse parserServerView(String xmlText) throws Exception
    {
        if (TextUtils.isEmpty(xmlText))
        {
            return null;
        }
        ViewResponse response = null;
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
                    response = new ViewResponse();
                    break;
                }
                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:
                {
                    if ("version".equals(xpp.getName()))
                    {

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

    /**
     * 解析布局目录下的配置文件
     * 
     * @param filePath
     * @return
     */
    public static String parseLayoutConfig(String filePath)
    {
        String layoutDirName = null;
        if (TextUtils.isEmpty(filePath))
        {
            return layoutDirName;
        }

        File file = new File(filePath);
        if (!file.exists())
        {
            return layoutDirName;
        }

        FileInputStream inputStream = null;
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        try
        {
            inputStream = new FileInputStream(file);
            // 设置输入流 并指明编码方式
            xpp.setInput(inputStream, "UTF-8");
            // 产生第一个事件
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                switch (eventType)
                {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                    {
                        break;
                    }
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                    {
                        if ("layoutDir".equalsIgnoreCase(xpp.getName()))
                        {
                            layoutDirName = xpp.getText();
                        }
                    }
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        }
        catch (Exception e)
        {
            LogX.e("", "parseLayoutConfig meet exception！", e);
        }
        finally
        {
            IOUtils.close(inputStream);
        }
        return layoutDirName;
    }

    /**
     * 生成布局配置文件内容
     * 
     * @return
     */
    public static String buildLayoutConfigContent(String layoutDirName)
    {
        StringBuffer fileContentBuf = new StringBuffer();
        fileContentBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        fileContentBuf.append("<config>");
        fileContentBuf.append("<layoutDir>").append(layoutDirName).append("</layoutDir>");
        fileContentBuf.append("</config>");
        return fileContentBuf.toString();
    }
}
