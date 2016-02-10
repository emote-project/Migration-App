package uk.ac.hw.lirec.emys3d;

import min3d.core.GlMaterial;
import min3d.core.Object3dContainer;
import min3d.vos.Number3d;
import android.util.Log;

/**
 * @author iw24
 *  Class to store the EmysModel and build the hierarchy.
 *  Construct it with a parsed model of emys, then add the base
 *  to the scene to use it.
 */
public class EmysModel   {
	
	public enum Emotion {
		NEUTRAL ("neutral",0,0,0,0,0,0,0,5),
		ANGER ("anger",30,0,20,-10,0,0,10,100),
		JOY ("joy",0,0,0,5,10,0,0,5),
		SADNESS ("sadness",-40,0,0,0,0,30,0,5),
		SURPRISE ("surprise",0,20,5,10,10,0,10,100),
		SLEEP ("sleep",0,0,45,0,0,5,0,100);
		
		public final float  eyeRoll, eyesOut, eyesOpen, upperAngle, lowerAngle, bow,move,blinkRate;
		private final String mName;
		
		//IMPORTANT no scaling here, as it'll happen when emotion is set
		Emotion(String name, float mEyeRoll, float mEyesOut, float mEyesOpen,
				float mUpperAngle, float mLowerAngle, float mBow, float mMove, float rate) {
			eyeRoll = mEyeRoll;
			eyesOut = mEyesOut;
			eyesOpen = mEyesOpen;
			upperAngle = mUpperAngle;
			lowerAngle = mLowerAngle;
			bow = mBow;
			move = mMove;
			blinkRate = rate;
			mName = name;
		}
		public String getName() {
			return mName;
		}
		
		public static Emotion getEmotion(String em) {
			for (Emotion retEm : Emotion.values() ){
				if (retEm.mName.equalsIgnoreCase(em)) return retEm;
			}
			//otherwise neutral
			return Emotion.NEUTRAL;
			
		}
	}

	// The components, formatting mirrors hierarchy.
	public Object3dContainer 
	base,
		neck,
			headcentre,
				top,
				bottom,
				middle,
					nose,
					stalkL,
						ballL,
							lidL,
					stalkR,
						ballR,
							lidR;
	
	/*
	 * Some fields for controlling animation.
	 * These define the target state for the components.
	 */
	private float mEyeRoll, mEyesOut, mEyesOpen, mUpperAngle, mLowerAngle, mBow;
	// This one's a bit different, as it defines how much random movement to add in.
	//sets a maximum bound on rotation from ideal plane
	private float mMove;
	private Number3d mBobCur = new Number3d(0,0,0),
					mBobTarget = new Number3d(0,0,0); //this will store degrees rotation really.
	
	//max time in seconds between a blink
	private float mBlinkRate = 5;
	private long mNextBlink;
	private float mEyesBeforeBlink;
	private boolean mBlinking = false;
	
	private Emotion mCurEmotion = Emotion.NEUTRAL;
		
	private long mTimeLastFrame;

	//todo used for speech
	private boolean mSpeaking = false;
	private boolean mOpening = true;
	private float mLowerBeforeSpeak;

	
	
	//speeds of rotation should be in degrees/second
	private static final float 
		EYELID_ROT_SPEED = 90,
		EYELID_OPEN_SPEED = 720,
		HEAD_PART_SPEED = 90,
		BOB_SPEED = 7, //speed for random head movement
		EYES_OUT_SPEED = 120, //units/second 
		EYES_OUT_SCALE = 0.7f, //scale to use same units as desktop as input
		EYES_CLOSED_ANGLE = -45, //how much to blink the eyes.
		MOUTH_OPEN_ANGLE = -5f,	///used for speaking
		MOUTH_CLOSED_ANGLE = 5f;
		
	public EmysModel(Object3dContainer parsed) {
		buildEmys(parsed);
	}
	

	public void selectEmotion(EmysModel.Emotion em) {
		setEmotion(em.eyeRoll, em.eyesOut, em.eyesOpen, em.upperAngle, em.lowerAngle, em.bow, em.move, em.blinkRate);
		mCurEmotion = em;
	}
	public Emotion getEmotion() {
		return mCurEmotion;
	}
	
