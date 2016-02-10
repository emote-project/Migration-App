package uk.ac.hw.lirec.emys3d;

import java.util.HashMap;
import java.util.Locale;

import uk.ac.hw.lirec.emys3d.EmysModel.Emotion;
import uk.ac.uk.lirec.emys3d.R;
import min3d.core.Object3dContainer;
import min3d.core.RendererActivity;
import min3d.parser.IParser;
import min3d.parser.Parser;
import min3d.vos.Light;
import min3d.vos.Number3d;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

/**
 * @author Iain Wallace
 *
 * This provides an activity you can subclass that implements a glsurfaceview with an EMYS
 * head in it. You implment getGlSurfaceViewContainerLayout() which should return a linearlayout that will
 * be replaced by the glsurfaceview when it's all loaded.
 * 
 * Methods are provided to set the expression, and to get the head to speak, which will be 
 * output using the TTS system, optionally you can register an OnUtteranceCompletedListener
 * e.g. to synch up subtitles. The HwuMobileCompanion or 3dTester are good examples of how to use this.
 *
 * IMPORTANT there are some SDK version based checks in here used as a simple hack to detect
 * a honeycomb tablet, they'll obviously cause issues on a ICS/JB phone.
 * 
 * TODO re-implement the tablet hacks in a safe way.
 */
public abstract class Emys3DActivity extends RendererActivity implements SensorEventListener, OnInitListener {
	
	protected EmysModel emys;
	private Emotion mCurEmotion = Emotion.NEUTRAL;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Number3d mAccVals;
	private Number3d mInitAccVals;
	private boolean mAccelInit = false;
	private final float FILTERING_FACTOR = .3f;
	
	private TextToSpeech mTts;
	private OnUtteranceCompletedListener mUtteranceListener;
	private int mTargetZ = 200;
	
	/**
	 * @return should return a layout to contain the glsurfaceview.
	 */
	protected abstract LinearLayout getGlSurfaceViewContainerLayout();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccVals = new Number3d();
        mInitAccVals = new Number3d();
        
        //TODO add the check that TTS is installed
        mTts = new TextToSpeech(this, this);
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		//Check if the surfaceview's already there
		if (getGlSurfaceViewContainerLayout().indexOfChild(_glSurfaceView) == -1)
			setGlSurfaceView(getGlSurfaceViewContainerLayout());
	}


	private void setGlSurfaceView(LinearLayout glSurfaceViewLayout) {

		glSurfaceViewLayout.addView(_glSurfaceView);
		//glSurfaceViewLayout.setVisibility(View.INVISIBLE);
        
        //Setup a listener so that we re-focus on the user when the screen's touched.
        _glSurfaceView.setOnTouchListener(new OnTouchListener() {
           @Override
           public boolean onTouch(View v, MotionEvent event) {
        	   mAccelInit = false;
        	   return true;
           }
        });
	}
	
	protected void setSpeechCompletedListener( OnUtteranceCompletedListener utter) {
		mUtteranceListener = utter;
	}

	@Override
	public void initScene() {
		
		Light whiteLight = new Light();
		whiteLight.position.setAll(-10, 10, -5);
		Light whiteLight2 = new Light();
		whiteLight2.position.setAll(10, 10, 0);
		
			
		scene.lights().add(whiteLight);

		Log.v("3D DEBUG","Loading...");

		IParser parser = Parser.createParser(Parser.Type.MAX_3DS,
				getResources(), getResources().getResourceName(R.raw.emys), false);
		
		Log.v("3D DEBUG","Parsing...");
		parser.parse();
		Log.v("3D DEBUG","Parsed!");
		
		Object3dContainer emysLoaded = parser.getParsedObject();
		
		
		//load the model
		emys = new EmysModel(emysLoaded);
		emys.selectEmotion(mCurEmotion);
		
		scene.addChild(emys.base);
		
		int numChild = emysLoaded.numChildren();
		Log.v("3D DEBUG","Number of children: "+numChild);
		for (int i = 0; i < numChild; ++i) {
			Log.v("3D DEBUG","child no: "+i+" is: "+emysLoaded.getChildAt(i).name()+" at: "+emysLoaded.getChildAt(i).position());
			
		}
		
		//scene.camera().target = new Number3d(0,0,0);
		scene.camera().frustum.zFar((float)300.0);
	
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				onEmysLoaded();
			}
		});
		
	}
	
	/**
	 * A method provided so that you can have things happen once emys is loaded.
	 */
	protected abstract void onEmysLoaded();

	//IMPORTANT this happens in the GLthread, not the main UI thread.
	@Override
	public void updateScene() {
		emys.updateAnimation();
	}
	
	@Override
	public void onInit(int status) {
		mTts.setLanguage(Locale.UK);
		final Intent broadcastIntent = new Intent();
		mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
			@Override
			public void onUtteranceCompleted(String utteranceId) {
				emys.stopSpeaking();
				if (mUtteranceListener != null)	
					mUtteranceListener.onUtteranceCompleted(utteranceId);
				
				// Added by Meiyii: Broadcast the completion of TTS utterance
				broadcastIntent.setAction("hw.emote.eatreasurehunt.tts");
				sendBroadcast(broadcastIntent);
			}
	    });
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTts.shutdown();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
		if (!mAccelInit) {
			mInitAccVals.x = (float) event.values[1];
			mInitAccVals.y = (float) event.values[0];
			mAccelInit = true;
		}
		mAccVals.x = (float) (-(event.values[1]-mInitAccVals.x) * FILTERING_FACTOR + mAccVals.x * (1.0 - FILTERING_FACTOR));
		mAccVals.y = (float) ((event.values[0]-mInitAccVals.y) * FILTERING_FACTOR + mAccVals.y * (1.0 - FILTERING_FACTOR));
			
		//xy swapped for portrait
		scene.camera().position.x = mAccVals.y * 15f;
        scene.camera().position.y = mAccVals.x * 15f;
        scene.camera().position.z = -200;
        
        scene.camera().target.x = -scene.camera().position.x;
        scene.camera().target.y = -scene.camera().position.y;
        scene.camera().target.z = mTargetZ;
        
        //tablet hack ahoy!
        if (Build.VERSION.SDK_INT > 10) {
			//xy swapped for portrait
			scene.camera().position.y = mAccVals.y *  15f;
	        scene.camera().position.x = -mAccVals.x * 15f;
	        scene.camera().position.z = -200;
	        
	        scene.camera().target.x = -scene.camera().position.x;
	        scene.camera().target.y = -scene.camera().position.y;
	        scene.camera().target.z = mTargetZ;
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//nothing to do here
	}
	
	public void blankScreen() {
		mTargetZ = -400;
		scene.camera().target.z = mTargetZ;
	}
	public void unBlankScreen() {
		mTargetZ = 200;
		scene.camera().target.z = mTargetZ;
	}
	public boolean isBlank() {
		return (mTargetZ == -400);
	}
	
	public void setExpression (Emotion em) {
		mCurEmotion = em;
		emys.selectEmotion(mCurEmotion);
	}
	
	public Emotion getEmysEmotion() {return mCurEmotion;}
	
	public void speakText(String text) {
		
		HashMap<String,String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
		mTts.speak(text, TextToSpeech.QUEUE_ADD, params);
		emys.startSpeaking();
	}	
}
