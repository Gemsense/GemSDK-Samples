package com.gemsense.basicdemo.render;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.gemsense.gemsdk.utils.QuatUtils;

/**
 * Created by Shtutman on 28.05.2015.
 */
public class CubeView extends GLSurfaceView {
    private CubeRenderer renderer;

    public void setOrientation(float[] quat) {
        renderer.setOrientationMatrix(QuatUtils.toMatrixRH(quat));
    }

    public CubeView(Context context) {
        this(context, null);
    }

    public CubeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(1);
        renderer = new CubeRenderer();
        setRenderer(renderer);
    }
}
