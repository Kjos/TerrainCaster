using UnityEngine;
using System.Collections;

public class FPSScript : MonoBehaviour {
	float t = 0.0f;
	
	// Use this for initialization
	void Start () {
	}
	
	// Update is called once per frame
	void Update() {
		if (t > 1.0f) {
			guiText.text = "FPS: " + 1.0f / Time.deltaTime ;
			t = 0.0f;
		}
			
		t += Time.deltaTime;
	}
}
