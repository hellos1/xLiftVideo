package com.anjie.lift.view;

public class ArrowsView extends BaseView
{
    private String imagePath;

    public ArrowsView()
    {
        super(ViewType.Arrows);
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(" imagePath:").append(imagePath);
        return builder.toString();
    }
}
