package com.gemsense.gesturedemo.drawingview;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *
 */
public class PointStripRenderer implements GLSurfaceView.Renderer {
    private FloatBuffer vBuffer;
    private FloatBuffer cBuffer;
    private int currentLength = 0;

    public void updateData(float[] pointData) {
        currentLength = pointData.length / 3;

        vBuffer.clear();
        vBuffer.put(pointData);
        vBuffer.position(0);

        float[] colorData = new float[pointData.length / 3 * 4];
        for (int i = 0; i < colorData.length; i++) {
            if(((i + 1) % 4) == 0) {
                //Alpha channel
                colorData[i] = 0.8f / colorData.length * i;
            }
            else {
                //RGB channels
                colorData[i] = 0f;
            }
        }
        cBuffer.clear();
        cBuffer.put(colorData);
        cBuffer.position(0);
    }

    public PointStripRenderer(int stripLength) {
        ByteBuffer bytBuf= ByteBuffer.allocateDirect(stripLength * 3 * 4);
        bytBuf.order(ByteOrder.nativeOrder());
        vBuffer = bytBuf.asFloatBuffer();

        bytBuf= ByteBuffer.allocateDirect(stripLength * 4 * 4);
        bytBuf.order(ByteOrder.nativeOrder());
        cBuffer = bytBuf.asFloatBuffer();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig eglConfig) {
        gl.glClearColor(1f, 1f, 1f, 1f);

//        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_SMOOTH);
        //gl.glEnable(GL10.GL_LINE_SMOOTH);
        gl.glEnable(GL10.GL_BLEND);
        gl.glDisable(GL10.GL_DITHER);

        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLineWidth(4f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        float[] proj = new float[16];
        float factor = (float)w / (float)h;

        if (factor > 1f) {
            Matrix.orthoM(proj, 0, -factor, factor, -1f, 1f, 0.1f, 50f);
        }
        else {
            Matrix.orthoM(proj, 0, -1f, 1f, -factor, factor, 0.1f, 50f);
        }

        gl.glMultMatrixf(proj, 0);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);// | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, cBuffer);

        gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, currentLength);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}
