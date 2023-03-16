package com.xhunmon.opengles.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.xhunmon.opengles.utils.OpenGLUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * description: 矩阵的使用：https://learnopengl-cn.github.io/01%20Getting%20started/07%20Transformations/#_1  <br>
 *
 * @author cxh
 * @date 2023/3/5
 */
public class MatrixRenderer implements GLSurfaceView.Renderer {

    //良好的编程习惯：attribute的变量以a_开头；uniform的变量以u_开头
    private final static String VERTEX_GLSL = "" +
            "attribute vec4 a_Position;\n" +
            "uniform mat4 u_TranslateMatrix;\n" +
            "void main(){\n" +
            "    gl_Position = a_Position * u_TranslateMatrix;\n" +
            "    //gl_Position = a_Position;\n" +
            "}";
    //等价于VERTEX_GLSL，如果a_Position传递只有3个分量时，第四个分量默认值是1.0f
    private final static String VERTEX_GLSL2 = "" +
            "attribute vec4 a_Position;\n" +
            "void main(){\n" +
            "    gl_Position = a_Position;\n" +
            "}";

    //不设置导致一些设备显示不出来。用于定义数据精度，Opengl中可以设置三种类型的精度（lowp,medium 和 highp），对于Vertex Shader来说，Opengl使用的是默认最高精度级别（highp），因此没有定义
    private final static String FLAG_GLSL = "" +
            "precision mediump float;\n" +
            "uniform vec4 u_Color;\n" +
            "void main(){\n" +
            "    gl_FragColor = u_Color;\n" +
            "}";

    private int programs;

    private final static float[] VERTEX_DATA = {
            -0.5f, -0.5f, +0.0f, +1.0f,
            +0.0f, -0.5f, +0.0f, +1.0f,
            -0.5f, +0.0f, +0.0f, +1.0f
    };

    double radian = Math.PI * 30 / 180.0;
    float cosB = (float) Math.cos(radian);
    float sinB = (float) Math.sin(radian);

    private final float[] MATRIX_DATA = {
            //0不转换：即原顶点输出
//            1.0f, 0.0f, 0.0f, 0.0f,
//            0.0f, 1.0f, 0.0f, 0.0f,
//            0.0f, 0.0f, 1.0f, 0.0f,
//            0.0f, 0.0f, 0.0f, 1.0f,

            //1缩放：VERTEX_DATA * MATRIX_DATA = VERTEX_DATA的X 、Y向量为原来的一半
//            0.5f, 0.0f, 0.0f, 0.0f,
//            0.0f, 0.5f, 0.0f, 0.0f,
//            0.0f, 0.0f, 0.5f, 0.0f,
//            0.0f, 0.0f, 0.0f, 1.0f,

            //2旋转：沿z轴旋转30°
            cosB, -sinB, 0.0f, 0.0f,
            sinB, cosB, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    //设置颜色，依次为红绿蓝和透明通道
    private final static float[] FLAG_COLOR = {1.0f, 0.5f, 0.2f, 1.0f};
    private FloatBuffer vertexBuffer;
    //    private FloatBuffer matrixBuffer;
    private int aPosition;
    private int uColor;
    private int uTranslateMatrix;


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        vertexBuffer = OpenGLUtils.allocateBuffer(VERTEX_DATA);//将float数组申请一块buffer

        programs = OpenGLUtils.loadPrograms(VERTEX_GLSL, FLAG_GLSL);//创建、链接顶点和片元着色器程序

        //获取顶点着色器的a_Position成员句柄
        aPosition = GLES20.glGetAttribLocation(programs, "a_Position");
        uTranslateMatrix = GLES20.glGetUniformLocation(programs, "u_TranslateMatrix");

        //获取片元着色器的vColor成员的句柄
        uColor = GLES20.glGetUniformLocation(programs, "u_Color");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 渲染窗口大小发生改变或者屏幕方法发生变化时候回调
        //surface左下角坐标和宽高组成的长方形映射在OpenGL（-1~1）坐标上
//        GLES20.glViewport(0, 0, width, height);
        GLES20.glViewport(0, 0, width, width);//正方形，方便看
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清空旧画板背景颜色，设置新的
        GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(programs);//激活这个程序对象，调用后每个着色器调用和渲染调用都会使用这个程序对象

        //将vertexBuffer数据传递给a_Position;
        //size：指定indx中vertexBuffer分配给每个顶点的分量数，1~4；如果size为1，则第2、3分量为0，第4分量为1
        // Stride(步长)：一个顶点数据的长度；它告诉我们在连续的顶点属性组之间的间隔，这里一个顶点3个分量*每个分量4个字节
        GLES20.glVertexAttribPointer(aPosition, 4, GLES20.GL_FLOAT, false, 4 * 4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);//启用顶点的句柄

        //往uTranslateMatrix传递矩阵值
        GLES20.glUniformMatrix4fv(uTranslateMatrix, 1, false, MATRIX_DATA, 0);

        GLES20.glUniform4fv(uColor, 1, FLAG_COLOR, 0);//设置绘制三角形的颜色

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);//绘制三角形

        //--------------------对应着释放------------------
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDeleteProgram(programs);
        GLES20.glDisable(GLES20.GL_BLEND);
    }
}