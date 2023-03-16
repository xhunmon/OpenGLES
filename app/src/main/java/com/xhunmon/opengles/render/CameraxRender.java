package com.xhunmon.opengles.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import com.xhunmon.opengles.utils.CameraxHelper;
import com.xhunmon.opengles.utils.OpenGLUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * description: 流程：
 * 1：打开摄像头 --》回调onUpdated，拿到到摄像头的SurfaceTexture
 * 2：SurfaceTexture绑定OpenGL的图层，然后设置监听，将回调onFrameAvailable--》让外面的GLSurfaceView主动进行绘制请求requestRender
 * 3：requestRender请求后，将回调到onDrawFrame，然后进行绘制
 * <p>
 *
 * @author cxh
 * @date 2023/3/6
 */
public class CameraxRender implements GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener {

    //良好的编程习惯：attribute的变量以a_开头；uniform的变量以u_开头；varying的变量以v_开头；
    private final static String VERTEX_GLSL = "" +
            "attribute vec4 a_Position;\n" +
            "attribute vec4 a_TexCoord;\n" +
            "uniform mat4 u_Matrix;\n" +// uniform=static
            "varying vec2 v_TexCoord;\n" +
            "void main(){\n" +
            "    gl_Position = a_Position;\n" +
            "    v_TexCoord = (u_Matrix * a_TexCoord).xy;\n" +
            "}";

    //不设置导致一些设备显示不出来。用于定义数据精度，Opengl中可以设置三种类型的精度（lowp,medium 和 highp），对于Vertex Shader来说，Opengl使用的是默认最高精度级别（highp），因此没有定义
    private final static String FLAG_GLSL = "" +
            "#extension GL_OES_EGL_image_external: require\n" +//申请了这个，才能用samplerExternalOES
            "precision mediump float;\n" +//不设置导致一些设备显示不出来。用于定义数据精度，Opengl中可以设置三种类型的精度（lowp,medium 和 highp），对于Vertex Shader来说，Opengl使用的是默认最高精度级别（highp），因此没有定义
            "varying vec2 v_TexCoord;\n" +
            "uniform samplerExternalOES u_Sampler;\n" +//samplerExternalOES 是android用来渲染 相机数据
            "void main() {\n" +
            "    gl_FragColor = texture2D(u_Sampler, v_TexCoord);\n" +
            "    //gl_FragColor = vec4(rgba.r, rgba.g, rgba.b, rgba.a);\n" +
            "}";

    private final float[] VERTEX = {
            -1.0f, -1.0f,//左下角 1
            1.0f, -1.0f,//右下角 2
            -1.0f, 1.0f,//左上角 3
            1.0f, 1.0f//右上角 4
            //绘制时是两个三角形：1-2-3 & 2-3-4
    };

    //纹理坐标需要对应OpenGL顶点坐标才能显示的正，参考：https://xhunmon.github.io/VABlog/AFPlayer/02_opengl_es.html#%E7%BA%B9%E7%90%86%E5%9D%90%E6%A0%87texture-coordinate
    private final float[] TEXTURE = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };
    private FloatBuffer vertexBuffer, textureBuffer;
    private int[] glLayout = new int[1];
    private int programs, aPosition, aTexCoord, uMatrix, uSampler;
    private float[] mtx = new float[16];//固定是4x4的矩阵

    private SurfaceTexture cameraTexture;
    private SurfaceTexture.OnFrameAvailableListener surfaceTextureListener;

    public CameraxRender(Context context, SurfaceTexture.OnFrameAvailableListener listener) {
        surfaceTextureListener = listener;
        new CameraxHelper((LifecycleOwner) context, this);//打开摄像头，在 onUpdated 回调
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        cameraTexture.attachToGLContext(glLayout[0]);//绑定第0个图层
        cameraTexture.setOnFrameAvailableListener(surfaceTextureListener);//设置监听，回到onFrameAvailable

        //清空旧画板背景颜色，设置新的
        GLES20.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        vertexBuffer = OpenGLUtils.allocateBuffer(VERTEX);
        textureBuffer = OpenGLUtils.allocateBuffer(TEXTURE);

        programs = OpenGLUtils.loadPrograms(VERTEX_GLSL, FLAG_GLSL);

        aPosition = GLES20.glGetAttribLocation(programs, "a_Position");
        aTexCoord = GLES20.glGetAttribLocation(programs, "a_TexCoord");
        uMatrix = GLES20.glGetUniformLocation(programs, "u_Matrix");
        uSampler = GLES20.glGetUniformLocation(programs, "u_Sampler");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        cameraTexture.updateTexImage();
        cameraTexture.getTransformMatrix(mtx);//从SurfaceTexture获取到摄像头最新的矩阵数据

        GLES20.glUseProgram(programs);

        vertexBuffer.position(0);//从第0个坐标读起
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);//生效

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(aTexCoord);//生效

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);//开启0号纹理单元（最多32个纹理单元）
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glLayout[0]);//绑定到上面定义的纹理对象上
        GLES20.glUniform1i(uSampler, 0);//将0号纹理传递给片元着色器的取样器变量u_Sampler

        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mtx, 0);//往uMatrix传递矩阵值

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);//进行绘制渲染
    }

    @Override
    public void onUpdated(Preview.PreviewOutput output) {
        Log.d("glse", "onUpdated------------");
        cameraTexture = output.getSurfaceTexture();
    }

    public void release() {
        //清除缓存。。。。。。。。。。。
        //清除缓存。。。。。。。。。。。
        GLES20.glDisableVertexAttribArray(aPosition);//失效
        GLES20.glDisableVertexAttribArray(aTexCoord);//失效
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//重新绑定为0号对象（意味清除）
        GLES20.glDisable(GLES20.GL_BLEND);//关闭颜色混合功能，将片元颜色和颜色缓冲区的颜色进行混合。https://blog.csdn.net/hebbely/article/details/70155214
        GLES20.glDeleteProgram(programs);
    }
}
