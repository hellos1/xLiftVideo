package com.anjie.lift.view;

public class FloorView extends CTextView
{
    public FloorView()
    {
        super(ViewType.Floor);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        return builder.toString();
    }
}
