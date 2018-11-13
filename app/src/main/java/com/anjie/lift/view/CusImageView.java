package com.anjie.lift.view;

public class CusImageView extends BaseView
{
    private String imagePath;

    public CusImageView()
    {
        super(ViewType.CusImage);
    }

    protected CusImageView(ViewType viewType)
    {
        super(viewType);
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public void setImagePath(String sourcePath)
    {
        this.imagePath = sourcePath;
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
