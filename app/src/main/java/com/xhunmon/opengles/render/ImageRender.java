package com.xhunmon.opengles.render;


import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.xhunmon.opengles.utils.OpenGLUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * description: 纹理映射：将图片映射（贴）在由两个三角形绘制的矩形上面去。而此时，图片就是纹理。  <br>
 *
 * @author cxh
 * @date 2023/3/6
 */
public class ImageRender implements GLSurfaceView.Renderer {


    //良好的编程习惯：attribute的变量以a_开头；uniform的变量以u_开头；varying的变量以v_开头；
    private final static String VERTEX_GLSL = "" +
            "attribute vec4 a_Position;\n" +
            "attribute vec2 a_TexCoord;\n" +
            "varying vec2 v_TexCoord;\n" +//varying变量是把v_TexCoord数据从顶点着色器传递到v_TexCoord
            "void main(){\n" +
            "    gl_Position = a_Position;\n" +
            "    v_TexCoord = a_TexCoord;\n" +
            "}";

    //不设置导致一些设备显示不出来。用于定义数据精度，Opengl中可以设置三种类型的精度（lowp,medium 和 highp），对于Vertex Shader来说，Opengl使用的是默认最高精度级别（highp），因此没有定义
    private final static String FLAG_GLSL = "" +
            "precision mediump float;\n" +//不设置导致一些设备显示不出来。用于定义数据精度，Opengl中可以设置三种类型的精度（lowp,medium 和 highp），对于Vertex Shader来说，Opengl使用的是默认最高精度级别（highp），因此没有定义
            "uniform sampler2D u_Sampler;\n" +//sampler意为"取样器"，把图片的颜色，一小块一小块的取出来
            "varying vec2 v_TexCoord;\n" +//从顶点着色器传递过来的值
            "void main() {\n" +
            "    gl_FragColor = texture2D(u_Sampler, v_TexCoord);\n" +//采集方法，获取指定纹理单元u_Sampler和对应纹理坐标v_TexCoord的颜色值
            "}";


    private int programs;
    int[] textures = new int[1];

    //屏幕左下1/4 pixo matic
    float[] VERTEX = {
            -1.0f, -1.0f,//左下角 1
            1.0f, -1.0f,//右下角 2
            -1.0f, 1.0f,//左上角 3
            1.0f, 1.0f//右上角 4
            //绘制时是两个三角形：1-2-3 & 2-3-4
    };

    //纹理坐标需要对应OpenGL顶点坐标才能显示的正，参考：https://xhunmon.github.io/VABlog/AFPlayer/02_opengl_es.html#%E7%BA%B9%E7%90%86%E5%9D%90%E6%A0%87texture-coordinate
    float[] TEXTURE = {
            0.0f, 1.0f,//左下
            1.0f, 1.0f,//右下
            0.0f, 0.0f,//左上
            1.0f, 0.0f//右上
    };
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private int aPosition;
    private int aTexCoord;
    private int uSampler;
    private Bitmap bitmap;

    public ImageRender(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        vertexBuffer = OpenGLUtils.allocateBuffer(VERTEX);
        textureBuffer = OpenGLUtils.allocateBuffer(TEXTURE);

        programs = OpenGLUtils.loadPrograms(VERTEX_GLSL, FLAG_GLSL);

        aPosition = GLES20.glGetAttribLocation(programs, "a_Position");
        aTexCoord = GLES20.glGetAttribLocation(programs, "a_TexCoord");
        uSampler = GLES20.glGetUniformLocation(programs, "u_Sampler");//用来接收纹理图像


        //加载Bitmap图片
        GLES20.glGenTextures(1, textures, 0);//绑定纹理的个数
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);//GL_TEXTURE_2D与片元着色器sampler2D对应上
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);//显示宽高，CameraView设置的宽高
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清空旧画板背景颜色，设置新的
        GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(programs);//激活这个程序对象，调用后每个着色器调用和渲染调用都会使用这个程序对象

        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);//生效

        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(aTexCoord);//生效

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);//开启0号纹理单元（最多32个纹理单元）
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);//绑定到上面定义的纹理对象上
        GLES20.glUniform1i(uSampler, 0);//将0号纹理传递给片元着色器的取样器变量u_Sampler

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);//进行绘制渲染

        //清除缓存。。。。。。。。。。。
        GLES20.glDisableVertexAttribArray(aPosition);//失效
        GLES20.glDisableVertexAttribArray(aTexCoord);//失效
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//重新绑定为0号对象（意味清除）
        GLES20.glDisable(GLES20.GL_BLEND);//关闭颜色混合功能，将片元颜色和颜色缓冲区的颜色进行混合。https://blog.csdn.net/hebbely/article/details/70155214
        GLES20.glDeleteProgram(programs);
    }
}
