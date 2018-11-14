package com.anjie.lift.download;

import java.io.File;

import com.anjie.common.http.DownloadCallback;
import com.anjie.common.http.HttpDownload;
import com.anjie.common.http.HttpExecutor;
import com.anjie.common.log.LogX;
import com.anjie.common.threadpool.Future;
import com.anjie.common.threadpool.FutureListener;
import com.anjie.lift.app.AppInfoManager;
import com.anjie.lift.app.AppPreference;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.parse.VersionParser;
import com.anjie.lift.server.request.VersionRequest;
import com.anjie.lift.server.response.VersionResponse;
import com.anjie.lift.utils.AppUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * 升级APK管理类
 */
public class ApkDownloadManager
{
    /**
     * 日志标签
     */
    private static final String TAG = "ApkDownloadManager";

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 是否有APK下载失败的URL
     */
    private static final String APK_DOWNLOAD_FAILED_PATH = "apk_failed_path";

    /**
     * 请求服务器周期 24小时一次
     */
    private static final long VersionExpire = 24 * 60 * 60;

    /**
     * APK下载的管理类
     * 
     * @param context
     */
    public ApkDownloadManager(Context context)
    {
        this.mContext = context;
    }

    /**
     * 请求服务器版本信息
     */
    public void requestServerVersionInfo()
    {
        AppPreference appSp = new AppPreference(mContext);
        boolean isNeedRequest = appSp.validVersionExpire(VersionExpire);
        if (!isNeedRequest)
        {
            // 有效期内,不需要请求
            // 检查是否有下载失败的APK任务
            validDownloadFailed(appSp);
            return;
        }

        LogX.d(TAG, "requestVersionInfo");
        VersionRequest request = new VersionRequest(false);
        // 设置请求的信息
        HttpExecutor.getInstance().submit(request, new FutureListener<String>()
        {
            @Override
            public void onFutureDone(Future<String> future)
            {
                if (future == null)
                {
                    LogX.d(TAG, "Request Server playList failed");
                    return;
                }
                handleResponse(future.get());
            }
        });
    }

    /**
     * 处理版本服务器返回信息
     * 
     * @param xmlText
     */
    private void handleResponse(String xmlText)
    {
        VersionResponse resp = null;
        try
        {
            resp = VersionParser.parserServerVersion(xmlText);
        }
        catch (Exception e)
        {
            LogX.d(TAG, "Handle Sever response Play list meet exception.");
        }

        if (resp != null && resp.isRespSuccess())
        {
            // 更新播放列表请求时间
            new AppPreference(mContext).updateVersionExpire();
            checkVersion(resp.getVersionCode(), resp.getPath());
        }
        else
        {
            LogX.d(TAG, "Handle Sever response Play list Failed.");
        }
    }

    /**
     * 验证下载版本号
     * 
     * @param versionCode
     */
    private void checkVersion(int versionCode, String path)
    {
        int localApkVersionCode = AppUtils.getVersionCode(mContext);
        if (versionCode > localApkVersionCode)
        {
            // 服务器有高版本
            downloadApk(path);
        }
        else
        {
            LogX.i(TAG, "check Version,it is latest version.");
        }
    }

    /**
     * 开始下载APK的任务
     *
     * @param path
     * apkPath
     */
    private void downloadApk(final String path)
    {
        if (TextUtils.isEmpty(path))
        {
            return;
        }
        LogX.d(TAG, "begin to download apk.");

        // 下载地址
        String downloadUrl;
        String downloadHost = AppInfoManager.getInstance().getDownloadHost();
        if (path.endsWith(".apk"))
        {
            downloadUrl = downloadHost + "/" + path;
        }
        else
        {
            downloadUrl = downloadHost + "/" + path + ".apk";
        }
        String filePath = FileManager.getInstance().getAppFileRoot();
        HttpDownload download = new HttpDownload(downloadUrl, filePath, FileManager.APK_NAME, new DownloadCallback()
        {
            @Override
            public void onSuccess()
            {
                LogX.d(TAG, "Download onSuccess");
                // 处理下载成功任务
                installApk();
            }

            @Override
            public void onError(int code, String message)
            {
                LogX.e(TAG, "Download failed:" + code + ",message:" + message);
                updateDownloadApkFailed(path);
                // 处理下载失败的任务
            }
        }, false);
        HttpExecutor.getInstance().submit(download);
    }

    /**
     * 检测是否有下载失败的任务
     */
    private void validDownloadFailed(AppPreference appSp)
    {
        String downloadPath = appSp.getString(APK_DOWNLOAD_FAILED_PATH, null);
        if (!TextUtils.isEmpty(downloadPath))
        {
            // 有下载失败的任务
            downloadApk(downloadPath);
        }
    }

    /**
     * 更新下载失败信息
     *
     * @param path
     * file path
     */
    private void updateDownloadApkFailed(String path)
    {
        AppPreference appSp = new AppPreference(mContext);
        appSp.putString(APK_DOWNLOAD_FAILED_PATH, path);
    }

    /**
     * 安装下载完的APK
     * 
     *
     */
    private void installApk()
    {
        // APK的路径
        String apkPath = FileManager.getInstance().getAppFileRoot() + File.separator + FileManager.APK_NAME;
        // 清空本地下载失败的记录
        updateDownloadApkFailed("");

        // 准备下载文件
        File apkfile = new File(apkPath);
        if (!apkfile.exists())
        {
            return;
        }
        // 通过Intent安装APK文件
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        mContext.startActivity(installIntent);
        // TODO 无法实现静默安装
        // 本进程结束
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