	//set an emotion directly
	//IMPORTANT this may be called from a different thread to the renderer, so be CAREFUL.
	public void setEmotion(float eyeRoll, float eyesOut, float eyesOpen,
			float upperAngle, float lowerAngle, float bow, float move, float blinkrate) {
		this.mEyeRoll = eyeRoll;
		this.mEyesOut = eyesOut*EYES_OUT_SCALE;
		this.mEyesOpen = -eyesOpen;
		this.mUpperAngle = upperAngle;
		this.mLowerAngle = -lowerAngle;
		this.mBow = -bow;
		this.mMove = move*0.8f;
		this.mNextBlink = System.currentTimeMillis() + (long)(Math.random()*mBlinkRate*1000);
		this.mBlinkRate = blinkrate;
	}

	/**
	 *  This should be called in the animation loop.
	 */
	public void updateAnimation () {
		long timeElapsed = System.currentTimeMillis() - mTimeLastFrame;
		mTimeLastFrame = System.currentTimeMillis();
		
		updateBlink();
		updateSpeaking();
		updateEyeRoll(timeElapsed);
		updateEyesOut(timeElapsed);
		updateEyesOpen(timeElapsed);
		updateUpper(timeElapsed);
		updateLower(timeElapsed);
		updateBowHead(timeElapsed);
		updateMove(timeElapsed);
	}
	public void startSpeaking() {
		
		if (!mSpeaking) {
			
			mLowerBeforeSpeak = mLowerAngle;
		}	
		mSpeaking = true;
		Log.v("EMYS","START SPEAKING ENDS");
	}
	public void stopSpeaking() {
		mSpeaking = false;
		mLowerAngle = mLowerBeforeSpeak;
		Log.v("EMYS","STOP SPEAKING");
	}
	private void updateSpeaking() {
		if (mSpeaking)  {
			//either need to open mouth,  close if open
			if (bottom.rotation().x == mLowerAngle) {
				mOpening = !mOpening;
			} 
			if (mOpening) {
				mLowerAngle = MOUTH_OPEN_ANGLE;
			}else {
				//closing
				mLowerAngle = MOUTH_CLOSED_ANGLE;
			}
		}
		//otherwise we're we're waiting to blink.
	}
	
	private void updateBlink() {
		// either waiting to blink, just blinked or blinking shut/open
		if ((!mBlinking) && (System.currentTimeMillis() > mNextBlink)) {
			//time to setup a blink
			mEyesBeforeBlink = mEyesOpen;
			mEyesOpen = EYES_CLOSED_ANGLE; 
			mBlinking = true;
		} else if (mBlinking) {
			//either still blinking down, or need to set to back up.
			if (lidR.rotation().x == mEyesOpen) {
				//we've closed the eyes, so make them open and prepare for next blink
				mEyesOpen = mEyesBeforeBlink;
				mNextBlink = System.currentTimeMillis() + (long)(Math.random()*mBlinkRate*1000);
				mBlinking = false;
			}
		}
		//otherwise we're we're waiting to blink.
	}



