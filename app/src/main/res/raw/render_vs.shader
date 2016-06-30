#version 310 es

layout(location = 0) in vec3 vPosition;
layout(location = 1) in vec3 vNormal;

uniform mat4 uMVPMat;
uniform mat4 uMVMat;
uniform vec3 uLightPos;

out vec3 fPosCamera;
out vec3 fNrmCamera;
out vec3 fLightPos;

void main(void) {
    gl_Position = uMVPMat * vec4(vPosition, 1.0);

    fPosCamera = (uMVMat * vec4(vPosition, 1.0)).xyz;
    fNrmCamera = (transpose(inverse(uMVMat)) * vec4(vNormal, 0.0)).xyz;
    fLightPos  = (uMVMat * vec4(uLightPos, 1.0)).xyz;
}