package com.gemsense.basicdemo.render;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Shtutman on 28.05.2015.
 */
public class AccelRenderer implements GLSurfaceView.Renderer {
    private volatile float[] orientation = new float[16];
    private volatile float[] acceleration = new float[3];

    private Line accLines[] = new Line[] {
            new Line(new float[] {0.8f,   0f,   0f, 1f}),
            new Line(new float[] {  0f, 0.8f,   0f, 1f}),
            new Line(new float[] {  0f,   0f, 0.8f, 1f})
    };

    public void setOrientationMatrix(float[] mat) {
        orientation = mat;
    }

    public void setAcceleration(float[] acc) {
        acceleration = acc;
    }

    public AccelRenderer() {
        Matrix.setIdentityM(orientation, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        gl.glClearColor(0.93f, 0.93f, 0.93f, 1f); //Material light background
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        gl.glEnable(GL10.GL_SMOOTH);
        gl.glDisable(GL10.GL_DITHER);

        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLineWidth(5f);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float[] proj = new float[16];
        Matrix.orthoM(proj, 0, -1f, 1f, -1f, 1f, 0f, 50f);
        gl.glMultMatrixf(proj, 0);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        gl.glPushMatrix();
        gl.glScalef(0.4f, 1f, 1f);

        //Acceleration values
        for (int i = 0; i < 3; i++) {
            drawAxis(gl, i);
        }

        gl.glPopMatrix();
    }

    void drawAxis(GL10 gl, int num) {
        float[] axisAcc = new float[3];
        axisAcc[0] = acceleration[num];

        gl.glPushMatrix();
            gl.glTranslatef(0f, (1 - num) * 0.666f, 0f);
            gl.glScalef(axisAcc[0], axisAcc[1], axisAcc[2]);
            accLines[num].draw(gl);
        gl.glPopMatrix();
    }
}
