//
// Based on the default Processing light vertex shader:
// 
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightFrag.glsl
//

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Varyings
varying vec4 vFrontColor;
varying vec4 vBackColor;

//
// Main program
//
void main() {
	// Fragment shader output
	gl_FragColor = gl_FrontFacing ? vFrontColor : vBackColor;
}
