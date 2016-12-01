//
// Based on the default Processing light shaders:
// 
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightVert.glsl
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightFrag.glsl
//

// Matrix uniforms
uniform mat4 modelviewMatrix;
uniform mat4 transformMatrix;
uniform mat3 normalMatrix;

// Scan specific uniforms
uniform float time;
uniform int effect;

// Vertex attributes
attribute vec4 position;
attribute vec3 normal;
attribute vec3 barycenter;
attribute vec4 color;

// Light attributes
attribute vec4 ambient;
attribute vec4 specular;
attribute vec4 emissive;
attribute float shininess;

// Varyings
varying vec3 vWcPosition;
varying vec3 vEcPosition;
varying vec3 vEcNormal;
varying vec3 vBarycenter;
varying vec4 vColor;
varying vec4 vAmbient;
varying vec4 vSpecular;
varying vec4 vEmissive;
varying float vShininess;

//
// The pulsation effect 
//
vec3 pulsationEffect(vec3 normalVector) {
	return 7.0 * normalVector * (1.0 - cos(0.004 * time));
}

//
// Main program
//
void main() {
	// Apply some of the effects
	vec4 wcPosition = position;

	if(effect == 1) {
		wcPosition.xyz += pulsationEffect(normal);
	}

	// Save the varyings
	vWcPosition = wcPosition.xyz;
	vEcPosition = vec3(modelviewMatrix * wcPosition);
	vEcNormal = normalize(normalMatrix * normal);
	vBarycenter = barycenter;
	vColor = color;
	vAmbient = ambient;
	vSpecular = specular;
	vEmissive = emissive;
	vShininess = shininess;
	
	// Vertex shader output
	gl_Position = transformMatrix * wcPosition;
}