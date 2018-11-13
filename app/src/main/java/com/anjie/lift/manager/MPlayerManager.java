package com.anjie.lift.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.anjie.common.http.HttpDownload;
import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.common.threadpool.PriorityThreadFactory;
import com.anjie.lift.activity.UICode;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.parse.PlayerListParser;
import com.anjie.lift.player.PlayerElement;
import com.anjie.lift.player.PlayerTask;
import com.anjie.lift.player.TaskCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 播放器管理类
 */
public final class MPlayerManager
{

    /**
     * 日志标签
     */
    private static final String TAG = "MPlayer";

    /**
     * 线程生命时间
     */
    private static final int KEEP_ALIVE_TIME = 1800;
    public  boolean isInitVideo = true;

    /**
     * 播放器管理单实例
     */
    private static MPlayerManager instance = new MPlayerManager();

    /**
     * 播放器
     */
    private MediaPlayer mPlayer = null;

    /**
     * 播放器播放视频需要的
     */
    private SurfaceHolder surfaceHolder;

    /**
     * 线程池
     */
    protected Executor mExecutor;

    /**
     * UI刷新Handler消息
     */
    private Handler handler;

    /**
     * 播放列表
     */
    private List<PlayerElement> mPlayList = new ArrayList<PlayerElement>();

    /**
     * 锁
     */
    private final Object lock = new Object();

    public AtomicBoolean has_set_display = new AtomicBoolean(false);

    /**
     * 播放列表指针
     */
    private int index = 0;

    /**
     * 是否停止播放
     */
    private AtomicBoolean mIsStop = new AtomicBoolean(false);

    /**
     * 媒体是否已经进入准备状态
     */
    private AtomicBoolean isMediaPrepared = new AtomicBoolean(false);

    /**
     * 当前播放任务
     */
    private PlayerTask currentPlayTask;

    /**
     * 当前播放任务
     */
    private PlayerTask currentAudioTask;

    /**
     * 任务回调函数
     */
    private TaskCallback mCallback = new TaskCallback()
    {
        @Override
        public void onFinish()
        {
            // 状态置位
            isMediaPrepared.set(false);

            if (mIsStop.get())
            {
                LogX.d(TAG, "PlayElement onFinish() mIsStop.");
                // 如果已经停止播放器,不要再继续了
                return;
            }
            LogX.d(TAG, "------------------playTask finished");
            startPlayTask();
        }

        @Override
        public void notifyPlayElement(PlayerElement element)
        {
            if (handler != null)
            {
                handler.sendMessage(handler.obtainMessage(UICode.PLAY_ELEMENT, element));
            }
        }

        @Override
        public void notifyMediaPrepared(MediaPlayer mp)
        {
            playMedia(mp);
        }
    };

    /**
     * 任务回调函数
     */
    private TaskCallback audioCallback = new TaskCallback()
    {
        @Override
        public void onFinish()
        {
            // 状态置位
            isMediaPrepared.set(false);

            if (mIsStop.get())
            {
                LogX.d(TAG, "PlayElement onFinish() mIsStop.");
                // 如果已经停止播放器,不要再继续了
                return;
            }
            LogX.d(TAG, "------------audio finished");
            startAudio();
        }

        @Override
        public void notifyPlayElement(PlayerElement element)
        {
            if (handler != null)
            {
                handler.sendMessage(handler.obtainMessage(UICode.PLAY_ELEMENT, element));
            }
        }

        @Override
        public void notifyMediaPrepared(MediaPlayer mp)
        {
            playMedia(mp);
        }
    };
    /**
     * 私有构造
     */
    private MPlayerManager()
    {
        mPlayer = new MediaPlayer();
        int value = ConfigManager.getInstance().getVolume();
        setMediaVolume(value);
        mExecutor = new ThreadPoolExecutor(1, 1, KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("Player", android.os.Process.THREAD_PRIORITY_BACKGROUND));
    }

    /**
     * 单实例
     *
     * @return
     */
    public static MPlayerManager getInstance()
    {
//        if (instance == null){
//            instance = new MPlayerManager();
//        }
        return instance;
    }

    /**
     * 设置播放器和刷新UI的Handler
     *
     * @param handler
     */
    public void setMediaPlayer(Handler handler)
    {
        this.handler = handler;
    }

    /**
     * 设置播放视频的SurfaceHolder
     *
     * @param holder SurfaceHolder
     */
    public void setDisplay(SurfaceHolder holder)
    {
        surfaceHolder = holder;
//        mPlayer.setDisplay(holder);

        if (isMediaPrepared.get())
        {
            /*2018.07.27playMedia的时候，如果SurfaceHolder没有创建好，可以在SurfaceHolder创建的时候
            设置mPlayer的holder ***/
            mPlayer.setDisplay(holder);
            //如果MedIaPlayer已经准备好任务
            mPlayer.start();
        }
    }