	/**
	 * @param timeElapsed time since last frame
	 * move head randomly, one frame. 
	 */
	private void updateMove(long timeElapsed) {
		// want to bob about X,Y
		
		float increment = BOB_SPEED * (timeElapsed/1000.0f);
		float currentX = mBobCur.x;
		float currentY = mBobCur.y;
		
		//remove the old angle
		headcentre.rotation().x -= mBobCur.x;
		headcentre.rotation().y -= mBobCur.y;
		
		if (mBobCur.equals(mBobTarget)) {
			//set target back to 0,0
			mBobTarget.setAll(0, 0, 0);
			if ( (mMove > 0) && (mBobCur.equals(mBobTarget)) ){
				mBobTarget.x = (float) (Math.random()*mMove);
				mBobTarget.y = (float) (Math.random()*mMove);
			}
		}
		else {
			if (currentX < mBobTarget.x) {
				mBobCur.x += increment;
				if (mBobCur.x > mBobTarget.x) mBobCur.x = mBobTarget.x;
			}
			//greater than case
			else {
				mBobCur.x -= increment;
				if (mBobCur.x < mBobTarget.x) mBobCur.x = mBobTarget.x;
			}
			if (currentY < mBobTarget.y) {
				mBobCur.y += increment;
				if (mBobCur.y > mBobTarget.y) mBobCur.y = mBobTarget.y;
			}
			//greater than case
			else {
				mBobCur.y -= increment;
				if (mBobCur.y < mBobTarget.y) mBobCur.y = mBobTarget.y;
			}
		}
		//set the head to angle the delta	
		headcentre.rotation().x += mBobCur.x;
		headcentre.rotation().y += mBobCur.y;
	}
	/**
	 * @param timeElapsed time since last frame
	 * bow head one frame.
	 */
	private void updateBowHead(long timeElapsed) {
		
		float increment = HEAD_PART_SPEED * (timeElapsed/1000.0f);
		float current = headcentre.rotation().x;
		
		if (current == mBow)
			return;
		else if (current < mBow) {
			headcentre.rotation().x += increment;
			if (headcentre.rotation().x > mBow) headcentre.rotation().x = mBow;
		}
		//greater than case
		else {
			headcentre.rotation().x -= increment;
			if (headcentre.rotation().x < mBow) headcentre.rotation().x = mBow;
		}	
	}
	/**
	 * @param timeElapsed time since last frame
	 * roll the eyes one frame.
	 */
	private void updateEyeRoll(long timeElapsed) {
		// value in mEyeRoll is target for right eye z rotate
		
		float increment = EYELID_ROT_SPEED * (timeElapsed/1000.0f);
		float current = lidR.rotation().z;
		
		if (current == mEyeRoll)
			return;
		else if (current < mEyeRoll) {
			lidR.rotation().z += increment;
			lidL.rotation().z += -increment;
			if (lidR.rotation().z > mEyeRoll) {
				lidR.rotation().z = mEyeRoll;
				lidL.rotation().z = -mEyeRoll;
			}
		}
		//greater than case
		else {
			lidR.rotation().z -= increment;
			lidL.rotation().z -= -increment;
			if (lidR.rotation().z < mEyeRoll) {
				lidR.rotation().z = mEyeRoll;
				lidL.rotation().z = -mEyeRoll;
			}
		}
		
	}
	/**
	 * @param timeElapsed time since last frame
	 * extend the eyes one frame.
	 */
	private void updateEyesOut(long timeElapsed) {
		
		float increment = EYES_OUT_SPEED * (timeElapsed/1000.0f);
		float current = stalkR.position().z;
		
		//add the local O and subtract parent
		//-eyes out for axis
		if (current == (-mEyesOut + stalkR.getLocalO().z - middle.getLocalO().z))
			return;
		else if (current < (-mEyesOut + stalkR.getLocalO().z - middle.getLocalO().z)) {
			stalkR.position().z += increment;
			stalkL.position().z += increment;
			if (stalkR.position().z > (-mEyesOut + stalkR.getLocalO().z - middle.getLocalO().z)) {
				stalkR.position().z = (-mEyesOut + stalkR.getLocalO().z - middle.getLocalO().z);
				stalkL.position().z = (-mEyesOut + stalkL.getLocalO().z - middle.getLocalO().z);
			}
		}
		//greater than case
		else {
			stalkR.position().z -= increment;
			stalkL.position().z -= increment;
			if (stalkR.position().z < (-mEyesOut + stalkR.getLocalO().z - middle.getLocalO().z)) {
				stalkR.position().z = (-mEyesOut + stalkR.getLocalO().z - middle.getLocalO().z);
				stalkL.position().z = (-mEyesOut + stalkL.getLocalO().z - middle.getLocalO().z);
			}
		}	
	}
	/**
	 * @param timeElapsed time since last frame
	 * roll the eyes one frame.
	 */
	private void updateEyesOpen(long timeElapsed) {
		// value in mEyeOpen is target for right eye x rotate
		
		float increment = EYELID_OPEN_SPEED * (timeElapsed/1000.0f);
		float current = lidR.rotation().x;
		
		if (current == mEyesOpen)
			return;
		else if (current < mEyesOpen) {
			lidR.rotation().x += increment;
			lidL.rotation().x += increment;
			if (lidR.rotation().x > mEyesOpen) lidR.rotation().x = mEyesOpen;
			if (lidL.rotation().x > mEyesOpen) lidL.rotation().x = mEyesOpen;
		}
		//greater than case
		else {
			lidR.rotation().x -= increment;
			lidL.rotation().x -= increment;
			if (lidR.rotation().x < mEyesOpen) lidR.rotation().x = mEyesOpen;
			if (lidL.rotation().x < mEyesOpen) lidL.rotation().x = mEyesOpen;
		}	
	}
	/**
	 * @param timeElapsed time since last frame
	 * rotate upper head one frame.
	 */
	private void updateUpper(long timeElapsed) {
		
		float increment = HEAD_PART_SPEED * (timeElapsed/1000.0f);
		float current = top.rotation().x;
		
		if (current == mUpperAngle)
			return;
		else if (current < mUpperAngle) {
			top.rotation().x += increment;
			if (top.rotation().x > mUpperAngle) top.rotation().x = mUpperAngle;
		}
		//greater than case
		else {
			top.rotation().x -= increment;
			if (top.rotation().x < mUpperAngle) top.rotation().x = mUpperAngle;
		}	
	}
	/**
	 * @param timeElapsed time since last frame
	 * rotate lower head one frame.
	 */
	private void updateLower(long timeElapsed) {
		
		float increment = HEAD_PART_SPEED * (timeElapsed/1000.0f);
		float current = bottom.rotation().x;
		
		if (mSpeaking)
			increment /=1.5; //AWFUL CAITLIN HACK TOO
		
		if (current == mLowerAngle)
			return;
		else if (current < mLowerAngle) {
			bottom.rotation().x += increment;
			if (bottom.rotation().x > mLowerAngle) bottom.rotation().x = mLowerAngle;
		}
		//greater than case
		else {
			bottom.rotation().x -= increment;
			if (bottom.rotation().x < mLowerAngle) bottom.rotation().x = mLowerAngle;
		}	
	}

