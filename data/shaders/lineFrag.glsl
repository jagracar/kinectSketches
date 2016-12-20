//
// Based on the default Processing point shaders:
// 
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LineVert.glsl
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LineFrag.glsl
//

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// Scan specific uniforms
uniform float time;
uniform int effect;
uniform int invertEffect;
uniform vec4 effectColor;
uniform int fillWithColor;
uniform sampler2D mask;
uniform int cursorArraySize;
uniform vec3 cursorArray[150];

// Varyings
varying vec3 vWcPosition;
varying vec4 vColor;

// Constants
const float zero_float = 0.0;
const float one_float = 1.0;
const vec3 zero_vec3 = vec3(0);

// 
// Clasic 3D Perlin noise implementation by Stefan Gustavson.
// https://github.com/ashima/webgl-noise
//
vec3 mod289(vec3 x) {
	return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x) {
	return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x) {
	return mod289(((x * 34.0) + 1.0) * x);
}

vec4 taylorInvSqrt(vec4 r) {
	return 1.79284291400159 - 0.85373472095314 * r;
}

vec3 fade(vec3 t) {
	return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

float cnoise(vec3 P) {
	vec3 Pi0 = floor(P);
	vec3 Pi1 = Pi0 + vec3(1.0);	
	Pi0 = mod289(Pi0);
	Pi1 = mod289(Pi1);
	vec3 Pf0 = fract(P);
	vec3 Pf1 = Pf0 - vec3(1.0);
	vec4 ix = vec4(Pi0.x, Pi1.x, Pi0.x, Pi1.x);
	vec4 iy = vec4(Pi0.yy, Pi1.yy);
	vec4 iz0 = Pi0.zzzz;
	vec4 iz1 = Pi1.zzzz;
	vec4 ixy = permute(permute(ix) + iy);
	vec4 ixy0 = permute(ixy + iz0);
	vec4 ixy1 = permute(ixy + iz1);
	vec4 gx0 = ixy0 * (1.0 / 7.0);
	vec4 gy0 = fract(floor(gx0) * (1.0 / 7.0)) - 0.5;
	gx0 = fract(gx0);
	vec4 gz0 = vec4(0.5) - abs(gx0) - abs(gy0);
	vec4 sz0 = step(gz0, vec4(0.0));
	gx0 -= sz0 * (step(0.0, gx0) - 0.5);
	gy0 -= sz0 * (step(0.0, gy0) - 0.5);
	vec4 gx1 = ixy1 * (1.0 / 7.0);
	vec4 gy1 = fract(floor(gx1) * (1.0 / 7.0)) - 0.5;
	gx1 = fract(gx1);
	vec4 gz1 = vec4(0.5) - abs(gx1) - abs(gy1);
	vec4 sz1 = step(gz1, vec4(0.0));
	gx1 -= sz1 * (step(0.0, gx1) - 0.5);
	gy1 -= sz1 * (step(0.0, gy1) - 0.5);
	vec3 g000 = vec3(gx0.x,gy0.x,gz0.x);
	vec3 g100 = vec3(gx0.y,gy0.y,gz0.y);
	vec3 g010 = vec3(gx0.z,gy0.z,gz0.z);
	vec3 g110 = vec3(gx0.w,gy0.w,gz0.w);
	vec3 g001 = vec3(gx1.x,gy1.x,gz1.x);
	vec3 g101 = vec3(gx1.y,gy1.y,gz1.y);
	vec3 g011 = vec3(gx1.z,gy1.z,gz1.z);
	vec3 g111 = vec3(gx1.w,gy1.w,gz1.w);
	vec4 norm0 = taylorInvSqrt(vec4(dot(g000, g000), dot(g010, g010), dot(g100, g100), dot(g110, g110)));
	g000 *= norm0.x;
	g010 *= norm0.y;
	g100 *= norm0.z;
	g110 *= norm0.w;
	vec4 norm1 = taylorInvSqrt(vec4(dot(g001, g001), dot(g011, g011), dot(g101, g101), dot(g111, g111)));
	g001 *= norm1.x;
	g011 *= norm1.y;
	g101 *= norm1.z;
	g111 *= norm1.w;
	float n000 = dot(g000, Pf0);
	float n100 = dot(g100, vec3(Pf1.x, Pf0.yz));
	float n010 = dot(g010, vec3(Pf0.x, Pf1.y, Pf0.z));
	float n110 = dot(g110, vec3(Pf1.xy, Pf0.z));
	float n001 = dot(g001, vec3(Pf0.xy, Pf1.z));
	float n101 = dot(g101, vec3(Pf1.x, Pf0.y, Pf1.z));
	float n011 = dot(g011, vec3(Pf0.x, Pf1.yz));
	float n111 = dot(g111, Pf1);
	vec3 fade_xyz = fade(Pf0);
	vec4 n_z = mix(vec4(n000, n100, n010, n110), vec4(n001, n101, n011, n111), fade_xyz.z);
	vec2 n_yz = mix(n_z.xy, n_z.zw, fade_xyz.y);
	float n_xyz = mix(n_yz.x, n_yz.y, fade_xyz.x); 
	return 2.2 * n_xyz;
}

//
// The Perlin noise effect 
//
bool perlinNoiseEffect(vec3 positionVector) {
	return cnoise(vec3(0.025 * positionVector.xy, 0.0002 * time)) > 0.1;
}

//
// The hole effect 
//
bool holeEffect(vec3 positionVector) {
	return positionVector.z < -70.0 - 40.0 * cos(0.002 * time);
}

//
// The circle effect 
//
bool circleEffect(vec3 positionVector) {
	return length(positionVector.xy) < 90.0 * (0.9 + cos(0.001 * time));
}

//
// The vertical cut effect 
//
bool verticalCutEffect(vec3 positionVector) {
	return abs(positionVector.x) < 110.0 * (0.9 - cos(0.001 * time));
}

//
// The mask effect
//
bool maskEffect(vec3 positionVector, sampler2D texture) {
	vec4 maskValue = texture2D(texture, vec2(0.5 + positionVector.x/500, 0.5 - positionVector.y/500));
	return all(greaterThan(maskValue.rgb, vec3(0.5)));
}

//
// The cursor effect
//
bool cursorEffect(vec3 positionVector) {
	for(int i = 0; i < cursorArraySize; i++){
		if(length(positionVector - cursorArray[i]) < 10.0){
			return true;
		}		
	}
	
	return false;
}

//
// Main program
//
void main() {	
	// Apply some of the effects
	bool masked = false;

	if (effect == 2) {
		masked = perlinNoiseEffect(vWcPosition) != (invertEffect == 1);
	} else if (effect == 3) {
		masked = holeEffect(vWcPosition) != (invertEffect == 1);
	} else if (effect == 4) {
		masked = circleEffect(vWcPosition) != (invertEffect == 1);
	} else if (effect == 5) {
		masked = verticalCutEffect(vWcPosition) != (invertEffect == 1);
	} else if (effect >= 8 && effect < 11) {
		masked = maskEffect(vWcPosition, mask) != (invertEffect == 1);
	} else if (effect == 11){
		masked = cursorEffect(vWcPosition) != (invertEffect == 1);
	}
	
	// Fragment shader output
	if (masked) {
		if(fillWithColor == 1) {
			gl_FragColor = effectColor;
		} else {
			discard;
		}
	} else {
		gl_FragColor = vColor;
	}
}
