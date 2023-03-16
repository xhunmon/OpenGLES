package com.xhunmon.opengles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.xhunmon.opengles.cameraxapi.CameraxApiActivity;

public class MainActivity extends AppCompatActivity {

    private final static int CAMERA_CODE = 0x01;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, CAMERA_CODE);
            }

            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                }, CAMERA_CODE);
            }
        }
    }

    private void showRender(int type) {
        Intent intent = new Intent(this, RenderActivity.class);
        intent.putExtra(RenderActivity.KEY_RENDER, type);
        startActivity(intent);
    }

    public void clickCameraxApi(View view) {
        startActivity(new Intent(this, CameraxApiActivity.class));
    }

    public void clickImageTexture(View view) {
        showRender(RenderActivity.TYPE_IMAGE_TEXTURE);
    }

    public void clickPoint(View view) {
        showRender(RenderActivity.TYPE_POINT1);
//        showRender(SingleActivity.TYPE_POINT2);
    }

    public void clickLine(View view) {
        showRender(RenderActivity.TYPE_LINE);
    }

    public void clickTriangle(View view) {
        showRender(RenderActivity.TYPE_TRIANGLE);
    }

    public void clickQuadrangle(View view) {
        showRender(RenderActivity.TYPE_QUADRANGLE);
    }

    public void clickMatrix(View view) {
        showRender(RenderActivity.TYPE_MATRIX);
    }

    public void clickCameraxGL(View view) {
        showRender(RenderActivity.TYPE_CAMERAX_RENDER);
    }

    public void clickFboRender(View view) {
        showRender(RenderActivity.TYPE_FBO_RENDER);
    }
}