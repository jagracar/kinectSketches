#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Uniforms
uniform vec4 lightPosition[8];
uniform vec3 lightNormal[8];
uniform int effect;

// Varyings
varying vec3 vWcPosition;
varying vec4 vColor;
varying vec3 vEcNormal;

//
// The circle effect 
//
bool circleEffect(vec3 aa) {
	return aa.x < 0.0;
}

//
// Main program
//
void main() {
	// Apply some of the effects
	bool masked = false;
	
	if(effect == 2){
		masked = circleEffect(vWcPosition);
	}
	
	if(masked){
		discard;
	}
	
	if(gl_FrontFacing) {
		gl_FragColor = vColor;  
	} else {
		gl_FragColor = vec4(vec3(0.01) + vec3(1.0) * max(0.0, dot(normalize(vEcNormal), lightNormal[1])), 1.0);  
	}
}
