package com.xhunmon.opengles.fbo;

import android.opengl.GLES20;

import com.xhunmon.opengles.utils.OpenGLUtils;

import java.nio.FloatBuffer;

/**
 * description: AFilter是以ImageRender为基类，  <br>
 *
 * @author cxh
 * @date 2023/3/6
 */
public class AFilter {

    private FloatBuffer vertexBuffer, textureBuffer;
    protected int programs, aPosition, aTexCoord, uSampler;
    protected int width, height;//作用在glViewport上，而已控制顶点坐标范围


    /**
     * 必须含有基础变量，否者失败
     *
     * @param vertCode R.raw.base_vert.vert 为基础
     * @param fragCode R.raw.base_frag.frag 为基础
     */
    public AFilter(String vertCode, String fragCode) {
        vertexBuffer = OpenGLUtils.allocateBuffer(vertexOpenGL());
        textureBuffer = OpenGLUtils.allocateBuffer(vertexTexture());

        programs = OpenGLUtils.loadPrograms(vertCode, fragCode);

        aPosition = GLES20.glGetAttribLocation(programs, "a_Position");
        aTexCoord = GLES20.glGetAttribLocation(programs, "a_TexCoord");
        uSampler = GLES20.glGetUniformLocation(programs, "u_Sampler");//用来接收纹理图像
    }

    public void onSizeChange(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int onDraw(int texture) {
        onGlViewport(0, 0, width, height);

        GLES20.glUseProgram(programs);//激活这个程序对象，调用后每个着色器调用和渲染调用都会使用这个程序对象

        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);//生效

        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(aTexCoord);//生效

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);//开启0号纹理单元（最多32个纹理单元）
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);//绑定到上面定义的纹理对象上
        GLES20.glUniform1i(uSampler, 0);//将0号纹理传递给片元着色器的取样器变量u_Sampler

        onBeforeDraw(texture);//子类的自定义属性操作

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);//进行绘制渲染
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return texture;
    }

    /**
     * 着色器OpenGL的顶点坐标
     */
    protected float[] vertexOpenGL() {
        return new float[]{
                -1.0f, -1.0f,//左下角 1
                1.0f, -1.0f,//右下角 2
                -1.0f, 1.0f,//左上角 3
                1.0f, 1.0f//右上角 4
                //绘制时是两个三角形：1-2-3 & 2-3-4
        };
    }

    /**
     * 纹理（图片）坐标
     */
    protected float[] vertexTexture() {
        //纹理坐标需要对应OpenGL顶点坐标才能显示的正，参考：https://xhunmon.github.io/VABlog/AFPlayer/02_opengl_es.html#%E7%BA%B9%E7%90%86%E5%9D%90%E6%A0%87texture-coordinate
        return new float[]{
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
    }

    /**
     * 设置窗口大小
     */
    protected void onGlViewport(int x, int y, int width, int height) {
        GLES20.glViewport(x, y, width, height);//显示宽高
    }

    protected void onBeforeDraw(int texture) {

    }

    public void release() {
        //清除缓存。。。。。。。。。。。
        GLES20.glDisableVertexAttribArray(aPosition);//失效
        GLES20.glDisableVertexAttribArray(aTexCoord);//失效
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//重新绑定为0号对象（意味清除）
        GLES20.glDisable(GLES20.GL_BLEND);//关闭颜色混合功能，将片元颜色和颜色缓冲区的颜色进行混合。https://blog.csdn.net/hebbely/article/details/70155214
        GLES20.glDeleteProgram(programs);
    }
}
