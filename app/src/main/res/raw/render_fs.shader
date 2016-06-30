#version 310 es

in mediump vec3 fPosCamera;
in mediump vec3 fNrmCamera;
in mediump vec3 fLightPos;

out mediump vec4 outColor;

mediump vec3 diffuseColor = vec3(0.75164, 0.60648, 0.22648);
mediump vec3 specularColor = vec3(0.580594, 0.223257, 0.0695701);
mediump float shininess = 51.2;

void main(void) {
    mediump vec3 V = normalize(-fPosCamera);
    mediump vec3 N = normalize(fNrmCamera);
    mediump vec3 L = normalize(fLightPos - fPosCamera);
    mediump vec3 H = normalize(V + L);

    mediump float NdotL = max(0.0, dot(N, L));
    mediump float NdotH = max(0.0, dot(N, H));

    mediump vec3 rgb = NdotL * diffuseColor + NdotH * pow(NdotH, shininess);

    outColor = vec4(rgb, 1.0);
}