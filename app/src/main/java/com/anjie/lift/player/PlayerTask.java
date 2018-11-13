package com.anjie.lift.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.text.TextUtils;

import com.anjie.common.log.LogX;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.manager.MPlayerManager;
import com.anjie.lift.player.PlayerElement.ElementType;

import java.io.File;
import java.io.FileInputStream;

/**
 * 播放任务
 */
public class PlayerTask implements Runnable
{
    /**
     * 日志标签
     */
    private static final String TAG = "PlayerTask";

    /**
     * 播放器
     */
    private MediaPlayer mPlayer = null;

    /**
     * 播放片源
     */
    private PlayerElement playElement;

    /**
     * 任务回调函数
     */
    private TaskCallback mCallback;

    /**
     * 是否已经停止
     */
    private boolean isStop = false;

    /**
     * 播放结束监听器
     */
    private OnCompletionListener completeListener = new OnCompletionListener()
    {
        @Override
        public void onCompletion(MediaPlayer mediaplayer)
        {
            LogX.d(TAG, "onCompletion()");
            resetPlayer();
            onTaskFinish();
        }
    };

    /**
     * 播放错误监听器
     */
    private OnErrorListener errorListener = new OnErrorListener()
    {
        @Override
        public boolean onError(MediaPlayer player, int what, int extra)
        {
            LogX.d(TAG, "OnErrorListener.onError() what:" + what + ",extra:"
                    + extra);
            resetPlayer();
            onTaskFinish();
            return true;
        }
    };

    /**
     * 播放准备监听器
     */
    private OnPreparedListener preparedListener = new OnPreparedListener()
    {
        @Override
        public void onPrepared(MediaPlayer mp)
        {
            if (mCallback != null)
            {
                mCallback.notifyMediaPrepared(mp);
            }
        }
    };

    /**
     * 播放任务
     *
     * @param player
     *            播放器
     * @param playElement
     *            播放元素
     * @param callback
     *            回调函数通知播放管理
     */
    public PlayerTask(MediaPlayer player, PlayerElement playElement,
            TaskCallback callback)
    {
        this.mPlayer = player;
        this.playElement = playElement;
        this.mCallback = callback;
    }

    public void run()
    {
        if (playElement == null)
        {
            // 异常情况
            LogX.e(TAG, "current PlayElement is null.");
            sleepWait(2);
            onTaskFinish();
            return;
        }
        if (playElement.getType() == ElementType.video) {
            //if (){}else {}在此处增加SharePreference标志位的判断，如果是旋转屏幕后第一次播放则initVideo()，否则直接playVideo();
            // 播放视频
            // initVideo();

            playVideo(playElement);

        }
        else if (playElement.getType() == ElementType.audio)
        {
            // 播放音频
            playAudio(playElement);
        }
        else if (playElement.getType() == ElementType.image)
        {
            // 播放图片
            playImage(playElement);
        }
        else if (playElement.getType() == ElementType.url)
        {
            // 加载网页
            playUrl(playElement);
        }
        else
        {
            LogX.w(TAG, "playElement.getType() is unKnow.");
            // 异常情况
            onTaskFinish();
        }
    }

    /**
     * 停止任务
     */
    public void stopTask()
    {
        isStop = true;
    }

    /**
     * 播放音乐
     *
     * @param element
     *            音乐文件路径
     */
    private void playAudio(PlayerElement element)
    {
        String path = element.getFilePath();
        try
        {
            mCallback.notifyPlayElement(element);
            // 重置播放器
            resetPlayer();
            // 获取视频的绝对路径
            if (TextUtils.isEmpty(path))
            {
                throw new IllegalArgumentException("play Audio path is empty.");
            }

            // 兼容"/"处理(播放列表)
            // <path>/image/default.jpg</path>
            // <path>image/default.jpg</path>
            String filterPath = path;
            if (path.startsWith(File.separator))
            {
                filterPath = path.substring(File.separator.length(),
                        path.length());
            }

            // 完整的绝对路径
            String fullPath = FileManager.getInstance().getAppFileRoot() + filterPath;
            LogX.d(TAG, "Current PlayTask Audio.full path:" + fullPath);

            // 播放音乐必须使用这种方式,否则会报IOException
            File file = new File(fullPath);
            FileInputStream fis = new FileInputStream(file);
            // 设置片源路径
            mPlayer.setDataSource(fis.getFD());
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(false);
            mPlayer.setOnCompletionListener(completeListener);
            mPlayer.setOnErrorListener(errorListener);
//            mPlayer.setOnPreparedListener(preparedListener);
//            mPlayer.prepareAsync();
            mPlayer.prepare();
            LogX.d(TAG,"===========before start");
            mPlayer.start();
            //sleepWait(10);
            LogX.d(TAG,"===========after start");
        }
        catch (Exception e)
        {
            LogX.e(TAG, "playAudio meet Exception!", e);
            sleepWait(2);
            // 任务结束
            onTaskFinish();
        }
    }

