package com.anjie.lift.config;

/**
 * 电梯配置信息
 */
public final class ElevatorInfo
{
    /**
     * 安杰云
     */
    public static final int ANJIE_CLOUD = 1;

    /**
     * Best云
     */
    public static final int BEST_CLOUD = 2;

    /**
     * 旋转角度
     */
    private int rot;

    /**
     * 0-非全屏,1-全屏播放
     */
    private int screenMode;

    /**
     * 连接的云类型(1-安杰云;2-Best云)
     */
    private int cloudType;

    /**
     * <!--0 4g, 1 Ethernet, 2 单机-->
     */
    private int net;

    /**
     * 本地IP地址
     */
    private String localIp;

    /**
     * 子网掩码
     */
    private String netMask;

    /**
     * 网关地址
     */
    private String gateWay;

    /**
     * DNS地址
     */
    private String dns;

    /**
     * 服务器地址
     */
    private String server;

    /**
     * 项目PID地址
     */
    private String pid;

    /**
     * 项目DID地址
     */
    private String did;

    /**
     * 版本信息
     */
    private String version;

    public int getRot()
    {
        return rot;
    }

    public void setRot(int rot)
    {
        this.rot = rot;
    }

    public int getNet()
    {
        return net;
    }

    public void setNet(int netType)
    {
        this.net = netType;
    }

    public String getLocalIp()
    {
        return localIp;
    }

    public void setLocalIp(String localIp)
    {
        this.localIp = localIp;
    }

    public String getNetMask()
    {
        return netMask;
    }

    public void setNetMask(String netMask)
    {
        this.netMask = netMask;
    }

    public String getGateWay()
    {
        return gateWay;
    }

    public void setGateWay(String gateWay)
    {
        this.gateWay = gateWay;
    }

    public String getDns()
    {
        return dns;
    }

    public void setDns(String dns)
    {
        this.dns = dns;
    }

    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public String getDid()
    {
        return did;
    }

    public void setDid(String did)
    {
        this.did = did;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public int getCloudType()
    {
        return cloudType;
    }

    public void setCloudType(int cloudType)
    {
        this.cloudType = cloudType;
    }

    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("{rot:").append(rot);
        buf.append(",net:").append(net);
        buf.append(",localip:").append(localIp);
        buf.append(",netmask:").append(netMask);
        buf.append(",gateway:").append(gateWay);
        buf.append(",dns:").append(dns);
        buf.append(",server:").append(server);
        buf.append(",PID:").append(pid);
        buf.append(",did:").append(did);
        buf.append(",version:").append(version).append("}");
        return buf.toString();
    }
}
