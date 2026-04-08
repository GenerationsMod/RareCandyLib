#version 330 core

/* Super Duper-compatible MRT layout:
 *   location 0 -> scene color
 *   location 1 -> normal
 *   location 2 -> albedo
 *   location 3 -> material
 */
layout(location = 0) out vec4 sceneColOut;
layout(location = 1) out vec3 normalDataOut;
layout(location = 2) out vec3 albedoDataOut;
layout(location = 3) out vec3 materialDataOut;

in vec2 texCoord0;
in float vertexDistance;
flat in vec3 fragNormal;
flat in vec3 fragTangent;
flat in vec3 fragBitangent;
in vec3 fragViewDir;
in vec3 worldPos;

uniform vec4 ColorModulator;

uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

uniform sampler2D diffuse;
uniform sampler2D mask;
uniform sampler2D layer;
uniform sampler2D emission;
uniform sampler2D paradoxMask;

uniform int colorMethod;
uniform int effect;

uniform vec3 tint;

uniform int frame;

uniform vec3 baseColor1;
uniform vec3 baseColor2;
uniform vec3 baseColor3;
uniform vec3 baseColor4;
uniform vec3 baseColor5;
uniform vec3 emiColor1;
uniform vec3 emiColor2;
uniform vec3 emiColor3;
uniform vec3 emiColor4;
uniform vec3 emiColor5;
uniform float emiIntensity1;
uniform float emiIntensity2;
uniform float emiIntensity3;
uniform float emiIntensity4;
uniform float emiIntensity5;

uniform vec3 teraTint;

vec3 toLinear(vec3 color) {
    return pow(max(color, vec3(0.0)), vec3(2.2));
}

const float DEFAULT_METALLIC = 0.04;
const float TERASTAL_SMOOTHNESS = 0.9;

vec4 adjust(vec4 color) {
    return clamp(color * 2.0, 0.0, 1.0);
}

float adjustScalar(float color) {
    return clamp(color * 2.0, 0.0, 1.0);
}

float getMaskIntensity() {
    return texture(mask, texCoord0).r;
}

vec3 applyEmission(vec3 base, vec3 emissionColor, float intensity) {
    return base + (emissionColor - base) * intensity;
}

vec4 layered(vec2 texCoord) {
    vec4 color = texture(diffuse, texCoord);
    vec4 layerMasks = adjust(texture(layer, texCoord));
    float maskColor = adjustScalar(getMaskIntensity());

    vec3 base = mix(color.rgb, color.rgb * baseColor1, layerMasks.r);
    base = mix(base, color.rgb * baseColor2, layerMasks.g);
    base = mix(base, color.rgb * baseColor3, layerMasks.b);
    base = mix(base, color.rgb * baseColor4, layerMasks.a);
    base = mix(base, color.rgb * baseColor5, maskColor);

    base = mix(base, applyEmission(base, emiColor1, emiIntensity1), layerMasks.r);
    base = mix(base, applyEmission(base, emiColor2, emiIntensity2), layerMasks.g);
    base = mix(base, applyEmission(base, emiColor3, emiIntensity3), layerMasks.b);
    base = mix(base, applyEmission(base, emiColor4, emiIntensity4), layerMasks.a);
    base = mix(base, applyEmission(vec3(0.0), emiColor5, emiIntensity5), maskColor);

    return vec4(base, color.a);
}

vec4 masked(vec2 texCoord) {
    vec4 color = texture(diffuse, texCoord);
    float maskValue = texture(mask, texCoord).x;
    color.rgb = mix(color.rgb, color.rgb * baseColor1, maskValue);
    return color;
}

vec4 getColor(vec2 texCoord) {
    if (colorMethod == 1) {
        return layered(texCoord);
    } else if (colorMethod == 2) {
        return masked(texCoord);
    }

    return texture(diffuse, texCoord);
}

const float edgeThreshold = 0.08;
const float blockSize = 0.0015;

const vec2 offsets[8] = vec2[](
    vec2(-1.0, -1.0), vec2(0.0, -1.0), vec2(1.0, -1.0),
    vec2(-1.0,  0.0),                   vec2(1.0,  0.0),
    vec2(-1.0,  1.0), vec2(0.0,  1.0), vec2(1.0,  1.0)
);

float detectEdge(vec2 uv) {
    float centerIntensity = dot(getColor(uv).rgb, vec3(0.2126, 0.7152, 0.0722));
    float diffSum = 0.0;

    for (int i = 0; i < 8; i++) {
        vec2 offsetUV = uv + offsets[i] * blockSize;
        float neighborIntensity = dot(getColor(offsetUV).rgb, vec3(0.2126, 0.7152, 0.0722));
        diffSum += abs(centerIntensity - neighborIntensity);
    }

    return smoothstep(edgeThreshold - 0.02, edgeThreshold + 0.02, diffSum / 8.0);
}

vec3 bilateralFilter(vec2 uv) {
    vec3 colorSum = vec3(0.0);
    float weightSum = 0.0;

    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            vec2 offsetUV = uv + vec2(float(i), float(j)) * 0.003;
            vec3 sampleColor = texture(diffuse, offsetUV).rgb;
            float spatialWeight = exp(-float(i * i + j * j) / 2.0);
            float colorWeight = exp(-dot(sampleColor - texture(diffuse, uv).rgb, sampleColor - texture(diffuse, uv).rgb) / 0.1);
            float weight = spatialWeight * colorWeight;
            colorSum += sampleColor * weight;
            weightSum += weight;
        }
    }

    return colorSum / max(weightSum, 0.0001);
}

vec4 cartoon(vec4 inColor) {
    float edge = detectEdge(texCoord0);
    vec3 filtered = bilateralFilter(texCoord0);
    return vec4(mix(filtered, inColor.rgb, edge), inColor.a);
}

