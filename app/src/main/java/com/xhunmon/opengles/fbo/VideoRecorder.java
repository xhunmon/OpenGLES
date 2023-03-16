package com.xhunmon.opengles.fbo;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.xhunmon.opengles.utils.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * description: 获取surface中数据进行编码输出到文件中  <br>
 *
 * @author cxh
 * @date 2023/3/15
 */
public class VideoRecorder {
    private static final String TAG = "VideoRecorder";
    private MediaCodec mMediaCodec;
    private int mWidth;
    private int mHeight;
    private String mPath;
    private Surface mSurface;
    private Handler mHandler;
    private EGLContext mGlContext;
    private EGLEnv eglEnv;
    private boolean isStart;
    private Context mContext;
    private long startTime;

    public VideoRecorder(Context context, String path, EGLContext glContext, int width, int height) {
        mContext = context.getApplicationContext();
        mPath = path;
        mWidth = width;
        mHeight = height;
        mGlContext = glContext;
    }

    public void start() throws IOException {
        //使用MediaCodec对视频流进行编码成H264
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                mWidth, mHeight);
        //颜色空间 从 surface当中获得
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities
                .COLOR_FormatSurface);
        //码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000_000);
        //帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        //关键帧间隔
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        //创建编码器
        mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        //配置编码器
        mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//输入数据     byte[]    gpu  mediaprojection

        mSurface = mMediaCodec.createInputSurface();

//        视频  编码一个可以播放的视频
        //混合器 (复用器) 将编码的h.264封装为mp4
        //开启编码
        mMediaCodec.start();
//        重点    opengl   gpu里面的数据画面   肯定要调用   opengl 函数
//线程
        //創建OpenGL 的 環境
        HandlerThread handlerThread = new HandlerThread("codec-gl");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                eglEnv = new EGLEnv(mContext, mGlContext, mSurface, mWidth, mHeight);
                isStart = true;
            }
        });
    }

    public void fireFrame(final int textureId, final long timestamp) {
        if (!isStart) {
            return;
        }
        //录制用的opengl已经和handler的线程绑定了 ，所以需要在这个线程中使用录制的opengl
        mHandler.post(new Runnable() {
            public void run() {
                eglEnv.draw(textureId, timestamp);
                codec(false);
            }
        });
    }

    private long timeStamp;

    private void codec(boolean endOfStream) {
        //给个结束信号
        if (endOfStream) {
            mMediaCodec.signalEndOfInputStream();
        }
        //2000毫秒 手动触发输出关键帧
        if (System.currentTimeMillis() - timeStamp >= 2000) {
            Bundle params = new Bundle();
            //立即刷新 让下一帧是关键帧，为了输出sps、pps
            params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
            mMediaCodec.setParameters(params);
            timeStamp = System.currentTimeMillis();
        }
        //从GPU的Surface画布中获取到数据
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int index = mMediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);//从输出队列中取出编码操作之后的数据
        Log.i(TAG, "run: " + index);
        if (index > 0) {
            ByteBuffer buffer = mMediaCodec.getOutputBuffer(index);//获取编解码之后的数据输出流队列
            MediaFormat mediaFormat = mMediaCodec.getOutputFormat(index);//获取配置的编码参数
            Log.i(TAG, "mediaFormat: " + mediaFormat.toString());
            byte[] outData = new byte[bufferInfo.size];
            buffer.get(outData);
            if (startTime == 0) {
                startTime = bufferInfo.presentationTimeUs / 1000;// 将pts 微妙转为毫秒
            }
//            FileUtils.writeContent(outData);
            FileUtils.writeBytes(outData, mPath);
            mMediaCodec.releaseOutputBuffer(index, false);//处理完成，释放ByteBuffer数据
        }

    }

    public void stop() {
        isStart = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                codec(true);
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
                eglEnv.release();
                eglEnv = null;
                mSurface = null;
                mHandler.getLooper().quitSafely();
                mHandler = null;
            }
        });
    }
}
