// Uniforms
uniform mat4 modelviewMatrix;
uniform mat4 transformMatrix;
uniform mat3 normalMatrix;
uniform mat4 modelviewInvMatrix;
uniform vec4 lightPosition[8];
uniform int effect;
uniform int time;

// Attributes
attribute vec4 position;
attribute vec4 color;
attribute vec3 normal;

// Varyings
varying vec3 vWcPosition;
varying vec4 vColor;
varying vec3 vEcNormal;

//
// The pulsation effect 
//
vec3 pulsationEffect(vec3 normalVector) {
	return 7.0 * normalVector * (1.0 - cos(0.004 * time));
}

//
// Normalizes the vector coordinates to have values between -0.5 and 0.5 
//
vec3 normalizeCoordinates(vec3 vector) {
	return vec3(vector)/500.0;
}

//
// Main program
//
void main() {
	// Get the position in world coordinates
	vec4 wcPosition = modelviewMatrix * position * modelviewInvMatrix;
  
  	// Apply some of the effects
	vec4 newWcPosition = wcPosition;
	
	if(effect == 1) {
		newWcPosition.xyz += pulsationEffect(normal);
	}

	// Save the varyings
	vWcPosition = newWcPosition.xyz;
	vColor = color;
	vEcNormal = normalize(normalMatrix * normal);
  
	// Vertex in clip coordinates
	gl_Position = transformMatrix * newWcPosition;
}
