using UnityEngine;
using System.Collections;

[ExecuteInEditMode]
[AddComponentMenu("Terrain/TerrainCaster")]
public class TerrainCaster : MonoBehaviour
{	
	
	public enum FilterMode 
	{
	  Point, Bilinear, Trilinear
	}
		
	public Texture2D heightTexture;
	public Texture2D heightTextureLinear;
	public Texture2D colorTexture;
	public FilterMode heightFilterMode = FilterMode.Bilinear;
	public FilterMode heightLinearFilterMode = FilterMode.Bilinear;
	public FilterMode colorFilterMode = FilterMode.Bilinear;
	public float height = 1.0f;
	public float scale = 20.0f;
	public float noise = 1.0f;
	public float stepSize = 0.001f;
	public float LOD = 2.0f;
	public float skip = 0.3f;
	public float skipMin = 0.1f;
	public float roof = 0.01f;
	public float shadowOffset = 0.01f;
	public float smoothOffset = 0.01f;
	public bool debug = true;
	
	private Color[] pixelBuffer;
	private bool isSupported = true;
	private int errorCode = 0;
	
	private float CAMERA_NEAR = 0.5f;
	private float CAMERA_FAR = 50.0f;
	private float CAMERA_FOV = 60.0f;	
	private float CAMERA_ASPECT_RATIO = 1.333333f;
	
	public Shader fogShader;
	private Material fogMaterial = null;	
	
	int GetDistance(int x, int y) {
		return (int)new Vector2(x,y).magnitude;	
	}
	
	void BuildTexture() {
		heightTexture.filterMode = (UnityEngine.FilterMode) heightFilterMode;
		heightTexture.wrapMode = TextureWrapMode.Repeat;
		heightTextureLinear.filterMode = (UnityEngine.FilterMode) heightLinearFilterMode;
		heightTextureLinear.wrapMode = TextureWrapMode.Repeat;
		colorTexture.filterMode = (UnityEngine.FilterMode) colorFilterMode;
		colorTexture.wrapMode = TextureWrapMode.Repeat;
	}
	
	void AttachTexture(Material material) {
		BuildTexture();
		material.SetTexture("_HeightMap", heightTexture);
		material.SetTexture("_HeightMapLin", heightTextureLinear);
		material.SetTexture("_ColorMap", colorTexture);
		material.SetFloat("_Height", height);
		material.SetFloat("_Scale", scale);
		material.SetFloat("_Noise", noise);
		material.SetFloat("_StepSize", stepSize);
		material.SetFloat("_HeightMapSize", heightTexture.width);
		material.SetFloat("_LOD", LOD);
		material.SetFloat("_Skip", skip * scale);// 255.0f / heightTexture.width);
		material.SetFloat("_Roof", roof);
		material.SetFloat("_SkipMin", skipMin);
		material.SetFloat("_Scale", scale);
		material.SetFloat("_ShadowOffset", shadowOffset * height);
		material.SetFloat("_SmoothOffset", smoothOffset);
	}
	
	Material CheckShaderAndCreateMaterial (Shader s, Material m2Create) {
		if (!s) { 
			Debug.Log("Missing shader in " + this.ToString ());
			enabled = false;
			return null;
		}
			
		if (s.isSupported && m2Create && m2Create.shader == s) 
			return m2Create;
		
		if (!s.isSupported) {
			NotSupported (2);
			Debug.Log("The shader " + s.ToString() + " on effect "+this.ToString()+" is not supported on this platform!");
			return null;
		}
		else {
			m2Create = new Material (s);	
			m2Create.hideFlags = HideFlags.DontSave;
			BuildTexture();
			AttachTexture(m2Create);			
			if (m2Create) 
				return m2Create;
			else return null;
		}
	}
	
	void NotSupported(int code) {
		enabled = false;	
		isSupported = false;
		errorCode = code;
	}
	
	bool CheckSupport(bool needDepth) {
		isSupported = true;
		
		if (!SystemInfo.supportsImageEffects || !SystemInfo.supportsRenderTextures) {
			NotSupported (0);
			return false;
		}		
		
		if(needDepth && !SystemInfo.SupportsRenderTextureFormat (RenderTextureFormat.Depth)) {
			NotSupported (1);
			return false;
		}
		
		if(needDepth)
			camera.depthTextureMode |= DepthTextureMode.Depth;	
		
		return true;
	}
	
