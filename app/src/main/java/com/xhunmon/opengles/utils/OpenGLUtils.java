package com.xhunmon.opengles.utils;

import android.content.Context;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * description:   <br>
 *
 * @author cxh
 * @date 2023/1/11
 */
public class OpenGLUtils {
    public static String readRawTextFile(Context context, int rawId) {
        InputStream is = context.getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String readAssetsText(Context context, String name) {
        try {
            InputStream is = context.getAssets().open(name);
            return readText(is);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String readText(InputStream is) throws IOException {
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        is.close();
        return sb.toString();
    }

    private static int loadShader(int shaderType, String shaderCode) {
        int shader = GLES20.glCreateShader(shaderType);//创建一个Shader
        GLES20.glShaderSource(shader, shaderCode);//把Shader的程序代码和那个Shader进行关联
        GLES20.glCompileShader(shader);//编译代码
        return shader;
    }

    public static int loadPrograms(String vertexCode, String fragmentCode) {
        int shaderProgram = GLES20.glCreateProgram();//创建一个程序对象
        GLES20.glAttachShader(shaderProgram, loadShader(GLES20.GL_VERTEX_SHADER, vertexCode));//把顶点着色器附加到程序对象上
        GLES20.glAttachShader(shaderProgram, loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode));//把片段着色器附加到程序对象上
        GLES20.glLinkProgram(shaderProgram);//链接它们
        //在draw时调用
//        GLES20.glUseProgram(shaderProgram);//激活这个程序对象，调用后每个着色器调用和渲染调用都会使用这个程序对象
        return shaderProgram;
    }

    /**
     * 通过 ByteBuffer 向底层申请一块内存，并且把数据写到内存中
     *
     * @param data
     * @return
     */
    public static FloatBuffer allocateBuffer(float[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 4);//申请字节空间
        bb.order(ByteOrder.nativeOrder());//设置排序方式
        FloatBuffer floatBuffer = bb.asFloatBuffer();
        floatBuffer.put(data);//将坐标数据转换为FloatBuffer，用以传入OpenGL ES程序
        floatBuffer.position(0);//指向开头的地方
        return floatBuffer;
    }
}