float getParadoxIntensity() {
    vec2 effectTexCoord = texCoord0;

    if (frame >= 0) {
        effectTexCoord *= 4.0;
        effectTexCoord = fract(effectTexCoord);
        effectTexCoord *= 0.25;
        effectTexCoord.x += float(frame % 4) / 4.0;
        effectTexCoord.y += float(frame / 4) / 4.0;
    }

    return clamp(texture(paradoxMask, effectTexCoord).r * 2.0, 0.0, 1.0);
}

vec4 paradox(vec4 color) {
    return mix(color, vec4(1.0), getParadoxIntensity());
}

vec4 galaxy(vec4 inColor) {
    float brightness = dot(inColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 gradientColor = mix(vec3(0.2, 0.0, 0.3), vec3(0.6, 0.1, 0.7), smoothstep(0.5, 1.0, brightness));
    inColor.rgb *= 0.3;
    inColor.rgb = mix(inColor.rgb, gradientColor, smoothstep(0.5, 1.0, brightness));
    return mix(inColor, vec4(1.0), getParadoxIntensity());
}

vec4 pastel(vec4 inColor) {
    vec2 wrappedUV = fract(texCoord0 * 5.0);
    float gradient = (sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159) + 1.0) * 0.5;
    vec3 pastelColor = mix(vec3(0.8, 0.9, 1.0), vec3(1.0, 0.8, 0.9), gradient);
    return vec4(mix(inColor.rgb, pastelColor, 0.5), inColor.a);
}

vec4 shadow(vec4 inColor) {
    float grayscale = dot(inColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec2 wrappedUV = fract(texCoord0 * 5.0);
    float gradient = (sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159) + 1.0) * 0.5;
    vec3 shadowColor = mix(vec3(0.1, 0.1, 0.2), vec3(0.05, 0.05, 0.1), gradient);
    return vec4(clamp(mix(vec3(grayscale), shadowColor, 0.7) * 0.9, 0.0, 1.0), inColor.a);
}

vec4 sketch(vec4 inColor) {
    float diffSum = 0.0;

    for (int i = 0; i < 8; i++) {
        vec3 neighborColor = getColor(texCoord0 + offsets[i] * 0.0015).rgb;
        diffSum += abs(dot(neighborColor - inColor.rgb, vec3(0.2126, 0.7152, 0.0722)));
    }

    float value = smoothstep(0.01, 0.03, diffSum * 0.125);
    return vec4(value, value, value, inColor.a);
}

vec4 vintage(vec4 inColor) {
    float grayscale = dot(inColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    return vec4(vec3(grayscale), inColor.a);
}

vec4 process(vec4 color) {
    if (effect == 1) return cartoon(color);
    if (effect == 2) return galaxy(color);
    if (effect == 3) return paradox(color);
    if (effect == 4) return pastel(color);
    if (effect == 5) return shadow(color);
    if (effect == 6) return sketch(color);
    if (effect == 7) return vintage(color);
    return color;
}

mat3 getTBN() {
    vec3 N = normalize(fragNormal);
    vec3 T = normalize(fragTangent);
    vec3 B = normalize(fragBitangent);
    return mat3(T, B, N);
}

#define TERA_LIGHT_DIRECT vec3(0.3, 0.9, 0.0)
#define FACET_RES 8.0
#define SHIMMER_BANDS 5.0
#define GAMMA_CORRECTION 1.4
#define SHIMMER_STRENGTH 0.8
#define IRIDESCENCE_STRENGTH 0.2

vec3 calculateTersaalizationEffect(vec3 baseColor) {
    mat3 TBN = getTBN();
    vec3 N = normalize(TBN[2]);
    vec3 V = normalize(fragViewDir);

    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 5.0);
    float lightResponse = max(dot(N, TERA_LIGHT_DIRECT), 0.0);

    vec3 faceted = normalize(floor(N * FACET_RES + 0.5) / FACET_RES);
    float facetResponse = pow(max(dot(faceted, V), 0.0), 6.0);

    float shimmer = fresnel * 0.5 + lightResponse * 0.3 + facetResponse * 0.8;
    shimmer = pow(clamp(shimmer, 0.0, 1.0), GAMMA_CORRECTION);
    shimmer = floor(shimmer * SHIMMER_BANDS) / SHIMMER_BANDS;

    vec3 viewInTangent = normalize(TBN * V);
    float angleNoise = abs(sin(dot(viewInTangent.xy, vec2(12.9898, 78.233)) * 43758.5453));
    shimmer += angleNoise * 0.02;

    vec3 iridescent = vec3(1.0, 0.9, 0.8) + vec3(0.05, -0.02, 0.03) * sin(shimmer * 20.0);
    vec3 directionalTint = mix(teraTint, vec3(1.0), lightResponse);

    return baseColor
        + directionalTint * shimmer * SHIMMER_STRENGTH
        + iridescent * shimmer * IRIDESCENCE_STRENGTH;
}

void main() {
    vec4 baseColor = process(getColor(texCoord0)) * ColorModulator;

    if (baseColor.a < 0.004) {
        discard;
    }

    baseColor.rgb *= tint;

    vec3 teraColor = calculateTersaalizationEffect(baseColor.rgb);
    vec3 linearBaseColor = toLinear(baseColor.rgb);

    sceneColOut = vec4(toLinear(teraColor), baseColor.a);
    normalDataOut = normalize(fragNormal);
    albedoDataOut = linearBaseColor;
    materialDataOut = vec3(DEFAULT_METALLIC, TERASTAL_SMOOTHNESS, 0.5);
}
