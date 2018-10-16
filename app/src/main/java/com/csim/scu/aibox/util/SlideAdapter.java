package com.csim.scu.aibox.util;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.csim.scu.aibox.R;

/**
 * Created by kenny on 2018/10/8.
 */

public class SlideAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;
    private int[] slidePictures = {R.drawable.introduce_1, R.drawable.introduce_2};

    public SlideAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return slidePictures.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (RelativeLayout)object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);
        RelativeLayout slide = view.findViewById(R.id.slide);
        slide.setBackgroundResource(slidePictures[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout)object);
    }
}
