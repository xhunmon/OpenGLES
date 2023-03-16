package com.xhunmon.opengles.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.xhunmon.opengles.utils.OpenGLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * description:   <br>
 *
 * @author cxh
 * @date 2023/2/19
 */
public class PointRenderer implements GLSurfaceView.Renderer {

    //GLSL（OpenGL Shader Language 着色器语言）：@link：https://xhunmon.github.io/VABlog/OpenGL/02_glsl.html
    //顶点着色器程序-----gl_Position和gl_PointSize是GPU内部变量，放置顶点坐标信息；gl_PointSize 需要绘制点的大小,(只在gl.POINTS模式下有效)；vec4：是float类型，有4个分量的一个变量名称
    private final static String VERTEX_GLSL = "" +
            "void main(){" +
            "gl_Position=vec4(0.0,0.0,0.0,1.0);" +
            "gl_PointSize=200.0;" +
            "}";

    //片元着色器程序-----gl_FragColor是GPU内部变量，就是显示顶点gl_Position的颜色
    private final static String FLAG_GLSL = "" +
            "void main(){" +
            "gl_FragColor=vec4(1.0,0.0,0.0,1.0);" +
            "}";

    //程序对象对象的句柄
    private int programs;


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        programs = OpenGLUtils.loadPrograms(VERTEX_GLSL, FLAG_GLSL);//创建、链接顶点和片元着色器程序
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

        GLES20.glDrawArrays(GLES20.GL_POINTS,0,1);//绘制一个点，下标从0开始，数量1个
    }
}
