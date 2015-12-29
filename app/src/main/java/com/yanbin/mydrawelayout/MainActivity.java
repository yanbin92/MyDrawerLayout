package com.yanbin.mydrawelayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.yanbin.mydrawelayout.widgest.MyDrawerLayout;
import com.yanbin.mydrawelayout.widgest.MyMainLinearLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        final ImageView iv_head= (ImageView) findViewById(R.id.iv_head);
        MyMainLinearLayout mmll= (MyMainLinearLayout) findViewById(R.id.mmll);

        MyDrawerLayout mDrawerLayout= (MyDrawerLayout) findViewById(R.id.mDrawerLayout);
        mmll.setDrawerLayout(mDrawerLayout);

        final   ListView lv_main= (ListView) findViewById(R.id.lv_main);
        final   ListView lv_left= (ListView) findViewById(R.id.lv_left);

        mDrawerLayout.setOnDragEventListener(new MyDrawerLayout.onDragEventListener() {
            @Override
            public void onDrag(float fraction) {
                //更改图片的透明度  0-->1  >>  1-0
                ViewHelper.setAlpha(iv_head,(1-fraction));

            }

            @Override
            public void onOpen() {
                Random random=new Random();
                lv_left.smoothScrollToPosition(random.nextInt(50));
            }

            @Override
            public void onClose() {
                //左右晃动 ImageView
//                iv_head.setTranslationX();
                ObjectAnimator anim = ObjectAnimator.ofFloat(iv_head, "translationX", 0f, 15f)
                        .setDuration(500);
                anim.setInterpolator(new CycleInterpolator(4));
                anim .start();


            }
        });

        lv_left.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,Cheeses.sCheeseStrings){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view=super.getView(position, convertView, parent);

                TextView textView= (TextView) view;
                textView.setTextColor(Color.WHITE);
                return view;
            }
        });

        lv_main.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Cheeses.NAMES));
    }
}
