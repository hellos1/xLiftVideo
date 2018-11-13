package com.anjie.elevator.view;

import android.support.annotation.NonNull;

/**
 * 基础布局
 */
public abstract class XBaseView implements Comparable<XBaseView>{
    public enum ViewType{
        Time,
    }
    private int left;
    private int top;
    private int right;
    private int bottom;

    private int zorder;

    @Override
    public int compareTo(@NonNull XBaseView another) {
        return 0;
    }
}
