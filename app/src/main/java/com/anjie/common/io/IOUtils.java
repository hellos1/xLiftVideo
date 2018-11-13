package com.anjie.common.io;

import android.text.TextUtils;

import com.anjie.common.log.LogX;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.zip.ZipFile;

/**
 * IO工具类
 */
public class IOUtils
{
    /**
     * 关闭输入流
     * 
     * @param inputStream
     *            输入流
     */
    public static void close(InputStream inputStream)
    {
        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                LogX.e("close InputStream meet Exception.", e);
            }
            finally
            {
                inputStream = null;
            }
        }
    }

    /**
     * 关闭写出流
     * 
     * @param output
     *            输出流
     */
    public static void close(OutputStream output)
    {
        if (output != null)
        {
            try
            {
                output.close();
            }
            catch (IOException e)
            {
                LogX.e("close OutputStream meet Exception.", e);
            }
            finally
            {
                output = null;
            }

        }
    }

    public static void close(BufferedReader bufferedReader)
    {
        if (bufferedReader != null)
        {
            try {

                bufferedReader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                bufferedReader = null;
            }

        }
    }

    /**
     * 关闭随机文件
     * 
     * @param accessFile
     *            随机文件
     */
    public static void close(RandomAccessFile accessFile)
    {
        if (accessFile != null)
        {
            try
            {
                accessFile.close();
            }
            catch (IOException e)
            {
                LogX.e("close RandomAccessFile meet Exception.", e);
            }
            finally
            {
                accessFile = null;
            }
        }
    }

    /**
     * 关闭链接HttpURLConnection
     * 
     * @param connection
     */
    public static void closeConnection(HttpURLConnection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.disconnect();
            }
            catch (Exception e)
            {
                LogX.e("close HttpURLConnection meet Exception.", e);
            }
            finally
            {
                connection = null;
            }
        }
    }

    /**
     * 关闭IO的流
     * 
     * @param zipFile
     */
    public static void close(ZipFile zipFile)
    {
        if (zipFile != null)
        {
            try
            {
                zipFile.close();
            }
            catch (IOException e)
            {
                LogX.e("close ZipFile meet Exception.", e);
            }
            finally
            {
                zipFile = null;
            }
        }
    }

    /**
     * 安全的删除一个文件
     * 
     * @param file
     * @return
     */
    public static boolean deleteFileSafely(File file)
    {
        if (file != null && file.exists())
        {
            String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
            File tmp = new File(tmpPath);
            file.renameTo(tmp);
            return tmp.delete();
        }
        return false;
    }

    /**
     * 删除文件
     * 
     * @param filePath
     * @return
     */
    public static boolean deleteFileSafely(String filePath)
    {
        if (TextUtils.isEmpty(filePath))
        {
            return false;
        }
        File file = new File(filePath);
        return deleteFileSafely(file);
    }
}
