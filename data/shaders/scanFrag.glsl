//
// Based on the default Processing light shaders:
// 
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightVert.glsl
// https://github.com/processing/processing/blob/master/core/src/processing/opengl/shaders/LightFrag.glsl
//

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

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
varying vec3 vEcPosition;
varying vec3 vEcNormal;
varying vec3 vBarycenter;
varying vec4 vColor;
varying vec4 vAmbient;
varying vec4 vSpecular;
varying vec4 vEmissive;
varying float vShininess;

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
// Calculates how close is the pixel to one of the grid lines
//
// Uses the method explained in the following tutorial: 
// http://madebyevan.com/shaders/grid/
//
float gridFactor(vec3 positionVector) {
	float coord = 0.07 * positionVector.z + 0.0003 * time;
    float derivative = fwidth(coord);
	return smoothstep(0.5 - derivative, 0.5, abs(fract(coord) - 0.5));
}

//
// Calculates how close is the pixel to the triangle edges
//
// Uses the method explained in the following tutorial: 
// http://codeflow.org/entries/2012/aug/02/easy-wireframe-display-with-barycentric-coordinates
//
float edgeFactor(vec3 barycentricVector) {
    vec3 derivative = fwidth(barycentricVector);
    vec3 relCoord = smoothstep(vec3(0.0), derivative, barycentricVector);
    return 1.0 - min(min(relCoord.x, relCoord.y), relCoord.z);
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
	// Position in eye coordinates
	vec3 ecPosition = vEcPosition;

	// Normal in eye coordinates
	vec3 ecNormal = normalize(vEcNormal);
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
			totalFrontSpecular += lightSpecular[i] * falloff * spotf * blinnPhongFactor(lightDir, ecPosition, ecNormal, vShininess);
			totalBackSpecular += lightSpecular[i] * falloff * spotf * blinnPhongFactor(lightDir, ecPosition, ecNormalInv, vShininess);
		}     
	}    
	
	// Calculate the fragment colors
	vec4 fragFrontColor = vColor;
	
	if (illuminateFrontFace == 1) {
		fragFrontColor = vec4(totalAmbient, 0) * vAmbient + 
		                 vec4(totalFrontDiffuse, 1) * vColor + 
		                 vec4(totalFrontSpecular, 0) * vSpecular + 
		                 vec4(vEmissive.rgb, 0);
    }
    
	vec4 fragBackColor = vec4(totalAmbient, 0) * backColor + 
                         vec4(totalBackDiffuse, 1) *  backColor + 
                         vec4(totalBackSpecular, 0) * backColor + 
                         vec4(vEmissive.rgb, 0);
	
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

	// Calculate the effect colors
	vec4 effectFrontColor = vec4(0.0);
	vec4 effectBackColor = vec4(0.0);
	
	if ((masked && fillWithColor == 1) || effect == 6 || effect == 7) {
		effectFrontColor = vec4(totalAmbient, 0) * effectColor +
                           vec4(totalFrontDiffuse, 1) * effectColor + 
                           vec4(totalFrontSpecular, 0) * effectColor + 
                           vec4(vEmissive.rgb, 0); 
		
		effectBackColor = vec4(totalAmbient, 0) * effectColor + 
                          vec4(totalBackDiffuse, 1) * effectColor + 
                          vec4(totalBackSpecular, 0) * effectColor + 
                          vec4(vEmissive.rgb, 0); 
	}
	
	if (effect == 6) {
		float factor = (invertEffect == 1) ? 1 - gridFactor(vWcPosition) : gridFactor(vWcPosition);
		vec4 gridLineColor = vec4(1.0, 0.0, 0.0, 1.0);
		fragFrontColor = vec4(mix(effectFrontColor, gridLineColor, factor));
		fragBackColor = vec4(mix(effectBackColor, gridLineColor, factor));
	} else if (effect == 7) {
		float factor = (invertEffect == 1) ? 1 - edgeFactor(vBarycenter) : edgeFactor(vBarycenter);
		vec4 gridLineColor = vec4(1.0, 0.0, 0.0, 1.0);
		fragFrontColor = vec4(mix(effectFrontColor, gridLineColor, factor));
		fragBackColor = vec4(mix(effectBackColor, gridLineColor, factor));
	}
	
	// Fragment shader output
	if (masked) {
		if(fillWithColor == 1) {
			gl_FragColor = gl_FrontFacing ? effectFrontColor : effectBackColor;
		} else {
			discard;
		}
	} else {
		gl_FragColor = gl_FrontFacing ? fragFrontColor : fragBackColor;
	}
}
