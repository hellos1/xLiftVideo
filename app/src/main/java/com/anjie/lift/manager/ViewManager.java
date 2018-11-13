package com.anjie.lift.manager;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.anjie.common.io.IOUtils;
import com.anjie.common.log.LogX;
import com.anjie.common.storage.FileCacheService;
import com.anjie.common.system.SystemPropertiesProxy;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.app.FileManager;
import com.anjie.lift.parse.ViewParser;
import com.anjie.lift.view.BaseView;
import com.anjie.lift.view.BaseView.ViewType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 视图管理类
 */
public class ViewManager
{
    /**
     * 日志标签
     */
    private static final String TAG = "ViewManager";

    /**
     * 单实例
     */
    private static ViewManager instance;

    /**
     * 视图容器
     */
    private List<BaseView> mViewList = null;

    /**
     * 当前视图的版本号
     */
    private int viewVersion = -1;

    /**
     * 当前使用的布局文件目录
     */
    private String currentLayoutDir;

    public int width = 0;

    public  void setWidth(int screen_w){
        width = screen_w;
    }

    /**
     * 默认的布局文件
     */
    private static final String defaultLayoutFileName = "layout.zip";

    private static final String smallLayoutFileName = "layout800.zip";

    /**
     * 单实例
     *
     * @return
     */
    public synchronized static ViewManager getInstance()
    {
        if (instance == null)
        {
            instance = new ViewManager();
        }
        return instance;
    }

    /**
     * 私有构造函数
     */
    private ViewManager()
    {
        currentLayoutDir = FileManager.getInstance().getDefaultLayoutDir();
    }

    /**
     * 添加视图
     * 
     * @param viewList
     */
    public synchronized void addViewList(List<BaseView> viewList)
    {
        this.mViewList = viewList;
    }

    /**
     * 获取视图
     * 
     * @return
     */
    public synchronized List<BaseView> getViewList()
    {
        return mViewList;
    }

    /**
     * 当前视图的版本号
     * 
     * @return
     */
    public int getViewVersion()
    {
        return viewVersion;
    }

    /**
     * 获取当前使用的布局文件目录
     * 
     * @return
     */
    public String getCurrentLayoutDir()
    {
        return currentLayoutDir;
    }

    /**
     * 获取视图
     * 
     * @param viewType
     * @return
     */
    public BaseView getView(ViewType viewType)
    {
        if (mViewList == null)
        {
            return null;
        }
        BaseView baseView = null;
        for (BaseView view : mViewList)
        {
            if (view != null && view.getViewType() == viewType)
            {
                baseView = view;
                break;
            }
        }
        return baseView;
    }

    /**
     * 获取指定类型的视图列表
     * 
     * @param viewType
     * @return
     */
    public List<BaseView> getViewList(ViewType viewType)
    {
        if (mViewList == null)
        {
            return null;
        }
        List<BaseView> mList = new ArrayList<BaseView>();
        for (BaseView view : mViewList)
        {
            if (view != null && view.getViewType() == viewType)
            {
                mList.add(view);
            }
        }
        return mList;
    }

