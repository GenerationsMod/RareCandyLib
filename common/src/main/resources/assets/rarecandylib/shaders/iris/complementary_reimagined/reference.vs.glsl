#ifdef VERTEX_SHADER

out vec2 texCoord;
out vec2 lmCoord;

flat out vec3 upVec, sunVec, northVec, eastVec;
out vec3 normal;

out vec4 glColor;

#if defined GENERATED_NORMALS || defined COATED_TEXTURES || defined POM || defined IPBR && defined IS_IRIS
out vec2 signMidCoordPos;
flat out vec2 absMidCoordPos;
flat out vec2 midCoord;
#endif

#if defined GENERATED_NORMALS || defined CUSTOM_PBR
flat out vec3 binormal, tangent;
#endif

#ifdef POM
out vec3 viewVector;

out vec4 vTexCoordAM;
#endif

//Attributes//
#if defined GENERATED_NORMALS || defined COATED_TEXTURES || defined POM || defined IPBR && defined IS_IRIS
attribute vec4 mc_midTexCoord;
#endif

#if defined GENERATED_NORMALS || defined CUSTOM_PBR
attribute vec4 at_tangent;
#endif

//Common Variables//

//Common Functions//

//Includes//

//Program//
void main() {
    gl_Position = ftransform();

    texCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    lmCoord  = GetLightMapCoordinates();

    lmCoord.x = min(lmCoord.x, 0.9);
    //Fixes some servers/mods making entities insanely bright, while also slightly reducing the max blocklight on a normal entity

    glColor = gl_Color;

    normal = normalize(gl_NormalMatrix * gl_Normal);

    upVec = normalize(gbufferModelView[1].xyz);
    eastVec = normalize(gbufferModelView[0].xyz);
    northVec = normalize(gbufferModelView[2].xyz);
    sunVec = GetSunVector();

    #if defined GENERATED_NORMALS || defined COATED_TEXTURES || defined POM || defined IPBR && defined IS_IRIS
    midCoord = (gl_TextureMatrix[0] * mc_midTexCoord).st;
    vec2 texMinMidCoord = texCoord - midCoord;
    signMidCoordPos = sign(texMinMidCoord);
    absMidCoordPos  = abs(texMinMidCoord);
    #endif

    #if defined GENERATED_NORMALS || defined CUSTOM_PBR
    binormal = normalize(gl_NormalMatrix * cross(at_tangent.xyz, gl_Normal.xyz) * at_tangent.w);
    tangent  = normalize(gl_NormalMatrix * at_tangent.xyz);
    #endif

    #ifdef POM
    mat3 tbnMatrix = mat3(
    tangent.x, binormal.x, normal.x,
    tangent.y, binormal.y, normal.y,
    tangent.z, binormal.z, normal.z
    );

    viewVector = tbnMatrix * (gl_ModelViewMatrix * gl_Vertex).xyz;

    vTexCoordAM.zw  = abs(texMinMidCoord) * 2;
    vTexCoordAM.xy  = min(texCoord, midCoord - texMinMidCoord);
    #endif

    #ifdef GBUFFERS_ENTITIES_GLOWING
    if (glColor.a > 0.99) gl_Position.z *= 0.01;
    #endif

    #ifdef FLICKERING_FIX
    if (entityId == 50008 || entityId == 50012) { // Item Frame, Glow Item Frame
        if (dot(normal, upVec) > 0.99) {
            vec4 position = gbufferModelViewInverse * gl_ModelViewMatrix * gl_Vertex;
            vec3 comPos = fract(position.xyz + cameraPosition);
            comPos = abs(comPos - vec3(0.5));
            if ((comPos.y > 0.437 && comPos.y < 0.438) || (comPos.y > 0.468 && comPos.y < 0.469)) {
                gl_Position.z += 0.0001;
            }
        }
        if (gl_Normal.y == 1.0) { // Maps
            normal = upVec * 2.0;
        }
    } else if (entityId == 50084) { // Slime, Chicken
        gl_Position.z -= 0.00015;
    }

    #if SHADOW_QUALITY == -1
    if (glColor.a < 0.5) gl_Position.z += 0.0005;
    #endif
    #endif
}

#endif