package com.csim.scu.aibox.view.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.util.SlideAdapter;

public class IntroduceActivity extends AppCompatActivity {
    private ViewPager introducePager;
    private SlideAdapter slideAdapter;
    int currentPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduce);
        introducePager = findViewById(R.id.introducePager);
        slideAdapter = new SlideAdapter(this);
        introducePager.setAdapter(slideAdapter);
        introducePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        set();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void set() {
        introducePager.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            float startY;
            float endX;
            float endY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startX=event.getX();
                        startY=event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        endX=event.getX();
                        endY=event.getY();
                        WindowManager windowManager= (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                        Point size = new Point();
                        windowManager.getDefaultDisplay().getSize(size);
                        int width = size.x;
                        if(currentPosition == 1 && startX - endX > 0 && startX - endX >= (width / 4)){
                            currentPosition = 0;
                            SharedPreferences pref = getSharedPreferences("preferences", MODE_PRIVATE);
                            pref.edit().putBoolean("first", false).apply();
                            Intent intent = new Intent(IntroduceActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        break;
                }
                return false;
            }

        });


    }
}
