package com.xhunmon.opengles;

import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.xhunmon.opengles.render.CameraxRender;
import com.xhunmon.opengles.render.ImageRender;
import com.xhunmon.opengles.render.LineRenderer;
import com.xhunmon.opengles.render.MatrixRenderer;
import com.xhunmon.opengles.render.Point2Renderer;
import com.xhunmon.opengles.render.PointRenderer;
import com.xhunmon.opengles.render.QuadrangleRenderer;
import com.xhunmon.opengles.render.TriangleRenderer;
import com.xhunmon.opengles.fbo.FboRender;

public class RenderActivity extends AppCompatActivity {
    public static final String KEY_RENDER = "KEY_RENDER";
    public static final int TYPE_POINT1 = 0;
    public static final int TYPE_POINT2 = 1;
    public static final int TYPE_LINE = 2;
    public static final int TYPE_TRIANGLE = 3;
    public static final int TYPE_QUADRANGLE = 4;
    public static final int TYPE_MATRIX = 5;
    public static final int TYPE_IMAGE_TEXTURE = 6;
    public static final int TYPE_CAMERAX_RENDER = 7;
    public static final int TYPE_FBO_RENDER = 8;

    private GLSurfaceView glSurfaceView1;
    private CheckBox cb_grey, cb_scale, cb_sticky, cb_reorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);
        glSurfaceView1 = findViewById(R.id.glSurfaceView);
        cb_grey = findViewById(R.id.cb_grey);
        cb_scale = findViewById(R.id.cb_scale);
        cb_sticky = findViewById(R.id.cb_sticky);
        cb_reorder = findViewById(R.id.cb_reorder);
        glSurfaceView1.setEGLContextClientVersion(2);//使用2.0稳定版本
        glSurfaceView1.setRenderer(getRenderer(getIntent().getIntExtra(KEY_RENDER, 0)));
        /*渲染方式，RENDERMODE_WHEN_DIRTY表示被动渲染，只有在调用requestRender或者onResume等方法时才会进行渲染。RENDERMODE_CONTINUOUSLY表示持续渲染*/
        glSurfaceView1.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private GLSurfaceView.Renderer getRenderer(int type) {
        switch (type) {
            case TYPE_POINT2:
                return new Point2Renderer();
            case TYPE_LINE:
                return new LineRenderer();
            case TYPE_TRIANGLE:
                return new TriangleRenderer();
            case TYPE_QUADRANGLE:
                return new QuadrangleRenderer();
            case TYPE_MATRIX:
                return new MatrixRenderer();
            case TYPE_IMAGE_TEXTURE:
                return new ImageRender(BitmapFactory.decodeResource(getResources(), R.drawable.demo));
            case TYPE_CAMERAX_RENDER:
                return new CameraxRender(this, surfaceTexture -> glSurfaceView1.requestRender());
            case TYPE_FBO_RENDER:
                FboRender fboRender = new FboRender(this, surfaceTexture -> glSurfaceView1.requestRender());
                cb_grey.setVisibility(View.VISIBLE);
                cb_scale.setVisibility(View.VISIBLE);
                cb_sticky.setVisibility(View.VISIBLE);
                cb_reorder.setVisibility(View.VISIBLE);
                cb_grey.setOnCheckedChangeListener((buttonView, isChecked) -> glSurfaceView1.queueEvent(() -> fboRender.enableGrey(isChecked)));
                cb_scale.setOnCheckedChangeListener((buttonView, isChecked) -> glSurfaceView1.queueEvent(() -> fboRender.enableScale(isChecked)));
                cb_sticky.setOnCheckedChangeListener((buttonView, isChecked) -> glSurfaceView1.queueEvent(() -> fboRender.enableSticky(isChecked)));
                cb_reorder.setOnCheckedChangeListener((buttonView, isChecked) -> glSurfaceView1.queueEvent(() -> fboRender.changeRecord(isChecked)));
                return fboRender;
            case TYPE_POINT1:
            default:
                return new PointRenderer();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView1 != null) glSurfaceView1.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceView1 != null) glSurfaceView1.onPause();
    }
}