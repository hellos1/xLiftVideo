package com.anjie.lift.player;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * 播放元素
 * 
 */
public class PlayerElement implements Parcelable
{

    public enum ElementType
    {
        video, image, audio, url
    }

    /**
     * 播放元素类型
     */
    private ElementType mType;

    /**
     * 播放文件路径
     */
    private String filePath;

    /**
     * 完整路径
     */
    private String fullPath;

    /**
     * 播放图片时间(秒);
     */
    private int playImageTime;

    /**
     * 播放元素类型
     * 
     * @param type
     */
    public PlayerElement(ElementType type)
    {
        this.mType = type;
    }

    public void setFilePath(String path)
    {
        this.filePath = path;
    }

    public String getFilePath()
    {
        return filePath;
    }

    public String getFullPath()
    {
        return fullPath;
    }

    public void setFullPath(String fullPath)
    {
        this.fullPath = fullPath;
    }

    public ElementType getType()
    {
        return mType;
    }

    public void setImageShowTime(int seconds)
    {
        this.playImageTime = seconds;
    }

    public int getImageShowTime()
    {
        return playImageTime;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        if (mType == ElementType.image)
        {
            builder.append("[image:").append(filePath).append(",playImageTime:").append(playImageTime).append("]");
        }
        else if (mType == ElementType.video)
        {
            builder.append("[video:").append(filePath).append("]");
        }
        else if (mType == ElementType.audio)
        {
            builder.append("[audio:").append(filePath).append("]");
        }
        else if (mType == ElementType.url)
        {
            builder.append("[url:").append(filePath).append("]");
        }
        else
        {
            builder.append("[unknow:").append(filePath).append("]");
        }
        return builder.toString();
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
        final PlayerElement other = (PlayerElement) obj;
        if (filePath == null)
        {
            return false;
        }
        if (filePath.equals(other.filePath))
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (TextUtils.isEmpty(filePath))
        {
            return 0;
        }
        return filePath.hashCode();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(filePath);
        dest.writeString(fullPath);
        dest.writeInt(playImageTime);
        switch (mType){
            case video:
                dest.writeInt(1);
                break;
            case image:
                dest.writeInt(2);
                break;
            case audio:
                dest.writeInt(3);
                break;
            case url:
                dest.writeInt(4);
                break;
            default:
                dest.writeInt(0);
                break;

        }
    }

    private PlayerElement(Parcel parcel)
    {
        filePath = parcel.readString();
        fullPath = parcel.readString();
        playImageTime = parcel.readInt();

        int type = parcel.readInt();
        if (type == 1)
        {
            mType = ElementType.video;
        }
        else if (type == 2)
        {
            mType = ElementType.image;
        }
        else if (type == 3)
        {
            mType = ElementType.audio;
        }
        else if (type == 4)
        {
            mType = ElementType.url;
        }
    }
    public static final Parcelable.Creator<PlayerElement> CREATOR = new Parcelable.Creator<PlayerElement>()
    {
        public PlayerElement createFromParcel(Parcel in)
        {
            return new PlayerElement(in);
        }

        public PlayerElement[] newArray(int size)
        {
            return new PlayerElement[size];
        }
    };
}
