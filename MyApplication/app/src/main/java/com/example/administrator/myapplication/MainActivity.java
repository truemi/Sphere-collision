package com.example.administrator.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.administrator.myapplication.widget.PoolBallView;

public class MainActivity extends AppCompatActivity {

    private Sensor        mDefaultSensor;
    private SensorManager mSensorManager;
    private PoolBallView  poolBall;
    private int[]               imgs      = {R.mipmap.i_01, R.mipmap.i_02, R.mipmap.i_03,R.mipmap.i_04, R.mipmap.i_05, R.mipmap.i_06};
    private SensorEventListener listerner = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                int x = (int) event.values[0];
                int y = (int) (event.values[1] * 2.0f);
                if (lastX != x || lastY != y) {//防止频繁回调,画面抖动
                    poolBall.getBallView().rockBallByImpulse(-x, y);
                    Log.e("陀螺仪 ", x + "<----陀螺仪Y: " + y + "<-----");
                }

                lastX = x;
                lastY = y;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    private int lastX;
    private int lastY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        poolBall = (PoolBallView) findViewById(R.id.pool_ball);
        Button btn = (Button) findViewById(R.id.btn);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mDefaultSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("btn", "点击了");
                poolBall.getBallView().rockBallByImpulse();
            }
        });
        init();
    }

    private void init() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        for (int i = 0; i < imgs.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(imgs[i]);
            imageView.setTag(R.id.circle_tag, true);
            poolBall.addView(imageView, layoutParams);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "点击了气泡", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        poolBall.getBallView().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        poolBall.getBallView().onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(listerner, mDefaultSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(listerner);
    }


}
