package com.anjie.common.http;

import android.text.TextUtils;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.common.threadpool.Job;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

/**
 * HTTP的下载任务
 */
public class HttpDownload implements Job<Void>
{
    /**
     * 日志标签
     */
    private static final String TAG = "HttpDownload";

    /**
     * 数据缓冲区的大小
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * 下载临时文件后缀名
     */
    public static final String suffixName = ".download";

    /**
     * 是否已经被取消
     */
    private boolean isCancel = false;

    /**
     * 网络地址
     */
    private String downloadUrl;

    /**
     * 联网回调接口
     */
    private DownloadCallback mCallback;

    /**
     * HTTP连接对象
     */
    private HttpURLConnection connection;

    /**
     * 输入流
     */
    private InputStream is = null;

    /**
     * 下载目录
     */
    private String localDir;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 是否支持断点续传
     */
    private boolean isSupportBreakPoint = false;

    /**
     * 文件总大小
     */
    private long fileTotalSize = 0;

    /**
     * HTTP下载任务
     * 
     * @param downloadUrl
     * @param localDir fileName
     * @param callback
     * @param isSupportBreakPoint
     */
    public HttpDownload(String downloadUrl, String localDir, String fileName, DownloadCallback callback, boolean isSupportBreakPoint)
    {
        this.mCallback = callback;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.localDir = localDir;
        this.isSupportBreakPoint = isSupportBreakPoint;
    }

    @Override
    public Void run()
    {
        try
        {
            // 执行网络连接操作(发送HTTP请求，并处理网络返回数据)
            executeDownload();
        }
        catch (InterruptedException e)
        {
            // 暂停取消时的打断异常
            if (isCancel)
            {
                onCancelCallBack();
            }
            else
            {
                handlerException(e);
            }
        }
        catch (SocketException e)
        {
            // 无网络时会抛出该异常
            handlerException(e);
        }
        catch (Exception e)
        {
            // 其他异常
            handlerException(e);
        }
        catch (Error e)
        {
            // 错误处理
            handlerException(new Exception("Download meet Error."));
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.closeConnection(connection);
        }
        return null;
    }

    /**
     * 异常处理
     * 
     * @param exception
     */
    private void handlerException(Exception exception)
    {
        if (mCallback != null)
        {
            mCallback.onError(-1, exception.getMessage());
        }
    }

    /**
     * 校验下载地址是否正确
     * 
     * @return
     */
    private boolean validDownloadArg()
    {
        // 校验下载地址
        if (TextUtils.isEmpty(downloadUrl) || !downloadUrl.startsWith("http"))
        {
            LogX.e(TAG, "Download URL invalid:" + downloadUrl);
            return false;
        }
        // 校验文件路径
        if (TextUtils.isEmpty(localDir))
        {
            LogX.e(TAG, "Download localDir invalid:" + localDir);
            return false;
        }
        // 校验文件名
        if (TextUtils.isEmpty(fileName))
        {
            LogX.e(TAG, "Download fileName invalid:" + fileName);
            return false;
        }
        return true;
    }

    /**
     * 获取本地文件大小
     * 
     * @return
     */
    private long getLocalFileSize()
    {
        File file = new File(localDir, fileName + suffixName);
        if (file.exists())
        {
            return file.length();
        }
        return 0;
    }

    /**
     * 文件是否已经下载
     * 
     * @return
     */
    private boolean isFileHasDownload()
    {
        File file = new File(localDir, fileName);
        if (file.exists())
        {
            return true;
        }
        return false;
    }

    /**
     * 实现了联网写读功能
     * 
     * @throws Exception
     */
    private void executeDownload() throws Exception
    {
        if (!validDownloadArg())
        {
            // 校验下载条件不通过
            throw new IllegalArgumentException("Download valid arguments failed.");
        }

        if (!isSupportBreakPoint)
        {
            // 不支持断点续传,删除临时文件
            if (!FileCacheService.deleteFile(localDir, fileName + suffixName))
            {
                throw new IOException("don't support breakpoint download.delete temp file failed!!");
            }
        }

        // 如果该文件已经存在,则回调下载成功
        if (isFileHasDownload())
        {
            LogX.i(TAG, "file exist download success.");
            downloadSuccess();
            return;
        }
        // 检查是否已经超时，或者被取消
        if (isCancel)
        {
            throw new InterruptedException("User cancel the download task.");
        }
        // 构建连接
        URL url = new URL(downloadUrl);
        connection = (HttpURLConnection) url.openConnection();
        // 不使用Cache
        connection.setUseCaches(false);
        // 设置连接主机超时（单位：毫秒）
        connection.setConnectTimeout(30000);
        // 设置从主机读取数据超时（单位：毫秒）
        connection.setReadTimeout(15000);
        // 设置请求类型
        connection.setRequestMethod("GET");

        // 获取本地文件大小
        long localFileSize = getLocalFileSize();
        LogX.d(TAG, "localFileSize:" + localFileSize);
        int responseCode = 0;
        if (isSupportBreakPoint && localFileSize > 0)
        {
            connection.addRequestProperty("Range", "bytes=" + String.valueOf(localFileSize) + "-");
            responseCode = connection.getResponseCode();
            // 断点续传返回码:206
            if (responseCode != HttpStatus.SC_PARTIAL_CONTENT)
            {
                // 删除临时文件,后续重新下载
                FileCacheService.deleteFile(localDir, fileName + suffixName);
                throw new IllegalStateException("Support BreakPoint download server responseCode:" + responseCode);
            }
        }
        else
        {
            responseCode = connection.getResponseCode();
            // 正常请求状态
            if (responseCode != HttpStatus.SC_OK)
            {
                throw new IllegalStateException("download server responseCode:" + responseCode);
            }
        }
        // 如果已经取消任务
        if (isCancel)
        {
            throw new InterruptedException("User cancel the download task.");
        }
        // 获取文件的长度
        long downloadFileSize = (long) connection.getContentLength();
        if (downloadFileSize <= 0)
        {
            throw new IOException("Can't getContentLength():" + downloadFileSize);
        }
        // 获取网络数据输入流
        is = connection.getInputStream();

        // 总文件大小 = 已经下载的 + 需要下载(getContentLength);如果不支持断点续传localFileSize值为0
        fileTotalSize = localFileSize + downloadFileSize;
        LogX.d(TAG, "Get download getContentLength():" + downloadFileSize + ",fileSize:" + fileTotalSize);
        readDownloadData(localFileSize, downloadFileSize);
    }

