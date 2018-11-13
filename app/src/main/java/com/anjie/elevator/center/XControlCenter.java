package com.anjie.elevator.center;

import com.example.screensdk.ProgramMedia;
import com.example.screensdk.ProgramTopic;
import com.example.screensdk.ScreenFirmware;
import com.example.screensdk.ScreenIoTClientSDK;

/**
 * 控制中心
 */
public class XControlCenter {

    /**
     * 单实例
     */
    private static final XControlCenter instance = new XControlCenter();

    /**
     * SDK媒体监听器
     */
    ScreenIoTClientSDK.IMediaActionListener iMediaActionListener = new ScreenIoTClientSDK.IMediaActionListener() {

        @Override
        public boolean OnSetTextCommand(String strText, String strPlayZone) {
            boolean bResult = false;
            if(strPlayZone.equalsIgnoreCase(ProgramTopic.TAG_TOPIC_CONTENT_TITLE)) {
                // Change the title here
//                title = strText;
            } else if (strPlayZone.equalsIgnoreCase(ProgramTopic.TAG_TOPIC_CONTENT_NOTICE)) {
                // Change the notice here
//                notice = strText;
            }
            return true;
        }

        @Override
        public boolean OnMediaPlayCommand(String strMediaID, String strPlayZone, ProgramMedia.MediaPlayCommand mediaPlayCommand) {
            return false;
        }

        @Override
        public boolean OnMediaDownloadStatus(String s, ProgramMedia.MediaDownloadStatus mediaDownloadStatus) {
            return false;
        }

        @Override
        public boolean OnSnapShotCommand() {
            return false;
        }

        @Override
        public boolean OnScreenLayoutEvent(String s) {
            return false;
        }
    };

    /**
     * SDK设置监听器
     */
    ScreenIoTClientSDK.ISettingsActionListener iSettingsActionListener = new ScreenIoTClientSDK.ISettingsActionListener() {

        @Override
        public boolean OnDisplaySettingsCommand(String s, String s1) {
            return false;
        }

        @Override
        public boolean OnSystemSettingsCommand(String s, String s1) {
            return false;
        }

        @Override
        public boolean OnRestoreDefaultSettings() {
            return false;
        }
    };

    /**
     * SDK固件监听器
     */
    ScreenIoTClientSDK.IFirmwareListener iFwActionListener = new ScreenIoTClientSDK.IFirmwareListener() {
        @Override
        public boolean OnFirmwareUploadedEvent(ScreenFirmware fwObj) {
            // New Firmware Uploaded
            // Confirm to continue Upgrading.
            // Check if needed
            ScreenIoTClientSDK.getInstance().NotifyFirmwareDownload();
            return true;
        }

        @Override
        public boolean OnFirmwareDownloadStatus(ScreenFirmware fwObj, ScreenFirmware.FirmwareDownloadStatus efirmwareDownloadStatus) {
            // Firmware Download status here
            if (efirmwareDownloadStatus == ScreenFirmware.FirmwareDownloadStatus.FW_DOWNLOAD_STATUS_ERROR) {
                // Handle Error
            } else if (efirmwareDownloadStatus == ScreenFirmware.FirmwareDownloadStatus.FW_DOWNLOAD_STATUS_FINISHED) {
                //Check whether to upgrading
                if (true) {
                    // do something backup, close resource
                    // upgrading with new FW, restart
                }

            }
            return true;
        }
    };

    /**
     * SDK云端紧急事件监听器
     */
    ScreenIoTClientSDK.IUrgentEventListener iUeListener = new ScreenIoTClientSDK.IUrgentEventListener() {
        @Override
        public boolean OnForceAllStop() {
            return false;
        }

        @Override
        public boolean OnForceAllResume() {
            return false;
        }
    };

    private XControlCenter() {

    }

    public static XControlCenter getInstance() {
        return instance;
    }

    public void init() {

    }

}
