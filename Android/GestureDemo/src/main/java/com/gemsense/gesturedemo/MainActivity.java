package com.gemsense.gesturedemo;

import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.gemsense.common.GemSensorsData;
import com.gemsense.gemsdk.Gem;
import com.gemsense.gemsdk.GemAbstractListener;
import com.gemsense.gemsdk.GemListener;
import com.gemsense.gemsdk.GemManager;
import com.gemsense.gemsdk.GemSDKUtilityApp;
import com.gemsense.gemsdk.OnSensorsAbstractListener;
import com.gemsense.gemsdk.gesture.OnGestureListener;
import com.gemsense.gesturedemo.drawingview.OpenGlGestureView;
import com.gemsense.gemsdk.gesture.Gesture;
import com.gemsense.gemsdk.gesture.GesturePoint;
import com.gemsense.gemsdk.gesture.GestureScore;
import com.gemsense.gemsdk.gesture.StreamGestureManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private StreamGestureManager gestureManager;
    private Gem gem;
    private OpenGlGestureView drawingView;
    private MediaPlayer recognitionSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gestureManager = new StreamGestureManager(this);

        //Generate sample gestures
        initCircleGesture();
        initSwirlGesture();
        initPigtailGesture();

        //Load gesture recognition sound
        recognitionSound = MediaPlayer.create(this, R.raw.success_sound);

        //Initialize drawing visualization
        drawingView = (OpenGlGestureView)findViewById(R.id.drawing_view);

        findViewById(R.id.drawing_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gem.calibrateOrigin();
            }
        });

        //Configure gesture recognition callback
        gestureManager.setOnGestureListener(new OnGestureListener() {
            @Override
            public void onGesture(ArrayList<GestureScore> scores) {
                if(scores.size() > 0) {
                    GestureScore score = scores.get(0);

                    //Choose target view depending on recognized gesture name
                    int imageId = -1;
                    switch (score.name) {
                        case "Circle" : imageId = R.id.image1; break;
                        case "Swirl" : imageId = R.id.image2; break;
                        case "Pigtail" : imageId = R.id.image3; break;
                    }

                    //Play animation
                    TransitionDrawable tr =
                            (TransitionDrawable)getResources().getDrawable(R.drawable.gesture_highlight);

                    if(tr != null) {
                        findViewById(imageId).setBackground(tr);
                        tr.startTransition(800);
                    }

                    //Play sound
                    recognitionSound.start();

                    //Center pointer and clear visualization view
                    gem.calibrate();
                    drawingView.reset();

                    Log.i("GestureDemo", "Gesture recognized: " + score.name + " score: " + score.score);
                }
                else {
                    Log.i("GestureDemo", "Gesture not recognized");
                }
            }
        });
    }

    private void initGem(String address) {
        //Get a gem
        gem = GemManager.getDefault().getGem(address, new GemAbstractListener() {
            @Override
            public void onErrorOccurred(int i) {
                Toast.makeText(MainActivity.this, "Can't find a gem", Toast.LENGTH_SHORT).show();
            }
        });

        gem.setSensorsListener(new OnSensorsAbstractListener() {
            @Override
            public void onSensorsChanged(GemSensorsData gemSensorsData) {
                gestureManager.nextSample(gemSensorsData.quaternion, gemSensorsData.acceleration);
                drawingView.addNext(gemSensorsData.quaternion);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        GemManager.getDefault().unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Bind gem service to the application (GemSense software must be installed)
        GemManager.getDefault().bindService(this);

        //Get list of addresses from the utility app
        String[] whitelist = GemSDKUtilityApp.getWhiteList(this);

        if(whitelist.length > 0) {
            //If white list has been changed disconnect old one
            if(gem != null && !whitelist[0].equals(gem.getAddress())) {
                GemManager.getDefault().releaseGem(gem);
            }

            //It's possible to call it in OnCreate() if white list is not supposed to be changed
            initGem(whitelist[0]);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btn_reconnect) {
            gem.reconnect();
        }

        return super.onOptionsItemSelected(item);
    }


    //GENERATE GESTURE DATA

    private void  initCircleGesture() {
        GesturePoint[] circle_data = new GesturePoint[16];
        for (int i = 0; i < circle_data.length; i++) {
            float angle = (float)Math.PI * 2f / (circle_data.length - 1) * i;
            circle_data[i] = new GesturePoint(-(float)Math.cos(angle), (float)Math.sin(angle));
        }

        Gesture circle = new Gesture(circle_data);
        gestureManager.addGesture("Circle", circle);

        ((ImageView)findViewById(R.id.image1)).setImageBitmap(circle.toBitmap(250, 250, 3, Color.GRAY));
    }

    private void initSwirlGesture() {
        GesturePoint[] swirl_data = new GesturePoint[32];
        float swirl_factor = 0.1f;
        float swirl_rotations = 1.5f;
        for (int i = 0; i < swirl_data.length; i++) {
            float angle = (float)Math.PI * 2f / (swirl_data.length - 1) * swirl_rotations * i;
            float radius = 1f - (float)i / swirl_data.length * (1f - swirl_factor) + swirl_factor;

            swirl_data[i] = new GesturePoint(-(float)Math.cos(angle) * radius,
                    (float)Math.sin(angle) * radius);
        }

        Gesture swirl = new Gesture(swirl_data);
        gestureManager.addGesture("Swirl", swirl);

        ((ImageView)findViewById(R.id.image2)).setImageBitmap(swirl.toBitmap(250, 250, 3, Color.GRAY));
    }

    private void initPigtailGesture() {
        GesturePoint[] pigtail_data = new GesturePoint[32];

        float pigtail_t_start = -2f;
        float pigtail_t_end = 2f;

        for (int i = 0; i < pigtail_data.length; i++) {
            float t = pigtail_t_start + (pigtail_t_end - pigtail_t_start) /
                    (pigtail_data.length - 1) * i;

            pigtail_data[i] = new GesturePoint(
                    t*(t*t - 1f) / (3f*t*t + 1f), (t*t - 1f) / (3f*t*t + 1)
            );
        }

        Gesture pigtail = new Gesture(pigtail_data);
        gestureManager.addGesture("Pigtail", pigtail);

        ((ImageView)findViewById(R.id.image3)).setImageBitmap(pigtail.toBitmap(250, 250, 3, Color.GRAY));

    }
}