    /**
     * 读取下载业务的业务包数据
     * 
     * @param downloadedSize
     *            已下载文件大小
     * @param dataLength
     *            需要下载数据长度
     * @throws IOException
     *             抛出IO异常供调用者捕捉
     * @throws InterruptedException
     *             抛出中断异常供调用者捕捉
     */
    private void readDownloadData(long downloadedSize, long dataLength) throws Exception
    {
        RandomAccessFile accessFile = null;
        try
        {
            int readLength = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] tempBuff = null;
            long downloadLength = 0;
            // 接入文件
            accessFile = accessDownloadFile(dataLength);
            long startDownloadTime = System.currentTimeMillis();
            int lastPrecent = 0;
            while ((readLength = is.read(buffer)) != -1)
            {
                // 网络连接被暂停或取消，需要抛出中断异常，并关闭写文件线程
                if (isCancel)
                {
                    throw new InterruptedException("User cancel the download task.");
                }

                // 如果没有读到数据就不需要写文件了
                if (readLength <= 0)
                {
                    continue;
                }

                // 将读到的数据写入文件，写文件成功后通知上层回调下载进度
                tempBuff = new byte[readLength];
                System.arraycopy(buffer, 0, tempBuff, 0, readLength);
                int length = FileCacheService.writeFile(accessFile, tempBuff);
                if (length == -1)
                {
                    throw new IOException("Write file meet Exception.");
                }
                else
                {
                    downloadLength += length;
                    int tempPrecent = (int) ((downloadedSize + downloadLength) / fileTotalSize) * 100;
                    if (tempPrecent != lastPrecent)
                    {
                        lastPrecent = tempPrecent;
                        // 设置下载量（百分比）
                        onProgress(downloadedSize + downloadLength, fileTotalSize);
                    }
                }
            }
            // 保证下载完了文本进度显示100%
            if (dataLength != downloadLength)
            {
                LogX.w(TAG, "Length not match.");
                throw new IOException("Length not match.");
            }
            long seconds = (System.currentTimeMillis() - startDownloadTime) / 1000;
            LogX.d(TAG, "Current Download Task(" + fileName + ") cost: " + seconds + " seconds");
        }
        finally
        {
            IOUtils.close(accessFile);
        }
        validSuccess(fileTotalSize);
    }

    /**
     * 校验下载文件的大小
     * 
     * @param fileSize
     * @throws FileNotFoundException
     */
    private void validSuccess(long fileSize) throws Exception
    {
        File file = new File(localDir, fileName + suffixName);
        if (file.exists() && file.length() == fileSize)
        {
            if (file.renameTo(new File(localDir, fileName)))
            {
                if (isCancel)
                {
                    onCancelCallBack();
                    return;
                }
                downloadSuccess();
                LogX.d(TAG, "Download save path:" + file.getAbsolutePath() + ",FileName:" + fileName);
            }
            else
            {
                file.delete();
                throw new Exception("after download,rename file failed.");
            }
        }
        else
        {
            file.delete();
            throw new Exception("after download, valid file failed.");
        }
    }

    /**
     * 接入随机文件对象
     * 
     * @param fileLength
     * @return
     */
    private RandomAccessFile accessDownloadFile(long fileLength) throws IOException
    {
        RandomAccessFile randomAccessFile = null;
        // 判断储存空间是否够用
        File file = new File(localDir, fileName + suffixName);
        boolean isEnough = FileCacheService.spaceIsEnough(fileLength, localDir);
        if (!isEnough)
        {
            // 没有足够的存储空间
            throw new IOException("No enough Storage space.");
        }
        // 如果文件不存在
        if (!file.exists())
        {
            file.createNewFile();
        }
        randomAccessFile = new RandomAccessFile(file, "rw");
        return randomAccessFile;
    }

    /**
     * 取消连接任务
     */
    public void cancelTask()
    {
        isCancel = true;
    }

    /**
     * 设置取消回调
     */
    private void onCancelCallBack()
    {
        if (mCallback != null)
        {
            mCallback.onCancel();
        }
    }

    /**
     * 下载完成回调通知接口
     */
    private void downloadSuccess()
    {
        if (mCallback != null)
        {
            mCallback.onSuccess();
        }
    }

    /**
     * 回调网络接收的及时数据长度
     * 
     * @param getLength
     *            已下载文件大小
     * @param totalLength
     *            文件的总大小
     */
    private void onProgress(long getLength, long totalLength)
    {
        if (mCallback != null)
        {
            mCallback.onProgressChanged(getLength, totalLength);
        }
    }
}
