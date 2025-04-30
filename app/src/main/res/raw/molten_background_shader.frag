uniform float iTime;
uniform vec2 iResolution;
uniform vec2 fragCoord;

vec3 colorA = vec3(0.133, 0.588, 0.953); // blue
vec3 colorB = vec3(1.0, 0.341, 0.133);  // orange

float noise(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float fbm(vec2 p) {
    float f = 0.0;
    f += 0.5000 * noise(p);
    f += 0.2500 * noise(p * 2.0);
    f += 0.1250 * noise(p * 4.0);
    f += 0.0625 * noise(p * 8.0);
    return f;
}

// Main must return a color in SkSL
vec4 main(float2 fragCoord) {
    vec2 uv = fragCoord / iResolution;
    vec2 p = uv * 3.0;

    float t = iTime * 0.1;
    float n = fbm(p + float2(t, t));

    vec3 col = mix(colorA, colorB, n);
    return vec4(col, 1.0);
}
