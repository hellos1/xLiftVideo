package com.anjie.lift.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextClock;
import android.widget.TextView;

import com.anjie.common.log.LogX;
import com.anjie.lift.R;
import com.anjie.lift.app.AppInfoManager;
import com.anjie.lift.config.ConfigManager;
import com.anjie.lift.manager.ViewManager;
import com.anjie.lift.utils.AppUtils;
import com.anjie.lift.view.ADTextView;
import com.anjie.lift.view.ArrowsView;
import com.anjie.lift.view.BGView;
import com.anjie.lift.view.BaseView;
import com.anjie.lift.view.BaseView.ViewType;
import com.anjie.lift.view.CusImageView;
import com.anjie.lift.view.CusTextView;
import com.anjie.lift.view.DateView;
import com.anjie.lift.view.FloorView;
import com.anjie.lift.view.MarqueeTextView;
import com.anjie.lift.view.PlayerView;
import com.anjie.lift.view.Position;
import com.anjie.lift.view.StatusView;
import com.anjie.lift.view.TimerView;
import com.anjie.lift.view.TitleView;

import java.util.List;

/**
 * 视图辅助类
 * 
 */
public final class ViewHelper
{
    /**
     * 日志标签
     */
    private static final String TAG = "ViewHelper";

    /**
     * 初始化背景
     * 
     * @param rootLayout
     */
    public static void initBackground(View rootLayout)
    {
        BaseView baseView = ViewManager.getInstance().getView(ViewType.Background);
        if (baseView != null)
        {
            BGView bgView = (BGView) baseView;
            Drawable drawable = AppUtils.getDrawable(bgView.getBackgroundPath());
            if (drawable != null)
            {
                LogX.d(TAG, "init background image:" + drawable);
                rootLayout.setBackground(drawable);
            }
            else
            {
                int color = bgView.getBackgroundColor();
                if (color != 0)
                {
                    rootLayout.setBackgroundColor(color);
                }
                LogX.d(TAG, "init background color:" + color);
            }
        }
    }

    /**
     * 初始化电梯运行方向视图
     */
    public static ImageView initDirectionView(Activity activity)
    {
        ImageView directionView = null;
        BaseView baseView = ViewManager.getInstance().getView(ViewType.Arrows);
        if (baseView == null)
        {
            return directionView;
        }
        directionView = new ImageView(activity);
        ArrowsView arrowsView = (ArrowsView) baseView;
        LogX.d(TAG, "init lift direction view:" + arrowsView.getPosition());
        directionView.setX(arrowsView.getX());
        directionView.setY(arrowsView.getY());
        directionView.setLayoutParams(new ViewGroup.LayoutParams(arrowsView.getWidth(), arrowsView.getHeight()));

        directionView.setBackgroundResource(R.drawable.none);
        directionView.setScaleType(ScaleType.FIT_XY);
        directionView.setAdjustViewBounds(true);
        return directionView;
    }

    /**
     * 初始化电梯运行方向视图
     */
    public static ImageView initStatusView(Activity activity)
    {
        ImageView statusView = null;
        BaseView baseView = ViewManager.getInstance().getView(ViewType.Status);
        if (baseView == null)
        {
            return statusView;
        }
        statusView = new ImageView(activity);
        StatusView myStatusView = (StatusView) baseView;
        LogX.d(TAG, "init lift status view." + myStatusView.getPosition());
        statusView.setX(myStatusView.getX());
        statusView.setY(myStatusView.getY());
        statusView.setLayoutParams(new ViewGroup.LayoutParams(myStatusView.getWidth(), myStatusView.getHeight()));

        statusView.setBackgroundResource(R.drawable.none);
        statusView.setScaleType(ScaleType.FIT_XY);
        statusView.setAdjustViewBounds(true);
        return statusView;
    }

