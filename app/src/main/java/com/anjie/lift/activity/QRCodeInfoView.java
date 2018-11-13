package com.anjie.lift.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anjie.common.log.LogX;
import com.anjie.lift.BuildConfig;
import com.anjie.lift.app.AppInfoManager;
import com.anjie.lift.manager.ScreenAdapter;
import com.anjie.lift.utils.AppUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.shbst.androiddevicesdk.DeviceSDK;

import java.util.Hashtable;

/**
 * 显示MAC地址的二维码视图封装
 * 
 * Created by jimmy on 2017/10/7.
 */
public class QRCodeInfoView
{
    /**
     * 日志标签
     */
    private static final String TAG = "QRCodeInfoView";

    /**
     * 二维码白色底
     */
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * 二维码黑色内容
     */
    private static final int BLACK = 0xFF000000;

    /**
     * 显示二维码的时间(20秒)
     */
    private static final int SHOW_QRCODE_TIME = 300;

    /**
     * 二维码宽度
     */
    private static final int QR_WIDTH = 120;

    /**
     * 二维码高度
     */
    private static final int QR_HEIGHT = 120;

    /**
     * 局对布局根
     */
    private AbsoluteLayout rootLayout;

    /**
     *
     */
    private TextView subTextInfo;

    /**
     * 二维码视图
     */
    private ImageView qrCodeView;

    /**
     * 构造函数
     */
    public QRCodeInfoView()
    {

    }

    /**
     * 显示MAC的二维码内容,显示完成后会定时任务移除二维码显示控件
     * 
     */
    public void showMacQRCodeView(Activity activity, AbsoluteLayout rootLayout)
    {
        this.rootLayout = rootLayout;
        qrCodeView = createImageView(activity);
        subTextInfo = createTextView(activity);
        // 先添加二维码，再添加底部文字,底部文件TextView覆盖在ImageView上
        rootLayout.addView(qrCodeView);
        rootLayout.addView(subTextInfo);

        // 异步任务操作
        new ShowQRCodeTask(SHOW_QRCODE_TIME).execute();
    }

    /**
     * 创建二维码图像显示位置
     * 
     * @param activity
     *            Activity
     * @return ImageView
     */
    private ImageView createImageView(Activity activity)
    {
        ImageView imageView = new ImageView(activity);
        imageView.setX(0);
        imageView.setY(0);
        imageView.setLayoutParams(
                new ViewGroup.LayoutParams(QR_WIDTH, QR_HEIGHT));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        return imageView;
    }

    /**
     * 创建底部文字
     * 
     * @param activity
     * @return
     */
    private TextView createTextView(Activity activity)
    {
        TextView textView = new TextView(activity);
        textView.setX(0);

        // 文本直接覆盖在二维码底部的间隙地方
        textView.setY(QR_WIDTH - 18);
        textView.setLayoutParams(new ViewGroup.LayoutParams(QR_WIDTH, 14));

        // 文字水平居中对齐
        textView.setGravity(Gravity.CENTER_HORIZONTAL);

        // 文字大小14
        textView.setTextSize(11);

        // 白底黑字，覆盖在二维码图片底部空隙地方
        textView.setBackgroundColor(Color.WHITE);
        textView.setTextColor(Color.BLACK);

        // 设置字体类型
        Typeface type = AppInfoManager.getInstance().getTypeFace();
        if (type != null)
        {
            textView.setTypeface(type);
        }
        return textView;
    }

    /**
     * 释放资源
     */
    public void onDestroy()
    {
        rootLayout = null;
        qrCodeView = null;
        subTextInfo = null;
    }

