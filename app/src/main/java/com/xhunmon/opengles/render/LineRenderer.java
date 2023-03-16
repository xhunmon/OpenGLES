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
public class LineRenderer implements GLSurfaceView.Renderer {

    //顶点着色器程序
    private final static String VERTEX_GLSL = "" +
            "attribute vec4 a_Position;" +
            "void main(){" +
            "gl_Position=a_Position;" +
            "}";

    //传递给a_Position的数据
    private final static float[] VERTEX_DATA = {
            -1f, -1.0f, 0.0f, 1.0f,//一个顶点的四个分量，左下角
            1.0f, 1.0f, 0.0f, 1.0f,//一个顶点的四个分量，右上角
    };

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

        vertexBuffer = OpenGLUtils.allocateBuffer(VERTEX_DATA);//将float数组申请一块buffer

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

        //将vertexBuffer数据传递给a_Position;
        // 步长(Stride)：一个顶点数据的长度；它告诉我们在连续的顶点属性组之间的间隔，这里一个顶点四个分量*每个分量4个字节
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 4 * 4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);//启用顶点的句柄


        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);//绘制一个点，下标从0开始，数量2个
    }
}
