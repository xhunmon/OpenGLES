attribute vec4 a_Position;//顶点坐标
attribute vec2 a_TexCoord;//纹理坐标
varying vec2 v_TexCoord;//传递给片元的纹理坐标

void main() {
    gl_Position = a_Position;
    v_TexCoord = a_TexCoord;
}