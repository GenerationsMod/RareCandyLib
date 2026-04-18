#version 460 // Or higher

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord;
in vec3 fragViewDir;
in vec3 worldPos;
flat in int drawId;

out vec4 outColor;

uniform vec4 ColorModulator;

#lib:structs
#lib:paradox
#lib:light
#lib:fog
#lib:material
#lib:terastal

void main() {
    DrawInfo drawInfo = drawInfos[drawId];
    Instance instance = instances[drawInfo.instance];

    Variant variant = variants[drawInfo.variant];

    Material material = materials[variant.material];

    outColor = getMaterialColor(texCoord, material, variant) * ColorModulator * instance.tint;

    if(instance.teraActive) {
        outColor.rgb = calculateTersaalizationEffect(outColor.rgb, instance.teraTint);
    } else if(material.useLight) {
        outColor = applyLight(outColor, instance.light, getEmission(texCoord, material, variant));
    }

    outColor = linear_fog(outColor, vertexDistance, FogStart, FogEnd, FogColor);
}