    boolean is_init = true;
    public void SetInit() {
        LogX.d(TAG, "-------------This is setInit audio");
        this.is_init=true;
    }

    /**
     * 播放视频
     */
    public void startPlayTask()
    {

        //播放时间到达屏保时间后，暂停播放；2018.08.01

        if (bootPlay.get()){
            CURRENT_POSITION = 0;
            TAG_RESTART = true;
            TAG_PAUSE = false;
            bootPlay.getAndSet(false);
            if (handler != null)
            {
                handler.sendEmptyMessageDelayed(UICode.PAUSE_PLAYER, 20 * 60 * 1000);
            }
        }

        // 关闭停止标志位
        mIsStop.set(false);
        PlayerElement playElement = getNextPlayElement();
        LogX.d(TAG, "playList: "+mPlayList);

        if(is_init==true)
        {
            is_init=false;
            startAudio();
        }

        //2018.06.22 防止资源复制失败，mPlayList为空的情况
        if (playElement == null)
        {
            return;
        }

        if(playElement.getType()!= PlayerElement.ElementType.audio) {
            currentPlayTask = new PlayerTask(mPlayer, playElement, mCallback);
            // 线程池执行
            mExecutor.execute(currentPlayTask);
        }
    }

    public  void startAudio()
    {
        PlayerElement audiElement = getAudiPlayElement();
        if(audiElement!= null) {
            currentAudioTask = new PlayerTask(mPlayer, audiElement, audioCallback);
            // 线程池执行
            mExecutor.execute(currentAudioTask);
        }
    }

    /**
     * 获取下一个播放元素
     *
     * @return
     */
    private PlayerElement getNextPlayElement()
    {

        PlayerElement playElement;
        MPlayerManager.getInstance().isInitVideo = false;//For Debug
        if(MPlayerManager.getInstance().isInitVideo) {
            MPlayerManager.getInstance().isInitVideo = false;
            playElement = new PlayerElement(PlayerElement.ElementType.video);
            ///mnt/sdcard/elevator/mnt/sdcard/elevator/initvideo.mp4
            playElement.setFilePath("/initvideo.mp4");
            
            is_init=false;
            LogX.d(TAG,"This is the initvideo playing fuck!!!!");
            return  playElement;
        }
        //is_init = true;
        synchronized (lock)
        {
            if (mPlayList == null || mPlayList.size() <= 0)
            {
                return null;
            }
            if (index >= mPlayList.size())
            {
                index = 0;
            }
            playElement = mPlayList.get(index);
            //FIX AUDIO
            if(playElement.getType()== PlayerElement.ElementType.audio)
            {
                index++;
                if (index >= mPlayList.size())
                {
                    index = 0;
                }
                playElement = mPlayList.get(index);
                //is_init=true;
            }
            //如果是视频，就不播放音频
            if(playElement.getType()== PlayerElement.ElementType.video)
            {
                is_init=false;
            }

            index++;
        }
        return playElement;
    }

    private PlayerElement getAudiPlayElement() {
        PlayerElement playElement = null;
       for(PlayerElement item: mPlayList)
       {
           if(item.getType()== PlayerElement.ElementType.audio) {
               playElement = item;
               break;
           }
       }
        return playElement;
    }

    /**
     * 停止播放器
     */
    public void stopPlayTask(boolean is_release)
    {
        LogX.d(TAG, "call stopPlayer");
        // 停止播放器任务
        mIsStop.set(true);
        if (currentPlayTask != null)
        {
            // 停止当前任务
            currentPlayTask.stopTask();
        }

        if (currentAudioTask != null)
        {
            // 停止当前任务
            currentAudioTask.stopTask();
        }

        if (mPlayer != null)
        {
            try
            {
                if (mPlayer.isPlaying())
                {
                    mPlayer.stop();
                }
                mPlayer.reset();

                mPlayer.release();
                mPlayer=null;
                mPlayer=new MediaPlayer();

//                if(is_release == true) {
//                    mPlayer.release();
//                    mPlayer=null;
//                    mPlayer=new MediaPlayer();
//                    if (surfaceHolder != null) {
//                        surfaceHolder.getSurface().release();
//                    }
//                }else {
//                    mPlayer.release();
//                    mPlayer=null;
//                    mPlayer=new MediaPlayer();//Only support changed from audo to video
//                }
              //  MPlayerManager.getInstance().has_set_display.set(false);
            }
            catch (Exception e)
            {
                LogX.e(TAG, "PlayerManager stop Player meet Exception!", e);
            }
        }
    }

