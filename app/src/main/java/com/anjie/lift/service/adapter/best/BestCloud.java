package com.anjie.lift.service.adapter.best;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.anjie.common.log.LogX;
import com.anjie.lift.service.adapter.CloudAdapter;
import com.shbst.androiddevicesdk.DeviceSDK;
import com.shbst.androiddevicesdk.beans.Constants;
import com.shbst.androiddevicesdk.widget.DeviceSDKAdapter;
import com.shbst.androiddevicesdk.widget.MqttAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BestCloud implements CloudAdapter
{
    /**
     * 日志标签
     */
    private static final String TAG = "BestCloud";

    /**
     * 延迟时间
     */
    private static final int TIME_DELAY = 6 * 1000;

    /**
     * 本地IP地址
     */
    private String ipAddr = "192.168.0.105";

    /**
     * 是否已经注册上了
     */
    private boolean isRegistered = false;


    /**
     * Best的配置信息
     */
    private BestConfigInfo configInfo;

    /**
     * 工作线程
     */
    private BestHandler bestHandler;

    // 定时任务
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private enum HandlerCase
    {
        receive_mqtt_message, // 接收到MQTT消息
        connect_to_m2m, // 连接M2M服务器
        disconnect_from_m2m, // 断开M2M服务器
        send_mqtt_message, // 发送M2M服务器
        receive_server_message, // 接收到服务器消息
        sign_up_success, // 注册设备成功
        sign_up_error, // 注册设备出错
        sign_up_timeout, // 注册设备超时
        get_m2m_server_success, // 连接M2M服务器成功
        get_m2m_server_error, // 连接M2M服务器出错
        get_m2m_server_timeout // 连接M2M服务器超时
    }

    /**
     * Best云
     */
    public BestCloud()
    {
        configInfo = new BestConfigInfo();
        HandlerThread thread = new HandlerThread("BestCloudHandler");
        thread.start();
        // 构建BestHandler的线程
        bestHandler = new BestHandler(thread.getLooper());
    }

    /**
     * 初始化回调事件
     */
    private void initEvent()
    {
        DeviceSDK.getInstance().setDeviceSDKListener(deviceSDKAdapter);
        DeviceSDK.getInstance().setMqttListener(mqttAdapter);
    }



    @Override
    public void onCreate()
    {
        LogX.i(TAG, "onCreate()");
        // 注册事件
        initEvent();
        // 注册广播(屏幕点亮和网络连接上)
        //2018.05.11
        //registerBroadcastReceiver();
    }

    @Override
    public void onSyncWithServer()
    {
        LogX.d(TAG, "onSyncWithServer() isConnect Server:" + isRegistered);
        if (!isRegistered)
        {
            isRegistered = true;
            registerDevice();
        }

        // 检查是否有升级失败的任务
        BestVersionManager.getInstance().checkIfHasUpdateAPKTask();
    }

    @Override
    public void onDestroy()
    {
        LogX.d(TAG, "onDestroy()");
        //2018.05.11
        //unRegisterBroadcastReceiver();
        DeviceSDK.getInstance().setFileDownloadAdapter(null);
        DeviceSDK.getInstance().setMqttListener(null);
//        DeviceSDK.getInstance().setVisualIntercomListener(null);
        DeviceSDK.getInstance().setDeviceSDKListener(null);
    }

    /**
     * 注册设备
     */
    private void registerDevice()
    {
        LogX.d(TAG, "registerDevice, call device sign up."
                + configInfo.getMacAddress());
        // 注册时间6秒
        bestHandler.sendEmptyMessageDelayed(
                HandlerCase.sign_up_timeout.ordinal(), TIME_DELAY);
        // 注册设备MAC和ProductId
        DeviceSDK.getInstance().deviceSignUp(configInfo.getProductId(),
                configInfo.getMacAddress());
    }

    /**
     * 注册监听器和获取M2M监听器
     */
    private DeviceSDKAdapter deviceSDKAdapter = new DeviceSDKAdapter()
    {
        @Override
        public void onDeviceSignUp(int resultCode, String resultMessage)
        {
            bestHandler.removeMessages(HandlerCase.sign_up_timeout.ordinal());
            if (Constants.Result_Success == resultCode)
            {
                LogX.d(TAG, "onDeviceSignUp Success.");
                // 注册成功,开始获取M2M服务器信息
                bestHandler.sendEmptyMessage(
                        HandlerCase.sign_up_success.ordinal());
            }
            else
            {
                registerFailed();
                LogX.w(TAG, "onDeviceSignUp Failed.resultCode:" + resultCode
                        + ",resultMessage" + resultMessage);
            }
        }

        @Override
        public void onGetM2MServer(int resultCode, String resultMessage)
        {
            bestHandler.removeMessages(HandlerCase.get_m2m_server_timeout.ordinal());
            if (Constants.Result_Success == resultCode)
            {
                LogX.d(TAG, "onGetM2MServer Success.");
                // 连接云服务器
                DeviceSDK.getInstance()
                        .startServerMsgListener(configInfo.getMacAddress());
            }
            else
            {
                LogX.d(TAG, "onGetM2MServer Failed. resultCode:" + resultCode
                        + ",resultMessage" + resultMessage);
                //2018.05.11
                //registerFailed();
            }
        }
    };

    /**
     * MQTT连接监听器
     */
    private MqttAdapter mqttAdapter = new MqttAdapter()
    {
        @Override
        public void onConnection(final int result, final String message)
        {
            if (Constants.Result_Success == result)
            {
                // 成功连接云服务器！
                //2018.07.19
                //executor.scheduleAtFixedRate(new AckRunnable(), 0, 10, TimeUnit.SECONDS);
                LogX.d(TAG, "onConnection Success. message:" + message);
            }
            else
            {
                // 连接云服务器失败
                LogX.d(TAG, "onConnection Failed.result:" + result + ",message" + message);
            }
        }

        @Override
        public void onDisconnection(int result, final String message)
        {
            // 已断开m2m连接！
            LogX.i(TAG, "onDisconnection result:" + result + ",message" + message);
            // 与服务器断开,重置本地连接服务器状态
            //2018.05.11
            //registerFailed();
        }

        @Override
        public void onSend(int result, String message, final String json)
        {
            LogX.i(TAG, "onSend result:" + result + ",message" + message
                    + ",json:" + json);
        }

        @Override
        public void onReceive(int result, String message, String jsonMessage,
                int cmd, String uid)
        {
            LogX.i(TAG, "onReceive result:" + result + ",message:" + message
                            + ",jsonMessage:" + jsonMessage + ",cmd:" + cmd
                            + ",uid:" + uid);
            //2018.07.31 用于云端网页每次点击配置等等 时 上报当前一体机的配置信息

            if (TextUtils.isEmpty(jsonMessage) || jsonMessage.length() <= 2)
            {
                BestCloudAck.getInstance().createACK(uid,1);
            }

            if (Constants.Result_Success == result)
            {
                Message message1 = bestHandler.obtainMessage(HandlerCase.receive_mqtt_message.ordinal(),jsonMessage);
                bestHandler.sendMessage(message1);
            }
        }

        @Override
        public void onReceiveServerMsg(int result, String serverMsg)
        {
            LogX.i(TAG, "onReceiveServerMsg result:" + result + ",serverMsg:" + serverMsg);
            if (Constants.Result_Success == result)
            {
                bestHandler.sendMessage(bestHandler.obtainMessage(
                        HandlerCase.receive_server_message.ordinal(),
                        serverMsg));
            }
        }

        @Override
        public void upgrade(String url)
        {
            // 收到升级的MQTT消息
            LogX.d(TAG, "upgrade url -> " + url);
        }
    };

    /**
     * 注册失败
     */
    private void registerFailed()
    {
        LogX.d(TAG, "registerFailed()");
        isRegistered = false;
    }

    /**
     * 处理云端的
     * @param message
     */
    private void handleOnReceiveServerMsg(Message message)
    {
        if (message.obj instanceof String)
        {
            new BestCloudHelper().handleOnReceiveServerMsg((String) message.obj);
        }
    }

    /**
     * 处理MQTT消息
     * 
     * @param message
     */
    private void handlerReceiveMQTTMessage(Message message)
    {
        if (message.obj instanceof String)
        {
            new BestCloudHelper().handlerReceiveMQTTMessage((String) message.obj);
        }
    }

    /**
     * 连接M2M服务器
     */
    private void connectM2MServer()
    {
        bestHandler.sendEmptyMessageDelayed(
                HandlerCase.get_m2m_server_timeout.ordinal(), TIME_DELAY);
        DeviceSDK.getInstance().getM2MServer(configInfo.getMacAddress());
    }

    /**
     * Handler线程处理服务器下发的各种消息逻辑
     */
    private class BestHandler extends Handler
    {
        public BestHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg)
        {
            HandlerCase handlerCase = HandlerCase.values()[msg.what];
            switch (handlerCase)
            {
                case sign_up_success:
                    // 注册成功之后，开始连接M2M服务器
                    connectM2MServer();
                    break;
                case sign_up_error:
                case sign_up_timeout:
                    // 注册超时和失败
                    registerFailed();
                    break;
                case get_m2m_server_success:
                    // 成功获取m2m服务器信息,启动mqtt连接...
                    DeviceSDK.getInstance()
                            .startServerMsgListener(configInfo.getMacAddress());
                    break;
                case get_m2m_server_error:
                case get_m2m_server_timeout:
                    //2018.05.11
                    //registerFailed();
                    break;
                case connect_to_m2m:
                    break;
                case receive_mqtt_message:
                    handlerReceiveMQTTMessage(msg);
                    break;
                case disconnect_from_m2m:
                    break;
                case receive_server_message:
                    handleOnReceiveServerMsg(msg);
                    break;
                case send_mqtt_message:
                    ackServer();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 用于定时发送一体机当前配置信息的线程
     * 2019.07.20
     */
    private class AckRunnable implements Runnable
    {
        @Override
        public void run() {
            //new BestCloudAck().createACK(" ",1);
            BestCloudAck.getInstance().createACK(" ", 1);
            LogX.d(TAG, "Ack has send msg");
        }
    }

    public void ackServer()
    {
        //TODO
    }
}
