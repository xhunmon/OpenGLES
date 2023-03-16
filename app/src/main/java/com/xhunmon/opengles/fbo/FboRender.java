package com.xhunmon.opengles.fbo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import com.xhunmon.opengles.R;
import com.xhunmon.opengles.utils.CameraxHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * description: Camerax流程参考 CameraxRender
 * <p>
 *
 * @author cxh
 * @date 2023/3/6
 */
public class FboRender implements GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener {

    private List<AFilter> filters;
    private float[] mtx = new float[16];//固定是4x4的矩阵
    private int[] textures = new int[2];

    private SurfaceTexture cameraTexture;
    private SurfaceTexture.OnFrameAvailableListener surfaceTextureListener;
    private Context context;
    private CameraFilter cameraFilter;
    private int width, height;
    private VideoRecorder mRecorder;

    public FboRender(Context context, SurfaceTexture.OnFrameAvailableListener listener) {
        this.context = context;
        surfaceTextureListener = listener;
        new CameraxHelper((LifecycleOwner) context, this);//打开摄像头，在 onUpdated 回调
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        cameraTexture.attachToGLContext(textures[0]);//绑定第0个图层
        cameraTexture.setOnFrameAvailableListener(surfaceTextureListener);//设置监听，回到onFrameAvailable

        filters = new ArrayList<>();
        cameraFilter = new CameraFilter(context);
        filters.add(cameraFilter);//FBO图层0：摄像头数据
//        filters.add(new GreyFilter(context));//FBO图层0：灰色滤镜--位置固定，在CameraFilter之后，ShowCameraFilter之前
//        filters.add(new ScaleFilter(context));//FBO图层0：缩放滤镜--位置固定，在CameraFilter之后，ShowCameraFilter之前
        filters.add(new ShowCameraFilter(context));//将上面FBO图层0数据显示到屏幕
//        filters.add(new StickyFilter(context, BitmapFactory.decodeResource(context.getResources(), R.drawable.logo), textures, 1));//增加图层1贴纸

        initRecorder();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        for (AFilter filter : filters) {
            filter.onSizeChange(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        cameraTexture.updateTexImage();
        cameraTexture.getTransformMatrix(mtx);//从SurfaceTexture获取到摄像头最新的矩阵数据
        cameraFilter.setTransformMatrix(mtx);
        int id = textures[0];//注意FBO的id与原id不一致
        for (AFilter filter : filters) {
            id = filter.onDraw(id);
        }
        mRecorder.fireFrame(id, cameraTexture.getTimestamp());
    }

    @Override
    public void onUpdated(Preview.PreviewOutput output) {
        Log.d("glse", "onUpdated------------");
        cameraTexture = output.getSurfaceTexture();
    }

    private void initRecorder() {
        File file = new File(Environment.getExternalStorageDirectory(), "codec.h264");
        if (file.exists()) {
            file.delete();
        }
        mRecorder = new VideoRecorder(context, file.getAbsolutePath(), EGL14.eglGetCurrentContext(), 1080, 1980);
    }

    public void enableGrey(final boolean isChecked) {//需要在Opengl线程中操作--》GLSurfaceView.queueEvent
        if (isChecked) {
            GreyFilter greyFilter = new GreyFilter(context);
            filters.add(1, greyFilter);//FBO图层0：灰色滤镜--固定位置在1，因为onDraw是根据图层id来的
            greyFilter.onSizeChange(width, height);
        } else {
            for (AFilter filter : filters) {
                if (filter instanceof GreyFilter) {
                    filters.remove(filter);
                    filter.release();
                    filter = null;
                    break;
                }
            }
        }
    }

    public void enableScale(final boolean isChecked) {//需要在Opengl线程中操作--》GLSurfaceView.queueEvent
        if (isChecked) {
            ScaleFilter filter = new ScaleFilter(context);
            filters.add(1, filter);//FBO图层0：灰色滤镜--固定位置在1，因为onDraw是根据图层id来的
            filter.onSizeChange(width, height);
        } else {
            for (AFilter filter : filters) {
                if (filter instanceof ScaleFilter) {
                    filters.remove(filter);
                    filter.release();
                    filter = null;
                    break;
                }
            }
        }
    }

    public void enableSticky(final boolean isChecked) {//需要在Opengl线程中操作--》GLSurfaceView.queueEvent
        if (isChecked) {
            StickyFilter filter = new StickyFilter(context, BitmapFactory.decodeResource(context.getResources(), R.drawable.logo), textures, 1);
            filters.add(filter);//图层1：最后
            filter.onSizeChange(width, height);
        } else {
            for (AFilter filter : filters) {
                if (filter instanceof StickyFilter) {
                    filters.remove(filter);
                    filter.release();
                    filter = null;
                    break;
                }
            }
        }
    }

    public void changeRecord(boolean isCheck) {
        if (isCheck) {
            try {
                mRecorder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mRecorder.stop();
        }

    }
}
