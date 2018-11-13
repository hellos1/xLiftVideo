package com.anjie.common.storage;

import java.lang.reflect.Field;

import com.anjie.common.log.LogX;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.Settings;

/**
 * @author jimmy
 */
public class SystemStorage
{
    private static Context mContext;
    private Object[] mStorageVolumeList = null;
    private Object mStorageManager = null;
    static Field FIELD_SAVE_LOCATION = null;

    public static final int SETTING_NOT_SUPPORT = 0;
    public static final int SETTING_IN_INTERNEL = 1;
    public static final int SETTING_IN_SDCARD = 2;

    public SystemStorage(Context context)
    {
        mContext = context;
    }

    private void getStoragePathList()
    {
        try
        {
            if (null == mStorageManager)
            {
                mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
            }
            mStorageVolumeList = (Object[]) mStorageManager.getClass().getMethod("getVolumeList")
                    .invoke(mStorageManager);

        }
        catch (Exception e)
        {
            LogX.e("Storage_standard getStorageVolumeList failed!");
        }
    }

    private String getVolumePath(Object storageVolume)
    {
        String storagePath = null;
        if (null == storageVolume)
        {
            return "";
        }
        try
        {
            storagePath = (String) storageVolume.getClass().getMethod("getPath").invoke(storageVolume);
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard_getVolumePath");
        }
        return storagePath;
    }

