package com.anjie.common.storage;

import android.os.StatFs;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileCacheService
{

    /**
     * 日志标签
     */
    private static final String TAG = "FileMode";

    /**
     * 是否有足够的空间
     * 
     * @param fileSize
     * @param fileDir
     * @return
     */
    public static boolean spaceIsEnough(long fileSize, String fileDir)
    {
        if (!(fileSize <= getStorageFree(fileDir)))
        {
            return false;
        }
        return true;
    }

    /**
     * 查看目录可用空间
     * 
     * filePath@param
     * @return
     */
    private static long getStorageFree(String fileDir)
    {
        StatFs statfs = new StatFs(fileDir);
        long blocSize = statfs.getBlockSize();
        long availableBlock = statfs.getAvailableBlocks();
        long available = availableBlock * blocSize;
        return available;
    }

    /**
     * 写文件数据
     * 
     * @param file
     * @param byteBuffer
     * @return
     */
    public static int writeFile(RandomAccessFile file, byte[] byteBuffer)
    {
        int length = 0;
        if (byteBuffer != null)
        {
            if (file != null)
            {
                try
                {
                    file.seek(file.length());
                    file.write(byteBuffer);
                }
                catch (IOException e)
                {
                    // 如果出现异常，返回的字节数为-1，表示出现了异常，没有写入成功
                    return -1;
                }
                length = byteBuffer.length;
            }
            else
            {
                // 如果出现异常，返回的字节数为-1，表示出现了异常，没有写入成功
                return -1;
            }
        }
        return length;
    }

    /**
     * 写文件
     * 
     * @param filePath
     * @param content
     * @return
     */
    public static boolean writeFile(String filePath, String content)
    {
        boolean bResult = true;
        FileOutputStream outStream = null;
        try
        {
            outStream = new FileOutputStream(filePath);
            outStream.write(content.getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            // 重建错误就不需要播放列表变化
            bResult = false;
            LogX.e(TAG, "write file:" + filePath + " meet exception.", e);
        }
        finally
        {
            IOUtils.close(outStream);
        }
        return bResult;
    }

    /**
     * 写文件
     * 
     * @param in
     * @param filePath
     * @return
     */
    public static boolean writeFile(InputStream in, String filePath)
    {
        boolean bResult = false;
        File file = new File(filePath);
        if (file.exists())
        {
            file.delete();
        }
        RandomAccessFile randomAccessFile = null;
        try
        {
            file.createNewFile();
            if (in != null)
            {
                randomAccessFile = new RandomAccessFile(file, "rw");
                byte[] buffer = new byte[1024];
                int len = 0;
                while (true)
                {
                    len = in.read(buffer);
                    if (len == -1)
                    {
                        break;
                    }
                    writeFile(randomAccessFile, buffer);
                }
                bResult = true;
            }
        }
        catch (Exception e)
        {
            LogX.e("Write file:" + filePath + " meet exception.", e);
        }
        finally
        {
            IOUtils.close(randomAccessFile);
            IOUtils.close(in);
        }
        return bResult;
    }

    /**
     * 拷贝文件
     * 
     * @param srcFilePath
     *            源文件
     * @param disFilePath
     *            目标文件
     * @return
     */
    public static boolean copyFile(String srcFilePath, String disFilePath)
    {
        boolean bResult = true;
        File srcFile = new File(srcFilePath);
        if (!srcFile.exists())
        {
            // 源文件不存在
            return false;
        }
        try
        {
            FileInputStream input = new FileInputStream(srcFile);
            bResult = writeFile(input, disFilePath);
        }
        catch (Exception e)
        {
            bResult = false;
            LogX.d(TAG, "CopyFile meet exception.");
        }
        return bResult;
    }

    public static boolean copyFile(File srcFile, String disFilePath)
    {
        if (srcFile == null || !srcFile.exists())
        {
            return false;
        }
        boolean bResult = true;
        try
        {
            FileInputStream input = new FileInputStream(srcFile);
            bResult = writeFile(input, disFilePath);
        }
        catch (Exception e)
        {
            bResult = false;
            LogX.d(TAG, "CopyFile meet exception.");
        }
        return bResult;
    }

    /**
     * 删除文件
     * 
     * @param fileDir
     *            文件目录
     * @param fileName
     *            文件名
     * @return 删除是否成功
     */
    public static boolean deleteFile(String fileDir, String fileName)
    {
        boolean bResult = false;
        File file = new File(fileDir, fileName);
        if (file.exists())
        {
            bResult = file.delete();
            if (!bResult)
            {
                LogX.w(TAG, "Delete file failed!(" + fileDir + fileName + ")");
            }
        }
        else
        {
            // 文件不存在表示删除成功
            bResult = true;
        }
        return bResult;
    }

    /**
     * 解压文件到指定的目录
     * 
     * @param zipFilePath
     *            ZIP文件路径
     * @param folderPath
     *            解压目录
     * @return
     */
    public static boolean unzip(String zipFilePath, String folderPath)
    {
        boolean bResult = true;
        ZipEntry ze = null;
        byte[] buf = new byte[1024];
        ZipFile zipFile = null;
        OutputStream os = null;
        InputStream is = null;
        try
        {
            zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> zList = zipFile.entries();
            while (zList.hasMoreElements())
            {
                ze = zList.nextElement();
                if (!ze.isDirectory())
                {
                    LogX.d(TAG, "upZipFile fileName = " + ze.getName());
                    os = new BufferedOutputStream(new FileOutputStream(new File(folderPath, ze.getName())));
                    is = new BufferedInputStream(zipFile.getInputStream(ze));
                    int readLen = 0;
                    while ((readLen = is.read(buf, 0, 1024)) != -1)
                    {
                        os.write(buf, 0, readLen);
                    }
                    IOUtils.close(is);
                    IOUtils.close(os);
                }
            }
        }
        catch (IOException e)
        {
            LogX.e(TAG, "unzip file " + zipFilePath + " meet exception.", e);
        }
        finally
        {
            IOUtils.close(os);
            IOUtils.close(is);
            IOUtils.close(zipFile);
        }

        return bResult;
    }

    /**
     * 删除文件目录
     * 
     * @param fileDir
     *            文件目录名
     * @return 是否删除成功
     */
    public static boolean deleteFileDir(String fileDir)
    {
        return deleteDir(new File(fileDir));
    }

    /**
     * 递归删除文件夹
     * 
     * @param dir
     *            文件夹
     * @return 是否删除成功
     */
    public static boolean deleteDir(File dir)
    {
        if (dir == null || !dir.exists())
        {
            // 判空或者不存在
            return false;
        }
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            // 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++)
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
    /**
     * 删除文件
     *
     * @param filePath
     *            文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath)
    {
        File file = new File(filePath);
        if (file.exists())
        {
            return file.delete();
        }
        return false;
    }
}
