package rajawali.materials;

import java.util.Stack;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import android.graphics.Color;
import android.opengl.GLES20;

public abstract class AAdvancedMaterial extends AMaterial {
	protected static final int MAX_LIGHTS = RajawaliRenderer.getMaxLights(); 
	
	public static final String M_LIGHTS_VARS =
			"uniform vec3 uLightPos[" +MAX_LIGHTS+ "];\n" +
			"uniform float uLightPower[" +MAX_LIGHTS+ "];\n" +
			"uniform vec3 uLightColor[" +MAX_LIGHTS+ "];\n";	
	
	public static final String M_FOG_VERTEX_VARS =
			"\n#ifdef FOG_ENABLED\n" +
			"varying float vFogDepth;\n" +
			"#endif\n\n";
	public static final String M_FOG_VERTEX_DEPTH = 
			"\n#ifdef FOG_ENABLED\n" +
			"	vFogDepth = gl_Position.z;\n" +
			"#endif\n\n";
	public static final String M_FOG_FRAGMENT_VARS =
			"\n#ifdef FOG_ENABLED\n" +
			"uniform vec3 uFogColor;\n" +
			"uniform float uFogNear;\n" +
			"uniform float uFogFar;\n" +
			"uniform bool uFogEnabled;\n" +
			"varying float vFogDepth;\n" +
			"#endif\n\n";
	public static final String M_FOG_FRAGMENT_CALC = 
			"\n#ifdef FOG_ENABLED\n" +
			"	float fogDensity = 0.0;\n" +
			"	if(uFogEnabled == true){\n" +
			"		if (vFogDepth > uFogFar) {\n" +
			"			fogDensity = 1.0;\n" +
			"		}else if(vFogDepth > uFogNear) {\n" +
			"			float newDepth = vFogDepth - uFogNear;\n" +
			"			fogDensity = newDepth/(uFogFar - uFogNear);\n" +
			"		}else if (vFogDepth < uFogNear) {\n" +
			"			fogDensity = 0.0;\n" +
			"		}\n" +
			"	}\n" +
			"#endif\n\n";
	public static final String M_FOG_FRAGMENT_COLOR =
			"\n#ifdef FOG_ENABLED\n" +
			"	gl_FragColor = mix(gl_FragColor,vec4(uFogColor,1.0),fogDensity);\n" +
			"#endif\n\n";

	
	protected int muLightPosHandle;
	protected int muLightPowerHandle;
	protected int muLightColorHandle;
	protected int muNormalMatrixHandle;
	protected int muAmbientColorHandle;
	protected int muAmbientIntensityHandle;
	protected int muFogColorHandle;
	protected int muFogNearHandle;
	protected int muFogFarHandle;
	protected int muFogEnabledHandle;
	
	protected float[] mNormalMatrix;
	protected float[] mLightPos;
	protected float[] mLightPower;
	protected float[] mLightColor;
	protected float[] mTmp, mTmp2;
	protected float[] mAmbientColor, mAmbientIntensity;
	protected float[] mFogColor;
	protected float mFogNear, mFogFar;
	protected boolean mFogEnabled;

	protected android.graphics.Matrix mTmpNormalMatrix = new android.graphics.Matrix();
	protected android.graphics.Matrix mTmpMvMatrix = new android.graphics.Matrix();

	public AAdvancedMaterial() {
		super();
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader) {
		this(vertexShader, fragmentShader, false);
	}
	
	public AAdvancedMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		super(vertexShader, fragmentShader, isAnimated);
		
		final int maxLights = MAX_LIGHTS;
		mNormalMatrix = new float[9];
		mTmp = new float[9];
		mTmp2 = new float[9];
		mAmbientColor = new float[] {.2f, .2f, .2f, 1};
		mAmbientIntensity = new float[] { .3f, .3f, .3f, 1 };
		
		mLightPos = new float[maxLights * 3];
		mLightPower = new float[maxLights];
		mLightColor = new float[mLightPos.length];
		
