package com.gemsense.gesturedemo.drawingview;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.gemsense.gemsdk.algorithms.QuaternionProjector;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by Shtutman on 19.08.2015.
 */
public class OpenGlGestureView extends GLSurfaceView {
    private final static int capacity = 100;
    private PointStripRenderer renderer;

    private QuaternionProjector qProjector;
    private Deque<PointF> points;

    public OpenGlGestureView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(!isInEditMode()) {
            this.points = new ArrayDeque<>();
            this.qProjector = new QuaternionProjector(200f);

            setEGLContextClientVersion(1);
            renderer = new PointStripRenderer(100);
            setRenderer(renderer);
        }
    }

    public void addNext(float[] quaternion) {
        float[] xy = qProjector.projectOnSphere(quaternion);
        points.addLast(new PointF(xy[0], xy[1]));

        if (points.size() > capacity) points.removeFirst();

        float[] pointData = new float[points.size() * 3];
        int i = 0;
        for (PointF p : points) {
            pointData[i * 3] = p.x / 250f;
            pointData[i * 3 + 1] = p.y / 250f;
            pointData[i * 3 + 2] = -1f;
            i++;
        }

        renderer.updateData(pointData);
    }

    public void reset() {
        points.clear();
    }
}
