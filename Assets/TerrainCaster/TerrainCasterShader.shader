Shader "Hidden/TerrainCasterQuad" {
Properties {
	_MainTex ("Base (RGB)", 2D) = "black" {}
}

CGINCLUDE

	#include "UnityCG.cginc"

	uniform sampler2D _MainTex;
	uniform sampler2D _CameraDepthTexture;
	
	uniform float4 _MainTex_TexelSize;
	
	uniform sampler2D _HeightMap;
	uniform sampler2D _HeightMapLin;
	uniform sampler2D _ColorMap;
	
	uniform half _HeightMapSize;
	
	uniform float _Height;
	uniform half _Noise;
	uniform half _StepSize;
	uniform half _LOD;
	uniform half _Skip;
	uniform half _SkipMin;
	uniform half _Roof;
	uniform half _Scale;
	uniform half _ShadowOffset;
	uniform half _SmoothOffset;
	
	// for fast world space reconstruction
	
	uniform float4x4 _FrustumCornersWS;
	uniform float4 _CameraWS;
	 
	struct v2f {
		float4 pos : POSITION;
		float2 uv : TEXCOORD0;
		float2 uv_depth : TEXCOORD1;
		float4 interpolatedRay : TEXCOORD2;
	};
	
	v2f vert( appdata_img v )
	{
		v2f o;
		half index = v.vertex.z;
		v.vertex.z = 0.1;
		o.pos = mul(UNITY_MATRIX_MVP, v.vertex);
		o.uv = v.texcoord.xy;
		o.uv_depth = v.texcoord.xy;
		
		#if UNITY_UV_STARTS_AT_TOP
		if (_MainTex_TexelSize.y < 0)
			o.uv.y = 1-o.uv.y;
		#endif				
		
		o.interpolatedRay = _FrustumCornersWS[(int)index];
		o.interpolatedRay.w = index;
		
		return o;
	}
	
	float4 atlas(float x, float y, float tx, float ty)
	{
		float width = 16;
		float height = 9;
	    x /= width;
	    y /= height; 
	    
		x *= 10.0;
		y *= 10.0;
	    
	    x += tx * (1.0 / width);
	    y += ty * (1.0 / height);
	    
	    return tex2Dlod(_ColorMap, float4(x,y,0,0));
	}
	
	half4 fragTerrainCaster (v2f i) : COLOR
	{
		float origDepth = LinearEyeDepth(UNITY_SAMPLE_DEPTH(tex2D(_CameraDepthTexture,i.uv_depth)));
		float stepSize = _StepSize;
		float3 dir = normalize(i.interpolatedRay.xyz);
		float3 coord = _CameraWS.xyz;		
		
		float diff;
		float travDist = 0;
		bool found = false;
		bool rayDirUpwards = dir.y >= 0;
		float xAngle = length(dir.xz);
		float yAngle = length(dir.y);
		float func = dir.y / xAngle;
		int loopSize = 96;
		
		float3 values;
		float realHeight;
		float dy;
		float dx;
		float dx2;
		
		float coordY01;
		
		float4 debugColor = float4(1);
		
		if (coord.y > _Height) {
			if (rayDirUpwards) {
				loopSize = 0;
			} else {
				dy = coord.y - _Height + 0.01;
				
				diff = dy / yAngle;
				
				travDist += diff;
				coord += dir * diff;
			}
		}
		
	    for(int i = 1; i < loopSize; i++) { 
			if (travDist > origDepth || coord.y > _Height) {
				break;
			}
			
			values = tex2Dlod(_HeightMap, float4(coord.xz / _Scale,0,0)).rgb;
			
			coordY01 = coord.y / _Height;
			if (coordY01 < values.r) {
				found = true;
				break;
			}
			realHeight = values.r * _Height;
			
			diff = _SkipMin;
			
			if (values.g > 0) {
				dx = values.g * _Skip;
				dy = coord.y - realHeight;
				diff = min(dx / xAngle, dy / yAngle);
			} else {
				dx = 0;
			}
			
			if (values.b > values.g) {
				dx2 = (coordY01 - values.r) / ((1 - values.r) / values.b - func);
				if (dx2 > 0 && dx2 < 1.0) {
					diff = max(diff, dx2 / xAngle);
				} else { 
					dy = _Height - coord.y;
					diff = min(diff, dy / yAngle);
				}
			}
			
			diff += pow(float(i) / float(loopSize) * stepSize, 2.0) + travDist / _LOD;
			
			travDist += diff;
			coord += dir * diff;
	    }
		
		coord.xz /= _Scale;
		
		if (found) {
			float shade = (values.g + coordY01) / 2.0 * 0.8 + 0.2;
			float shadow = 1;
	    	for(int i = 0; i < 4; i++) { 
	    		shadow *= 1 + (values.r - tex2Dlod(_HeightMap, float4(coord.x + _ShadowOffset * i , coord.z - _ShadowOffset * i,0,0)).r) * 2.0;
	    	}
	    	
	    	shadow = clamp(shadow, 0, 1);
	    	shade *= shadow;
	    		    		    		    	
//			float y = coord.y / _Height;
//			if (y < values.r - _Roof) {  
//				float x = (coord.x + coord.z);
//				return atlas(x, y,0,0) * shade * float4(debugColor);
//			} else {
//				float x = coord.x;
//				float y = coord.z;
//				return atlas(x, y,0,0).gbra * shade * float4(debugColor);
//			}
			return tex2Dlod(_ColorMap, float4(coord.xz,0,0)) * shade;
		} else {
			return tex2Dlod(_MainTex, float4(i.uv, 0, 0)) * float4(debugColor); 
		}
	}


ENDCG

SubShader {
	Pass {
		ZTest Always Cull Off ZWrite Off
		Fog { Mode off }

		CGPROGRAM

		#pragma vertex vert
		#pragma fragment fragTerrainCaster
		#pragma fragmentoption ARB_precision_hint_fastest
		#pragma target 3.0
		#pragma glsl
		
		ENDCG
	}
}

Fallback off

}