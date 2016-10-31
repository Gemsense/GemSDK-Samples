package com.gemsense.basicdemo.render;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Shtutman on 28.05.2015.
 */
public class CubeRenderer implements GLSurfaceView.Renderer {
    private volatile float[] orientation = new float[16];
    private Axes axes = new Axes();
    private Cube cube = new Cube();

    public void setOrientationMatrix(float[] mat) {
        orientation = mat;
    }

    public CubeRenderer() {
        Matrix.setIdentityM(orientation, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        gl.glClearColor(0.93f, 0.93f, 0.93f, 1f); //Material light background

        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_DITHER);

        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLineWidth(5f);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float[] proj = new float[16];
        Matrix.perspectiveM(proj, 0, 45f, (float) w / (float) h, 0.1f, 50f);
        gl.glMultMatrixf(proj, 0);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();

        float[] lookAt = new float[16];
        Matrix.setLookAtM(lookAt, 0, 0f, 2f, -2f, 0f, 0f, 0f, 0f, 1f, 0f);

        //Apply camera
        gl.glMultMatrixf(lookAt, 0);
        //Change X axis direction to fit global coordinate system with gem coordinate system
        gl.glScalef(-1f, 1f, 1f);

        //Draw global axes
        gl.glPushMatrix();
             gl.glScalef(2f, 2f, 2f);
             axes.draw(gl);
        gl.glPopMatrix();

        //Transpose matrix cause raw OpenGL uses column-major order for matrices
        float[] orientationTransposed = new float[16];
        Matrix.transposeM(orientationTransposed, 0, orientation, 0);

        //Apply Gem rotation
        gl.glMultMatrixf(orientationTransposed, 0);

        //Draw local axes
        axes.draw(gl);
        //Draw cube
        cube.draw(gl);
    }
}
