package com.anjie.lift.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

import com.anjie.common.log.LogX;
import com.anjie.lift.manager.MPlayerManager;
import com.anjie.lift.player.PlayerElement;
import com.anjie.lift.view.Position;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 播放区域
 * <p>
 * Created by jimmy on 2017/6/25.
 */
final class PlayerRegion implements SurfaceHolder.Callback
{
    /**
     * 日志标签
     */
    private static final String TAG = "PlayerRegion";

    /**
     * URL加载区域
     */
    private WebView webView;

    /**
     * 图片显示显示区域
     */
    private ImageView imageView;

    /**
     * 适配区域
     */
    private SurfaceView surfaceView;

    /**
     * 播放区域坐标
     */
    private Position position;

    /**
     * 当前播放的任务
     */
    private AtomicReference<PlayerElement> currentTask = new AtomicReference<>();

    /**
     * WebView加载是否成功
     */
    private boolean isWebViewLoadSuccess = false;

    /**
     * 构造函数
     */
    PlayerRegion(Activity activity, AbsoluteLayout rootLayout, Position position)
    {
        surfaceView = new SurfaceView(activity);
        surfaceView.getHolder().addCallback(this);

        imageView = new ImageView(activity);
        webView = new WebView(activity);

        this.position = position;
        LogX.i(TAG, "KAKAposition:" + position);
        initPosition(surfaceView);
        initPosition(imageView);
        initPosition(webView);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        rootLayout.addView(surfaceView);
        rootLayout.addView(imageView);
        rootLayout.addView(webView);

        setViewVisibility(surfaceView, View.GONE);
        setViewVisibility(imageView, View.GONE);
        setViewVisibility(webView, View.GONE);

        onWebViewSetting(webView);
    }

    /**
     * 设置坐标
     *
     * @param view View
     */
    private void initPosition(View view)
    {
        AbsoluteLayout.LayoutParams lp;
        ViewGroup.LayoutParams tempLayout = view.getLayoutParams();
        if (tempLayout != null)
        {
            lp = (AbsoluteLayout.LayoutParams) tempLayout;
            lp.x = position.getX();
            lp.y = position.getY();
            lp.height = position.getHeight();
            lp.width = position.getWidth();
            LogX.i(TAG, "{x:" + lp.x + ",y:" + lp.y + ",width:" + lp.width + ",LIUCHUNheight:" + lp.height + "}");
        }
        else
        {
            lp = new AbsoluteLayout.LayoutParams(position.getWidth(), position.getHeight(), position.getX(), position.getY());
            LogX.i(TAG, "{x:" + lp.x + ",y:" + lp.y + ",width:" + lp.width + ",CHUNLIUheight:" + lp.height + "}");
        }
        LogX.i(TAG, "{x:" + lp.x + ",y:" + lp.y + ",width:" + lp.width + ",height:" + lp.height + "}");
        // 设置布局
        view.setLayoutParams(lp);
    }

