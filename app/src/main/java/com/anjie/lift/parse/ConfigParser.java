package com.anjie.lift.parse;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Xml;

import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.common.system.SystemPropertiesProxy;
import com.anjie.common.util.NumUtils;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.config.ElevatorInfo;
import com.anjie.lift.config.SavePowerMode;
import com.anjie.lift.config.USBSyncInfo;
import com.anjie.lift.usb.info.HiddenArea;
import com.anjie.lift.usb.info.HiddenArea.AreaType;
import com.anjie.lift.usb.info.KoneMediaInfo;
import com.anjie.lift.usb.info.KoneMediaInfo.MediaType;
import com.anjie.lift.usb.info.KoneUSBSyncInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 解析配置类
 */
public final class ConfigParser
{
    /**
     * 日志标签
     */
    private static final String TAG = "ConfigParser";

    /**
     * 解析USB同步资源方式
     * 
     * @return
     */
    public static USBSyncInfo parserUSBStorage(String filePath)
    {
        USBSyncInfo syncInfo = new USBSyncInfo();
        File file = new File(filePath);
        if (!file.exists())
        {
            // 同步配置文件不存在
            return syncInfo;
        }
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        try
        {
            InputStream in = new FileInputStream(file);
            // 设置输入流 并指明编码方式
            xpp.setInput(in, "UTF-8");
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
                        if ("SyncType".equalsIgnoreCase(xpp.getName()))
                        {
                            syncInfo.setMediaSyncType(NumUtils.parseSafeInt(xpp.nextText(), 0));
                        }
                        else if ("FontFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            syncInfo.setFontFileName(xpp.nextText());
                        }
                        else if ("UpFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            syncInfo.setLiftUpFileName(xpp.nextText());
                        }
                        else if ("DownFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            syncInfo.setLiftDownFileName(xpp.nextText());
                        }
                        else if ("ArriveFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            syncInfo.setLiftArriveFileName(xpp.nextText());
                        }
                        else if ("ViewFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            syncInfo.setViewFileName(xpp.nextText());
                        }
                        else if ("ApkFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            syncInfo.setApkFileName(xpp.nextText());
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {

                    }
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        }
        catch (Exception e)
        {
            LogX.e("Parse usbSync.xml meet Exception", e);
        }
        return syncInfo;
    }

    /**
     * 写电梯图标配置文件
     *
     * @param upFileName
     * @param downFileName
     * @param arriveFileName
     */
    public static void wirteLiftIconConfigFile(String upFileName, String downFileName, String arriveFileName)
    {
        StringBuffer confBuf = new StringBuffer();
        confBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        confBuf.append("<config>");
        confBuf.append("<UpFileName>").append(upFileName).append("</UpFileName>");
        confBuf.append("<DownFileName>").append(downFileName).append("</DownFileName>");
        confBuf.append("<ArriveFileName>").append(arriveFileName).append("</ArriveFileName>");
        confBuf.append("</config>");
        String appLiftIconConfigFile = FileManager.getInstance().getLiftIconConfigFile();
        FileCacheService.writeFile(appLiftIconConfigFile, confBuf.toString());
    }

    /**
     * 解析电梯运行方向图标
     *
     * @param filePath
     * @return
     */
    public static Bundle parseLiftIconConfigFile(String filePath)
    {
        Bundle bundle = new Bundle();
        File file = new File(filePath);
        if (!file.exists())
        {
            // 同步配置文件不存在
            return bundle;
        }
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        try
        {
            InputStream in = new FileInputStream(file);
            // 设置输入流 并指明编码方式
            xpp.setInput(in, "UTF-8");
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
                        if ("UpFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            bundle.putString("UpFileName", xpp.nextText());
                        }
                        else if ("DownFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            bundle.putString("DownFileName", xpp.nextText());
                        }
                        else if ("ArriveFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            bundle.putString("ArriveFileName", xpp.nextText());
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {

                    }
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        }
        catch (Exception e)
        {
            LogX.d("Parse liftIcon.xml meet Exception");
        }
        return bundle;
    }

    /**
     * 字体配置文件
     *
     * @param fileName
     */
    public static boolean writeFontConfigFile(String fileName)
    {
        StringBuffer confBuf = new StringBuffer();
        confBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        confBuf.append("<config>");
        confBuf.append("<FontFileName>").append(fileName).append("</FontFileName>");
        confBuf.append("</config>");
        String appFontConfigFile = FileManager.getInstance().getFontConfigFile();
        boolean bWriteResult = FileCacheService.writeFile(appFontConfigFile, confBuf.toString());
        return bWriteResult;
    }

    /**
     * 解析字体文件名
     *
     * @param filePath
     * @return
     */
    public static String parseFontFileName(String filePath)
    {
        String fileName = null;
        File file = new File(filePath);
        if (!file.exists())
        {
            // 同步配置文件不存在
            return null;
        }
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        try
        {
            InputStream in = new FileInputStream(file);
            // 设置输入流 并指明编码方式
            xpp.setInput(in, "UTF-8");
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
                        if ("FontFileName".equalsIgnoreCase(xpp.getName()))
                        {
                            fileName = xpp.nextText();
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {

                    }
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        }
        catch (Exception e)
        {
            LogX.d("Parse fontConfig.xml meet Exception");
        }
        return fileName;
    }

    /**
     * 解析电梯配置文件
     *
     * @return
     */
    public static ElevatorInfo parseElevatorInfo(String filePath)
    {
        ElevatorInfo info = null;
        File file = new File(filePath);
        if (!file.exists())
        {
            // 配置文件不存在
            return info;
        }
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        try
        {
            InputStream in = new FileInputStream(file);
            // 设置输入流 并指明编码方式
            xpp.setInput(in, "UTF-8");
            // 产生第一个事件
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                switch (eventType)
                {
                    // 判断当前事件是否为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                    {
                        // 文档开始构建对象
                        info = new ElevatorInfo();
                        break;
                    }
                    // 判断当前事件是否为标签元素开始事件
                    case XmlPullParser.START_TAG:
                    {
                        if ("rot".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setRot(NumUtils.parseSafeInt(xpp.nextText(), 0));
                        }
                        else if ("cloud".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setCloudType(NumUtils.parseSafeInt(xpp.nextText(), 0));
                        }
                        else if ("net".equalsIgnoreCase(xpp.getName()))
                        {
                            // 默认单机版
                            info.setNet(NumUtils.parseSafeInt(xpp.nextText(), 2));
                        }
                        else if ("localip".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setLocalIp(xpp.nextText());
                        }
                        else if ("netmask".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setNetMask(xpp.nextText());
                        }
                        else if ("gateway".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setGateWay(xpp.nextText());
                        }
                        else if ("dns".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setDns(xpp.nextText());
                        }
                        else if ("server".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setServer(xpp.nextText());
                        }
                        else if ("PID".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setPid(xpp.nextText());
                        }
                        else if ("DID".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setDid(xpp.nextText());
                        }
                        else if ("version".equalsIgnoreCase(xpp.getName()))
                        {
                            info.setVersion(xpp.nextText());
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {

                    }
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        }
        catch (Exception e)
        {
            LogX.d("Parse Elevator.xml meet Exception");
        }
        return info;
    }

    /**
     * 解析通力USB配置信息
     *
     * @param filePath
     *            USB文件路径
     * @return
     */
    public static KoneUSBSyncInfo parseKoneUSBSyncInfo(String filePath)
    {
        KoneUSBSyncInfo koneUSBConfig = null;
        if (TextUtils.isEmpty(filePath))
        {
            return koneUSBConfig;
        }
        File file = new File(filePath);
        if (!file.exists())
        {
            // 同步配置文件不存在
            return koneUSBConfig;
        }
        // 通力USB同步信息
        koneUSBConfig = new KoneUSBSyncInfo();
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser xpp = Xml.newPullParser();
        InputStream in = null;
        try
        {
            in = new FileInputStream(file);
            // 设置输入流 并指明编码方式
            xpp.setInput(in, "UTF-8");
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
                        if ("reset".equalsIgnoreCase(xpp.getName()))
                        {
                            String valueText = xpp.nextText();
                            if (!TextUtils.isEmpty(valueText) && "true".equalsIgnoreCase(valueText))
                            {
                                koneUSBConfig.setResetMode(true);
                            }
                        }
                        else if ("resource".equalsIgnoreCase(xpp.getName()))
                        {
                            // 解析资源标签
                            parseResource(xpp, koneUSBConfig);
                        }
                        else if ("parameter".equalsIgnoreCase(xpp.getName()))
                        {
                            LogX.d(TAG, "go to parameter");
                            // 解析配置参数标签
                            parseParameter(xpp, koneUSBConfig);
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        // 结束标签
                    }
                }
                // 进入下一个元素并触发相应事件
                eventType = xpp.next();
            }
        }
        catch (Exception e)
        {
            LogX.e("Parse kone usb config xml meet Exception", e);
            return null;
        }
        finally
        {
            if (in != null){//wangxu
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return koneUSBConfig;
    }


    /**
     * 解析KONE节能模式
     *
     * @param parser
     * @param koneUSBConfig
     */
    private static void parseSavePowerMode(XmlPullParser parser, KoneUSBSyncInfo koneUSBConfig)
    {
        final int initialDepth = parser.getDepth();
        SavePowerMode savePowerMode = null;
        try
        {
            outer_loop: while (true)
            {
                int eventType = parser.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = parser.getName();
                        if ("stageone".equalsIgnoreCase(elementName))
                        {
                            savePowerMode = parseSavePowerLevel(parser);
                            koneUSBConfig.setStageFirst(savePowerMode);
                        }
                        else if ("stagetwo".equalsIgnoreCase(elementName))
                        {
                            savePowerMode = parseSavePowerLevel(parser);
                            koneUSBConfig.setStageSecond(savePowerMode);
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (parser.getDepth() == initialDepth)
                        {
                            break outer_loop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse kone usb media info meet exception.", e);
        }
    }

    /**
     * 解析KONE节能模式等级
     *
     * @param parser
     * @return
     */
    private static SavePowerMode parseSavePowerLevel(XmlPullParser parser)
    {
        // 获取解析深度
        final int initialDepth = parser.getDepth();
        SavePowerMode savePowerMode = new SavePowerMode();
        try
        {
            outer_loop: while (true)
            {
                int eventType = parser.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = parser.getName();
                        if ("time".equalsIgnoreCase(elementName))
                        {
                            int time = NumUtils.parseSafeInt(parser.nextText(), -1);
                            savePowerMode.setTime(time);
                        }
                        else if ("brightness".equalsIgnoreCase(elementName))
                        {
                            int brightness = NumUtils.parseSafeInt(parser.nextText(), -1);
                            savePowerMode.setBrightness(brightness);
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (parser.getDepth() == initialDepth)
                        {
                            break outer_loop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse kone usb SavePowerLevel meet exception.", e);
            return null;
        }
        return savePowerMode;
    }

    /**
     * 解析KONE节能模式
     *
     * @param parser
     * @param koneUSBConfig
     */
    private static void parseResource(XmlPullParser parser, KoneUSBSyncInfo koneUSBConfig)
    {
        final int initialDepth = parser.getDepth();
        try
        {
            outer_loop: while (true)
            {
                int eventType = parser.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = parser.getName();
                        if ("title".equalsIgnoreCase(elementName))
                        {
                            koneUSBConfig.setTitle(parser.nextText());
                        }
                        else if ("scrollingtext".equalsIgnoreCase(elementName))
                        {
                            koneUSBConfig.setScrollText(parser.nextText());
                        }
                        else if ("picture".equalsIgnoreCase(elementName))
                        {
                            int interval = NumUtils.parseSafeInt(parser.getAttributeValue("", "interval"),0);
                            if (interval > 0)
                            {
                                koneUSBConfig.setPictureInterval(interval);
                            }
                            parsePictureItem(parser, koneUSBConfig);
                        }
                        else if ("video".equalsIgnoreCase(elementName))
                        {
                            KoneMediaInfo mediaInfo = new KoneMediaInfo(MediaType.Video, parser.nextText());
                            koneUSBConfig.addKoneMediaInfo(mediaInfo);
                        }
                        else if ("audio".equalsIgnoreCase(elementName))
                        {
                            KoneMediaInfo mediaInfo = new KoneMediaInfo(MediaType.Backgroud, parser.nextText());
                            koneUSBConfig.addKoneMediaInfo(mediaInfo);
                            koneUSBConfig.setPictureInterval(ConfigManager.getInstance().getImageInterval());//2018.04
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (parser.getDepth() == initialDepth)
                        {
                            break outer_loop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse kone usb resource info meet exception.", e);
        }
    }
    /**
     * 解析KONE配置参数部分
     *
     * @param parser
     * @param koneUSBConfig
     */
    private static void parseParameter(XmlPullParser parser, KoneUSBSyncInfo koneUSBConfig)
    {
        final int initialDepth = parser.getDepth();
        try
        {
            outer_loop: while (true)
            {
                int eventType = parser.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = parser.getName();
                        if ("volume".equalsIgnoreCase(elementName))
                        {
                            int volume = NumUtils.parseSafeInt(parser.nextText(), -1);
                            if (volume >= 0)
                            {
                                koneUSBConfig.setVolume(volume);
                            }
                        }
                        else if ("brightness".equalsIgnoreCase(elementName))
                        {
                            int brightness = NumUtils.parseSafeInt(parser.nextText(), -1);
                            if (brightness >= 0)
                            {
                                koneUSBConfig.setBrightness(brightness);
                            }
                        }
                        else if("time".equalsIgnoreCase(elementName))
                        {
                            koneUSBConfig.setTimeFormat(parseFormat(parser));
                        }
                        else if("date".equalsIgnoreCase(elementName))
                        {
                            koneUSBConfig.setDateFormat(parseFormat(parser));
                        }
                        else if("timearea".equalsIgnoreCase(elementName))
                        {
                            koneUSBConfig.addHiddenArea(new HiddenArea(AreaType.Timer, parser.nextText()));
                        }
                        else if("standby".equalsIgnoreCase(elementName))
                        {
                            parseSavePowerMode(parser, koneUSBConfig);
                        }
                        else if("fullscreen".equalsIgnoreCase(elementName))
                        {
                            String value = parser.nextText();
                            if ("true".equalsIgnoreCase(value) && isSetFullScreen())
                            {
                                koneUSBConfig.setFullScreenValue(1);
                            }
                            else if ("false".equalsIgnoreCase(value))
                            {
                                koneUSBConfig.setFullScreenValue(0);
                            }
                        }
                        else if("scrollingarea".equalsIgnoreCase(elementName))
                        {
                            String stt = parser.nextText();
                            koneUSBConfig.addHiddenArea(new HiddenArea(AreaType.ScrollText, stt));
                        }
                        else if("titlearea".equalsIgnoreCase(elementName))
                        {
                            koneUSBConfig.addHiddenArea(new HiddenArea(AreaType.Title, parser.nextText()));
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (parser.getDepth() == initialDepth)
                        {
                            break outer_loop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse kone usb parameter info meet exception.", e);
        }
    }

    /**
     * 解析KONE播放图片Item
     *
     * @param parser
     * @param koneUSBConfig
     */
    private static void parsePictureItem(XmlPullParser parser, KoneUSBSyncInfo koneUSBConfig)
    {
        final int initialDepth = parser.getDepth();
        try
        {
            outer_loop: while (true)
            {
                int eventType = parser.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = parser.getName();
                        if ("item".equalsIgnoreCase(elementName))
                        {
                            KoneMediaInfo mediaInfo = new KoneMediaInfo(MediaType.Picture, parser.nextText());
                            koneUSBConfig.addKoneMediaInfo(mediaInfo);
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (parser.getDepth() == initialDepth)
                        {
                            break outer_loop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse kone usb picture item info meet exception.", e);
        }
    }

    /**
     * 解析KONE时间日志格式标签
     *
     * @param parser
     * @return 格式标签
     */
    private static String parseFormat(XmlPullParser parser)
    {
        String format = null;
        final int initialDepth = parser.getDepth();
        try
        {
            outer_loop: while (true)
            {
                int eventType = parser.next();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                    {
                        String elementName = parser.getName();
                        if ("format".equalsIgnoreCase(elementName))
                        {
                            format = parser.nextText();
                        }
                    }
                    case XmlPullParser.END_TAG:
                    {
                        if (parser.getDepth() == initialDepth)
                        {
                            break outer_loop;
                        }
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse format meet exception.", e);
        }
        return format;
    }

    /**
     * 2018.07.19 只有在15寸屏且为横屏时才允许  全屏
     */
    private static int screenSize = ConfigManager.getInstance().getScreenSizeMsg();
    private static boolean isSetFullScreen()
    {
        if (screenSize != 150)
        {
            return false;
        }
        String type = SystemPropertiesProxy
                .get(AppContext.getInstance().getContext(),"persist.sys.hwrotation");
        int displayType = Integer.valueOf(type);
        return displayType <= 0;
    }
}
