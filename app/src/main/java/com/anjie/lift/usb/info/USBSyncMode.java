package com.anjie.lift.usb.info;

import android.text.TextUtils;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.parse.PlayerListParser;
import com.anjie.lift.player.PlayerElement;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * USB同步模式基类
 */
public abstract class USBSyncMode
{
    /**
     * 是否有USB/media/下的配置文件,如果有则支持同步
     * 
     * @param mediaPath
     * @return
     */
    public abstract boolean isHasUSBSyncConfig(String mediaPath);

    /**
     * 文件是否存在
     * 
     * @param fileDir
     * @param fileName
     * @return
     */
    protected boolean isFileExist(String fileDir, String fileName)
    {
        File file = new File(fileDir, fileName);
        return file.exists();
    }

    /**
     * 执行USB同步任务
     * 
     * @return
     */
    public abstract boolean doSyncUSBDevice(String usbMediaPath);

    /**
     * 构建播放列表内容
     * 
     * @param mList
     * @return
     */
    protected String buildPlayItem(List<String> mList)
    {
        //Parse the current playlist
        InputStream inputStream = null;
        List<PlayerElement> newPlayList = null;
        try
        {
            String playListPath = FileManager.getInstance().getPlayListPath();
            File playListFile = new File(playListPath);
            if (playListFile.exists()) {
                inputStream = new FileInputStream(playListFile);

                // 解析出新的播放列表
                newPlayList = PlayerListParser.parse(inputStream);
            }

        }
        catch (Exception e)
        {
            LogX.e("Loading parse PlayList meet exception!", e);
        }
        finally
        {
            // 关闭流
            IOUtils.close(inputStream);
        }
        boolean is_pic = false;
        boolean is_audio = false;
        boolean is_video = false;

        if (mList != null && mList.size() > 0)
        {
            StringBuffer buf = new StringBuffer();
            int num = 0;
            buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            buf.append("<playlist>");
            for (String item : mList)
            {
                if (!TextUtils.isEmpty(item))
                {
                    if (item.startsWith(FileManager.IMAGE_DIR))
                    {
                        buf.append("<item type=\"image\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                        is_pic = true;
                    }
                    else if (item.startsWith(FileManager.VIDEO_DIR))
                    {
                        buf.append("<item type=\"video\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                        is_video=true;
                    }
                    else if (item.startsWith(FileManager.AUDIO_DIR))
                    {
                        buf.append("<item type=\"audio\">");
                        buf.append("<path>").append(item).append("</path>");
                        buf.append("</item>");
                        num++;
                        is_audio=true;
                    }
                }
            }

            for(PlayerElement newitem: newPlayList)
            {
                if(newitem.getType()== PlayerElement.ElementType.audio)
                {
                    if(is_pic || is_video){//如果这次加的是图片或者视频，把以前的audio加进去
                        buf.append("<item type=\"audio\">");
                        buf.append("<path>").append(newitem.getFilePath()).append("</path>");
                        buf.append("</item>");
                        num++;
                    }

                }else  if(newitem.getType()== PlayerElement.ElementType.image)
                {
                    if(is_audio){//如果这次是音频，把以前的图片加进去
                        buf.append("<item type=\"image\">");
                        buf.append("<path>").append(newitem.getFilePath()).append("</path>");
                        buf.append("</item>");
                        num++;
                    }
                }
                else  if(newitem.getType()== PlayerElement.ElementType.video)
                {
                    if(is_audio) {//如果这次是音频，把以前的视频加进去
                        buf.append("<item type=\"video\">");
                        buf.append("<path>").append(newitem.getFilePath()).append("</path>");
                        buf.append("</item>");
                        num++;
                    }
                }
            }
            buf.append("</playlist>");
            // 返回播放列表内容
            return num > 0 ? buf.toString() : null;
        }
        return null;
    }
}
