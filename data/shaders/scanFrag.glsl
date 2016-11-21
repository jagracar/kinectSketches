//
// Based on the default Processing light vertex shader:
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightFrag.glsl
//

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Matrix uniforms
uniform mat4 modelviewMatrix;
uniform mat4 transformMatrix;
uniform mat3 normalMatrix;

// Light uniforms
uniform int lightCount;
uniform vec4 lightPosition[8];
uniform vec3 lightNormal[8];
uniform vec3 lightAmbient[8];
uniform vec3 lightDiffuse[8];
uniform vec3 lightSpecular[8];      
uniform vec3 lightFalloff[8];
uniform vec2 lightSpot[8];

// Scan specific uniforms
uniform int illuminateFrontFace;
uniform vec4 backColor;
uniform int time;
uniform int effect;
uniform int invertEffect;
uniform int fillWithColor;
uniform vec4 effectColor;

// Varyings
varying vec3 vWcPosition;
varying vec4 vColor;
varying vec3 vEcNormal;
varying vec4 vAmbient;
varying vec4 vSpecular;
varying vec4 vEmissive;
varying float vShininess;

// Constants
const float zero_float = 0.0;
const float one_float = 1.0;
const vec3 zero_vec3 = vec3(0);

//
// Calculates the falloff factor
//
float falloffFactor(vec3 lightPos, vec3 vertPos, vec3 coeff) {
	vec3 lpv = lightPos - vertPos;
	vec3 dist = vec3(one_float);
	dist.z = dot(lpv, lpv);
	dist.y = sqrt(dist.z);
	return one_float / dot(dist, coeff);
}

//
// Calculates the spot factor
//
float spotFactor(vec3 lightPos, vec3 vertPos, vec3 lightNorm, float minCos, float spotExp) {
	vec3 lpv = normalize(lightPos - vertPos);
	vec3 nln = -one_float * lightNorm;
	float spotCos = dot(nln, lpv);
	return spotCos <= minCos ? zero_float : pow(spotCos, spotExp);
}

//
// Calculates the Lamber factor
//
float lambertFactor(vec3 lightDir, vec3 vecNormal) {
	return max(zero_float, dot(lightDir, vecNormal));
}

//
// Calculates the BlinnPhong factor
//
float blinnPhongFactor(vec3 lightDir, vec3 vertPos, vec3 vecNormal, float shine) {
	vec3 np = normalize(vertPos);
	vec3 ldp = normalize(lightDir - np);
	return pow(max(zero_float, dot(ldp, vecNormal)), shine);
}

//
// The hole effect 
//
bool holeEffect() {
	return vWcPosition.z < -30.0 - 40.0 * (1.0 + cos(0.002 * time));
}

//
// The circle effect 
//
bool circleEffect() {
	return length(vWcPosition.xy) < 80.0 * (1.0 + cos(0.001 * time));
}

//
// The vertical cut effect 
//
bool verticalCutEffect() {
	return abs(vWcPosition.x) < 95.0 * (1.0 - 1.2 * cos(0.001 * time));
}

//
// Main program
//
void main() {
	// Apply some of the effects
	bool masked = false;
	
	if(effect == 2) {
		masked = holeEffect() != (invertEffect == 1);
	} else if(effect == 3) {
		masked = circleEffect() != (invertEffect == 1);
	} else if(effect == 4) {
		masked = verticalCutEffect() != (invertEffect == 1);
	}

	// Vertex in eye coordinates
	vec3 ecVertex = vec3(modelviewMatrix * vec4(vWcPosition, 1.0));
	
	// Normal vector in eye coordinates
	vec3 ecNormal = vEcNormal;
	vec3 ecNormalInv = ecNormal * -one_float;
  
	// Light calculations
	vec3 totalAmbient = vec3(0, 0, 0);
	vec3 totalFrontDiffuse = vec3(0, 0, 0);
	vec3 totalFrontSpecular = vec3(0, 0, 0);
	vec3 totalBackDiffuse = vec3(0, 0, 0);
	vec3 totalBackSpecular = vec3(0, 0, 0);
	
	for (int i = 0; i < 8; i++) {
		if (lightCount == i) break;
    	
		vec3 lightPos = lightPosition[i].xyz;
		bool isDir = lightPosition[i].w < one_float;
		float spotCos = lightSpot[i].x;
		float spotExp = lightSpot[i].y;
    
		vec3 lightDir;
		float falloff;    
		float spotf;
		
		if (isDir) {
			falloff = one_float;
			lightDir = -one_float * lightNormal[i];
		} else {
			falloff = falloffFactor(lightPos, ecVertex, lightFalloff[i]);  
			lightDir = normalize(lightPos - ecVertex);
		}
		
		spotf = spotExp > zero_float ? spotFactor(lightPos, ecVertex, lightNormal[i], spotCos, spotExp) : one_float;
    
		if (any(greaterThan(lightAmbient[i], zero_vec3))) {
			totalAmbient += lightAmbient[i] * falloff;
		}
		
		if (any(greaterThan(lightDiffuse[i], zero_vec3))) {
			totalFrontDiffuse += lightDiffuse[i] * falloff * spotf * lambertFactor(lightDir, ecNormal);
			totalBackDiffuse += lightDiffuse[i] * falloff * spotf * lambertFactor(lightDir, ecNormalInv);
		}
		
		if (any(greaterThan(lightSpecular[i], zero_vec3))) {
			totalFrontSpecular += lightSpecular[i] * falloff * spotf * blinnPhongFactor(lightDir, ecVertex, ecNormal, vShininess);
			totalBackSpecular += lightSpecular[i] * falloff * spotf * blinnPhongFactor(lightDir, ecVertex, ecNormalInv, vShininess);
		}     
	}    
	
	// Calculating final color as result of all lights (plus emissive term).
	// Transparency is determined exclusively by the diffuse component.
	vec4 vertColor;
	
	if (illuminateFrontFace == 1) {
		vertColor = vec4(totalAmbient, 0) * vAmbient + 
                    vec4(totalFrontDiffuse, 1) * vColor + 
                    vec4(totalFrontSpecular, 0) * vSpecular + 
                    vec4(vEmissive.rgb, 0);
	} else {
		vertColor = vColor;
	}
  
	vec4 backVertColor = vec4(totalAmbient, 0) * backColor + 
                    vec4(totalBackDiffuse, 1) *  backColor + 
                    vec4(totalBackSpecular, 0) * backColor + 
                    vec4(vEmissive.rgb, 0);


	if (masked) {
		if(fillWithColor == 1) {
			if (gl_FrontFacing) {
				gl_FragColor = vec4(totalAmbient, 0) * effectColor + 
                               vec4(totalFrontDiffuse, 1) * effectColor + 
                               vec4(totalFrontSpecular, 0) * effectColor + 
                               vec4(vEmissive.rgb, 0); 
			} else {
				gl_FragColor = vec4(totalAmbient, 0) * effectColor + 
                               vec4(totalBackDiffuse, 1) * effectColor + 
                               vec4(totalBackSpecular, 0) * effectColor + 
                               vec4(vEmissive.rgb, 0); 
			}
		} else {
			discard;
		}
	} else if (gl_FrontFacing) {
		gl_FragColor = vertColor;  
	} else {
		gl_FragColor = backVertColor;  
	}
}