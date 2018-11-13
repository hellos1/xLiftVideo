package com.anjie.elevator.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.AbsoluteLayout;
import android.widget.TextView;

import com.anjie.elevator.center.XControlCenter;
import com.anjie.elevator.view.XBaseView;
import com.anjie.lift.R;

import java.util.ArrayList;
import java.util.List;

public class MainScreenActivity extends Activity{

    /**
     * Root相对布局
     */
    private AbsoluteLayout rootLayout = null;

    /**
     * UI线程操作
     */
    private Handler mUIHandler;

    /**
     * 当前所有视图的列表
     */
    private List<XBaseView> currentViewList = new ArrayList<>();

    /**
     * 标题
     */
    private TextView titleTextView;

    private class UIRunnable implements Runnable {
        private int msg;

        public UIRunnable() {

        }

        @Override
        public void run() {

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        XControlCenter.getInstance().init();
        // 构建UI的Handler操作界面
        mUIHandler = new Handler();
        //
        initScreen();

        XControlCenter.getInstance().init();
    }

    /**
     * 出初始化屏幕
     */
    private void initScreen() {
        // 获取根布局
        rootLayout = (AbsoluteLayout) findViewById(R.id.root_layout);
        rootLayout.removeAllViews();
    }
}
