package com.anjie.lift.service.adapter.best;

import android.text.TextUtils;

import com.anjie.common.log.LogX;

import org.json.JSONObject;

/**
 * Best的Resource Item
 */
public class BestResItem
{
    /**
     * 资源的类型
     */
    private ResourceType type;

    /**
     * 资源的内容
     */
    private String content;

    /**
     * 资源类型定义
     */
    public enum ResourceType
    {
        // backgroud 不要修改,服务器下发的单词就是这个 (音频)
        //2018.05.03 背景音乐改为audio
        Title, ScrollText, Picture, Video, Backgroud, URL, audio
    }

    public ResourceType getType()
    {
        return type;
    }

    public void setType(ResourceType type)
    {
        this.type = type;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    /**
     * 通过JSONObject构建BestResItem数据
     * 
     * @param jsonObj
     * @return
     */
    public static BestResItem getBestResItem(JSONObject jsonObj)
    {
        if (jsonObj == null)
        {
            return null;
        }

        BestResItem item = null;
        //2018.05.03 音乐的catalogtype为空，但是catalog为audio
        String type = jsonObj.optString("catalogtype", null);
        String content = jsonObj.optString("url", null);
        String audioType = jsonObj.optString("catalog", null);


        for (ResourceType resType : ResourceType.values())
        {
            //2018.05.10 判断条件增加resType.toString().equalsIgnoreCase(audioType)，以适配将catalogtype改为空的背景音乐
            if (resType.toString().equalsIgnoreCase(type) || resType.toString().equalsIgnoreCase(audioType))
            {
                item = new BestResItem();
                item.setType(resType);
                //2018.05.03  content加上.trim()兼容带空格的文件名
                item.setContent(content.trim());
                break;
            }
        }
        LogX.i("BestCloud", "This type is " + type + ",match item:" + item);
        return item;
    }

    public String toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            // TODO 枚举类型转换JSON
            jsonObject.put("type", type);
            jsonObject.put("content", content);
        }
        catch (Exception e)
        {
            return null;
        }
        return jsonObject.toString();
    }

    @Override
    public String toString()
    {
        return "{type:" + type + ",url:" + content + "}";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final BestResItem other = (BestResItem) obj;
        if (content == null)
        {
            return false;
        }
        if (content.equals(other.content))
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (TextUtils.isEmpty(content))
        {
            return 0;
        }
        return content.hashCode();
    }
}
