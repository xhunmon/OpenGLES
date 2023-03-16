package com.xhunmon.opengles.fbo;

import android.content.Context;

import com.xhunmon.opengles.utils.OpenGLUtils;

/**
 * description:   <br>
 *
 * @author cxh
 * @date 2023/3/15
 */
public class GreyFilter extends AFboFilter {
    public GreyFilter(Context context) {
        super(OpenGLUtils.readAssetsText(context, "base_vert.vert"), OpenGLUtils.readAssetsText(context, "grey_frag.frag"));
    }
}
