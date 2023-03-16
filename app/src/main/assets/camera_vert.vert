attribute vec4 a_Position;//顶点坐标
attribute vec4 a_TexCoord;//纹理坐标
uniform mat4 u_Matrix;//采样纹理图片的矩阵，全局global的static变量
varying vec2 v_TexCoord;//传递给片元的纹理坐标

void main() {
    gl_Position = a_Position;
    v_TexCoord = (u_Matrix * a_TexCoord).xy;
}