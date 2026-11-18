#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <violet:sky_common.glsl>

// Sober: a black night with silver smoke and twinkling stars, and the dripping chrome
// "discord.gg/soberclient" running five times around the horizon, slowly turning.
uniform sampler2D Sampler0;

in vec3 skyDir;

out vec4 fragColor;

const float REPEAT = 5.0;
// one copy spans 72 degrees; the picture is 3:1, so it is 24 degrees high
const float BAND_LOW = 0.12;
const float BAND_HIGH = BAND_LOW + 6.28318531 / REPEAT / 3.0;

vec3 sober_band(vec3 dir, float t) {
    float elevation = asin(clamp(dir.y, -1.0, 1.0));
    float v = (elevation - BAND_LOW) / (BAND_HIGH - BAND_LOW);
    if (v <= 0.0 || v >= 1.0) return vec3(0.0);
    float azimuth = atan(dir.z, dir.x) / ys_TAU + 0.5 + t * 0.004;
    vec2 uv = vec2(fract(azimuth * REPEAT), 1.0 - v);
    vec3 text = texture(Sampler0, uv).rgb;
    // a soft breathing glow, like the neon in the picture
    float breath = 0.92 + 0.08 * sin(t * 1.6 + floor(azimuth * REPEAT) * 2.1);
    return text * breath;
}

vec3 sober_stars(vec3 dir, float t) {
    vec3 p = dir * 170.0;
    vec3 cell = floor(p);
    float h = ys_hash13(cell);
    if (h < 0.982) return vec3(0.0);
    vec3 f = fract(p) - 0.5;
    float d = length(f);
    float twinkle = 0.55 + 0.45 * sin(t * (1.5 + h * 3.0) + h * 40.0);
    return vec3(smoothstep(0.16, 0.0, d) * twinkle * (h - 0.982) / 0.018);
}

void main() {
    vec3 dir = normalize(skyDir);
    float t = ColorModulator.x;
    vec3 col = vec3(0.010, 0.010, 0.013);
    // silver haze along the horizon and slow smoke
    col += vec3(0.055, 0.055, 0.062) * exp(-abs(dir.y) * 5.0);
    float smoke = ys_fbm3(dir * 2.6 + vec3(t * 0.012, t * 0.006, 0.0), 4);
    col += vec3(0.035, 0.035, 0.040) * smoke * smoke * (0.6 + 0.4 * dir.y);
    col += sober_stars(dir, t) * 0.9;
    col += sober_band(dir, t) * 1.05;
    fragColor = vec4(clamp(col * ColorModulator.y, 0.0, 1.0), 1.0);
}
