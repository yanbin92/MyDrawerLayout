package com.yanbin.mydrawelayout.widgest;

import android.animation.ArgbEvaluator;
import android.animation.TypeEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * 侧滑面板
 * ViewDragHelper
 * 应用场景: 扩展菜单, 更强大的抽屉面板
 * Created by yanbin 2015-12-28
 *
 */
public class MyDrawerLayout extends FrameLayout{
    ViewDragHelper mDragHelper;
    private View mMainView;
    private View mLeftView;
    private int mHeight;
    private int mWidth;
    //拖拽范围
    private int mRange;
    private State state;
    private onDragEventListener onDragEventListener;
    enum State{
        Open,Close,Draging;
    }
    public interface onDragEventListener{
        void onDrag(float fraction);
        void onOpen();
        void onClose();
    }



    public MyDrawerLayout(Context context) {
        this(context, null);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        state=State.Close;
        mDragHelper=ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            /**
             * 根据返回结果, 决定了要不要滑动当前View
             * @param child  当时触摸到的子View
             * @param pointerId 多点触摸, 手指id
             */
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child==mLeftView||child==mMainView;
            }
            //2. 返回拖拽范围, 此时并不影响他能移动到的位置. 返回一个>0的值即可
            @Override
            public int getViewHorizontalDragRange(View child) {
                return mRange;
            }
            /**当View被捕获时，此方法被调用
             */
            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
            }

            //3. 根据建议值left 修正横向的位置, 此时还未发生真正的移动
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                // child : 当前拖拽的View
                // left : 建议值 --> left = oldLeft + dx;
                // dx : 建议值 和 当前oldLeft位置的差值(变化量)
                // Log.d(TAG, "left: " + left + " dx: " + dx + " oldLeft: " + child.getLeft());
                //只有mMainView才发生真正移动
                if(child==mMainView)
                    left = fixLeft(left);
                return left;
            }
            // 4. 处理当View移动之后, 要做的事情(伴随动画)
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                // left : changedView的最新位置
                // dx : 刚刚横向的变化量
//                Log.d("TAG", "onViewPositionChanged: " + " left: " + left + " dx: " + dx);
                //左面板 滑动时将滑动变化量传递给 mMainView
                if(changedView==mLeftView){

                    int newLeft=mMainView.getLeft()+dx;
                    newLeft=fixLeft(newLeft);
                    mMainView.layout(newLeft,0,newLeft+mWidth,mHeight);

                    mLeftView.layout(0,0,mWidth,mHeight);
                }
                dispatchDragEvent(mMainView.getLeft());
                //兼容版本 在 clampViewPositionHorizontal  之后执行onViewPositionChanged
                // 2.3之前被调用后没有invalidate 如果不加下面这句在2.3版本就不会执行
                invalidate();

            }
            // 5. 处理View被释放时, 要做的事情(执行 开启动画\关闭动画)
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                // releasedChild : 被释放的View
                // xvel : 释放时, 横向的速度 向右+ 向左-
                // yvel : 释放时, 纵向的速度 向下+ 向上-