	void ReportAutoDisable () {
		Debug.LogWarning ("The image effect " + this.ToString() + " has been disabled as it's not supported on the current platform. ErrorCode: " + errorCode);
	}
	
	bool CheckResources () {	
		CheckSupport(true);
	
		fogMaterial = CheckShaderAndCreateMaterial(fogShader, fogMaterial);
		
		if(!isSupported)
			ReportAutoDisable ();
		return isSupported;				
	}

	void OnRenderImage (RenderTexture source, RenderTexture destination) {	
		if(CheckResources()==false) {
			Graphics.Blit (source, destination);
			return;
		}
		if (debug) {
			AttachTexture(fogMaterial);
		}
			
		CAMERA_NEAR = camera.nearClipPlane;
		CAMERA_FAR = camera.farClipPlane;
		CAMERA_FOV = camera.fieldOfView;
		CAMERA_ASPECT_RATIO = camera.aspect;
	
		Matrix4x4 frustumCorners = Matrix4x4.identity;		
		Vector4 vec;
		Vector3 corner;
	
		float fovWHalf = CAMERA_FOV * 0.5f;
		
		Vector3 toRight = camera.transform.right * CAMERA_NEAR * Mathf.Tan (fovWHalf * Mathf.Deg2Rad) * CAMERA_ASPECT_RATIO;
		Vector3 toTop = camera.transform.up * CAMERA_NEAR * Mathf.Tan (fovWHalf * Mathf.Deg2Rad);
	
		Vector3 topLeft = (camera.transform.forward * CAMERA_NEAR - toRight + toTop);
		float CAMERA_SCALE = topLeft.magnitude * CAMERA_FAR/CAMERA_NEAR;	
			
		topLeft.Normalize();
		topLeft *= CAMERA_SCALE;
	
		Vector3 topRight = (camera.transform.forward * CAMERA_NEAR + toRight + toTop);
		topRight.Normalize();
		topRight *= CAMERA_SCALE;
		
		Vector3 bottomRight = (camera.transform.forward * CAMERA_NEAR + toRight - toTop);
		bottomRight.Normalize();
		bottomRight *= CAMERA_SCALE;
		
		Vector3 bottomLeft = (camera.transform.forward * CAMERA_NEAR - toRight - toTop);
		bottomLeft.Normalize();
		bottomLeft *= CAMERA_SCALE;
				
		frustumCorners.SetRow (0, topLeft); 
		frustumCorners.SetRow (1, topRight);		
		frustumCorners.SetRow (2, bottomRight);
		frustumCorners.SetRow (3, bottomLeft);		
								
		fogMaterial.SetMatrix ("_FrustumCornersWS", frustumCorners);
		fogMaterial.SetVector ("_CameraWS", camera.transform.position);
		
		CustomGraphicsBlit (source, destination, fogMaterial, 0);
	}
	
	static void CustomGraphicsBlit (RenderTexture source, RenderTexture dest, Material fxMaterial, int passNr) {
		RenderTexture.active = dest;
		       
		fxMaterial.SetTexture ("_MainTex", source);	        
	        	        
		GL.PushMatrix ();
		GL.LoadOrtho ();	
	    	
		fxMaterial.SetPass (passNr);	
		
	    GL.Begin (GL.QUADS);
							
		GL.MultiTexCoord2 (0, 0.0f, 0.0f); 
		GL.Vertex3 (0.0f, 0.0f, 3.0f); // BL
		
		GL.MultiTexCoord2 (0, 1.0f, 0.0f); 
		GL.Vertex3 (1.0f, 0.0f, 2.0f); // BR
		
		GL.MultiTexCoord2 (0, 1.0f, 1.0f); 
		GL.Vertex3 (1.0f, 1.0f, 1.0f); // TR
		
		GL.MultiTexCoord2 (0, 0.0f, 1.0f); 
		GL.Vertex3 (0.0f, 1.0f, 0.0f); // TL
		
		GL.End ();
	    GL.PopMatrix ();
	}	
}
