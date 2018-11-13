package com.anjie.lift.manager;

import com.anjie.common.system.SystemPropertiesProxy;
import com.anjie.lift.app.AppContext;
import com.anjie.lift.config.ConfigManager;

/**
 * 适配不同尺寸
 * Created by xu on 2018/4/20.
 */

public class ScreenAdapter {

    /**
     * 通力KM号，1024*768横竖屏; 800*600横竖屏
     */
    private static final String km_1024_h = "KM";
    private static final String km_1024_v = "KM";
    private static final String km_800_h = "KM";
    private static final String km_800_v = "KM";

    /**
     * 默认的布局文件
     */
    private static final String defaultLayoutFileName = "layout.zip";
    private static final String smallLayoutFileName = "layout800.zip";

    /**
     * 单实例
     */
    private static ScreenAdapter instance = new ScreenAdapter();

    /**
     * 本机横屏的width
     */
    private int screenSizeMsg = ConfigManager.getInstance().getScreenSizeMsg();

    /**
     * 私有化构造函数
     */
    private ScreenAdapter() {

    }

    /**
     * 单利模式
     * @return instance
     */
    public static ScreenAdapter getInstance(){
        if (instance == null){
            instance = new ScreenAdapter();
        }
        return instance;
    }

    /**
     *
     * @param length
     * @return TextSize
     */
    //不同屏幕下楼层显示(两位楼层和三位楼层)字体大小的适配
//    public int getFloorTextSize(int length){
//        switch (screenSizeMsg){
//            case 800:
//                if (length <= 2){
//                    return 210;
//                }else {
//                    return 185;
//                }
//            default:
//                if (length <= 2){
//                    return 260;
//                }else {
//                    return 210;
//                }
//        }
//    }
    public int getFloorTextSize(int length){
        if (length <= 2){
            return 260;
        }else {
            return 210;
        }
    }

    /**
     * 串口地址
     * @return path
     */
//    public String getSerialPortPath(){
//        String path = "/dev/ttyS2";
//        if (screenSizeMsg == 800)
//        {
//            path = "/dev/ttyS4";
//        }
//        return path;
//    }

    /**
     * 二维码中的KM号
     * @return QRCodeKM
     */
    public String getQRCodeKM(){
        String km = km_1024_h;
        String rot =  SystemPropertiesProxy.get(AppContext.getInstance().getContext(),"persist.sys.hwrotation");
        if (screenSizeMsg == 150 && rot.equals("0")){
            km = "KM51334677V000";
        }else if (screenSizeMsg == 150 && rot.equals("270")){
            km = "KM51342704V000";
        }else if (screenSizeMsg == 104 && rot.equals("180")){
            km = "KM51333760";
        }else if (screenSizeMsg == 104 && rot.equals("90")){
            km = "KM51333758";
        }else {
            //
        }
        return km;
    }


}