//                Log.d(TAG, "onViewReleased: xvel: " + xvel );
                if(xvel==0&&mMainView.getLeft()>mRange*0.5){
                    open();
                }else if(xvel>0){
                    open();
                }else{
                    close();
                }

            }
        });
    }

    /**
     * 分发拖拽事件 执行动画和回调
     * @param left mMainView的left位置
     */
    private void dispatchDragEvent(int left) {
        float fraction= left*1.0f/mRange;
        //0-->1 百分比
        Log.i("TAG", "fraction" + fraction);
        if (onDragEventListener != null)
            onDragEventListener.onDrag(fraction);
        //执行动画
        animViews(fraction);

        State preState=state;
        state=updateCurrentState(fraction);

        //只有状态变化的时候才执行回调 ！！
        if(preState!=state) {
            if (onDragEventListener != null) {
                if (state == State.Open) {
                    onDragEventListener.onOpen();
                } else if (state == State.Close) {
                    onDragEventListener.onClose();
                }
            }
        }



    }

    private void animViews(float fraction) {
        //        - 左面板 : 平移动画, 缩放动画, 透明度变化
        //        - 主面板 : 缩放动画
        //        - 背景动画 :由暗变亮
        //平移动画 -mWidth/2-->0.0f 使用估值器 TypeEvaluator
        ViewHelper.setTranslationX(mLeftView, evaluate(fraction, -mWidth / 2.0f, 0));
        //缩放动画 0.5f-->1
        ViewHelper.setScaleX(mLeftView, evaluate(fraction, 0.5f, 1));
        ViewHelper.setScaleY(mLeftView, evaluate(fraction, 0.5f, 1));
        //透明度变化 0.5-->1
        ViewHelper.setAlpha(mLeftView, evaluate(fraction, 0.5f, 1));

        // 主面板 : 缩放动画
        ViewHelper.setScaleY(mMainView, evaluate(fraction, 1f, 0.8f));
        ViewHelper.setScaleX(mMainView, evaluate(fraction, 1f, 0.8f));

        // 背景动画 :由暗变亮
        getBackground().setColorFilter((Integer) evaluateColor(fraction, Color.argb(88, 0, 0, 0), Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    private State updateCurrentState(float fraction) {

        if(fraction==1){
            return State.Open;
        }else if(fraction==0){
            return State.Close;
        }else{
            return State.Draging;
        }





    }

    public Object evaluateColor(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int)((startA + (int)(fraction * (endA - startA))) << 24) |
                (int)((startR + (int)(fraction * (endR - startR))) << 16) |
                (int)((startG + (int)(fraction * (endG - startG))) << 8) |
                (int)((startB + (int)(fraction * (endB - startB))));
    }

    public Float evaluate(float fraction, Number startValue, Number endValue) {
        float startFloat = startValue.floatValue();
        return startFloat + fraction * (endValue.floatValue() - startFloat);
    }
    public void close() {
        close(true);
    }

    private void close(boolean isSmooth) {
        if(isSmooth){
            // a. 触发一个平滑的动画开始, 移动到finalLeft, finalTop位置
            if(mDragHelper.smoothSlideViewTo(mMainView,0,0)){
                // 返回true表示, 需要刷新重绘界面, 让刚刚修改的值生效, 传父View
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }else{
            mMainView.layout(0,0,0+mWidth,mHeight);
        }
    }

    /**
     * 是否平滑 移动
     * @param isSmooth
     */
    private void open(boolean isSmooth) {
        if(isSmooth){
            // a. 触发一个平滑的动画开始, 移动到finalLeft, finalTop位置
            if(mDragHelper.smoothSlideViewTo(mMainView,mRange,0)){
                // 返回true表示, 需要刷新重绘界面, 让刚刚修改的值生效, 传父View
                //要让子view移动 只有父布局知道 子孩子的位置啊
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }else{
            mMainView.layout(mRange,0,mRange+mWidth,mHeight);
        }
    }

    @Override
    public void computeScroll() {
        //true if animation should continue
        if(mDragHelper.continueSettling(true)){
            // 返回true表示, 需要刷新重绘界面, 让刚刚修改的值生效, 传父View
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void open() {
        open(true);
    }

    /**
     *修正 left
     */
    private int fixLeft(int left) {
        if (left > mRange) {
            left = mRange;
        } else if (left < 0) {
            left = 0;
        }
        return left;
    }

    //模版代码 ViewDragHelper初始化
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth=getMeasuredWidth();
        mHeight=getMeasuredHeight();
        mRange= (int) (0.7*mWidth);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //初始化位置

    }

    /**
     *  当xml被填充成View, 所有子View都被添加后, 此方法会被调用
     * 当结束绘制时调用 可以用于获取子孩子
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //数据有效性校验 子孩子必须是ViewGroup的子类
        int childCount=getChildCount();
        if(childCount<2){
            throw new IllegalStateException("at least has two child");
        }
        if(!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)){
            throw new IllegalArgumentException("child must be extends ViewGroup");
        }
        mLeftView=getChildAt(0);
        mMainView=getChildAt(1);

    }








    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public MyDrawerLayout.onDragEventListener getOnDragEventListener() {
        return onDragEventListener;
    }

    public void setOnDragEventListener(MyDrawerLayout.onDragEventListener onDragEventListener) {
        this.onDragEventListener = onDragEventListener;
    }
}
