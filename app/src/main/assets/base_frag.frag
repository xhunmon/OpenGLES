precision mediump float;//不设置导致一些设备显示不出来。用于定义数据精度，Opengl中可以设置三种类型的精度（lowp,medium 和 highp），对于Vertex Shader来说，Opengl使用的是默认最高精度级别（highp），因此没有定义
uniform sampler2D u_Sampler;//纹理采样器， uniform = static，全局唯一
varying vec2 v_TexCoord;//从顶点着色器传递过来的纹理坐标

void main() {
    gl_FragColor = texture2D(u_Sampler, v_TexCoord);//采集方法，获取指定纹理单元u_Sampler和对应纹理坐标v_TexCoord的颜色值
}