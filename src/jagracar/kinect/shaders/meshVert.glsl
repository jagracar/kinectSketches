//
// Based on the default Processing light vertex shader:
// 
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightVert.glsl
//

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

// Vertex attributes
attribute vec4 position;
attribute vec3 normal;
attribute vec4 color;

// Light attributes
attribute vec4 ambient;
attribute vec4 specular;
attribute vec4 emissive;
attribute float shininess;

// Varyings
varying vec4 vFrontColor;
varying vec4 vBackColor;

// Constants
const float zero_float = 0.0;
const float one_float = 1.0;
const vec3 zero_vec3 = vec3(0);

//
// Calculates the light falloff factor
//
float falloffFactor(vec3 lightPos, vec3 vertPos, vec3 coeff) {
	vec3 lpv = lightPos - vertPos;
	vec3 dist = vec3(one_float);
	dist.z = dot(lpv, lpv);
	dist.y = sqrt(dist.z);
	return one_float / dot(dist, coeff);
}

//
// Calculates the light spot factor
//
float spotFactor(vec3 lightPos, vec3 vertPos, vec3 lightNorm, float minCos, float spotExp) {
	vec3 lpv = normalize(lightPos - vertPos);
	vec3 nln = -one_float * lightNorm;
	float spotCos = dot(nln, lpv);
	return spotCos <= minCos ? zero_float : pow(spotCos, spotExp);
}

//
// Calculates the Lambert illumination factor
//
float lambertFactor(vec3 lightDir, vec3 vecNormal) {
	return max(zero_float, dot(lightDir, vecNormal));
}

//
// Calculates the Blinn Phong illumination factor
//
float blinnPhongFactor(vec3 lightDir, vec3 vertPos, vec3 vecNormal, float shine) {
	vec3 np = normalize(vertPos);
	vec3 ldp = normalize(lightDir - np);
	return pow(max(zero_float, dot(ldp, vecNormal)), shine);
}

//
// Main program
//
void main() {
	// Position in eye coordinates
	vec3 ecPosition = vec3(modelviewMatrix * position);
	
	// Normal in eye coordinates
	vec3 ecNormal = normalize(normalMatrix * normal);
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
			falloff = falloffFactor(lightPos, ecPosition, lightFalloff[i]);  
			lightDir = normalize(lightPos - ecPosition);
		}
		
		spotf = spotExp > zero_float ? spotFactor(lightPos, ecPosition, lightNormal[i], spotCos, spotExp) : one_float;
    
		if (any(greaterThan(lightAmbient[i], zero_vec3))) {
			totalAmbient += lightAmbient[i] * falloff;
		}
		
		if (any(greaterThan(lightDiffuse[i], zero_vec3))) {
			totalFrontDiffuse += lightDiffuse[i] * falloff * spotf * lambertFactor(lightDir, ecNormal);
			totalBackDiffuse += lightDiffuse[i] * falloff * spotf * lambertFactor(lightDir, ecNormalInv);
		}
		
		if (any(greaterThan(lightSpecular[i], zero_vec3))) {
			totalFrontSpecular += lightSpecular[i] * falloff * spotf * blinnPhongFactor(lightDir, ecPosition, ecNormal, shininess);
			totalBackSpecular += lightSpecular[i] * falloff * spotf * blinnPhongFactor(lightDir, ecPosition, ecNormalInv, shininess);
		}     
	}    

	// Update the varyings	
	vFrontColor = color;
	
	if (illuminateFrontFace == 1) {
		vFrontColor = vec4(totalAmbient, 0) * ambient + 
		              vec4(totalFrontDiffuse, 1) * color + 
		              vec4(totalFrontSpecular, 0) * specular + 
		              vec4(emissive.rgb, 0);
	}
	
	vBackColor = vec4(totalAmbient, 0) * backColor + 
                 vec4(totalBackDiffuse, 1) *  backColor + 
                 vec4(totalBackSpecular, 0) * backColor + 
                 vec4(emissive.rgb, 0);
	
	// Vertex shader output
	gl_Position = transformMatrix * position;
}