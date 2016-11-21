//
// Based on the default Processing light vertex shader:
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightFrag.glsl
//

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Varyings
varying vec4 vertColor;
varying vec4 backVertColor;

//
// Main program
//
void main() {
	gl_FragColor = gl_FrontFacing ? vertColor : backVertColor;
}