    private boolean isVolumeMounted(Object storageVolume)
    {
        boolean mountble = false;
        String storagePath = null;
        String state = null;
        try
        {
            if (null == storageVolume)
            {
                LogX.e("null == storageVolume failed in isVolumeRemoveble");
            }
            if (null == mStorageManager)
            {
                mStorageManager = mContext.getSystemService(Context.STORAGE_SERVICE);
            }
            storagePath = (String) storageVolume.getClass().getMethod("getPath").invoke(storageVolume);
            if (null == storagePath || 0 == storagePath.length())
            {
                return false;
            }

            state = (String) mStorageManager.getClass().getMethod("getVolumeState", String.class)
                    .invoke(mStorageManager, storagePath);

            if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(state))
            {
                LogX.e("MEDIA_MOUNTED");
                mountble = true;
            }
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.isVolumeRemoveble()");
        }
        LogX.e("mountble:" + mountble);
        return mountble;
    }

    private boolean isVolumeRemoveble(Object storageVolume)
    {
        boolean removeble = false;
        try
        {
            if (null == storageVolume)
            {
                LogX.e("null == storageVolume failed in isVolumeRemoveble");
            }
            removeble = (Boolean) storageVolume.getClass().getMethod("isRemovable").invoke(storageVolume);

        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.isVolumeRemoveble()");
        }
        return removeble;
    }

    private boolean isOtgVolume(Object storageVolume)
    {
        boolean isOtg = false;
        Object OtgVolume = null;
        try
        {
            if (null == storageVolume)
            {
                LogX.e("null == storageVolume failed in isOtgVolume");
            }
            if (null == mStorageManager)
            {
                mStorageManager = mContext.getSystemService(Context.STORAGE_SERVICE);
            }
            OtgVolume = (Object) mStorageManager.getClass().getMethod("getOtgVolume").invoke(mStorageManager);
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.isVolumeRemoveble()");
        }
        LogX.e("OtgVolume=" + OtgVolume);
        if (null != OtgVolume && OtgVolume.equals(storageVolume))
            isOtg = true;
        return isOtg;
    }

    public String getInternelStorageDirectory()
    {
        // get internel storage directory
        String path = null;
        if (null == mStorageVolumeList)
        {
            getStoragePathList();
        }
        try
        {
            for (int i = 0; i < mStorageVolumeList.length; i++)
            {
                if (!isVolumeRemoveble(mStorageVolumeList[i]))
                {
                    path = getVolumePath(mStorageVolumeList[i]);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.getICSVirtualStorageDirectory()");
        }
        return path;
    }

    public String getSDcardStorageDirectory()
    {
        // get sd card directory
        String path = null;
        if (null == mStorageVolumeList)
        {
            getStoragePathList();
        }
        try
        {
            for (int i = 0; i < mStorageVolumeList.length; i++)
            {
                if (isVolumeRemoveble(mStorageVolumeList[i]) && !isOtgVolume(mStorageVolumeList[i]))
                {
                    path = getVolumePath(mStorageVolumeList[i]);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.getICSVirtualStorageDirectory()");
        }
        return path;
    }

    /**
     * StorageVolume --> Object
     * 
     * @return
     */
    public Object getUsbStorageVolume()
    {
        return getInternelStorageVolume();
    }

    /**
     * StorageVolume --> Object
     * 
     * @return
     */
    public Object getInternelStorageVolume()
    {
        Object UsbVolume = null;
        if (null == mStorageVolumeList)
        {
            getStoragePathList();
        }
        try
        {
            for (int i = 0; i < mStorageVolumeList.length; i++)
            {
                if (!isVolumeRemoveble(mStorageVolumeList[i]))
                {
                    UsbVolume = (Object) mStorageVolumeList[i];
                    break;
                }
            }
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.getICSVirtualStorageDirectory()");
        }
        return UsbVolume;
    }

    public Object getSdCardVolume()
    {
        Object SdCardVolume = null;
        if (null == mStorageVolumeList)
        {
            getStoragePathList();
        }
        try
        {
            for (int i = 0; i < mStorageVolumeList.length; i++)
            {
                if (isVolumeRemoveble(mStorageVolumeList[i]) && !isOtgVolume(mStorageVolumeList[i]))
                {
                    SdCardVolume = (Object) mStorageVolumeList[i];
                    break;
                }
            }
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.getICSVirtualStorageDirectory()");
        }
        return SdCardVolume;
    }

    public String getSdCardVolumeState()
    {
        Object sdCardVolume = null;
        String state = null;
        String storagePath = null;
        sdCardVolume = getSdCardVolume();
        try
        {
            if (null == mStorageManager)
            {
                mStorageManager = mContext.getSystemService(Context.STORAGE_SERVICE);
            }
            storagePath = (String) sdCardVolume.getClass().getMethod("getPath").invoke(sdCardVolume);
            if (null == storagePath || 0 == storagePath.length())
            {
                return state;
            }

            state = (String) mStorageManager.getClass().getMethod("getVolumeState", String.class)
                    .invoke(mStorageManager, storagePath);
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.getSdCardVolumeState()");
        }

        return state;
    }

    public static boolean isSaveLocationSettingSupport()
    {
        boolean saveLocation = false;
        try
        {
            FIELD_SAVE_LOCATION = Settings.System.class.getDeclaredField("SAVE_LOCATION");
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            FIELD_SAVE_LOCATION = null;
        }
        if (FIELD_SAVE_LOCATION != null)
        {
            saveLocation = true;
        }
        return saveLocation;
    }

    public static int getSaveLocationInSetting()
    {
        int saveLocation = SETTING_NOT_SUPPORT;
        String name = null;
        int tempLocal;
        if (!isSaveLocationSettingSupport())
        {
            LogX.e("not support save location Setting");
            return saveLocation;
        }
        else
        {
            LogX.e("support save location Setting");
            try
            {
                name = (String) FIELD_SAVE_LOCATION.get(Settings.System.class);
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
                name = null;
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
                name = null;
            }
            if (name == null)
            {
                LogX.e("not support save location Setting");
                return saveLocation;
            }

            tempLocal = Settings.System.getInt(mContext.getContentResolver(), name, 1);
            if (tempLocal == 0)
            {
                LogX.e("SETTING_IN_INTERNEL");
                saveLocation = SETTING_IN_INTERNEL;
            }
            else if (tempLocal == 1)
            {
                LogX.e("SETTING_IN_SDCARD");
                saveLocation = SETTING_IN_SDCARD;
            }
            return saveLocation;
        }

    }

    /**
     * StorageVolume --> Object
     * 
     * @return
     */
    private Object getCurrentVolume()
    {
        Object mSDCardVolume = null;
        Object mUSBVolume = null;
        Object mCurrentVolume = null;

        if (null == mStorageVolumeList)
        {
            getStoragePathList();
        }
        LogX.e("getCurrentVolume(),mStorageVolumeList.length=" + mStorageVolumeList.length);
        try
        {
            for (int i = 0; i < mStorageVolumeList.length; i++)
            {
                if (isVolumeRemoveble(mStorageVolumeList[i]))
                {
                    if (null == mSDCardVolume && !isOtgVolume(mStorageVolumeList[i]))
                    {
                        mSDCardVolume = (Object) mStorageVolumeList[i];
                        LogX.e("mSDCardVolume=" + mSDCardVolume);
                    }
                }
                else
                {
                    mUSBVolume = (Object) mStorageVolumeList[i];
                    LogX.e("mUSBVolume=" + mUSBVolume);
                }
            }
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.getICSVirtualStorageDirectory()");
        }

        if (isVolumeMounted(mSDCardVolume))
        {
            mCurrentVolume = mSDCardVolume;
            LogX.e("mCurrentVolume:" + mCurrentVolume);
        }
        else
        {
            mCurrentVolume = mUSBVolume;
        }

        if (null == mStorageManager)
        {
            mStorageManager = mContext.getSystemService(Context.STORAGE_SERVICE);
        }

        return mCurrentVolume;
    }

    /**
     * StorageVolume --> Object
     * 
     * @return
     */
    public String getCurrentVolumePath()
    {
        Object mCurrentVolume = null;
        String path = null;
        mCurrentVolume = getCurrentVolume();
        path = getVolumePath(mCurrentVolume);
        LogX.e("getVolumePath:" + path);
        return path;
    }

    /**
     * StorageVolume --> Object
     * 
     * @return
     */
    public String getDescription(Object volume)
    {
        if (volume != null)
        {
            String description;
            if (Reflect.hasMethod(volume.getClass(), "getDescription", new Class[] {Context.class }))
            {
                description = (String) Reflect.invoke(volume,
                        Reflect.getMethod(volume, "getDescription", new Class[] {Context.class }),
                        new Object[] {mContext });
                LogX.e("description: " + description);
                return description;
            }
        }
        return "";
    }

    /*
     * has SD card added by GAOPENG_20100805
     */
    public boolean hasExternalStorage()
    {
        // return
        // Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        // modify for standard interface
        return Environment.MEDIA_MOUNTED.equals(getSdCardVolumeState());
    }

    /**
     * StorageVolume --> Object
     * 
     * @return
     */
    public boolean isFileInSdCard(String filePath)
    {
        Object mSdCardVolume = null;
        String storagePath = null;
        mSdCardVolume = getSdCardVolume();
        try
        {
            if (null == mStorageManager)
            {
                mStorageManager = mContext.getSystemService(Context.STORAGE_SERVICE);
            }
            storagePath = (String) mSdCardVolume.getClass().getMethod("getPath").invoke(mSdCardVolume);
            if (null == storagePath || 0 == storagePath.length())
            {
                return true;
            }
            storagePath = storagePath + "/";
            int sdPathLength = storagePath.length();
            int filePathLength = filePath.length();
            int pathLength = Math.min(sdPathLength, filePathLength);
            String storagePath2 = storagePath.substring(0, pathLength);
            String filePath2 = filePath.substring(0, pathLength);
            if (storagePath2.equals(filePath2))
                return true;
            else
                return false;
        }
        catch (Exception e)
        {
            LogX.e("Storage_Standard.isFileInSdCard()");
        }
        return true;
    }

    public void clearStorageList()
    {
        mStorageVolumeList = null;
    }
}
