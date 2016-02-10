package hw.emote.eatreasurehunt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hw.emote.dialogsystem.AndroidDialogInterface;
import hw.emote.dialogsystem.AndroidDialogProvider;
import hw.emote.dialogsystem.DialogSystem;
import hw.emote.feedbackparser.FeedbackItem;
import hw.emote.feedbackparser.FeedbackTemplate;
import hw.emote.feedbackparser.XMLFeedbacksReader;
import hw.emote.routeparser.Question;
import hw.emote.routeparser.Route;
import hw.emote.routeparser.Step;
import hw.emote.routeparser.XMLRouteReader;
import uk.ac.hw.lirec.emys3d.Emys3DActivity;
import uk.ac.hw.lirec.emys3d.EmysModel.Emotion;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Mei Yii Lim
 *
 * This is the main activity for the EATreasureHunt application. 
 * 
 * It implements the OnTouchListener which controls the drag and zoom functionalities of the Map,
 * Android Dialog Provider for the Dialog system and OnInitListener to detect the end of TTS utterance
 * LocationListener detects the location changes
 */

public class EATreasureHuntActivity extends Emys3DActivity implements OnTouchListener, AndroidDialogProvider, OnInitListener{
    
	// Intent
	private Intent mIntent;
	private String mUserID;
	private String mSelectedMemoryType;
	private String mSelectedRoute;
	private String mLogFileName;
	
	// Layout components
	private TextView mTxtExtraPrize;
	private ImageView mTreasureMap;
	private ImageView mMapScale;
	private ImageView mCompass;
	private ScrollView mScroll;
	private TextView mTxtSubtitles;
	private ImageView mSymbol;
	private Spinner spAnswers;
	private Button mBtnProceed;
	private Button mBtnPlayAgain;
	private TextView mTxtError;	
	
	// Translate and zoom related variables
	private static final float MIN_ZOOM = 0.5f;
	private static final float MAX_ZOOM = 2.0f;
	private Matrix mMatrix = new Matrix();
	private Matrix mSavedMatrix = new Matrix();
	private Matrix mScaleMatrix = new Matrix();
	private Matrix mSavedScaleMatrix = new Matrix();
	private PointF mStartPoint = new PointF();
	private PointF mMidPoint = new PointF();
	private float mOldDist = 1f;
	private float mScale = 1f;
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;
	private int mode = NONE;
	
	//dialog system
	private DialogSystem mDialogSystem;
	private AndroidDialogInterface mDi = new AndroidDialogInterface(this,this);
	
	// Feedback file and related variables
	private ArrayList<FeedbackTemplate> mFeedbacks;
	private FeedbackTemplate mFeedback;
	
	// Steps file and related variables
	private ArrayList<Route> mRoutes;
	private Step mStep;
	private ArrayList<Question> mQuestions;
	private Route mRoute;
	private String mAnswer;
	private static int mCurrentStep = 0;
	private static int mCurrentQuestion = 0;
	private static int mCorrectAnswers = 0;
	private int mTreasureHuntMode;	
	private static final int CLUE = 0;
	private static final int QUESTION = 1;
	private static final int ANSWER = 2;
	private static final int CHANGE = 3;
	//private static final int RECAP = 4;
	private String mTTSText;
	private int mResumeStep;
	private boolean mLoadMap;
	private Integer[] mImageIcons;
	
	// Extra prize dialog box components
	private Dialog mDialog;
	private Spinner mSpQ1Answers; 
	private Spinner mSpQ2Answers;
	private TextView mTxtDialogError;
	private Button mBtnSubmit;
	private Button mBtnClose;
	private String q1Answer;
	private String q2Answer;
	private boolean mPrizeQAnswered;
	
	// External Storage Log 
	private EALogManager mEALogManager;

	// the compass picture angle 
 	private float mCurrentDegree;
 	private float mAzimuth;
 	private float mDeclination;
 	private float mBearing;
 	
 	// migration 
 	private Button mBtnMigrate;
	private HashMap<String, String> mMigrateData;
	//private String mMigrateFrom = "192.168.1.21";
	private String mMachineIP;
	private Boolean mMigrated = false;
 	//used to play sounds for migration, if called in the dialog script
 	private MediaPlayer mMediaPlayer;
 	
 	// List of skill levels
	private ArrayList<String> mHighSkills = new ArrayList<String>();
	private ArrayList<String> mMedSkills = new ArrayList<String>();
	private ArrayList<String> mLowSkills = new ArrayList<String>();		
	private ArrayList<String> mToolsUsed = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Retrieve information passed over from the setup activity
		mIntent = getIntent();
		mUserID = mIntent.getStringExtra(EAGeneral.USER_ID);
		mMachineIP = mIntent.getStringExtra(EAGeneral.MACHINE_IP);
		mSelectedMemoryType = mIntent.getStringExtra(EAGeneral.MEMORY_TYPE);
		mSelectedRoute = mIntent.getStringExtra(EAGeneral.ROUTE);
		mLogFileName = mIntent.getStringExtra(EAGeneral.LOG_FILE);
		//Toast.makeText(getApplicationContext(), "machine IP :: "+ mMachineIP, Toast.LENGTH_LONG).show();
		//Log.d(getClass().getSimpleName(),"user id " + mUserID + " feedback " + mSelectedFeedbackType + " route " + mSelectedRoute );
		
