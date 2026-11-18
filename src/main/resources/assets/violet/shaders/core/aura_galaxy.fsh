#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <violet:sky_common.glsl>
#moj_import <violet:aura_galaxy.glsl>

in vec3 viewDir;

out vec4 fragColor;

void main() {
    fragColor = ys_galaxyAura(normalize(viewDir), ColorModulator.x, ColorModulator.yzw);
}
