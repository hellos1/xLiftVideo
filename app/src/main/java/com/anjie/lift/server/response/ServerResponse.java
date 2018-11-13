package com.anjie.lift.server.response;

public abstract class ServerResponse
{
    /**
     * 服务器处理结果
     */
    protected int resultCode = Failed;

    /**
     * 服务器返回成功
     */
    public static final int Success = 0;

    /**
     * 服务器返回失败
     */
    public static final int Failed = 1;

    /**
     * 是否返回成功码
     * 
     * @return
     */
    public boolean isRespSuccess()
    {
        return resultCode == Success;
    }

    public void setResultCode(int resultCode)
    {
        this.resultCode = resultCode;
    }
}