    /**
     * 初始化电梯楼层显示View
     */
    public static TextView initFloorNumView(Activity activity)
    {
        TextView floorNumView = null;
        BaseView baseView = ViewManager.getInstance().getView(ViewType.Floor);
        if (baseView == null)
        {
            return null;
        }
        LogX.d(TAG, "init lift number view.");
        FloorView floorView = (FloorView) baseView;
        floorNumView = new TextView(activity);
        floorView.updateTextViewPosition(floorNumView);
        floorView.updateTextInfo(floorNumView);
        floorNumView.setText(" ");
        floorNumView.setGravity(Gravity.CENTER);
        //floorNumView.setLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT);

        Typeface type = AppInfoManager.getInstance().getTypeFace();
        if (type != null)
        {
            floorNumView.setTypeface(type);
        }
        // 字体加粗
        // TextPaint tp = floorNumView.getPaint();
        // tp.setFakeBoldText(true);
        return floorNumView;
    }

    /**
     * 初始化广告的文本
     *
     * @param rootLayout
     * @param activity
     * @return
     */
    public static MarqueeTextView initADTextView(ViewGroup rootLayout, Activity activity)
    {
        BaseView baseView = ViewManager.getInstance().getView(ViewType.ADText);
        if (baseView == null)
        {
            LogX.d(TAG, "initADTextView has no ADTextView.");
            return null;
        }
        ADTextView adView = (ADTextView) baseView;
        LogX.d(TAG, "initADTextView." + adView.getPosition());
        MarqueeTextView adTextView = new MarqueeTextView(activity);
        adView.updateTextViewPosition(adTextView);
        adView.updateTextInfo(adTextView);
        // 移动马灯广告文字只支持单行
        adTextView.setMaxLines(1);
        // 设置字体的类型
        Typeface type = AppInfoManager.getInstance().getTypeFace();
        if (type != null)
        {
            adTextView.setTypeface(type);
        }
        // 更新配置修改的滚动文字
        String scrollText = ConfigManager.getInstance().getScrollText();
        if (scrollText != null)
        {
            adTextView.setText(scrollText);
        }
        rootLayout.addView(adTextView);
        return adTextView;
    }

    /**
     * 初始化时间视图
     *
     * @param activity
     * @return
     */
    public static TextClock initTimeView(ViewGroup rootLayout, Activity activity)
    {
        // 时间、日期、星期多标签
        BaseView baseView = ViewManager.getInstance().getView(ViewType.Timer);
        int diff = 0;
        if (baseView == null)
        {
            return null;
        }
        LogX.d(TAG, "init time view.");
        TimerView timerView = (TimerView) baseView;
        Typeface type = AppInfoManager.getInstance().getTypeFace();
        TextClock textClock = new TextClock(activity);

        // 设置字体格式
        if (type != null)
        {
            textClock.setTypeface(type);
        }
        // 设置时间格式24小时制
        // 格式参考http://www.android-doc.com/reference/java/text/SimpleDateFormat.html
        String format = ConfigManager.getInstance().getTimeFormat();
        if (!TextUtils.isEmpty(format))
        {
            //优先读取SP中的记录
            textClock.setFormat24Hour(format);
        }
        else
        {
            // 没有才读取布局中格式
            textClock.setFormat24Hour(timerView.getFormat());
        }
        timerView.updateTextViewPosition_new(textClock,diff);

        timerView.updateTextInfo(textClock);

        //LIUCHUN
        textClock.setGravity(Gravity.CENTER);
        rootLayout.addView(textClock);

        return textClock;
    }

    /**
     * 初始化时间视图
     *
     * @param activity
     * @return
     */
    public static TextClock initDateView(ViewGroup rootLayout, Activity activity)
    {
        // 时间、日期、星期多标签
        BaseView baseView = ViewManager.getInstance().getView(ViewType.Date);
        if (baseView == null)
        {
            return null;
        }
        LogX.d(TAG, "init date view.");
        DateView dateView = (DateView) baseView;
        Typeface type = AppInfoManager.getInstance().getTypeFace();
        TextClock textClock = new TextClock(activity);
        dateView.updateTextViewPosition(textClock);
        dateView.updateTextInfo(textClock);
        // 设置字体格式
        if (type != null)
        {
            textClock.setTypeface(type);
        }
        // 设置时间格式24小时制
        // 格式参考http://www.android-doc.com/reference/java/text/SimpleDateFormat.html
        String format = ConfigManager.getInstance().getDateFormat();
        if (!TextUtils.isEmpty(format))
        {
            //优先读取SP中的记录
            textClock.setFormat24Hour(format);
        }
        else
        {
            // 没有才读取布局中格式
            textClock.setFormat24Hour(dateView.getFormat());
        }
        textClock.setGravity(Gravity.CENTER);
        rootLayout.addView(textClock);

        return textClock;
    }

