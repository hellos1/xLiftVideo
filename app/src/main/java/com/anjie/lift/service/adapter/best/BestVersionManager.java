package com.anjie.lift.service.adapter.best;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.anjie.common.http.DownloadCallback;
import com.anjie.common.http.HttpDownload;
import com.anjie.common.http.HttpExecutor;
import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.service.ResourceService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.trinea.android.common.util.ShellUtils;

/**
 * Best云升级APK管理类
 */
final class BestVersionManager
{
    /**
     * 日志标签
     */
    private static final String TAG = "BestVersionManager";

    /**
     * 单实例
     */
    public static BestVersionManager instance = new BestVersionManager();

    /**
     * 是否正在下载
     */
    private AtomicBoolean isDownloading = new AtomicBoolean(false);

    /**
     * 获取单实例
     * 
     * @return 实例
     */
    public static BestVersionManager getInstance()
    {
        return instance;
    }

    /**
     * 升级APK
     * 
     * @param versionName 版本名
     * @param url 升级URL
     */
    void updateApk(String versionName, String url)
    {
        if (TextUtils.isEmpty(versionName) || TextUtils.isEmpty(url))
        {
            LogX.w(TAG,"versionName or url is empty.");
            return;
        }
        // Android的APK是有版本名VersionName(String)和版本号VersionCode(int)类型组成
        // 通常我们是用versionCode的int值来判断升级,所以我们要定义VersionName个VersionCode的对应关系
        // 建议设计规则每一位版本号用两个数字: 2.1.3 -> 020103 -> 20103
        if (!versionName.equals(getVersionName()))
        {
            // 先简单按版本号字符串不相等处理
            downloadApk(url);
        }
    }

    /**
     * 下载APK任务
     * 
     * @param url 下载地址
     */
    private void downloadApk(final String url)
    {
        if (isDownloading.getAndSet(true))
        {
            LogX.d(TAG, "is downloading. return.");
            // 如果正在下载,返回
            return;
        }
        LogX.d(TAG, "begin to download APK to update.");
        String appRootPath = FileManager.getInstance().getAppFileRoot();
        // 先检查是否已经存在该APK了
        FileCacheService.deleteFile(appRootPath,FileManager.APK_NAME);
        // 下载监听回调接口
        DownloadCallback callback = new DownloadCallback()
        {
            @Override
            public void onError(int code, String message)
            {
                LogX.d(TAG, "APK Download failed.code:" + code + ",message:" + message);
                isDownloading.set(false);
                // 下载失败,缓存升级URL
                new BestPreferences().cacheUpdateUrl(url);
            }

            @Override
            public void onSuccess()
            {
                LogX.d(TAG, "APK Download Success");
                isDownloading.set(false);
                // 清空缓存的升级URL
                new BestPreferences().cacheUpdateUrl(null);
                installAPK();
            }
        };
        // 执行下载APK任务,支持断点续传
        HttpDownload httpDownload = new HttpDownload(url, appRootPath,
                FileManager.APK_NAME, callback, true);
        HttpExecutor.getInstance().submit(httpDownload);
    }

    /**
     * 获取自己的版本名称
     * 
     * @return 版本名
     */
    private String getVersionName()
    {
        Context context = AppContext.getInstance().getContext();
        PackageManager pm = context.getPackageManager();
        String versionName = null;
        try
        {
            if (pm != null)
            {
                PackageInfo pkgInfo = pm.getPackageInfo(context.getPackageName(), 0);
                versionName = pkgInfo.versionName;
            }
        }
        catch (Exception e)
        {
            versionName = null;
        }
        return versionName;
    }

    /**
     * 检查是否有升级任务
     * 
     * @return 是否有升级APK任务
     */
    boolean checkIfHasUpdateAPKTask()
    {
        String updateUrl = new BestPreferences().getCacheUpdateURL();
        if (TextUtils.isEmpty(updateUrl))
        {
            return false;
        }
        // 下载APK
        downloadApk(updateUrl);
        return true;
    }

    /**
     *  安装APK应用
     */
    private void installAPK()
    {
        // APK的路径
        String apkPath = FileManager.getInstance().getAppFileRoot() + FileManager.APK_NAME;
        // 准备下载文件
        File apkFile = new File(apkPath);
        if (!apkFile.exists())
        {
            return;
        }
        Context context = AppContext.getInstance().getContext();

        // 停止后台服务
        Intent intent = new Intent();
        intent.setClass(context, ResourceService.class);
        context.stopService(intent);

        // 通过Intent安装APK文件,使用系统签名的就可以静默安装；  注意:此处不是静默安装
//        Intent installIntent = new Intent(Intent.ACTION_VIEW);
//        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
//        context.startActivity(installIntent);
        // 本进程结束
        //android.os.Process.killProcess(android.os.Process.myPid());

        //安装完成后执行打开
        LogX.d(TAG, "installAPK() start.");
        String[] commands = new String[] { "mount -o rw,remount /system", "cp /mnt/sdcard/elevator/LiftVideo.apk /system/app/" };
        ShellUtils.CommandResult result = ShellUtils.execCommand(commands, true);
        //installSilent(apkPath);
        LogX.d(TAG, "installAPK() finish." + result);

    }


    //静默安装  Segmentation fault?? 权限不够??
    private void installSilent(String apkPath) {
        String cmd = "pm install -r " + apkPath;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
            //静默安装需要root权限
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();
            //执行命令
            process.waitFor();
            //获取返回结果
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
                Log.e(TAG, ": success msg");
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
                Log.e(TAG, ": error msg");
            }
            Log.e(TAG, "successMsg: " + successMsg.length());
            Log.e(TAG, "errorMsg: " + errorMsg.length());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Log.e(TAG, "成功消息:"+ successMsg.toString() + "\n" + "错误信息:" + errorMsg.toString());
    }

}
