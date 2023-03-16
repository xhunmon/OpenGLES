package com.xhunmon.opengles.fbo;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.xhunmon.opengles.utils.OpenGLUtils;


/**
 * 多图图层进行贴纸，并且编码输出到文件
 *
 * @author cxh
 * @date 2023/2/21
 */
public class StickyFilter extends AFilter {
    private Bitmap bitmap;
    //    private int[] frameTextures = new int[1];
    private int[] frameTextures;
    private int index;
    private int bottomX, bottomY, imgWidth, imgHeight;

    public StickyFilter(Context context, Bitmap bitmap, int[] frameTextures, int index) {
        super(OpenGLUtils.readAssetsText(context, "base_vert.vert"), OpenGLUtils.readAssetsText(context, "base_frag.frag"));
        this.bitmap = bitmap;
        this.index = index;
        this.frameTextures = frameTextures;
        loadBitmap();
    }

    //加载Bitmap图片
    private void loadBitmap() {
        GLES20.glGenTextures(1, frameTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[index]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        blendFunc();
    }


    @Override
    public int onDraw(int texture) {
//        loadBitmap();
        blendFunc();
        super.onDraw(frameTextures[index]);
        return frameTextures[index];
    }


    private void blendFunc() {
        GLES20.glEnable(GLES20.GL_BLEND);
//      GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);//使用这个混合算法可以合成带透明度的贴纸。参考：https://www.jianshu.com/p/2fb9d90b57f0
    }

    @Override
    public void onSizeChange(int width, int height) {
        super.onSizeChange(width, height);
        imgWidth = bitmap.getWidth();
        imgHeight = bitmap.getHeight();
        bottomX = width - imgWidth;
        bottomY = height - imgHeight;
    }

    @Override
    protected float[] vertexTexture() {//Image的图片
        return new float[]{
                0.0f, 1.0f,//左下
                1.0f, 1.0f,//右下
                0.0f, 0.0f,//左上
                1.0f, 0.0f//右上
        };
    }

    @Override
    protected void onGlViewport(int x, int y, int width, int height) {
        super.onGlViewport(bottomX, bottomY, imgWidth, imgHeight);//显示宽高，CameraView设置的宽高
    }
}