    /**
     * 初始化自定义文本布局
     *
     * @param rootLayout
     *            根布局
     * @param activity
     *            所属Activity
     */
    public static void initCustomTextView(ViewGroup rootLayout, Activity activity)
    {
        List<BaseView> mTextViewList = ViewManager.getInstance().getViewList(ViewType.CusText);
        if (mTextViewList != null && mTextViewList.size() > 0)
        {
            TextView textView = null;
            for (BaseView baseView : mTextViewList)
            {
                textView = initTextView(baseView, activity);
                if (textView != null)
                {
                    rootLayout.addView(textView);
                }
            }
        }
    }

    /**
     * 初始化文本布局视图
     *
     * @param baseView
     * @param activity
     * @return
     */
    private static TextView initTextView(BaseView baseView, Activity activity)
    {
        if (baseView == null)
        {
            return null;
        }
        CusTextView cusTextView = (CusTextView) baseView;
        TextView textView = new TextView(activity);
        // 位置属性
        cusTextView.updateTextViewPosition(textView);
        cusTextView.updateTextInfo(textView);

        textView.setSelected(true);
        textView.setEllipsize(TruncateAt.MARQUEE);
        // 无限循环
        textView.setMarqueeRepeatLimit(-1);
        textView.setText(cusTextView.getText());
        Typeface type = AppInfoManager.getInstance().getTypeFace();
        LogX.d(TAG, "init custom text view. Text:" + cusTextView.getText());
        if (type != null)
        {
            textView.setTypeface(type);
        }
        return textView;
    }

    /**
     * 初始化自定义
     *
     * @param rootLayout
     * @param activity
     */
    public static void initCustomImageView(ViewGroup rootLayout, Activity activity)
    {
        List<BaseView> mTextViewList = ViewManager.getInstance().getViewList(ViewType.CusImage);
        if (mTextViewList != null && mTextViewList.size() > 0)
        {
            ImageView imageView = null;
            for (BaseView baseView : mTextViewList)
            {
                imageView = initImageView(baseView, activity);
                if (imageView != null)
                {
                    rootLayout.addView(imageView);
                }
            }
        }
    }