    /**
     * 生成Bitmap图片
     * 
     * @param content
     *            二维码内容
     * @param w
     *            宽度
     * @param h
     *            高度
     * @return Bitmap
     */
    private Bitmap getQRCodeBitMap(String content, int w, int h)
    {
        if (TextUtils.isEmpty(content) || w <= 0 || h <= 0)
        {
            // 参数内容校验
            return null;
        }

        Bitmap bitmap;
        BitMatrix result;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 内边距大小,值选取范围:1-4
        hints.put(EncodeHintType.MARGIN, 2);

        try
        {
            result = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, w,
                    h, hints);
        }
        catch (WriterException e)
        {
            LogX.e(TAG, "Create QRCode meet WriterException.", e);
            return null;
        }
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++)
        {
            int offset = y * w;
            for (int x = 0; x < w; x++)
            {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);

        return bitmap;
    }

    /**
     * 显示二维码Mac的任务,线程获取MAC地址并生成二维码，交由主线程刷新到UI控件上
     */
    private class ShowQRCodeTask extends AsyncTask<String, String, QRCodeInfo>
    {
        /**
         * 显示时间(秒)
         */
        private int showSeconds;

        /**
         * 显示任务
         * 
         * @param showSeconds
         *            显示时长,-1表示永久显示
         */
        private ShowQRCodeTask(int showSeconds)
        {
            this.showSeconds = showSeconds;
        }

        @Override
        protected QRCodeInfo doInBackground(String[] params)
        {
            // 后台线程
            String macAddress = AppUtils.getMacAddress();
            String formatAddress = "S/N ";
            if (!TextUtils.isEmpty(macAddress))
            {
                // 过滤MAC地址的:
               // formatAddress = macAddress.replace(":", "");
                // 转换成大写
                formatAddress += macAddress.toUpperCase();
                formatAddress = formatAddress.replace(":", "");
            }
            //formatAddress+=" Android4.4.2 APP_V2.0.7 SDK_V1.0.9 KM12345678G01 P/N A3N121212P02.100AT174500001 Producer_AJ";
            formatAddress+= " "+getAndroidVersion()
                    + " APP_V" + getAppVersion()
                    + " SDK_V" + getDeviceSDKVersion()
                    + " " + getScreenMK()
                    + " P/N A3N124217P03. 105AT180200020 Producer_AJ";
            LogX.d(TAG, "QRCode message: " + formatAddress);
           // String qrCodeText = getQRCodeContent(formatAddress);
            Bitmap bitmap = getQRCodeBitMap(formatAddress, QR_WIDTH, QR_HEIGHT);
            // 可能存在的耗时操作放在线程中
            return new QRCodeInfo(formatAddress, bitmap);
        }

//        private String saveMyBitmap(Bitmap mBitmap) {
//            File f = new File("/sdcard/elevator/" + "ORCode.png");
//            try {
//                f.createNewFile();
//            } catch (IOException e) {
//                LogX.e(TAG, "在保存图片时出错：" + e.getMessage());
//            }
//            FileOutputStream fOut = null;
//            try {
//                fOut = new FileOutputStream(f);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            try {
//                mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//            } catch (Exception e) {
//                return "create_bitmap_error";
//            }
//            try {
//                if (fOut != null){
//                    fOut.flush();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            try {
//                if (fOut != null){
//                    fOut.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return "/sdcard/elevator" + "QRCode.png";
//        }

        @Override
        protected void onPostExecute(QRCodeInfo qrCodeInfo)
        {
            // 此系统方法运行在UI主线程,因为需要操作ImageView,TextView
            if (qrCodeInfo != null)
            {
                boolean isNeedTimeTask = false;
                if (qrCodeView != null)
                {
                    qrCodeView.setImageBitmap(qrCodeInfo.getQrCodeBitmap());
                    qrCodeView.setVisibility(View.VISIBLE);
                    isNeedTimeTask = true;
                }

                if (subTextInfo != null)
                {
                    //subTextInfo.setText("SN " + qrCodeInfo.getSubTitle());
                    subTextInfo.setText(qrCodeInfo.getSubTitle());
                    isNeedTimeTask = true;
                }

                // 需要定时任务隐藏,并且有显示时间规定才需要执行移除控件任务
                if (isNeedTimeTask && showSeconds > 0)
                {
                    // new 出来的Handler值主线程
                    new Handler().postDelayed(new UIRemoveTask(),
                            showSeconds * 1000);
                }
            }
            super.onPostExecute(qrCodeInfo);
        }
    }


    //2018.04.26
    /**
     * 获取DeviceSDK的版本
     */
    private String getDeviceSDKVersion()
    {
        return DeviceSDK.getSDKVersion();
    }

    private String getAndroidVersion()
    {
        return "Android4.4.2";
    }

    private String getAppVersion()
    {
        return BuildConfig.VERSION_NAME;
    }

    private String getScreenMK(){
        return ScreenAdapter.getInstance().getQRCodeKM();
    }

    private String getPN()
    {
        return null;
    }

    private String getSupplier()
    {
        return "NJAJ";
    }

    /**
     * 获取二维码信息内容
     * 
     * @return
     */
    private String getQRCodeContent(String formatMac)
    {
        return getMacInfo(formatMac) + " " + getKoneSN() + " " + getProductSN()
                + " " + getManufacturer();
    }

    /**
     * 获取MAC地址 格式：S/N + MAC 示例：S/N AA12BB34C56
     * 
     * @return
     */
    private String getMacInfo(String formatMac)
    {
        return formatMac;
    }

    /**
     * 获取KONE 流水号 格式：KM+流水号 示例：KM12345678G01
     * 
     * @return KONE 流水号
     */
    private String getKoneSN()
    {
        // TODO 根据硬件信息获取
        return "KM12345678G01";
    }

    /**
     * 获取产品序列号 格式：P/N + 产品序列号 示例:P/N A3N121212P02.100AT174500001
     * 产品序列号规则：型号+番号+"."+版本号+供应商号+年(2位)+周(2位)+流水号(5位)
     * 
     * @return 产品序列号
     */
    private String getProductSN()
    {
        // TODO 根据你们的硬件获取
        return "P/N A3N121212P02.100AT174500001";
    }

    /**
     * 获取制造商信息 格式：Screen Made in + 生产商
     * 
     * @return 造商信息
     */
    private String getManufacturer()
    {
        return "Screen Made in NJAJ";
    }

    /**
     * 封装的二维码信息(图标和底部文字)
     */
    private class QRCodeInfo
    {
        /**
         * 二维码信息的子标题
         */
        private String subTitle;

        /**
         * 二维码图片信息
         */
        private Bitmap qrCodeBitmap;

        public QRCodeInfo(String subTitle, Bitmap qrCodeBitmap)
        {
            this.subTitle = subTitle;
            this.qrCodeBitmap = qrCodeBitmap;
        }

        public Bitmap getQrCodeBitmap()
        {
            return qrCodeBitmap;
        }

        public String getSubTitle()
        {
            return subTitle;
        }
    }

    private class UIRemoveTask implements Runnable
    {
        private UIRemoveTask()
        {

        }

        @Override
        public void run()
        {
            if (rootLayout != null)
            {
                try
                {
                    rootLayout.removeView(qrCodeView);
                    rootLayout.removeView(subTextInfo);
                }
                catch (Exception e)
                {
                    LogX.e(TAG,
                            "remove qrCodeView and subTextInfo meet exception.",
                            e);
                }
            }
        }
    }
}