		if(RajawaliRenderer.isFogEnabled())
			mFogColor = new float[] { .8f, .8f, .8f };
	}
	
	@Override
	public void setLights(Stack<ALight> lights) {
		super.setLights(lights);
		
		ALight light;
		int index;
		Number3D pos;
		float[] color;
		for(int i=0; i<MAX_LIGHTS; i++) {
			light = mLights.get(i);
			pos = light.getPosition();
			color = light.getColor();
			mLightPower[i] = light.getPower();
			index = i*3;
			mLightPos[index] = -pos.x;
			mLightPos[index+1] = pos.y;
			mLightPos[index+2] = pos.z;
			mLightColor[index] = color[0];
			mLightColor[index+1] = color[1];
			mLightColor[index+1] = color[2];
		}
		
		GLES20.glUniform3fv(muLightPosHandle, MAX_LIGHTS, mLightPos, 0);
		GLES20.glUniform3fv(muLightColorHandle, MAX_LIGHTS, mLightColor, 0);
		GLES20.glUniform1fv(muLightPowerHandle, MAX_LIGHTS, mLightPower, 0);
	}
	
	public void setAmbientColor(float[] color) {
		mAmbientColor = color;
	}
	
	public void setAmbientColor(float r, float g, float b, float a) {
		setAmbientColor(new float[] { r, g, b, a });
	}
	
	public void setAmbientColor(int color) {
		setAmbientColor(new float[] { Color.red(color) / 255, Color.green(color) / 255, Color.blue(color) / 255, Color.alpha(color) / 255 });
	}
	
	public void setAmbientIntensity(float[] intensity) {
		mAmbientIntensity = intensity;
	}
	
	public void setAmbientIntensity(float r, float g, float b, float a) {
		setAmbientIntensity(new float[] { r, g, b, a });
	}
	
	public void setFogColor(int color) {
		mFogColor[0] = Color.red(color) / 255f;
		mFogColor[1] = Color.green(color) / 255f;
		mFogColor[2] = Color.blue(color) / 255f;
	}
	
	public void setFogNear(float near) {
		mFogNear = near;
	}
	
	public void setFogFar(float far) {
		mFogFar = far;
	}
	
	public void setFogEnabled(boolean enabled) {
		mFogEnabled = enabled;
	}
	
	@Override
	public void useProgram() {
		super.useProgram();
		GLES20.glUniform4fv(muAmbientColorHandle, 1, mAmbientColor, 0);
		GLES20.glUniform4fv(muAmbientIntensityHandle, 1, mAmbientIntensity, 0);
		if(mFogEnabled) {
			GLES20.glUniform3fv(muFogColorHandle, 1, mFogColor, 0);
			GLES20.glUniform1f(muFogNearHandle, mFogNear);
			GLES20.glUniform1f(muFogFarHandle, mFogFar);
			GLES20.glUniform1i(muFogEnabledHandle, mFogEnabled == true ? GLES20.GL_TRUE : GLES20.GL_FALSE);
		}
	}
	
	@Override
	public void setCamera(Camera camera) {
		super.setCamera(camera);
		if(camera.isFogEnabled()) {
			setFogColor(camera.getFogColor());
			setFogNear(camera.getFogNear());
			setFogFar(camera.getFogFar());
			setFogEnabled(true);
		} else {
			setFogEnabled(false);
		}
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader)
	{
		super.setShaders(vertexShader, fragmentShader);
		muNormalMatrixHandle = getUniformLocation("uNMatrix");
		muAmbientColorHandle = getUniformLocation("uAmbientColor");
		muAmbientIntensityHandle = getUniformLocation("uAmbientIntensity");
		
		muLightPosHandle = getUniformLocation("uLightPos"); 
		muLightColorHandle = getUniformLocation("uLightColor");
		muLightPowerHandle = getUniformLocation("uLightPower");
		
		if(RajawaliRenderer.isFogEnabled()) {
			muFogColorHandle = getUniformLocation("uFogColor");
			muFogNearHandle = getUniformLocation("uFogNear");
			muFogFarHandle = getUniformLocation("uFogFar");
			muFogEnabledHandle = getUniformLocation("uFogEnabled");
		}
	}
	
	@Override
	public void setModelMatrix(float[] modelMatrix) {
		super.setModelMatrix(modelMatrix);
		
		mTmp2[0] = modelMatrix[0]; mTmp2[1] = modelMatrix[1]; mTmp2[2] = modelMatrix[2]; 
		mTmp2[3] = modelMatrix[4]; mTmp2[4] = modelMatrix[5]; mTmp2[5] = modelMatrix[6];
		mTmp2[6] = modelMatrix[8]; mTmp2[7] = modelMatrix[9]; mTmp2[8] = modelMatrix[10];
		
		mTmpMvMatrix.setValues(mTmp2);
		
		mTmpNormalMatrix.reset();
		mTmpMvMatrix.invert(mTmpNormalMatrix);

		mTmpNormalMatrix.getValues(mTmp);
		mTmp2[0] = mTmp[0]; mTmp2[1] = mTmp[3]; mTmp2[2] = mTmp[6]; 
		mTmp2[3] = mTmp[1]; mTmp2[4] = mTmp[4]; mTmp2[5] = mTmp[7];
		mTmp2[6] = mTmp[2]; mTmp2[7] = mTmp[5]; mTmp2[8] = mTmp[8];
		mTmpNormalMatrix.setValues(mTmp2);
		mTmpNormalMatrix.getValues(mNormalMatrix);

	    GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false, mNormalMatrix, 0);
	}
}