    /**
     * WebView 设置
     *
     * @param webView WebView
     */
    private void onWebViewSetting(WebView webView)
    {
        WebSettings webSettings = webView.getSettings();

        // 如果访问的页面中要与Javascript交互，则WebView必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        // 支持插件
        // webSettings.setPluginsEnabled(true);

        // 设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); // 将图片调整到适合WebView的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        // 缩放操作
        webSettings.setSupportZoom(true); // 支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); // 设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); // 隐藏原生的缩放控件

        // 其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK); // 关闭WebView中缓存
        webSettings.setAllowFileAccess(true); // 设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); // 支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");// 设置编码格式

        // 加载
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LogX.i(TAG,"WebView onReceivedError.");
                // 加载页面出错
                isWebViewLoadSuccess = false;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
                LogX.i(TAG,"WebView onPageFinished.");
                // 加载页面完成
                isWebViewLoadSuccess = true;
            }
        });
    }

    /**
     * 播放元素
     *
     * @param element 播放元素
     */
    void playElement(PlayerElement element)
    {
        // 过滤任务
        if (filterPlayElement(element))
        {
            LogX.i(TAG, "filterPlayElement:" + element);
            return;
        }

        // 当前的任务
        currentTask.set(element);

        if (element.getType() == PlayerElement.ElementType.image)
        {
            playImage(element.getFullPath());
        }
        else if (element.getType() == PlayerElement.ElementType.video)
        {
            playVideo();
        }
        else if (element.getType() == PlayerElement.ElementType.audio)
        {
            playAudio();
        }
        else if (element.getType() == PlayerElement.ElementType.url)
        {
            loadWebView(element.getFilePath());
        }
    }

    /**
     * 拦截播放元素
     */
    private boolean filterPlayElement(PlayerElement element)
    {
        PlayerElement currElement = currentTask.get();
        if (element.getType() == PlayerElement.ElementType.url)
        {
            // 过滤WebView相同的URL和已经加载成功的，不需要再次打开
            if (element.equals(currElement) && isWebViewLoadSuccess)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 加载网页
     *
     * @param webUrl URL地址
     */
    private void loadWebView(String webUrl)
    {
        setViewVisibility(webView, View.VISIBLE);
        setViewVisibility(imageView, View.GONE);
        setViewVisibility(surfaceView, View.GONE);

        // 加载WebView的URL
        webView.loadUrl(webUrl);
        LogX.i(TAG, "Prepare WebView");
    }

    /**
     * 播放视频
     */
    private void playVideo()
    {
        setViewVisibility(webView, View.GONE);
        setViewVisibility(imageView, View.GONE);

        // 显示播放的surfaceView
        setViewVisibility(surfaceView, View.VISIBLE);
        LogX.i(TAG, "Prepare Video SurfaceView.");
    }

    /**
     * 播放图片
     *
     * @param filePath 图片文件地址
     */
    private void playImage(String filePath)
    {
        setViewVisibility(webView, View.GONE);
        setViewVisibility(imageView, View.VISIBLE);
        setViewVisibility(surfaceView, View.GONE);

        // 显示图片
        imageView.setImageBitmap(getLocalBitmap(filePath));
        LogX.i(TAG, "Prepare Image View.");
    }

    /**
     * 加载本地图片
     *
     * @param fileFullPath 本地图片完整地址
     * @return 图片
     */
    public Bitmap getLocalBitmap(String fileFullPath)
    {
        Bitmap bitmap = null;
        try
        {
            // 加载图片
            bitmap = BitmapFactory.decodeFile(fileFullPath);
        }
        catch (OutOfMemoryError error)
        {
            LogX.e(TAG, "getLocalBitmap meet OutOfMemoryError.");
        }
        catch (Throwable e)
        {
            LogX.e(TAG, "getLocalBitmap meet Throwable.", e);
            return null;
        }
        return bitmap;
    }

    /**
     * 播放音乐
     */
    private void playAudio()
    {
        setViewVisibility(webView, View.GONE);
        setViewVisibility(imageView, View.GONE);
        setViewVisibility(surfaceView, View.VISIBLE);

        LogX.i(TAG, "Prepare Audio View.");
    }

    /**
     * UI恢复状态
     */
    void onResume()
    {
        PlayerElement element = currentTask.get();
        if (element == null)
        {
            return;
        }

        if (element.getType() == PlayerElement.ElementType.audio || element.getType() == PlayerElement.ElementType.video)
        {
        }
    }

    /**
     * UI暂停状态
     */
    void onPause()
    {
        PlayerElement element = currentTask.get();
        if (element == null)
        {
            return;
        }

        if (element.getType() == PlayerElement.ElementType.audio || element.getType() == PlayerElement.ElementType.video)
        {

        }
    }

    /**
     * 释放WebView的资源,避免内存泄漏
     */
    void onFreeWebView()
    {
        if (webView != null)
        {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();

            ((ViewGroup) webView.getParent()).removeView(webView);
            webView.destroy();
            webView = null;
        }
    }

    /**
     * 销毁
     */
    void onDestroy()
    {
        onFreeWebView();
    }

    /**
     * 设置显示视图控件是否显示
     *
     * @param view       View
     * @param visibility 可见性值
     */
    private void setViewVisibility(View view, int visibility)
    {
        if (view != null)
        {
            view.setVisibility(visibility);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        // 播放视频画布创建
        LogX.i(TAG, "System call surfaceCreated()");
        MPlayerManager.getInstance().setDisplay(holder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        // 播放视频画布变化
        LogX.i(TAG, "System call surfaceChanged format:" + format + ",width:" + width + ",height:" + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // 播放视频画布销毁
        LogX.i(TAG, "System call surfaceDestroyed()");
        MPlayerManager.getInstance().setDisplay(null);
    }
}
