//
// Based on the default Processing point shaders:
// 
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/PointVert.glsl
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/PointFrag.glsl
//

// Matrix uniforms
uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

// Scan specific uniforms
uniform float time;
uniform int effect;

// Vertex attributes
attribute vec4 position;
attribute vec3 normal;
attribute vec4 color;
attribute vec2 offset;

// Varyings
varying vec3 vWcPosition;
varying vec4 vColor;

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
		//wcPosition.xyz += pulsationEffect(normal);
	}
	
	// Save the varyings
	vWcPosition = wcPosition.xyz;
	vColor = color;

	// Vertex shader output
	gl_Position = transformMatrix * wcPosition + projectionMatrix * vec4(offset.xy, 0, 0);	
}
