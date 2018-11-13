package android_serialport_api;

public interface LiftInfoListener
{
    public void onDataReceive(String floorNum, int direction, byte status);

    public void onError();
}
