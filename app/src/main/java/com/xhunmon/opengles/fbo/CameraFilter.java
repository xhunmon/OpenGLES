package com.xhunmon.opengles.fbo;

import android.content.Context;
import android.opengl.GLES20;

import com.xhunmon.opengles.utils.OpenGLUtils;

/**
 * description:   <br>
 *
 * @author cxh
 * @date 2023/3/12
 */
public class CameraFilter extends AFboFilter {
//public class CameraFilter extends AFilter {

    protected int uMatrix;
    protected float[] mtx;

    public CameraFilter(Context context) {
        super(OpenGLUtils.readAssetsText(context, "camera_vert.vert"), OpenGLUtils.readAssetsText(context, "camera_frag.frag"));
        uMatrix = GLES20.glGetUniformLocation(programs, "u_Matrix");//查询uniform变量的地址
    }

    @Override
    protected void onBeforeDraw(int textureId) {
        super.onBeforeDraw(textureId);
        //把矩阵数据发送给着色器，
        // 第二个参数告诉OpenGL我们将要发送多少个矩阵，这里是1。
        // 第三个参数询问我们我们是否希望对我们的矩阵进行置换(Transpose)，也就是说交换我们矩阵的行和列。OpenGL开发者通常使用一种内部矩阵布局，叫做列主序(Column-major Ordering)布局。GLM的默认布局就是列主序，所以并不需要置换矩阵，我们填GL_FALSE
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, mtx, 0);
    }

    public void setTransformMatrix(float[] mtx) {
        this.mtx = mtx;
    }

}