    /**
     * 初始化图片布局
     *
     * @param baseView
     * @param activity
     * @return
     */
    private static ImageView initImageView(BaseView baseView, Activity activity)
    {
        if (baseView == null)
        {
            return null;
        }
        CusImageView cusImageView = (CusImageView) baseView;
        LogX.d(TAG, "init custom image view." + cusImageView.getPosition());
        ImageView imageView = new ImageView(activity);

        imageView.setX(cusImageView.getX());
        imageView.setY(cusImageView.getY());

        imageView.setLayoutParams(new ViewGroup.LayoutParams(cusImageView.getWidth(), cusImageView.getHeight()));

        imageView.setMaxHeight(cusImageView.getHeight());
        imageView.setMinimumHeight(cusImageView.getHeight());
        imageView.setMaxWidth(cusImageView.getWidth());
        imageView.setMinimumWidth(cusImageView.getWidth());
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ScaleType.FIT_XY);
        String imagePath = cusImageView.getImagePath();
        Bitmap bitmap = AppUtils.loadBitmap(imagePath);
        if (bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
        }
        return imageView;
    }

    /**
     * 初始化标题区域
     * 
     * @param rootLayout
     *            根布局
     * @param activity
     *            Activity
     * @return 文本控件
     */
    public static TextView initTitleView(ViewGroup rootLayout, Activity activity)
    {
        BaseView baseView = ViewManager.getInstance().getView(ViewType.Title);
        if (baseView == null)
        {
            LogX.d(TAG, "initADTextView has no ADTextView.");
            return null;
        }
        TitleView viewParams = (TitleView) baseView;
        LogX.d(TAG, "initTitleView." + viewParams.getPosition());
        TextView titleView = new TextView(activity);
        viewParams.updateTextViewPosition(titleView);
        viewParams.updateTextInfo(titleView);
        Typeface type = AppInfoManager.getInstance().getTypeFace();
        //2018.02
        titleView.setMaxLines(1);
        titleView.setEllipsize(TruncateAt.END);
        if (type != null)
        {
            titleView.setTypeface(type);
        }
        // 处理标题的配合更改
       String text = ConfigManager.getInstance().getTitle();
        if (text != null)
        {
            titleView.setText(text);
        }
        //titleView.setText("KONE");
        rootLayout.addView(titleView);
        return titleView;
    }

    /**
     * 获取视频播放区域拉伸覆盖顶部(标题和时间)和底部区域(滚动文字)
     * 
     * @return 播放区域
     */
    public static PlayerView getExtTopBottomPlayView(boolean is_90)
    {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        PlayerView extTopPlayView = getExtTopPlayView(is_90);
        if (extTopPlayView != null)
        {
            x = extTopPlayView.getX();
            y = extTopPlayView.getY();
            width = extTopPlayView.getWidth();
            height = extTopPlayView.getHeight();
        }
        BaseView titleView = ViewManager.getInstance().getView(ViewType.ADText);
        if (titleView != null)
        {
            height += titleView.getHeight();
        }
        PlayerView playerView = new PlayerView();
        LogX.w(TAG,"LIUCHUN"+"EXT");
        playerView.setPosition(new Position(x, y, width, height));
        return playerView;
    }

    /**
     * 获取视频播放区域拉伸覆盖顶部(标题和时间)
     *
     * @return 播放区域
     */
    public static PlayerView getExtTopPlayView(boolean is_90)
    {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        int diff = 135;
        if(is_90==true)
        {
            diff = 78;
        }
        BaseView defaultPlayView = ViewManager.getInstance().getView(ViewType.Player);
        if (defaultPlayView != null)
        {
            x = defaultPlayView.getX();
            y = defaultPlayView.getY();
            width = defaultPlayView.getWidth();
            height = defaultPlayView.getHeight();
        }
        LogX.w(TAG,"LIUCHUN"+"EXT");


        PlayerView playerView = new PlayerView();
        Position position = new Position(x, y-diff, width, height + diff);
        playerView.setPosition(position);
        return playerView;
    }

    private static int getMinY()
    {
        int minY = 0;
        BaseView titleView = ViewManager.getInstance().getView(ViewType.Title);
        if (titleView != null)
        {
            minY = titleView.getY();
        }
        List<BaseView> timerViewList = ViewManager.getInstance().getViewList(ViewType.Timer);
        if (timerViewList != null)
        {
            // 时间控件有2个，取最小的一个
            for (BaseView baseView : timerViewList)
            {
                if (baseView.getY() < minY)
                {
                    // 取最顶部的
                    minY = baseView.getY();
                }
            }
        }
        return minY;
    }

    /**
     * 获取视频播放区域拉伸覆盖底部(滚动文字)
     *
     * @return 播放区域
     */
    public static PlayerView getExtBottomPlayView()
    {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        BaseView baseView1 = ViewManager.getInstance().getView(ViewType.Player);
        if (baseView1 != null)
        {
            PlayerView defaultPlayView = (PlayerView) baseView1;
            x = defaultPlayView.getX();
            y = defaultPlayView.getY();
            width = defaultPlayView.getWidth();
            height = defaultPlayView.getHeight();
        }

        // 扩展底部播放空间，就是拉升高度
        BaseView baseView2 = ViewManager.getInstance().getView(ViewType.ADText);
        if (baseView2 != null)
        {
            // 累加高度
            height += baseView2.getHeight();
        }

        PlayerView playerView = new PlayerView();
        LogX.w(TAG,"LIUCHUN"+"BTON");
        playerView.setPosition(new Position(x, y, width, height));
        return playerView;
    }
}
