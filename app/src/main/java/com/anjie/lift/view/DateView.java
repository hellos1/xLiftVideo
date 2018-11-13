package com.anjie.lift.view;

/**
 * 时间视图
 */
public class DateView extends CTextView
{
    /**
     * 时间显示格式
     */
    private String format;

    /**
     * 时间视图
     */
    public DateView()
    {
        super(ViewType.Date);
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" format:").append(format);
        return builder.toString();
    }
}