    /**
     * 加载View视图资源
     */
    public void loadViewRes()
    {
        InputStream inputStream = null;
        Context context = AppContext.getInstance().getContext();

        // 获取root/elevator/layout
        String layoutDir = FileManager.getInstance().getLayoutDir();
//        try {
//            Runtime.getRuntime().exec("rm -rf /sdcard/elevator/layout/*");
//        }catch (IOException ex)
//        {
//            ex.printStackTrace();
//        }

        // 新增布局目录校验,以避免恢复出厂设置后加载不到
        File layoutDirPath = new File(layoutDir);
        if (layoutDirPath.exists())
        {
            if (!layoutDirPath.isDirectory())
            {
                // 非文件夹目录类型，删除，重建目录
                layoutDirPath.delete();
                layoutDirPath.mkdir();
            }
        }
        else
        {
            // 生成布局目录
            layoutDirPath.mkdir();
        }
//
//        String  layoutStr = defaultLayoutFileName;
//        if(width == 800) {
//            layoutStr = smallLayoutFileName;
//        }

        //2018.04.03 下面一行
        String  layoutStr = defaultLayoutFileName;

        // 检查默认的布局文件root/elevator/layout/layout.zip
        File defaultLayoutZip = new File(layoutDir, layoutStr);
        if (!defaultLayoutZip.exists())
        {
            try
            {
                FileCacheService.writeFile(context.getAssets().open(layoutStr), defaultLayoutZip.getAbsolutePath());
            }
            catch (IOException e)
            {
                LogX.e(TAG, "copy assets defaultLayout.zip to app layout folder failed.", e);
            }
        }

        // 检查解压默认的布局root/elevator/layout/default
        File defaultDir = new File(layoutDir, "default");
        if (defaultDir.exists())
        {
            if (!defaultDir.isDirectory())
            {
                IOUtils.deleteFileSafely(defaultDir);
                defaultDir.mkdir();
                // 解压
                FileCacheService.unzip(defaultLayoutZip.getAbsolutePath(), defaultDir.getAbsolutePath());
            }
        }
        else
        {
            defaultDir.mkdir();
            // 解压
            FileCacheService.unzip(defaultLayoutZip.getAbsolutePath(), defaultDir.getAbsolutePath());
        }

        // 获取自定义配置文件 root/elevator/layout/layoutConfig.xml
        String layoutConfig = FileManager.getInstance().getCustomViewConfigPath();
        // 从布局配置文件中解析出当前layout目录下使用那个布局目录
        String cusViewDir = ViewParser.parseLayoutConfig(layoutConfig);
        if (!TextUtils.isEmpty(cusViewDir))
        {
            currentLayoutDir = FileManager.getInstance().getLayoutDir() + cusViewDir + File.separator;
        }
        else
        {
            currentLayoutDir = FileManager.getInstance().getDefaultLayoutDir();
        }

        try
        {
            File layoutFile = null;
            //必须有系统权限
            //SystemPropertiesProxy.set(context,"persist.sys.hwrotation","270");
            String rot =  SystemPropertiesProxy.get(context,"persist.sys.hwrotation");
            if(rot.equals("0"))
            {
                layoutFile = new File(currentLayoutDir, FileManager.VIEW_FILE_NAME);
            }
            else if(rot.equals("90"))
            {
                layoutFile = new File(currentLayoutDir, FileManager.VIEW_FILE_NAME_90);
            }
            else if(rot.equals("270"))
            {
                //2018.05.23增加270旋转支持
                layoutFile = new File(currentLayoutDir, FileManager.VIEW_FILE_NAME_90);
            }
            else
            {
                layoutFile = new File(currentLayoutDir, FileManager.VIEW_FILE_NAME);
            }
            if (layoutFile.exists())
            {
                LogX.d(TAG, "custom view file exist.");
                // 如果自定义下的文件在
                inputStream = new FileInputStream(layoutFile);
                Bundle extra = new Bundle();
                mViewList = ViewParser.parse(inputStream, extra);
                // 传递视图版本号
                viewVersion = extra.getInt("viewVersion", 0);
                LogX.d(TAG, "Finish parse view:" + mViewList);
            }
            else
            {
                LogX.w(TAG, "Warning!,custom layout folder custom_layout.xml not exist.");
            }
        }
        catch (Exception e)
        {
            LogX.e(TAG, "Parse view list meet exception!", e);
        }
        finally
        {
            IOUtils.close(inputStream);
        }
    }

    /**
     * 清除所有布局
     */
    public void clearAllLayout()
    {
        // 递归删除/elevator/layout/及其所有子目录和文件
        String layoutDir = FileManager.getInstance().getLayoutDir();
        boolean bResult = FileCacheService.deleteFileDir(layoutDir);
        LogX.d(TAG, "delete layout directory:" + bResult);
    }

    /**
     * 重置所有布局
     */
    public void resetAllLayout()
    {
        clearAllLayout();
        loadViewRes();
    }

}
