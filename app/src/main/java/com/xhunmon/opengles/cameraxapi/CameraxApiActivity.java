package com.xhunmon.opengles.cameraxapi;

import android.os.Bundle;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;

import com.xhunmon.opengles.R;

public class CameraxApiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax_api);
        previewWithTextureView();
    }

    private void previewWithTextureView() {
        TextureView textureView = findViewById(R.id.textureView);
        CameraxPreview cameraxPreview = new CameraxPreview(this, textureView);
        cameraxPreview.startCamera();
    }
}