    /**
     * 加载播放列表
     */
    public void loadPlayList(boolean is_delete)
    {
        InputStream inputStream = null;
        try
        {
            String playListPath = FileManager.getInstance().getPlayListPath();
            File playListFile = new File(playListPath);
            if (playListFile.exists())
            {
                inputStream = new FileInputStream(playListFile);
            }
            else
            {
                // APP目录下没有资源使用默认的图片
                String defImagePath = FileManager.getInstance().getDefImagePath();
                Context context = AppContext.getInstance().getContext();
                FileCacheService.writeFile(context.getAssets().open("default.jpg"), defImagePath);
                inputStream = context.getAssets().open("playlist.xml");
                // 写默认播放列表文件
                FileCacheService.writeFile(context.getAssets().open("playlist.xml"), playListPath);
                FileCacheService.copyFile("/system/app/kone.mp4", FileManager.getInstance().getVideoRealPath());
                //FileCacheService.copyFile("/system/app/initvideo.mp4", FileManager.getInstance().getAppFileRoot());//Fix issue
            }

            // 解析出新的播放列表
            List<PlayerElement> newPlayList = PlayerListParser.parse(inputStream);
            synchronized (lock)
            {
                mPlayList = newPlayList;
            }
            if(is_delete) {
                // 删除过期的
                deleteExpirePlayElement(newPlayList);
                LogX.d(TAG, "Loading parse PlayList:" + mPlayList);
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
    }

    /**
     * 获取当前的播放列表
     *
     * @return 播放列表
     */
    public List<PlayerElement> getPlayList()
    {
        synchronized (lock)
        {
            if (mPlayList == null || mPlayList.size() <= 0)
            {
                loadPlayList(false);
            }
            return new ArrayList<PlayerElement>(mPlayList);
        }
    }

    /**
     * 删除期满的视频
     *
     * @param newPlayList 新的播放列表
     */
    private void deleteExpirePlayElement(final List<PlayerElement> newPlayList)
    {
        final List<String> deletePlayFileList = new ArrayList<String>();
        // 添加所有文件
        deletePlayFileList.clear();
        deletePlayFileList.addAll(getAllPlayPath(FileManager.VIDEO_DIR));
        deletePlayFileList.addAll(getAllPlayPath(FileManager.IMAGE_DIR));
        deletePlayFileList.addAll(getAllPlayPath(FileManager.AUDIO_DIR));
        String filterPath;
        for (PlayerElement playElement : newPlayList)
        {
            // 兼容处理"/"开头的路径
            filterPath = playElement.getFilePath();
            if (filterPath.startsWith(File.separator))
            {
                filterPath = filterPath.substring(File.separator.length(), filterPath.length());
            }
            // 过滤掉新的播放列表文件
            deletePlayFileList.remove(filterPath);
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String appRootPath = FileManager.getInstance().getAppFileRoot();
                for (String playFilePath : deletePlayFileList)
                {
                    if (!"image/default.jpg".equals(playFilePath))
                    {
                        // 过滤掉默认文件
                        IOUtils.deleteFileSafely(appRootPath + playFilePath);
                    }
                }
            }
        }).start();
    }

    /**
     * 获取指定目录下所有文件(.download文件不在范围内)
     *
     * @param playType 播放类型
     * @return 播放元素文件名列表
     */
    private List<String> getAllPlayPath(String playType)
    {
        List<String> playFilePath = new ArrayList<String>();
        String fileDir;
        String[] fileList = null;
        if (FileManager.VIDEO_DIR.equals(playType))
        {
            fileDir = FileManager.getInstance().getVideoPathDir();
            fileList = new File(fileDir).list();
        }
        else if (FileManager.IMAGE_DIR.equals(playType))
        {
            fileDir = FileManager.getInstance().getImagePathDir();
            fileList = new File(fileDir).list();
        }
        else if (FileManager.AUDIO_DIR.equals(playType))
        {
            fileDir = FileManager.getInstance().getAudioPathDir();
            fileList = new File(fileDir).list();
        }

        if (fileList != null && fileList.length > 0)
        {
            for (String fileName : fileList)
            {
                if (fileName != null && !fileName.endsWith(HttpDownload.suffixName))
                {
                    if(!playFilePath.contains(playType + File.separator + fileName)) {
                        playFilePath.add(playType + File.separator + fileName);
                    }
                }
            }
        }
        return playFilePath;
    }

    /**
     * 设置媒体播放声音
     *
     * @param mediaVolume 音量大小
     */
    public void setMediaVolume(int mediaVolume)
    {
        if (mPlayer != null)
        {
           // float volume = Float.valueOf(mediaVolume) * (1f / 100f);

            float volume = setVolumeFloat(mediaVolume);
            mPlayer.setVolume(volume, volume);
        }
    }