    /**
     * 播放视频
     *
     * @param playElement
     *            视频元素
     */
    private void playVideo(PlayerElement playElement)
    {
        String path = playElement.getFilePath();
        try
        {
            // 回调函数通知播放区域准备相关控件显示VISIBLE
            mCallback.notifyPlayElement(playElement);
            //2018.07.06  播放完成后,400ms给surface holder准备
            if(MPlayerManager.getInstance().has_set_display.get())
            {
                LogX.d(TAG,"has_set_display +++++++++++++++ play video sleep 600ms ");
                Thread.sleep(600);
            }
            // 重置播放器
            resetPlayer();
            // 获取视频的绝对路径
            if (TextUtils.isEmpty(path))
            {
                throw new IllegalArgumentException("play video path is empty.");
            }

            // 兼容"/"处理(播放列表)
            // <path>/image/default.jpg</path>
            // <path>image/default.jpg</path>
            String filterPath = path;
            if (path.startsWith(File.separator))
            {
                filterPath = path.substring(File.separator.length(),
                        path.length());
            }

            // 完整的绝对路径
            String fullPath = FileManager.getInstance().getAppFileRoot()
                    + filterPath;
            LogX.d(TAG, "Current PlayTask Video.full path:" + fullPath);

            // 设置片源路径
            mPlayer.setDataSource(fullPath);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setLooping(false);
            mPlayer.setOnCompletionListener(completeListener);
            mPlayer.setOnErrorListener(errorListener);
            mPlayer.setOnPreparedListener(preparedListener);

            //2018.06.21 云端更新后，线程睡一秒以确保SurfaceView 成功创建
            if(!MPlayerManager.getInstance().has_set_display.get())
            {
                LogX.d(TAG,"+++++++++++++++play video sleep one second");
                sleepWait(1);
            }
            // 使用异步的prepare
            LogX.d(TAG,"===========PPPPP");
            mPlayer.prepareAsync();
        }
        catch (Exception e)
        {
            LogX.e(TAG, "playVideo meet Exception!", e);
            sleepWait(2);
            // 任务结束
            onTaskFinish();
        }
    }

    /**
     * 播放图片
     *
     * @param element
     *            图片的元素
     */
    private void playImage(PlayerElement element)
    {
        String imagePath = element.getFilePath();
        try
        {
            if (TextUtils.isEmpty(imagePath))
            {
                throw new IllegalArgumentException("play image path is empty!");
            }
            // 兼容处理"/"
            String filterPath = imagePath;
            if (imagePath.startsWith(File.separator))
            {
                filterPath = imagePath.substring(File.separator.length(),
                        imagePath.length());
            }

            // 完整路径
            String fullPath = FileManager.getInstance().getAppFileRoot()
                    + filterPath;
            element.setFullPath(fullPath);
            LogX.d(TAG, "Current PlayTask image.full path:" + fullPath);
            if (mCallback != null)
            {
                // 通知UI播放图片
                mCallback.notifyPlayElement(element);
            }

            // 播放时间获取顺序：1 从SP中取 2.从布局中取 3.默认值3
            int imageShowTime = ConfigManager.getInstance().getImageInterval();

            if (imageShowTime <= 0)
            {
                // 从布局中获取
                imageShowTime = element.getImageShowTime();
            }

            if(imageShowTime <= 0)
            {
                // 全部都获取不到，默认值3
                imageShowTime = 3;
            }
            sleepWait(imageShowTime);
        }
        catch (Exception e)
        {
            LogX.e("PlayerTask playImage meet exception!", e);
            sleepWait(2);
        }
        finally
        {
            // 任务结束
            onTaskFinish();
        }
    }

    /**
     * 播放一个网页内容
     * @param element 播放的Web网页
     */
    private void playUrl(PlayerElement element)
    {
        String url = element.getFilePath();
        try
        {
            if (TextUtils.isEmpty(url))
            {
                throw new IllegalArgumentException("play url path is empty!");
            }
            mCallback.notifyPlayElement(playElement);
            // 播放时间,网页播放的时间为2分钟加载一次任务。重复的任务,播放区域控件会做去重处理。
            sleepWait(120);
        }
        catch (Exception e)
        {
            LogX.e("PlayerTask url meet exception!", e);
            sleepWait(2);
        }
        finally
        {
            onTaskFinish();
        }
    }

    /**
     * 任务调用结束
     */
    private void onTaskFinish()
    {
        if (mCallback != null && !isStop)
        {
            mCallback.onFinish();
        }
    }

    /**
     * 重置播放器
     */
    private void resetPlayer()
    {
        if (mPlayer == null)
        {
            return;
        }
        try
        {
            if (mPlayer.isPlaying())
            {
                mPlayer.stop();
            }
            mPlayer.reset();
        }
        catch (Exception e)
        {
            LogX.e(TAG, "resetPlayer() player meet exception.", e);
        }
    }

    /**
     * 休眠等待
     * 
     * @param seconds 秒
     */
    private void sleepWait(int seconds)
    {
        if (seconds <= 0)
        {
            // 条件不符合不休眠
            return;
        }

        try
        {
            for (int i = 0; i < seconds; i++)
            {
                if (isStop)
                {
                    // 如果任务取消,及时结束任务
                    break;
                }
                // 每秒检测一次取消任务状态
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e)
        {
            LogX.e(TAG, "sleepWait InterruptedException!", e);
        }
    }
}
