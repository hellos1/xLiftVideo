package android_serialport_api;

import android.util.Log;

import com.anjie.common.log.LogX;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortReader
{
    /**
     * 日志标签
     */
    private static final String TAG = "SerialPortReader";

    /**
     * 串口
     */
    private SerialPort mSerialPort;

    /**
     * 串口地址
     */
    private String path = "/dev/ttyS2";

    private String path800="/dev/ttyS4";

    /**
     * 串口波特率
     */
    private static final int baudrate = 9600;

    /**
     * 输入流
     */
    private InputStream mInputStream;
    private OutputStream os;
    private byte[] heartBeats = {(byte)0x55, (byte)0x08, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x08, (byte) 0xAA};

    /**
     * 读线程
     */
    private ReadThread mReadThread;
    private WriteThread mWriteThread;

    /**
     * 电梯信息
     */
    private LiftInfoListener listener;

    /**
     * 上一次记录的楼层数据
     */
    private int lastFloorNum = 0;

    /**
     * 上一次的运行方向
     */
    private int lastDirection = LiftInfo.DIRECTION_ARRIVE;

    private OnDataReceiveListener onDataReceiveListener = new OnDataReceiveListener()
    {
        @Override
        public void onDataReceive(byte[] buffer, int size)
        {
            boolean isChange = true;
            int direction = getDirection(buffer[2]);
            lastFloorNum=(buffer[3]<<16)|(buffer[0]<<8)|buffer[1];

            if (isChange && listener != null)
            {
                listener.onDataReceive(getLiftFloorNumText(lastFloorNum), direction,buffer[4]);
            }
        }
    };
    
    private String getLiftFloorNumText(int floorNum)
    {
        char floor_hc = ((char) ((floorNum & 0xff0000) >> 16));
        String floor_h = String.valueOf(floor_hc);
        char floor_mc = ((char) ((floorNum & 0xff00) >> 8));
        String floor_m = String.valueOf(floor_mc);
        char floor_c = ((char) ((floorNum & 0xff)));
        String floor_l = String.valueOf(floor_c);
       // LogX.d(TAG, floor_h +"|"+ floor_m +"|"+ floor_l);
        if(floor_hc=='F')
        {
        	return floor_m + floor_l;
        }else {
        	return floor_h + floor_m + floor_l;
        }
    }
    /**
     * 获取电梯运行方向
     * 
     * @param buf
     * @return
     */
    private int getDirection(byte buf)
    {
        int direction = -1;
        //buf = 0x31;
        if (buf == 0x31)
        {
            direction = LiftInfo.DIRECTION_DOWN;
        }
        else if (buf == 0x32)
        {
            direction = LiftInfo.DIRECTION_UP;
        }
        else if (buf == 0x33)
        {
            direction = LiftInfo.DIRECTION_ARRIVE;
        }
        return direction;
    }

    /**
     * 获取楼层
     * 
     * @return
     */
    private int getFloorNum()
    {
        return 1;
    }

    public interface OnDataReceiveListener
    {
        public void onDataReceive(byte[] buffer, int size);
    }

    public SerialPortReader(LiftInfoListener listener)
    {
        this.listener = listener;
    }

    public void startMonitor()
    {
        boolean bResult = true;

        try
        {
            mSerialPort = new SerialPort(new File(path), baudrate, 0);//new SerialPort(new File(path), baudrate);
            mInputStream = mSerialPort.getInputStream();
            os = mSerialPort.getOutputStream();
        }
        catch (Exception e)
        {
            bResult = false;
            LogX.e("Open SerialPort meet exception.", e);
            if (listener != null)
            {
                listener.onError();
            }
        }

        if (bResult)
        {
            mReadThread = new ReadThread();
            mReadThread.start();
        }
    }

    public void stopMonitor()
    {
        if (mReadThread != null)
        {
            mReadThread.stopRead();
            mReadThread = null;
        }
    }

    //2018.05.18
    public void stopHeartBeats()
    {
        if (mWriteThread != null)
        {
            mWriteThread.stopWrite();
            mWriteThread = null;
        }
    }

    //2018.05.18 发送心跳包
    public void startHeartBeats()
    {
        mWriteThread = new WriteThread();
        mWriteThread.start();
    }

    //心跳线程
    private class WriteThread extends Thread
    {
        private boolean isStopWrite = false;
        public WriteThread()
        {

        }

        public void stopWrite()
        {
            isStopWrite = true;
        }

        @Override
        public void run()
        {
            super.run();

            if (mSerialPort == null || os==null)
            {
                Log.e(TAG, "serial port is null");
                return;
            }

            while (!isStopWrite && !isInterrupted())
            {
//                OutputStream os;
//                os = mSerialPort.getOutputStream();

                try
                {
                    if (os == null)
                    {
                        LogX.e(TAG, "output stream is null" );
                        return;
                    }
                    LogX.d(TAG, "start write");
//                    for (byte heartBeat : heartBeats)
//                    {
//                        os.write(heartBeat);
//                    }
                    os.write(heartBeats);
                    LogX.d(TAG, " send data ");
//                    os.flush();
                    Thread.sleep(30 * 1000);
                    LogX.d(TAG, "data send finished");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    LogX.e(TAG, "Serial port reader meet a exception" + e.getMessage());
                    return;
                }
            }
        }
    }

    private class ReadThread extends Thread
    {
        /**
         * 是否停止读取
         */
        private boolean isStop = false;

        public ReadThread()
        {

        }

        public void stopRead()
        {
            isStop = true;
        }
        public  String byte2HexStr(byte[] b) {  
            String hs = "";  
            String stmp = "";  
            for (int n = 0; n < b.length; n++) {  
                stmp = (Integer.toHexString(b[n] & 0XFF));
                if (stmp.length() == 1)  
                    hs = hs + "0" + stmp;  
                else  
                    hs = hs + stmp;  
            }  
            return hs.toUpperCase();  
        }  
        @Override
        public void run()
        {
            super.run();
            while (!isStop && !isInterrupted())
            {
                int size;
                try
                {
                    if (mInputStream == null)
                    {
                        return;
                    }
                    byte[] buffer = new byte[6];
                    size = mInputStream.read(buffer);
                    if (size ==6)
                    {
                        if (onDataReceiveListener != null)
                        {
                            onDataReceiveListener.onDataReceive(buffer, size);
                        }
                    }
                    Thread.sleep(10);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
