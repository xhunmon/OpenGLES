package com.xhunmon.opengles.fbo;

import android.opengl.GLES20;

/**
 * description: FBO相关操作：样指示数据存储、格式和渲染缓冲区图像的大小。去指定一个特殊的渲
 * 染缓冲区图像信息，需要设定缓冲区对象为当前缓冲区对象。
 * https://learnopengl-cn.readthedocs.io/zh/latest/04%20Advanced%20OpenGL/05%20Framebuffers/
 *
 * @author cxh
 * @date 2023/3/13
 */
public class AFboFilter extends AFilter {

    //    cpu中的buffer
    protected int[] frameBuffer;
    protected int[] frameTextures;

    public AFboFilter(String vertCode, String fragCode) {
        super(vertCode, fragCode);
    }

    @Override
    public void onSizeChange(int width, int height) {
        super.onSizeChange(width, height);
        //让摄像头的数据  先渲染到  fbo
        releaseFrame();//清除数据

        //FBO，之后所有的渲染操作将会渲染到当前绑定帧缓冲的附件中
        frameBuffer = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);//分配 1 个帧缓冲区对象名字，在 frameBuffer 中返回。
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);  //綁定FBO，在绑定到GL_FRAMEBUFFER目标之后，所有的读取和写入帧缓冲的操作将会影响当前绑定的帧缓冲。

        //纹理附件：把一个纹理附加到帧缓冲的时候，所有的渲染指令将会写入到这个纹理中，就像它是一个普通的颜色/深度或模板缓冲一样
        frameTextures = new int[1];
        GLES20.glGenTextures(frameTextures.length, frameTextures, 0);
        //进行配置纹理
        for (int i = 0; i < frameTextures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[i]);//绑定纹理，后续配置纹理，开始操作纹理
            /*
             * 指定一个二维的纹理图片
             * level
             *     指定细节级别，0级表示基本图像，n级则表示Mipmap缩小n级之后的图像（缩小2^n）
             * internalformat
             *     指定纹理内部格式，必须是下列符号常量之一：GL_ALPHA，GL_LUMINANCE，GL_LUMINANCE_ALPHA，GL_RGB，GL_RGBA。
             * width height
             *     指定纹理图像的宽高，所有实现都支持宽高至少为64 纹素的2D纹理图像和宽高至少为16 纹素的立方体贴图纹理图像 。
             * border
             *     指定边框的宽度。必须为0。
             * format
             *     指定纹理数据的格式。必须匹配internalformat。下面的符号值被接受：GL_ALPHA，GL_RGB，GL_RGBA，GL_LUMINANCE，和GL_LUMINANCE_ALPHA。
             * type
             *     指定纹理数据的数据类型。下面的符号值被接受：GL_UNSIGNED_BYTE，GL_UNSIGNED_SHORT_5_6_5，GL_UNSIGNED_SHORT_4_4_4_4，和GL_UNSIGNED_SHORT_5_5_5_1。
             * data
             *     指定一个指向内存中图像数据的指针。
             */
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);//放大过滤
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);//缩小过滤
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);//gpu    操作 完了
        }

        /*
         * 把创建好的纹理frameTextures，要做的最后一件事就是将它附加到帧缓冲上了
         * target：帧缓冲的目标（绘制、读取或者两者皆有）
         * attachment：我们想要附加的附件类型。当前我们正在附加一个颜色附件。注意最后的0意味着我们可以附加多个颜色附件。我们将在之后的教程中提到。
         * textarget：你希望附加的纹理类型
         * texture：要附加的纹理本身
         * level：多级渐远纹理的级别。我们将它保留为0。
         */
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, frameTextures[0], 0);


        //在完整性检查执行之前，我们需要给帧缓冲附加一个附件
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);//要保证所有的渲染操作在主窗口中有视觉效果，我们需要再次激活默认帧缓冲，将它绑定到0
    }

    @Override
    public int onDraw(int texture) {
        //这里先将FrameBuffer绑定到当前的绘制环境上，所以，在没解绑之前，所有的GL图形绘制操作，都不是直接绘制到屏幕上，而是绘制到这个FrameBuffer上！
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);//先把所有本来应该渲染到屏幕的数据先推进FBO
        super.onDraw(texture);//本来应该是显示的屏幕的，结果到了FBO中。
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);  //最后解绑FBO，下次如果直接调用super.onDraw则显示到屏幕
        return frameTextures[0];
    }

    @Override
    public void release() {
        releaseFrame();
        super.release();
    }

    private void releaseFrame() {
        if (frameTextures != null) {
            GLES20.glDeleteTextures(1, frameTextures, 0);
            frameTextures = null;
        }

        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        }
    }
}
