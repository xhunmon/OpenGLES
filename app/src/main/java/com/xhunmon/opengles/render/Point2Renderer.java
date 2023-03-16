package com.xhunmon.opengles.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.xhunmon.opengles.utils.OpenGLUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * description:   <br>
 *
 * @author cxh
 * @date 2023/2/19
 */
public class Point2Renderer implements GLSurfaceView.Renderer {

    //顶点着色器程序---attribute：只能存在于vertex shader中,一般用于保存顶点或法线数据,它可以在数据缓冲区中读取数据
    //良好的编程习惯：attribute的变量以a_开头；uniform的变量以u_开头
    private final static String VERTEX_GLSL = "" +
            "attribute vec4 a_Position;" +
            "void main(){" +
            "gl_Position=a_Position;" +
            "gl_PointSize=200.0;" +
            "}";

    //片元着色器程序
    private final static String FLAG_GLSL = "" +
            "void main(){" +
            "gl_FragColor=vec4(1.0,0.0,0.0,1.0);" +
            "}";

    //程序对象对象的句柄
    private int programs;
    private FloatBuffer vertexBuffer;
    private int aPosition;


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        programs = OpenGLUtils.loadPrograms(VERTEX_GLSL, FLAG_GLSL);//创建、链接顶点和片元着色器程序

        //获取顶点着色器的a_Position成员句柄
        aPosition = GLES20.glGetAttribLocation(programs, "a_Position");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清空旧画板背景颜色，设置新的
        GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(programs);//使用程序

        GLES20.glVertexAttrib4f(aPosition,0.0f,0.0f,0.0f,1.0f);//将值通过vPosition传递给gl_Position

        GLES20.glDrawArrays(GLES20.GL_POINTS,0,1);//绘制一个点，下标从0开始，数量1个
    }
}
