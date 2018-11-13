package com.anjie.lift.view;

public class Position
{
    private int x;

    private int y;

    private int width;

    private int height;

    public Position(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    public int getX()
    {
        return x;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getY()
    {
        return y;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append("x:").append(x).append(",");
        builder.append("y:").append(y).append(",");
        builder.append("w:").append(width).append(",");
        builder.append("h:").append(height);
        builder.append("]");
        return builder.toString();
    }
}
