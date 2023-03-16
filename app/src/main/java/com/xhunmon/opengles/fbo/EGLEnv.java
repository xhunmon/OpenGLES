package com.xhunmon.opengles.fbo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.xhunmon.opengles.R;

import java.util.ArrayList;
import java.util.List;

/**
 * description:   <br>
 *
 * @author cxh
 * @date 2023/3/14
 */
public class EGLEnv {

    private EGLDisplay mEglDisplay;
    //    调用 oepngl 的函数
    private EGLContext mEglContext;
    private final EGLConfig mEglConfig;
    private final EGLSurface mEglSurface;
    private List<AFilter> textureFilters;

    public EGLEnv(Context context, EGLContext mGlContext, Surface surface, int width, int height) {
        this.textureFilters = textureFilters;
        //EGLDisplay—它封装了与操作系统相关的连接
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }
        //初始化 EGL 内部数据，返回 EGL 的主次版本号
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }

        // 配置 属性选项
        int[] configAttribs = {
                EGL14.EGL_RED_SIZE, 8, //颜色缓冲区中红色位数
                EGL14.EGL_GREEN_SIZE, 8,//颜色缓冲区中绿色位数
                EGL14.EGL_BLUE_SIZE, 8, //
                EGL14.EGL_ALPHA_SIZE, 8,//
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //opengl es 2.0
                EGL14.EGL_NONE
        };
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        //EGL 根据属性选择一个配置
        if (!EGL14.eglChooseConfig(mEglDisplay, configAttribs, 0, configs, 0, configs.length,
                numConfigs, 0)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        mEglConfig = configs[0];//获取到配置
        int[] context_attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        //创建关联的上下文
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, mGlContext, context_attrib_list, 0);
        if (mEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }

        //创建EGLSurface
        int[] surface_attrib_list = {
                EGL14.EGL_NONE
        };
        //        录屏推流
        mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface, surface_attrib_list, 0);
        if (mEglSurface == null) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        /**
         * 绑定当前线程的显示器display mEglDisplay  虚拟 物理设备
         */
        if (!EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
//        for (int i = 0; i < textures.length; i++) {
//            ShowCameraFilter filter = new ShowCameraFilter(context);
//            filter.onSizeChange(width, height);
//            textureFilters.add(filter);
//        }
        textureFilters = new ArrayList<>();
        ShowCameraFilter filter = new ShowCameraFilter(context);
        filter.onSizeChange(width, height);
        textureFilters.add(filter);
        int[] textures = new int[1];
        StickyFilter filter2 = new StickyFilter(context, BitmapFactory.decodeResource(context.getResources(), R.drawable.logo), textures, 0);
        textureFilters.add(filter2);//图层1：最后
        filter2.onSizeChange(width, height);
    }

    public void draw(int textureId, long timestamp) {
        for (AFilter filter : textureFilters) {
            filter.onDraw(textureId);
        }
//           给帧缓冲   时间戳
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEglSurface, timestamp);
        //EGLSurface是双缓冲模式
        EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);
    }

    public void release() {
        EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroyContext(mEglDisplay, mEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEglDisplay);
    }
}
