package com.anjie.lift.service.adapter.best;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

/**
 * 贝斯特配置信息
 */
public class BestConfigInfo
{
    /**
     * MAC地址
     */
    private String macAddress = "12-23-AB-CD-34-25";

    /**
     * 产品编号
     */
    private String productId = "c81c3a1a628f4608aa9722fc74ddb028"; //"6c80f868b70e408aabd91987004fb700";

    /**
     * 构造函数
     */
    public BestConfigInfo(String macAddress, String productId)
    {
        this.macAddress = macAddress;
        this.productId = productId;
    }

    private String getMac() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/eth0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial.toUpperCase();
    }

    public BestConfigInfo()
    {
        this.macAddress = getMac();
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public String getProductId()
    {
        return productId;
    }
}