		// Initialise the layout components
		setContentView(R.layout.activity_eatreasure_hunt);
		mTxtExtraPrize = (TextView) findViewById(R.id.txtExtraPrize);
		mTreasureMap = (ImageView) findViewById(R.id.treasureMap); 
		mMapScale = (ImageView) findViewById(R.id.mapScale);
    	mTreasureMap.setOnTouchListener(this);       
    	mTreasureMap.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
    	    @Override
    	    public void onGlobalLayout() {
    	       // center the map upon load
    	       if (!mLoadMap) {
	    	       int width = mTreasureMap.getWidth();  
	    	       //Toast.makeText(getApplicationContext(), "map width :: "+ width + " scale width :: " + mMapScale.getWidth(), Toast.LENGTH_LONG).show();
	    	       mMatrix.set(mSavedMatrix);
	    	       mScaleMatrix.set(mSavedScaleMatrix);
				   mMatrix.postTranslate(mStartPoint.x - width/3, mStartPoint.y);
				   mTreasureMap.setImageMatrix(mMatrix);
				   mMapScale.setImageMatrix(mScaleMatrix);
				   mLoadMap = true; // map has been loaded
    	       }
    	    }
    	});
    	   	
    	// initialise compass variables
    	mCompass = (ImageView) findViewById(R.id.compass);
    	mCurrentDegree = Float.NaN;
    	mAzimuth = Float.NaN;
    	mDeclination = Float.NaN;
    	mBearing = Float.NaN;
    	
    	mScroll = (ScrollView) findViewById(R.id.clueScroll);    
    	mScroll.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	           @Override
	           public void onGlobalLayout() {
	               // Ready, move up
	               mScroll.fullScroll(View.FOCUS_UP);
	           }
	       });
    	mTxtSubtitles = (TextView) findViewById(R.id.txtSubtitles);
    	mSymbol = (ImageView) findViewById(R.id.symbol);
    	spAnswers = (Spinner) findViewById(R.id.spAnswers);
    	mBtnProceed = (Button) findViewById(R.id.btnProceed);
    	mBtnPlayAgain = (Button) findViewById(R.id.btnPlayAgain);
    	mBtnMigrate = (Button) findViewById(R.id.btnMigrate);
    	mTxtError = (TextView) findViewById(R.id.txtError);
    	
    	// disable buttons to prevent user from clicking before Emys loads
    	disableButtons();
    	
    	// dialog system 
        mDialogSystem = new DialogSystem(mDi);
        
        // initialise the data
        initData();
     
        // Link to Register Screen
        mTxtExtraPrize.setOnClickListener(new View.OnClickListener() {        	
        	
            @Override
			public void onClick(View view) {   
            	displayPrizeDialog();
            }
        });
        
        // Proceed button Click Event - Display the next clue
        mBtnProceed.setOnClickListener(new View.OnClickListener() {
 
            @Override
			public void onClick(View view) {
            	mSymbol.setVisibility(View.INVISIBLE);            	
            	if (mTreasureHuntMode == CLUE) {        			
	            	// introduction step 
	            	if(mCurrentStep == 0){	            		
	            		mBtnProceed.setText(getResources().getText(R.string.next));	  
	            	// locating starting point and clue steps
	            	} else if (mCurrentStep >= 1) {
	            		mBtnProceed.setText(getResources().getText(R.string.got_there));
	            	} 
	            	displayClue();
            	} else if (mTreasureHuntMode == QUESTION) {   
            		// if an answer is required
            		if (mQuestions.get(0).getAnswers().size() > 0) {
            			mBtnProceed.setText(getResources().getText(R.string.answer_question));
            		// paper task - no answer required
            		} else {
            			mBtnProceed.setText(getResources().getText(R.string.done));            			
            		}
            		displayQuestion();
            	} else if (mTreasureHuntMode == ANSWER) {
            		if (mAnswer.equals(getResources().getText(R.string.select_answer).toString())) {
            			mTxtError.setVisibility(View.VISIBLE);            			
            		} else {
            			mTxtError.setVisibility(View.INVISIBLE);
	            		mBtnProceed.setText(getResources().getText(R.string.next));
	            		displayFeedback();
            		}
            	} else if (mTreasureHuntMode == CHANGE) {
            		if (mCurrentStep < mRoute.getSteps().size()-1) {
            			mBtnProceed.setText(getResources().getText(R.string.done)); 
            			displayChangeInstruction();
            		}
            	} 
            	// Allow TTS replay
            	mBtnPlayAgain.setVisibility(View.VISIBLE);
            	//Log.d(getClass().getSimpleName(),"mode " + treasureHuntMode + " " + currentStep + " " + currentQuestion);
            }
        });
        
        // Proceed button Click Event - Display the next clue
        mBtnPlayAgain.setOnClickListener(new View.OnClickListener() {
 
            @Override
			public void onClick(View view) {
            	speakText(mTTSText);
            	disableButtons();
            }
        });
                
    	// start the location service - only once as it will be running continuously in the 
        // background until the app is destroyed
	    Intent locationService = new Intent( this, EALocationService.class );
	    locationService.putExtra(EAGeneral.LOG_FILE, mLogFileName);
	    startService(locationService);
	    // register broadcast receiver for declination
	    registerReceiver(declinationReceiver, new IntentFilter(EALocationService.BROADCAST_DECLINATION));
	}

	/** 
	 * ttsReceiver to receive broadcast on completion of tts utterance
	 */
	private BroadcastReceiver ttsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// disable buttons and close the log files at the last step
			if (mCurrentStep == mRoute.getSteps().size()) {
				disableButtons();
				// Log the final step
				mEALogManager.writeToLogFile("End of treasure hunt! \n");
				// Close the log file
			    mEALogManager.closeLogFile();
				stopLocationService();
			} else {
				// Enable the proceed and play again buttons
				enableButtons();
			}
			// Log.d(getClass().getSimpleName(), "ttsReceiver ");
		}
    };    
    
	@Override
	public void onResume() {      
	    super.onResume();
	    
	    // Log Manager
	 	mEALogManager = new EALogManager();
	 	if (mEALogManager.checkStorageStatus()) {
	 		mEALogManager.createLogFile(mLogFileName + ".txt");
	 	}
    	
	    // Restore preferences
        SharedPreferences huntData = getSharedPreferences(EAGeneral.DATA_FILE, 0);
        mTreasureHuntMode = huntData.getInt(EAGeneral.TREASURE_HUNT_MODE, CLUE);
        mCurrentStep = huntData.getInt(EAGeneral.CURRENT_STEP, 0);
        mCurrentQuestion = huntData.getInt(EAGeneral.CURRENT_QUESTION, 0);
        mCorrectAnswers = huntData.getInt(EAGeneral.CORRECT_ANSWERS, 0);
        String proceedString = huntData.getString(EAGeneral.PROCEED_STRING, getResources().getText(R.string.next).toString());
        mPrizeQAnswered = huntData.getBoolean(EAGeneral.PRIZE_Q_ANSWERED, false);
        mMigrated = huntData.getBoolean(EAGeneral.MIGRATED, false);
        //Log.d(getClass().getSimpleName(),"onResume " + treasureHuntMode + " " + currentStep + " " + currentQuestion + " " + proceedString + " " + getResources().getText(R.string.done).toString());
       
        // remember the resume step to prevent continuous decrease of step due to the user pausing and resuming the app
        mResumeStep = mCurrentStep;
        // map has not been loaded
        mLoadMap = false;
        
        // when in question and answer modes, retrieve the question of the previous step without decreasing the currentStep
        if (mTreasureHuntMode == ANSWER || (mTreasureHuntMode == QUESTION && mCurrentQuestion > 0)) {
        	mStep = mRoute.getSteps().get(mCurrentStep-1);
        	mQuestions = mStep.getQuestions();      
        // when in clue or first question mode, decrease the currentStep so that the previous clue before onPause 
        // can be displayed, this is necessary because currentStep was increased before onPause in displayClue()
        // not necessary when onPause occurred during feedback and steps where answer is not required as we will move 
        // to the next step onResume
        } else if ((mCurrentStep > 0 && !proceedString.equals(getResources().getText(R.string.next).toString()) && 
        		!proceedString.equals(getResources().getText(R.string.done).toString())) ||
        		mCurrentStep == 1) {      	
        	--mCurrentStep;
        } 
        
	    // If not already in question mode, remain in clue mode
	    if (mTreasureHuntMode == QUESTION && mCurrentQuestion == 0) {
	    	mTreasureHuntMode = CLUE;	    	
	    // if waiting for answer, re-present the question
	    } else if (mTreasureHuntMode == ANSWER) {
	    	mTreasureHuntMode = QUESTION;
	    }
	    
	    // the hunt already started
    	if (mCurrentStep > 0){
    		mTxtSubtitles.setText(R.string.continue_instruction);
    		mBtnProceed.setText(R.string.continue_hunt);
    		spAnswers.setVisibility(View.INVISIBLE);
    	}
    	
    	if (!mMigrated) {
	        mBtnMigrate.setVisibility(View.VISIBLE);
	        mBtnMigrate.setOnClickListener(new View.OnClickListener() {
	        	
	        	@Override
	        	public void onClick(View view) {
	        		new InviteMigrate().execute(mDi);
	        		
	        		/*try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		HashMap<String,String> hm = new HashMap<String,String>();
        	        hm.put("directionSkillLevel", "medium");
        	        hm.put("distanceSkillLevel", "low");
        	        hm.put("symbolSkillLevel", "high");
        	        hm.put("directionToolUsed", "false");
        	        hm.put("distanceToolUsed", "true");
        	        hm.put("symbolToolUsed", "true");
        	        migrateDataIn(hm);     
        	        mBtnMigrate.setVisibility(View.INVISIBLE);
            		enableButtons();
            		mTxtSubtitles.setText(R.string.start_instruction);
            		mMigrated = true;*/
	        	}
	        });
        }

    	
    	// Register ttsReceiver
    	this.registerReceiver(ttsReceiver, new IntentFilter("hw.emote.eatreasurehunt.tts"));
    	
    	// start the bearing service
	    Intent bearingService = new Intent( this, EABearingService.class );
	    startService(bearingService);
	    
	    // register broadcast receivers for bearing and declination
	    registerReceiver(azimuthReceiver, new IntentFilter(EABearingService.BROADCAST_AZIMUTH));
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    
	    // if the step has been decreased onResume and user has not moved to the next step, 
	    // restore the mCurrentStep to previous onPause step
	    if (mCurrentStep < mResumeStep) {
	    	mCurrentStep = mResumeStep;
	    }
	    
	    // Saving the data in shared preferences
	    SharedPreferences huntData = getSharedPreferences(EAGeneral.DATA_FILE, 0);
	    SharedPreferences.Editor editor = huntData.edit();
	    editor.putInt(EAGeneral.TREASURE_HUNT_MODE, mTreasureHuntMode);
	    editor.putInt(EAGeneral.CURRENT_STEP, mCurrentStep);
	    editor.putInt(EAGeneral.CURRENT_QUESTION, mCurrentQuestion);
	    editor.putInt(EAGeneral.CORRECT_ANSWERS, mCorrectAnswers);
	    editor.putString(EAGeneral.PROCEED_STRING, mBtnProceed.getText().toString());
	    editor.putBoolean(EAGeneral.PRIZE_Q_ANSWERED, mPrizeQAnswered);
	    editor.putBoolean(EAGeneral.MIGRATED, mMigrated);
	    //Log.d(getClass().getSimpleName(),"onPause " + treasureHuntMode + " " + currentStep + " " + currentQuestion + " " + mBtnProceed.getText().toString());

	    // Commit the edits!
	    editor.commit();
	    
	    // Close the log file
	    mEALogManager.closeLogFile();
	    
	    // Unregister ttsReceiver
	    this.unregisterReceiver(ttsReceiver);
	    
	    // stop the bearing service
	    Intent bearingService = new Intent( this, EABearingService.class );
	    stopService(bearingService);
	    // unregister broadcast receiver
	    unregisterReceiver(azimuthReceiver);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    Log.d(getClass().getSimpleName(),"App destroyed");
	   
	    stopLocationService();
	}
	
	/**
	 * Prevent the user from closing the app during the hunt
	 */
	@Override
	public void onBackPressed() {
	   return;
	}
	
	/**
	 * Stop the location service
	 */
	private void stopLocationService(){
		// stop the location service
	    Intent locationService = new Intent( this, EALocationService.class );
	    stopService(locationService);
	    // unregister broadcast receiver
	    unregisterReceiver(declinationReceiver);
	}
	
	/**
	 * Get the feedbacks and routes
	 */
	private void initData(){
		
		//Reading different feedback and steps from XML file
        try {
        	InputStream isFeedback = getAssets().open(EAGeneral.FEEDBACKS_FILE);
        	InputStream isRoute = getAssets().open(EAGeneral.ROUTES_FILE);
        	// Reading the potential feedback
			XMLFeedbacksReader fbReader = new XMLFeedbacksReader(isFeedback);
			mFeedbacks = fbReader.getFeedback();
			for (int i = 0; i < mFeedbacks.size(); i++) {
				FeedbackTemplate fbTemplates = mFeedbacks.get(i);
				// use only the affective feedback (June 2015 experiment)
				if (fbTemplates.getName().equals(EAGeneral.AFFECTIVE)){
					//Log.d(getClass().getSimpleName(),"fbTemplates " + mSelectedFeedbackType + " " + fbTemplates.getName());
					mFeedback = fbTemplates;
				}
			}
			// Reading the steps of the treasure hunt
			XMLRouteReader routeReader = new XMLRouteReader(isRoute);
			mRoutes = routeReader.getRoutes();			
			for (int i = 0; i < mRoutes.size(); i++) {
				Route route = mRoutes.get(i);
				if (route.getName().equals(mSelectedRoute)){
					//Log.d(getClass().getSimpleName(),"route " + mSelectedRoute + " " + route.getName());
					mRoute = route;
				}
			}			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	     					
	}
	
	/**
	 * Display the extra prize dialog
	 */
	private void displayPrizeDialog() {			
    	
		mDialog = new Dialog(this); 
    	
    	//tell the Dialog to use the dialog_extra_prize.xml as it's layout description
       	mDialog.setContentView(R.layout.dialog_extra_prize);
        mDialog.setTitle(R.string.extra_prize_on_offer);
        
        // get the components
        mSpQ1Answers = (Spinner) mDialog.findViewById(R.id.spQ1Answers);
        mSpQ2Answers = (Spinner) mDialog.findViewById(R.id.spQ2Answers);
        mTxtDialogError = (TextView) mDialog.findViewById(R.id.txtDialogError);
    	mBtnSubmit = (Button) mDialog.findViewById(R.id.btnSubmit);
       	mBtnClose = (Button) mDialog.findViewById(R.id.btnClose);	         	
        
        // if the user already answered the questions
        if (mPrizeQAnswered == true) {
        	mSpQ1Answers.setEnabled(false);
   			mSpQ2Answers.setEnabled(false);
   			mBtnSubmit.setEnabled(false);
   			mTxtDialogError.setText(R.string.already_answered);   			
        } else {	                   
	        // Create an ArrayAdapter using the answers		
	    	ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
			        R.array.Q1_answers, android.R.layout.simple_spinner_item);
	     	// Specify the layout to use when the list of choices appears
	     	adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	     	// Apply the adapter to the spinner
	     	mSpQ1Answers.setAdapter(adapter1);    
	     	mSpQ1Answers.setOnItemSelectedListener(new OnItemSelectedListener() {		     		
	            @Override
	            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {	
	            	// get the answer only upon user selection and ignore the selection during initialisation 
	            	q1Answer = parent.getItemAtPosition(pos).toString();  
		            //Log.d(getClass().getSimpleName(),"Answer selected: " + q1Answer);	            
	            }
	            
	            @Override
	            public void onNothingSelected(AdapterView<?> parent) {
	  				// default to the first item in the list
	            	q1Answer = parent.getItemAtPosition(0).toString();
	            	//Log.d(getClass().getSimpleName(),"No answer selected ");
	   			}			
	        });			     	
	        
	        // Create an ArrayAdapter using the answers
	    	ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
			        R.array.Q2_answers, android.R.layout.simple_spinner_item);
	     	// Specify the layout to use when the list of choices appears
	     	adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	     	// Apply the adapter to the spinner
	     	mSpQ2Answers.setAdapter(adapter2);    
	     	mSpQ2Answers.setOnItemSelectedListener(new OnItemSelectedListener() {		     		
	            @Override
	            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {	
	            	// get the answer only upon user selection and ignore the selection during initialisation 
	            	q2Answer = parent.getItemAtPosition(pos).toString();  
		            //Log.d(getClass().getSimpleName(),"Answer selected: " + q1Answer);	            
	            }
	            
	            @Override
	            public void onNothingSelected(AdapterView<?> parent) {
	  				// default to the first item in the list
	            	q2Answer = parent.getItemAtPosition(0).toString();
	            	//Log.d(getClass().getSimpleName(),"No answer selected ");
	   			}			
	        });		
        }     	
     	
       	mBtnSubmit.setOnClickListener(new View.OnClickListener() {
       		@Override
       		public void onClick(View v) {
       			String text = "";
       			// user didn't provide both answers
       			if (q1Answer.equals(getResources().getText(R.string.select_answer).toString()) ||
       					q2Answer.equals(getResources().getText(R.string.select_answer).toString())) {
       				text = getResources().getText(R.string.prompt_all_answer).toString();  
       				mPrizeQAnswered = false;
        		} else {
        			// user answers both questions correctly
        			if (q1Answer.equals(getResources().getText(R.string.q1_answer).toString()) &&
           					q2Answer.equals(getResources().getText(R.string.q2_answer).toString())) {
        				text = getResources().getText(R.string.both_correct).toString();
        			// user answers Q1 correctly
        			} else if (q1Answer.equals(getResources().getText(R.string.q1_answer).toString())) {
        				text = getResources().getText(R.string.q1_correct).toString();
        			// user answers Q2 correctly
        			} else if (q2Answer.equals(getResources().getText(R.string.q2_answer).toString())) {
        				text = getResources().getText(R.string.q2_correct).toString();
        			// user answers both questions wrongly
        			} else {
        				text = getResources().getText(R.string.both_wrong).toString();
        			}     
        			
        			// Disable the components to prevent the user answering the questions twice
        			mSpQ1Answers.setEnabled(false);
           			mSpQ2Answers.setEnabled(false);
           			mPrizeQAnswered = true;   
           			
           			// Log the user answers           			
           			mEALogManager.writeToLogFile("Extra Prize - Q1 Answer: " + q1Answer + " Q2 Answer: " + q2Answer + 
           					"\n Feedback: " + text + "\n");  
        		} 	       			
       			mTxtDialogError.setText(text); 
       		}
       	});	 
       	
       	mBtnClose.setOnClickListener(new View.OnClickListener() {
       		@Override
       		public void onClick(View v) {
       			mDialog.dismiss();
       		}
       	});	   
    	mDialog.show();	               
	}
	
	/**
	 * Displays the different clues/tasks of the treasure hunt
	 */
	private void displayClue(){
		String strClue = "";
		//Log.d(getClass().getSimpleName(),"displayClue " + mRoute.getSteps().size());
		if (mCurrentStep < mRoute.getSteps().size()) {
	    	mStep = mRoute.getSteps().get(mCurrentStep);
	    	
	    	// retrieving the task
	    	strClue = mStep.getTask();
	    	
	    	// do a recap prior to the treasure hunt for emphatic agent
    		if (mCurrentStep == 0) {
    			if (isAgentWithMemory()) {
    				strClue += " " + getRecapString();
    			} else {
    				strClue += " " + getResources().getText(R.string.use_skills).toString();
    			}
    		// remaining steps, add memory related phrases if this is an emphatic agent with memory
    		} else {  		    	
		    	if (isAgentWithMemory()) {	
		    		// add memory related phrases for the step if one exists	
		    		int stringClueID = getStringResourceId(mStep.getName());
				    if (stringClueID != 0) {
				    	int stringToolID = getStringResourceId(mStep.getName() + "_tool");
				    	String tool = getResources().getText(stringToolID).toString();
				    	if (mToolsUsed.contains(tool)) {
				    		int stringMemoryClueID = getStringResourceId(mStep.getName() + "_usedTool");
				    		strClue += " " + getResources().getText(stringMemoryClueID).toString();
				    	} else {
				    		strClue += " " + getResources().getText(stringClueID).toString();
				    	}				    		
				    }
		    	} else {
		    		// add tool related phrases for the step if one exists	
		    		int stringClueID = getStringResourceId(mStep.getName() + "_no_memory");
		    		if (stringClueID != 0) {
		    			strClue += " " + getResources().getText(stringClueID).toString();
		    		}
		    	}
    		}
    		strClue += "\n";
    		
    		// get the questions if they exist
			mQuestions = mStep.getQuestions();
    		// an exception for task R1_C10 (June 2015 experiment)
    		if (mStep.getName().equals("R1_C10")) {
    			strClue = strClue + getResources().getText(R.string.paper_task).toString();
    		} else {				
				for (int i = 0; i < mQuestions.size(); i++) {
					// get a specific question
					Question question = mQuestions.get(i); 
					strClue = strClue + (i + 1) + ") " + question.getQuestion() + "\n";
				}  		
    		}
			
			// Emys speaking
			emysSpeak(Emotion.NEUTRAL, strClue);
			
			// Log the step
			mEALogManager.writeToLogFile("Clue: " + mStep.getName() + "\n" + strClue + "\n");
			
			// increase currentStep for next iteration
			mCurrentStep++;
			
			// switch to question mode where an answer is required after displaying the clue
			if (mCurrentStep >= 2 && mQuestions.size() > 0) {
				mTreasureHuntMode = QUESTION;
	    	}	
		}
	}    
    
	/**
	 * Displays the questions related to each clue
	 */
	private void displayQuestion(){		
		String strQuestion = "";
		mAnswer = null;
		
		if (mCurrentQuestion < mQuestions.size()) {
	    	final Question question = mQuestions.get(mCurrentQuestion); 
			strQuestion = question.getQuestion() + "\n";
			
			// if an answer if required
			if (question.getAnswers().size() > 0) {				
				ArrayList<String> answers = new ArrayList<String>();
				if (question.getIsImage()) {
					int size = question.getAnswers().size();
					mImageIcons = new Integer[size];
					for (int i=0; i<size; i++) {	
						// Create an answer list with image icons	
						mImageIcons[i] = getImageResourceId(question.getAnswers().get(i));
					}
					// Create an ArrayAdapter using the answers
					ImageArrayAdapter adapter = new ImageArrayAdapter(this, mImageIcons);	
					// Specify the layout to use when the list of choices appears
			     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			     	// Apply the adapter to the spinner
			     	spAnswers.setAdapter(adapter);   
				} else {				
					// Create an answer list with the instruction as the first element				
					answers.add(getResources().getText(R.string.select_answer).toString());
					answers.addAll(question.getAnswers());
					// Create an ArrayAdapter using the answers
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
			     		android.R.layout.simple_spinner_item, answers);
					// Specify the layout to use when the list of choices appears
			     	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			     	// Apply the adapter to the spinner
			     	spAnswers.setAdapter(adapter);   
				}
		      
		     	spAnswers.setOnItemSelectedListener(new OnItemSelectedListener() {
		     		
		            @Override
		            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {	
		            	// get the answer only upon user selection and ignore the selection during initialisation
		            	mAnswer = parent.getItemAtPosition(pos).toString();
			            //Log.d(getClass().getSimpleName(),"Answer selected: " + mAnswer);		            
		            }
		            
		            @Override
		            public void onNothingSelected(AdapterView<?> parent) {
		  				// default to the first item in the list
		   				mAnswer = parent.getItemAtPosition(0).toString();
		            	//Log.d(getClass().getSimpleName(),"No answer selected ");
		   			}			
		        });		
		     	spAnswers.setVisibility(View.VISIBLE);
		    	// switch to answer mode after displaying the question
				mTreasureHuntMode = ANSWER;		
			} else {
				// switch to clue mode if the question does not require an answer on screen
				//mTreasureHuntMode = CLUE;
				mTreasureHuntMode = CHANGE;
			}	     	
			// Emys speaking
			emysSpeak(Emotion.NEUTRAL, strQuestion);
			
			// Log the question
			mEALogManager.writeToLogFile("Question: " + strQuestion);
		} 		
	}
	
	/**
	 * Displays the feedback to an answer
	 */
	private void displayFeedback() {
		FeedbackItem fbItem = new FeedbackItem();
		spAnswers.setVisibility(View.INVISIBLE);	
		Random rand = new Random();
		String correctAnswer = mQuestions.get(mCurrentQuestion).getCorrectAnswer();
		int correctImageId = -1;
		String strFeedback = "";
		
		// get the image id for the correct answer
		if (mQuestions.get(mCurrentQuestion).getIsImage()) {
			correctImageId = getImageResourceId(correctAnswer);
			correctAnswer = String.valueOf(correctImageId);
		} 
			
		// correct answer
		if (mAnswer.equals(correctAnswer)) {
			int i = rand.nextInt(mFeedback.getPositiveFeedback().getFeedbackItems().size());
			fbItem = mFeedback.getPositiveFeedback().getFeedbackItem(i);
			strFeedback = fbItem.getText() + getResources().getText(R.string.thats_correct).toString() + "\n";
			mCorrectAnswers++;
		// incorrect answer
		} else {
			int i = rand.nextInt(mFeedback.getNegativeFeedback().getFeedbackItems().size());
			fbItem = mFeedback.getNegativeFeedback().getFeedbackItem(i);
			strFeedback = fbItem.getText() + ". " + 
					getResources().getText(R.string.correct_answer).toString() + " ";
			
			// display the correct symbol
			if (mQuestions.get(mCurrentQuestion).getIsImage()) {
				mSymbol.setVisibility(View.VISIBLE);
				mSymbol.setBackgroundResource(correctImageId);
			} else {
				strFeedback += correctAnswer + ".\n";
			}
		}	
		
		// Emys speaking
		if (mCorrectAnswers == mQuestions.size()) {
			// Get a random sound emblem
			Random ran = new Random();
			int x = ran.nextInt(EAGeneral.EMBLEMS_NO) + 1;
			int emblemID = getRawResourceId("positive" + String.valueOf(x));
			
			playEmblem(emblemID);
		}
		emysSpeak(Emotion.getEmotion(fbItem.getGesture()), strFeedback);	
		
		if (mQuestions.get(mCurrentQuestion).getIsImage()) {
			correctAnswer = mQuestions.get(mCurrentQuestion).getCorrectAnswer();
		}
		// Log the feedback
		mEALogManager.writeToLogFile("Answer: " + correctAnswer + "\n Feedback: " + strFeedback);
				
		// increase question count
		mCurrentQuestion++; 
		
		// reset question count if all questions for this clue has been presented
		if (mCurrentQuestion == mQuestions.size()) {
			mCurrentQuestion = 0;
			mCorrectAnswers = 0;
			mTreasureHuntMode = CHANGE;
			spAnswers.setVisibility(View.INVISIBLE);
		} else {
			mTreasureHuntMode = QUESTION;
		}
	}
	/** 
	 * Displays the instruction to take turn
	 */
	private void displayChangeInstruction() {
		// take turn instruction
		String text = getResources().getText(R.string.take_turn).toString();
		// Emys speaking
		emysSpeak(Emotion.NEUTRAL, text);		
		// switch to clue mode
		mTreasureHuntMode = CLUE;	
	}
	
	/** 
	 * Emys speaking
	 */
	private void emysSpeak(Emotion emotion, String utterance) {
		// update subtitles
		mTxtSubtitles.setText(utterance);
		// set Emys emotion
		setExpression(emotion);
		// speak text
		speakText(utterance);
		mTTSText = utterance;
		// disable the proceed and play again buttons while Emys is speaking
		disableButtons();
	}
	
	/**
	 * disable the proceed and play again buttons 
	 */
	private void disableButtons() {
		mBtnProceed.setEnabled(false);
		mBtnPlayAgain.setEnabled(false);
	}
	
	/**
	 * enable the proceed and play again buttons
	 */
	private void enableButtons() {
		mBtnProceed.setVisibility(View.VISIBLE);
		mBtnPlayAgain.setVisibility(View.VISIBLE);
		mBtnProceed.setEnabled(true);
		mBtnPlayAgain.setEnabled(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.eatreasure_hunt, menu);
		return true;
	}

	@Override
	protected LinearLayout getGlSurfaceViewContainerLayout() {		
		return (LinearLayout) this.findViewById(R.id.glLayoutHolder);
	}

	@Override
	protected void onEmysLoaded() {
		// blank the screen so that Emys is not seen initially
    	if (mMigrated) {
    		enableButtons();
    	} else {
    		mDi.blankScreen();
    	}
	}

	@Override
	public void confirmDialog(String infoText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dismissConfirmDialog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void askMultiChoice(Integer numChoices, String[] options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dismissMultiDialog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void migrateDataIn(HashMap<String, String> migData) {
		//make sure the interpreter's not running.
		mDialogSystem.interruptDialogEvent();
		mDi.unBlankScreen();
		playMigrateInSound();
		putMigrationData(migData);
	}

	@Override
	public void navigateFromTo(String from, String to, String callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getFreeTextDialog() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void putMigrationData(HashMap<String, String> data) {
		mMigrateData = data;
		// **** this is just for debugging - needs to be commented
		/*Iterator<HashMap.Entry<String, String>> iterator = mMigrateData.entrySet().iterator() ;
        while(iterator.hasNext()){
            HashMap.Entry<String, String> dataEntry = iterator.next();
            Log.d(getClass().getSimpleName(), dataEntry.getKey() +" :: "+ dataEntry.getValue());
            Toast.makeText(getApplicationContext(), dataEntry.getKey() +" :: "+ dataEntry.getValue(), Toast.LENGTH_SHORT).show();
        }*/
	}

	@Override
	public void playMigrateOutSound() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playMigrateInSound() {
		mMediaPlayer = MediaPlayer.create(this, R.raw.in);
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {	
			@Override
			public void onCompletion(MediaPlayer mp) {
				mDi.stopWaitingAndNotify();
			}
		});
		mMediaPlayer.start(); 
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		ImageView view = (ImageView) v;
    	switch (event.getAction() & MotionEvent.ACTION_MASK) {
    		// user touches the screen
    		case MotionEvent.ACTION_DOWN:

    			mSavedMatrix.set(mMatrix);
    			mSavedScaleMatrix.set(mScaleMatrix);
    			//Log.d(getClass().getSimpleName(),"mSavedMatrix=" + mSavedMatrix.toString());
    			mStartPoint.set(event.getX(), event.getY());
    			mode = DRAG;
    			break;
    			
    		// user touches the screen with two fingers 
    		case MotionEvent.ACTION_POINTER_DOWN:
	
    			mOldDist = spacing(event);
	
    			if (mOldDist > 10f) {
    				mSavedMatrix.set(mMatrix);
    				mSavedScaleMatrix.set(mScaleMatrix);
    				midPoint(mMidPoint, event);
    				mode = ZOOM;
    			}
    			break;
	
    		// user releases the touch
    		case MotionEvent.ACTION_UP:
    			mode = NONE;
    			break;
	
    		case MotionEvent.ACTION_POINTER_UP:
    			mode = NONE;	
    			break;
	
    		case MotionEvent.ACTION_MOVE:    			
    			if (mode == DRAG) {       				    				
    				mMatrix.set(mSavedMatrix);
    				// calculate the distance for translation
    				float xDist = event.getX() - mStartPoint.x;
    				float yDist = event.getY() - mStartPoint.y;
    				
    				// retrieving the matrix scale and translations to check that the final values are within boundaries
    				float[] f = new float[9];    				
    				mMatrix.getValues(f);
    				float scale = f[Matrix.MSCALE_X];
    				float transX = f[Matrix.MTRANS_X];
    				float transY = f[Matrix.MTRANS_Y]; 
    				
    				// limit the translation boundaries taking into consideration the scale of the map
    				float mMaxX = mTreasureMap.getWidth()/2;
    				float mMinX = - (mTreasureMap.getWidth() * 1.5f) * scale;
    				float mMaxY = mTreasureMap.getHeight()/2;
    				float mMinY = - (mTreasureMap.getHeight() * 1.75f) * scale ;
    				//Log.d(getClass().getSimpleName(),"Max Min " + mMaxX + " " + mMinX + " " +  mMaxY + " " + mMinY + " mScale " + scale);    
    				//Log.d(getClass().getSimpleName(),"xDist yDist before" + xDist + " " + yDist);
    				
    				// calculate the new position
    				float totalX = transX + xDist;
    				float totalY = transY + yDist;
					
    				// setting the boundaries
    				if (totalX > mMaxX) {
						xDist = mMaxX-totalX;    					
					}
					if (totalX < mMinX) {
						xDist = mMinX-totalX;
					}
					if (totalY > mMaxY) {
						yDist = mMaxY-totalY;
					}
					if (totalY < mMinY) {
						yDist = mMinY-totalY;
					}
					
					// apply the translation
					mMatrix.postTranslate(xDist, yDist); 
					//Log.d(getClass().getSimpleName(),"xDist yDist after" + xDist + " " + yDist + " " + transX + " " + transY);
    				//Log.d(getClass().getSimpleName(),"matrix=" + mMatrix.toString());					
    			} else if (mode == ZOOM) {
    				float[] f = new float[9];
    				float newDist = spacing(event);
    				mScale = 1f;
    			
    				if (newDist > 10f) {
    					mMatrix.set(mSavedMatrix);
    					mScaleMatrix.set(mSavedScaleMatrix);
    					mScale = newDist / mOldDist;
    					//Log.d(getClass().getSimpleName(),"newDist " + newDist + " oldDist " + mOldDist);
    					mMatrix.postScale(mScale, mScale, mMidPoint.x, mMidPoint.y);
    					mScaleMatrix.postScale(mScale, 1.0f, 0.0f, 0.0f);    					
    				}
    				
    				// retrieving the matrix scales
    				mMatrix.getValues(f);
    				float scaleX = f[Matrix.MSCALE_X];
    				//float scaleY = f[Matrix.MSCALE_Y];
    				
    				// the minimum and maximum zoom levels
    				if(scaleX <= MIN_ZOOM) {
    					mScale = MIN_ZOOM/scaleX;
    				}    	
    				else if(scaleX >= MAX_ZOOM) {
    					mScale = MAX_ZOOM/scaleX;
       				} 
    				mMatrix.postScale(mScale, mScale, mMidPoint.x, mMidPoint.y);
    				mScaleMatrix.postScale(mScale, 1.0f, 0.0f, 0.0f); 
    			}    			
    			break;	
	     }
    	 view.setImageMatrix(mMatrix);
    	 mMapScale.setImageMatrix(mScaleMatrix);
	     return true;
	}
	
	/**
	 * Determine the space between the first two fingers
	 */
	@SuppressLint("FloatMath")
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
	    float y = event.getY(0) - event.getY(1);
	    return FloatMath.sqrt(x * x + y * y);
	}

	/**
	 * Calculate the mid point of the first two fingers
	 */
	private void midPoint(PointF point, MotionEvent event) {
	    float x = event.getX(0) + event.getX(1);
	   	float y = event.getY(0) + event.getY(1);
	   	point.set(x / 2, y / 2);
	}
	
	/** 
	 * Broadcast receiver for the azimuth value 
	 */
	private BroadcastReceiver azimuthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	// Get the Azimuth
        	mAzimuth = intent.getFloatExtra(EABearingService.AZIMUTH, mCurrentDegree);
    		updateCompass();
        }
    };  
    
    /** 
	 * Broadcast receiver for GPS declination 
	 */
    private BroadcastReceiver declinationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	// Gets the GPS declination
        	mDeclination = intent.getFloatExtra(EALocationService.DECLINATION, 0f);
    		updateCompass();
        }
    };  
    
    /**
     * Updates the compass bearing
     */
    private void updateCompass() {
    	if (!Float.isNaN(this.mAzimuth)) {
            if(Float.isNaN(mDeclination)) {
                Log.d(getClass().getSimpleName(), "Location is NULL bearing is not true north!");
                mBearing = mAzimuth;
            } else {
            	mBearing = mAzimuth + mDeclination;
            	//Log.d(getClass().getSimpleName(), "azimuth " + mAzimuth + " bearing " + mBearing);
            }
    	}
    	
    	RotateAnimation ra = new RotateAnimation(
        		mCurrentDegree, 
                -mBearing,
                Animation.RELATIVE_TO_SELF, 0.5f, 
                Animation.RELATIVE_TO_SELF,
                0.5f);
 
        ra.setDuration(250);
 
        ra.setFillAfter(true);
 
        mCompass.startAnimation(ra);
        mCurrentDegree = -mBearing;	
    }
    
    /**
     * Get the image resource ID for a symbol
     * @param symbolName
     * @return resource ID of the image
     */
    private int getImageResourceId(String symbolName) {
    	Resources resources = getApplicationContext().getResources();
		
		int resourceId = resources.getIdentifier(symbolName, 
				"drawable", getApplicationContext().getPackageName());
		return resourceId;
    }
    
    /**
     * Get the resource ID for a string
     * @param stringName
     * @return resource ID of the string
     */
    private int getStringResourceId(String stringName) {
    	Resources resources = getApplicationContext().getResources();
		
		int resourceId = resources.getIdentifier(stringName, 
				"string", getApplicationContext().getPackageName());
		return resourceId;
    }
    
    /**
     * Get the resource ID for the sound emblem 
     * @param rawName
     * @return resource ID of the sound emblem
     */
    private int getRawResourceId(String rawName) {
    	Resources resources = getApplicationContext().getResources();
		
		int resourceId = resources.getIdentifier(rawName, 
				"raw", getApplicationContext().getPackageName());
		return resourceId;
    }
    
    /**
     * Check is this is an emphatic agent with memory
     * @return true if emphatic agent
     */
    private boolean isAgentWithMemory() {
    	if (mSelectedMemoryType.equals(EAGeneral.MEMORY)) 
    		return true;
		return false;
    }
    
    /**
     * Get the recap string
     * @author Meiyii
     *
     */
    private String getRecapString() {
    	String strRecap = "";
    	// if migration data exists
		if (mMigrateData != null) {
    		Iterator<HashMap.Entry<String, String>> iterator = mMigrateData.entrySet().iterator() ;
            while(iterator.hasNext()){
                HashMap.Entry<String, String> dataEntry = iterator.next();
                String entry = dataEntry.getKey();
                String value = dataEntry.getValue();
                // Check the user's skill levels
                // direction skill
                if (entry.equals(EAGeneral.DIRECTION_LEVEL)) {
                	String directionSkill = (String) getResources().getText(R.string.direction_skill);
                	if (value.equals(EAGeneral.HIGH_LEVEL)) {
                		mHighSkills.add(directionSkill);
                	} else if (value.equals(EAGeneral.MEDIUM_LEVEL)) {
                		mMedSkills.add(directionSkill);
                	} else {
                		mLowSkills.add(directionSkill);
                	}	                      
                } 
                // distance skill
                if (entry.equals(EAGeneral.DISTANCE_LEVEL)) {
                	String distanceSkill = (String) getResources().getText(R.string.distance_skill);
                	if (value.equals(EAGeneral.HIGH_LEVEL)) {
                		mHighSkills.add(distanceSkill);
                	} else if (value.equals(EAGeneral.MEDIUM_LEVEL)) {
                		mMedSkills.add(distanceSkill);
                	} else {
                		mLowSkills.add(distanceSkill);
                	}	                      
                } 
                // symbols skill
                if (entry.equals(EAGeneral.SYMBOL_LEVEL)) {
                	String symbolSkill = (String) getResources().getText(R.string.symbol_skill);
                	if (value.equals(EAGeneral.HIGH_LEVEL)) {
                		mHighSkills.add(symbolSkill);
                	} else if (value.equals(EAGeneral.MEDIUM_LEVEL)) {
                		mMedSkills.add(symbolSkill);
                	} else {
                		mLowSkills.add(symbolSkill);
                	}	                      
                }   
                
                // tools use
                if (entry.equals(EAGeneral.DIRECTION_TOOL_USED)) {
                	String directionTool = (String) getResources().getText(R.string.direction_tool);
                	if (value.equals(EAGeneral.TRUE)) {
                		mToolsUsed.add(directionTool);
                	}                		
                }
                if (entry.equals(EAGeneral.DISTANCE_TOOL_USED)) {
                	String distanceTool = (String) getResources().getText(R.string.distance_tool);
                	if (value.equals(EAGeneral.TRUE)) {
                		mToolsUsed.add(distanceTool);
                	}                		
                }
                if (entry.equals(EAGeneral.SYMBOL_TOOL_USED)) {
                	String symbolTool = (String) getResources().getText(R.string.symbol_tool);
                	if (value.equals(EAGeneral.TRUE)) {
                		mToolsUsed.add(symbolTool);
                	}                		
                }
            }
            
            // if the user has more than one high skills
            if (mHighSkills.size() > 1) {
            	strRecap += (String) getResources().getText(R.string.recapHighSkills) + " ";
            	strRecap += getAllElements(mHighSkills) + ". ";
            // user has only one high skill, include medium skills too
            } else if (mHighSkills.size() == 1) {
            	strRecap += (String) getResources().getText(R.string.recapHighSkills) + " " + mHighSkills.get(0);
            	if (mMedSkills.size() > 1) {
            		strRecap += ", good at ";
            		strRecap += getAllElements(mMedSkills) + ". ";
            	} else if (mMedSkills.size() == 1) {
            		strRecap += ", good at " + mMedSkills.get(0);
            		if (mLowSkills.size() > 1) {
            			strRecap += ", learned ";
            			strRecap += getAllElements(mLowSkills) + ". ";
            		} else if (mLowSkills.size() == 1) {
            			strRecap += " and learned " + mLowSkills.get(0) + ". ";
            		}	                    	
            	}
            // if user has no high skill but more than one medium skills
            } else if (mMedSkills.size() > 1) {
        		strRecap += (String) getResources().getText(R.string.recapMedSkills) + " ";
        		strRecap += getAllElements(mMedSkills) + ". ";
            // if user has no high skill and only one medium skill, include low skills
            } else if (mMedSkills.size() == 1) {
            	strRecap += (String) getResources().getText(R.string.recapMedSkills) + " " + mMedSkills.get(0);
            	if (mLowSkills.size() > 1) {
            		strRecap += ", learned ";
            		strRecap += getAllElements(mLowSkills) + ". ";
            	} 
            // if user has no high or medium skills
            } else if (mLowSkills.size() > 1) {
            		strRecap += (String) getResources().getText(R.string.recapLowSkills) + " ";
            		strRecap += getAllElements(mLowSkills) + ". ";
            }                                  	
            strRecap += (String) getResources().getText(R.string.recapUseSkills) + " ";
            
            if (mToolsUsed.size() > 0) {
            	strRecap += (String) getResources().getText(R.string.recapTools) + " ";
	            if (mToolsUsed.size() > 1) {
	            	strRecap += getAllElements(mToolsUsed) + " ";
	            	strRecap += (String) getResources().getText(R.string.recapToolsUsefulness) + " ";
	            } else if (mToolsUsed.size() == 1) {
	            	strRecap += mToolsUsed.get(0) + " ";
	            	strRecap += (String) getResources().getText(R.string.recapToolUsefulness) + " ";
	            }	            
            }
            strRecap += (String) getResources().getText(R.string.goodLuck);
        }
		return strRecap;
    }
    
    /** 
     * Getting all elements of a string arraylist 
     * @param elements
     * @return a string of the elements
     */
    private String getAllElements(ArrayList<String> elements) {
    	String strElements = "";
    	// get all the elements in the ArrayList except the last two	                    	
    	for (int i = 0; i < elements.size() - 2; i++) {
    		strElements += elements.get(i) + ", ";
    	}	   
    	// get the last two elements in the ArrayList
    	strElements += elements.get(elements.size()-2) + " and " + elements.get(elements.size()-1);
    	return strElements;
    }
    
    /**
     * Play the sound emblem
     * @param emblemID
     */
    private void playEmblem(int emblemID) {
    	mMediaPlayer = MediaPlayer.create(this, emblemID);
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {	
			@Override
			public void onCompletion(MediaPlayer mp) {
				mDi.stopWaitingAndNotify();
			}
		});
		mMediaPlayer.start();
    }
    
    /**
     * AsyncTask for migration
     * @author Meiyii     *
     */
    private class InviteMigrate extends AsyncTask<AndroidDialogInterface, Void, HashMap<String, String>>{	
        // Do the long-running work in here
    	//HashMap<String, String> mData = new HashMap<String, String>();
        @Override
		protected HashMap<String, String> doInBackground(AndroidDialogInterface... mDi) {
        	try{
        		if(mDi[0].inviteMigrate(mMachineIP)) {
        			//Toast.makeText(getApplicationContext(), "Migration successful", Toast.LENGTH_LONG).show();
        		} 
        		return mMigrateData;
        	} catch (Exception e) {
      			e.printStackTrace();
      			return null;
      		}
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(HashMap<String, String> mMigrateData) {
        	mBtnMigrate.setVisibility(View.INVISIBLE);
    		enableButtons();
    		mTxtSubtitles.setText(getResources().getText(R.string.start_instruction).toString());
    		// Emys speaking
    		emysSpeak(Emotion.NEUTRAL, getResources().getText(R.string.start_instruction).toString());
    		mMigrated = true;
        }
    }	
}
