package com.example.administrator.myapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2018-05-17.
 */

public class PoolBallView extends FrameLayout {
    public Context mcontext;
    private BallView ballView;

    public PoolBallView(Context context) {
        this(context,null);
    }

    public PoolBallView(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public PoolBallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);//重写ondraw需要
        this.mcontext = context;
        ballView = new BallView(context, this);


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        ballView.onLayout(changed);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ballView.onDraw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ballView.onSizeChanged(w,h);
    }

    public BallView getBallView(){
        return this.ballView;
    }
}
