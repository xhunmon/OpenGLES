package com.xhunmon.opengles.fbo;

import android.content.Context;

import com.xhunmon.opengles.utils.OpenGLUtils;

/**
 * description:   <br>
 *
 * @author cxh
 * @date 2023/3/14
 */
public class ShowCameraFilter extends AFilter{

    public ShowCameraFilter(Context context) {
        super(OpenGLUtils.readAssetsText(context, "base_vert.vert"), OpenGLUtils.readAssetsText(context, "base_frag.frag"));
    }
}
