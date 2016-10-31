package com.gemsense.twogemsdemo.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.gemsense.gemsdk.utils.QuatUtils;

/**
 * Created by Shtutman on 28.05.2015.
 */
public class AccelView extends GLSurfaceView {
    private AccelRenderer renderer;

    public void setAcceleration(float[] acc) {
        renderer.setAcceleration(acc);
    }

    public void setOrientation(float[] quat) {
        renderer.setOrientationMatrix(QuatUtils.toMatrixRH(quat));
    }

    public AccelView(Context context) {
        this(context, null);
    }

    public AccelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(1);
        renderer = new AccelRenderer();
        setRenderer(renderer);
    }
}
