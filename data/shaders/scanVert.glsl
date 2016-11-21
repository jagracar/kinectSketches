//
// Based on the default Processing light vertex shader:
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightVert.glsl
//

// Matrix uniforms
uniform mat4 modelviewMatrix;
uniform mat4 transformMatrix;
uniform mat3 normalMatrix;

// Scan specific uniforms
uniform int time;
uniform int effect;

// Vertex attributes
attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

// Light attributes
attribute vec4 ambient;
attribute vec4 specular;
attribute vec4 emissive;
attribute float shininess;

// Varyings
varying vec3 vWcPosition;
varying vec4 vColor;
varying vec3 vEcNormal;
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
	// Get the position in world coordinates
	vec4 wcPosition = position;

  	// Apply some of the effects
	if(effect == 1) {
		wcPosition.xyz += pulsationEffect(normal);
	}

	// Save the varyings
	vWcPosition = wcPosition.xyz;
	vColor = color;
	vEcNormal = normalize(normalMatrix * normal);
	vAmbient = ambient;
	vSpecular = specular;
	vEmissive = emissive;
	vShininess = shininess;
	
	// Vertex shader output
	gl_Position = transformMatrix * wcPosition;
}