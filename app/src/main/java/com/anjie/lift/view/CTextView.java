package com.anjie.lift.view;

import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

public abstract class CTextView extends BaseView
{
    /**
     * 字体颜色
     */
    private int textColor;

    /**
     * 字体大小
     */
    private int textSize;

    /**
     * 对齐方式
     */
    private String textAlign;

    /**
     * 显示最大显示行数
     */
    private int maxLine;

    private String text;

    protected CTextView(ViewType viewType)
    {
        super(viewType);
    }

    public int getMaxLine()
    {
        return maxLine;
    }

    public void setMaxLine(int maxLine)
    {
        this.maxLine = maxLine;
    }

    public int getTextColor()
    {
        return textColor;
    }

    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
    }

    public int getTextSize()
    {
        return textSize;
    }

    public void setTextSize(int textSize)
    {
        this.textSize = textSize;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }


    public int getGravity()
    {
        if (TextUtils.isEmpty(textAlign))
        {
            return -1;
        }
        if (textAlign.equalsIgnoreCase("left"))
        {
            return Gravity.LEFT;
        }
        else if (textAlign.equalsIgnoreCase("center"))
        {
            return Gravity.CENTER;
        }
        else if (textAlign.equalsIgnoreCase("right"))
        {
            return Gravity.RIGHT;
        }
        return -1;
    }

    public void setTextAlign(String textAlign)
    {
        this.textAlign = textAlign;
    }

    /**
     * 更新位置布局信息
     * 
     * @param textView
     */
    public void updateTextViewPosition(TextView textView)
    {
        if (textView == null)
        {
            return;
        }
        textView.setX(getX());
        textView.setY(getY());
        textView.setWidth(getWidth());
        textView.setHeight(getHeight());
    }

    /**
     * 更新位置布局信息
     *
     * @param textView
     */
    public void updateTextViewPosition_new(TextView textView, int delta)
    {
        if (textView == null)
        {
            return;
        }

        textView.setX(getX()+delta);
        textView.setY(getY());
        textView.setWidth(getWidth());
        textView.setHeight(getHeight());
    }
    /**
     * 更新字体信息
     * 
     * @param textView
     */
    public void updateTextInfo(TextView textView)
    {
        if (textView == null)
        {
            return;
        }

        // 设置文本内容
        if (!TextUtils.isEmpty(text))
        {
            textView.setText(text);
        }

        // 设置字体大小
        if (textSize > 0)
        {
            textView.setTextSize(textSize);
        }
        // 设置字体颜色
        if (textColor != 0)
        {
            textView.setTextColor(textColor);
        }
        int gravity = getGravity();
        if (gravity >= 0)
        {
            // 对齐方式
            textView.setGravity(gravity);
        }

        // 显示行数
        if (maxLine == 1)
        {
            textView.setSingleLine(true);
        }
        else if (maxLine > 1)
        {
            textView.setMaxLines(maxLine);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" textColor:" + textColor);
        builder.append(" textSize:" + textSize);
        builder.append(" textAlign:" + textAlign);
        builder.append(" text:" + text);
        return builder.toString();
    }
}
