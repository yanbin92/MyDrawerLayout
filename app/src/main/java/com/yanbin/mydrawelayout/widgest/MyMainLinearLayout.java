package com.yanbin.mydrawelayout.widgest;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 用于处理事件拦截的父容器
 * 用于实现 在关闭状态下主界面处理Touch事件 其他不处理
 * 重要的是解决冲突的思想 自定义有冲突事件的容器的父布局 根据drag状态 决定主界面是否响应触摸事件
 * <p/>
 * Created by yanbin on 2015/12/29.
 */
public class MyMainLinearLayout extends LinearLayout {
    private MyDrawerLayout drawerLayout;

    public MyMainLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyMainLinearLayout(Context context) {
        super(context);
    }

   public void setDrawerLayout(MyDrawerLayout layout) {
        this.drawerLayout = layout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //在打开状态下主界面不处理Touch事件

        //如果当前是关闭状态 该怎么处理就怎么处理
        if (drawerLayout != null && drawerLayout.getState() == MyDrawerLayout.State.Close) {
            return super.onInterceptTouchEvent(ev);
        } else
            return true;//拦截
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //如果当前是关闭状态 该怎么处理就怎么处理
        if (drawerLayout != null && drawerLayout.getState() == MyDrawerLayout.State.Close) {
            //如果当前是关闭状态 该怎么处理就怎么处理
            return super.onTouchEvent(event);
        } else {
            //up事件中执行关闭

            if(event.getAction()==MotionEvent.ACTION_UP){
                if (drawerLayout != null)
                     drawerLayout.close();
            }
            return true;
        }
    }
}
