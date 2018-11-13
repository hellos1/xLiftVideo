package com.anjie.lift.view;

public abstract class BaseView
{
    public enum ViewType
    {
        Background, Timer, Date, Arrows, Floor, CusText, CusImage, Player, ADText, Status, Title
    }

    private Position position;

    private ViewType viewType;

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    protected BaseView(ViewType viewType)
    {
        this.viewType = viewType;
    }

    public ViewType getViewType()
    {
        return viewType;
    }

    public void setViewType(ViewType viewType)
    {
        this.viewType = viewType;
    }

    public int getX()
    {
        if (position != null)
        {
            return position.getX();
        }
        return 0;
    }

    public int getY()
    {
        if (position != null)
        {
            return position.getY();
        }
        return 0;
    }

    public int getHeight()
    {
        if (position != null)
        {
            return position.getHeight();
        }
        return 0;
    }

    public int getWidth()
    {
        if (position != null)
        {
            return position.getWidth();
        }
        return 0;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("viewType:" + viewType);
        builder.append(" Position:").append(position);
        return builder.toString();
    }
}
