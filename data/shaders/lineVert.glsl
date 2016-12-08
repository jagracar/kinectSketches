//
// Based on the default Processing point shaders:
// 
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LineVert.glsl
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LineFrag.glsl
//

// Matrix uniforms
uniform mat4 modelviewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

// Line uniforms
uniform vec4 viewport;

// Scan specific uniforms
uniform float time;
uniform int effect;

// Vertex attributes
attribute vec4 position;
attribute vec3 normal;
attribute vec4 color;
attribute vec4 direction;

// Varyings
varying vec3 vWcPosition;
varying vec4 vColor;

//
// The pulsation effect 
//
vec3 pulsationEffect(vec3 normalVector) {
	return 7.0 * normalVector * (1.0 - cos(0.004 * time));
}

vec3 clipToWindow(vec4 clip, vec4 viewport) {
	vec3 post_div = clip.xyz / clip.w;
	vec2 xypos = (post_div.xy + vec2(1.0, 1.0)) * 0.5 * viewport.zw;
	return vec3(xypos, post_div.z * 0.5 + 0.5);
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


	vec4 clipp = transformMatrix * wcPosition;
	vec4 clipq = clipp + transformMatrix * vec4(direction.xyz, 0);
  
	vec3 windowp = clipToWindow(clipp, viewport); 
	vec3 windowq = clipToWindow(clipq, viewport); 
	vec3 tangent = windowq - windowp;

	vec2 perp = normalize(vec2(-tangent.y, tangent.x));
	float thickness = direction.w;
	vec2 offset = perp * thickness;

	// Vertex shader output
	gl_Position = transformMatrix * wcPosition + vec4(offset.xy, 0, 0);
}