    /**
     * 开始播放媒体资源
     *
     * @param mp
     */
    private void playMedia(MediaPlayer mp)
    {
        if (mp != null && surfaceHolder != null)
        {
            if(MPlayerManager.getInstance().has_set_display.get()==false) {
                mPlayer.setDisplay(surfaceHolder);
                MPlayerManager.getInstance().has_set_display.set(true);
                setMediaVolume(ConfigManager.getInstance().getVolume());
                LogX.d(TAG,"xxxx"+String.valueOf(ConfigManager.getInstance().getVolume()));
                mp.start();
            } else {
                mp.start();
            }
        }
        else
        {
            isMediaPrepared.set(true);
        }
    }


    private static int CURRENT_POSITION = 0;
    public boolean isHasRestart = true;
    public boolean isHasPaused = false;
    public AtomicBoolean bootPlay = new AtomicBoolean(true);

    private boolean TagP = true;

//    //新增 暂停
//    public void pauseMedia(){
//        if (TagP){
//            TagP = false;
//            if (!isHasPaused){
//                isHasPaused = true;
//                isHasRestart = false;
//                if (mPlayer != null) {
//                    mPlayer.pause();
//                    XControlCenter.getInstance()
//                            .setBrightness(ConfigManager.getInstance().getStandByBrightness());
//                    CURRENT_POSITION = (mPlayer.getCurrentPosition());
//                    Log.e(TAG, "setPlayerPause: pause position ============ "+ CURRENT_POSITION);
//                }
//            }
//        }
//    }
//
//    //新增 重新播放
//    public void restartMedia(){
//        if (!isHasRestart){
//            isHasRestart = true;
//            isHasPaused = false;
//            if (mPlayer != null){
//                XControlCenter.getInstance().setBrightness(ConfigManager.getInstance().getBrightness());
//                mPlayer.seekTo(CURRENT_POSITION);
//                Log.e(TAG, "second way: CURRENT_POSITION ============" + CURRENT_POSITION);
//                mPlayer.start();
//            }
//        }
//    }


    public void setTagPAndSendMsg(){
        if (!TagP){
            TagP = true;
            if (handler != null)
            {
                handler.sendEmptyMessageDelayed(UICode.PAUSE_PLAYER, 20 * 60 * 1000);
            }
        }
    }

    public void setTagPTrue()
    {
        if (!TagP){
            TagP = true;
        }
    }

    public void setTagPFalse(){
        if (TagP){
            TagP = false;
        }
    }


    /**
     *
     * ===========================================第二种
     * ============================================
     * */
    private boolean TAG_RESTART = true;
    private boolean TAG_PAUSE = false;

    public void setPlayerPause() {
        if (TagP){
            TagP = false;
            bootPlay.getAndSet(false);
            if (!TAG_PAUSE){
                TAG_RESTART = false;
                TAG_PAUSE = true;
                if (mPlayer != null) {
                    mPlayer.pause();
                    ControlCenter.getInstance()
                            .setBrightness(ConfigManager.getInstance().getStandByBrightness());
                    CURRENT_POSITION = (mPlayer.getCurrentPosition());
                    Log.e(TAG, "setPlayerPause: pause position ============ "+ CURRENT_POSITION);
                }
            }
        }
    }

    public void setPlayerRestart() {
        if (!TAG_RESTART){
            TAG_PAUSE = false;
            TAG_RESTART = true;
            if (mPlayer != null){
                ControlCenter.getInstance().setBrightness(ConfigManager.getInstance().getBrightness());
                mPlayer.seekTo(CURRENT_POSITION);
                Log.e(TAG, "second way: CURRENT_POSITION ============" + CURRENT_POSITION);
                mPlayer.start();
                handler.removeMessages(UICode.PAUSE_PLAYER);
            }
        }
    }
    /**
     *  ================================================================
     */


    /**
     * 重置播放列表
     */
    public void resetPlayList()
    {
        String playListPath = FileManager.getInstance().getPlayListPath();
        File playListFile = new File(playListPath);
        if (playListFile.exists())
        {
            FileCacheService.deleteFile(playListPath);
        }
        loadPlayList(false);
    }



    /**
     * 调节声音可变曲线
     * @param volume
     * @return float
     */
    //2018.03
    private float setVolumeFloat(int volume){
        float voice = 0.08f;
        if (volume >= 0 && volume < 30){
            voice = (float) (volume * 0.00289);
        }else if (volume >= 30 && volume < 50){
            voice = (float) (volume * 0.00359);
        }else if (volume >= 50 && volume < 70){
            voice = (float) (volume * 0.00419);
        }else if (volume >= 70 && volume < 80){
            voice = (float) (volume * 0.00479);
        }else if (volume >= 80 && volume < 90){
            voice = (float) (volume * 0.00539);
        }else if (volume >= 90 && volume < 95){
            voice = (float) (volume * 0.00589);
        }else if (volume >= 95 && volume < 99){
            voice = (float) (volume * 0.00899);
        }else if (volume == 100){
            voice = (float) ((volume * 1.0)* (1f / 100f));
        }
        return voice;
    }
}
