package com.xhunmon.opengles.fbo;

import android.content.Context;
import android.opengl.GLES20;

import com.xhunmon.opengles.utils.OpenGLUtils;

/**
 * description: 缩放滤镜  <br>
 *
 * @author cxh
 * @date 2023/3/15
 */
public class ScaleFilter extends AFboFilter {
    private int uScale;

    public ScaleFilter(Context context) {
        super(OpenGLUtils.readAssetsText(context, "scale_vert.vert"), OpenGLUtils.readAssetsText(context, "base_frag.frag"));
        uScale = GLES20.glGetUniformLocation(programs, "u_Scale");
    }

    @Override
    protected void onBeforeDraw(int texture) {
        super.onBeforeDraw(texture);
        GLES20.glUniform1f(uScale, 2f);
    }
}
