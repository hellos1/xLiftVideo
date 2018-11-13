package com.anjie.lift.view;

public class BGView extends BaseView
{
    private int bgColor;

    private String bgPath;

    public BGView()
    {
        super(ViewType.Background);
    }

    public int getBackgroundColor()
    {
        return bgColor;
    }

    public void setBackgroundColor(int color)
    {
        this.bgColor = color;
    }

    public String getBackgroundPath()
    {
        return bgPath;
    }

    public void setBackground(String bgPath)
    {
        this.bgPath = bgPath;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" bgColor:").append(bgColor);
        builder.append(" bgPath:").append(bgPath);
        return builder.toString();
    }
}