	public void rebuildEmys() {
		setMaterials();
		buildHierarchy();
	}
	
	private void buildEmys(Object3dContainer parsed) {

		//note this loading by number DODGY but name-loading seems broken.
		base = new Object3dContainer(parsed.getChildAt(0));
		neck = new Object3dContainer(parsed.getChildAt(1));
		headcentre = new Object3dContainer(parsed.getChildAt(12));
		top = new Object3dContainer(parsed.getChildAt(2));
		bottom = new Object3dContainer(parsed.getChildAt(3));
		middle = new Object3dContainer(parsed.getChildAt(4));
		nose = new Object3dContainer(parsed.getChildAt(6));
		stalkL = new Object3dContainer(parsed.getChildAt(7));
		ballL = new Object3dContainer(parsed.getChildAt(9));
		lidL = new Object3dContainer(parsed.getChildAt(10));
		stalkR = new Object3dContainer(parsed.getChildAt(5));
		ballR = new Object3dContainer(parsed.getChildAt(8));
		lidR = new Object3dContainer(parsed.getChildAt(11));
		
		setMaterials();


		/////////////////
		buildHierarchy();
		setPositions(base);

	}
	
	private void setMaterials() {
		
		float[] amb = {1f, 1f, 1f,1f};
		float[] diff = {0.9f,0.9f,0.9f,1f};
		float[] spec = { 1f,1f,1f, 1f};
		float shiny = 30f;
		
		GlMaterial white = new GlMaterial(amb, diff, spec, shiny);
		
		amb = new float[] {0.3f, 0.3f, 0.3f,1f};
		diff = new float[] {0.4f,0.4f,0.4f,1f};
		spec = new float[] { 0.9f,0.9f,0.9f, 1f};
		shiny = 30f;
		
		GlMaterial darkGrey = new GlMaterial(amb, diff, spec, shiny);
		
		//set the colours etc.
		base.texturesEnabled(false);
		neck.texturesEnabled(false);
		headcentre.texturesEnabled(false);
		top.texturesEnabled(false);
		bottom.texturesEnabled(false);
		middle.texturesEnabled(false);
		
		nose.texturesEnabled(false);
		nose.setMaterial(darkGrey);
		
		stalkL.texturesEnabled(false);
		
		ballL.setMaterial(white);
		
		lidL.setMaterial(darkGrey);
		lidL.texturesEnabled(false);
		
		stalkR.texturesEnabled(false);
		ballR.setMaterial(white);
		
		lidR.setMaterial(darkGrey);
		lidR.texturesEnabled(false);
	}
	
	private void buildHierarchy() {
		
		ballR.addChild(lidR);
		stalkR.addChild(ballR);
		
		ballL.addChild(lidL);
		stalkL.addChild(ballL);
		
		middle.addChild(nose);
		middle.addChild(stalkL);
		middle.addChild(stalkR);
		
		headcentre.addChild(top);
		headcentre.addChild(middle);
		headcentre.addChild(bottom);
		
		neck.addChild(headcentre);
		base.addChild(neck);
		
	}
	
	private void setPositions(Object3dContainer root) {
		root.position().setAllFrom(root.getLocalO());
		if ((root.parent() != null) && (root.parent() instanceof Object3dContainer) ) {
			root.position().subtract( ((Object3dContainer)root.parent()).getLocalO() );
		}
		//set any children
		for (int i = 0; i < root.numChildren(); ++i) {
			setPositions(root.getChildAt(i));
			
		}
	}

}
