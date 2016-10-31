package com.gemsense.basicdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Shtutman on 02.11.2015.
 */
public class PedometerDataView extends LinearLayout {
    private static float AVG_STEP_LENGTH = 0.762f; //meters

    private int steps;
    private float walkTime;
    private float distance;
    private float avgSpeed;

    private TextView stepsText;
    private TextView walkTimeText;
    private TextView avgSpeedText;
    private TextView distanceText;

    public PedometerDataView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pedometer_data_view, this, true);

        stepsText = (TextView) findViewById(R.id.steps_count);
        walkTimeText = (TextView) findViewById(R.id.walk_time);
        avgSpeedText = (TextView) findViewById(R.id.avg_speed);
        distanceText = (TextView) findViewById(R.id.distance);

        if(!isInEditMode()) {
            setStepsCount(0);
            setWalkTime(0);
        }
    }

    public PedometerDataView(Context context) {
        this(context, null);
    }

    public void setStepsCount(int steps) {
        this.steps = steps;
        distance = steps * AVG_STEP_LENGTH;

        stepsText.setText(String.valueOf(steps));
        distanceText.setText(String.format("%d m", (int)distance));
    }

    public void setWalkTime(float time) {
        this.walkTime = time;

        int seconds = (int)(time % 60);
        int minutes = (int)time / 60;
        int hours = (int)time / 3600;

        String res = "Undefined";

        if(hours > 0) {
            res = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        else {
            res = String.format("%02d:%02d", minutes, seconds);
        }

        walkTimeText.setText(res);

        avgSpeed = time > 0f  ? distance / time * 3.6f : 0f;
        avgSpeedText.setText(String.format("%.1f km/h", avgSpeed));
    }
}
