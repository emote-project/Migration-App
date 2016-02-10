package hw.emote.eatreasurehunt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * @author Mei Yii Lim
 *
 * This is the setup activity for the EATreasureHunt application. 
 * 
 * It allows configuration of the experimental conditions: Memory, Route and userID
 * When user presses the "Go to Treasure Hunt button", it will start the EATreasureHunt Activity.
 */

public class EASetupActivity extends Activity {	
		
		// Interface components
		private RadioGroup mRadioMemoryType;
		private RadioGroup mRadioRoute;
		private EditText mEditUserID;
		private EditText mEditMachineIP;
		private Button mBtnStartApp;
		private Button mBtnReconfigureApp;
		private TextView mSetupError;
		
		// Setup data
		private int mMemoryType;
		private int mRoute;
		private String mUserID;
		private String mMachineIP;
		private String mLogFileName;
	
	 	@Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_easetup);
	        
	        mRadioMemoryType = (RadioGroup) findViewById(R.id.radioMemoryType);
	        mRadioRoute = (RadioGroup) findViewById(R.id.radioRoute);
	        mEditUserID = (EditText) findViewById(R.id.editUserID);
	        mEditMachineIP = (EditText) findViewById(R.id.editMachineIP);
	        mBtnStartApp = (Button) findViewById(R.id.btnStartApp);
	        mBtnReconfigureApp = (Button) findViewById(R.id.btnReconfigureApp);
	        mSetupError = (TextView) findViewById(R.id.txtSetupError);
	        
			// Start button Click Event - Go to the Treasure Hunt Activity
	        mBtnStartApp.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
				public void onClick(View view) {
	            	mMemoryType = mRadioMemoryType.getCheckedRadioButtonId();
	            	mRoute = mRadioRoute.getCheckedRadioButtonId();
	            	mUserID = mEditUserID.getText().toString();
	            	mMachineIP = mEditMachineIP.getText().toString();
	            	mLogFileName = "";
	            	
	            	// check that feedback type and route were selected and user ID has been entered
	            	if ((mMemoryType != -1) && (mRoute != -1) && (!mUserID.equals(""))) {
	            		mSetupError.setVisibility(View.INVISIBLE);
	            		Intent intent = new Intent(getApplicationContext(), EATreasureHuntActivity.class);
	            		intent.putExtra(EAGeneral.USER_ID, mUserID);
	            		intent.putExtra(EAGeneral.MACHINE_IP, mMachineIP);
	            		mLogFileName += mUserID;
	            		// setting the selected feedback type
	            		if (mMemoryType == R.id.rbMemory) {
	            			intent.putExtra(EAGeneral.MEMORY_TYPE, EAGeneral.MEMORY);
	            			mLogFileName += "_memory";
	            		} else {
	            			intent.putExtra(EAGeneral.MEMORY_TYPE, EAGeneral.NO_MEMORY);
	            			mLogFileName += "_no_memory";
	            		}
	            		// setting the selected route
	            		if (mRoute == R.id.rbRoute1) {
	            			intent.putExtra(EAGeneral.ROUTE, EAGeneral.ROUTE_1);
	            			mLogFileName += "_route1";
	            		} else {
	            			intent.putExtra(EAGeneral.ROUTE, EAGeneral.ROUTE_2);
	            			mLogFileName += "_route2";
	            		}
	            		intent.putExtra(EAGeneral.LOG_FILE, mLogFileName);
	            		disableComponents();
	            		mBtnReconfigureApp.setLongClickable(true);
	            		startActivity(intent);	            		
	            	} else {
	            		mSetupError.setVisibility(View.VISIBLE);
	            	}
	            }
	        });
	        
	        mBtnReconfigureApp.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
				   	resetComponents();
            		return false;
	            }
	        });
	            	
	    }
	 	
	 	private void disableComponents() {
	 		// disable all the components
    		for (int i = 0; i <mRadioMemoryType.getChildCount(); i++) 
    		{
    			mRadioMemoryType.getChildAt(i).setEnabled(false);      
    		}
    		for (int i = 0; i <mRadioRoute.getChildCount(); i++) 
    		{
    			mRadioRoute.getChildAt(i).setEnabled(false);      
    		}
    		mEditUserID.setEnabled(false);
    		mEditMachineIP.setEnabled(false);	
	 	}
	 	
	 	private void resetComponents() {
	 		mRadioMemoryType.clearCheck();
        	//mRadioRoute.clearCheck();
        	mEditUserID.setText("");
        	mEditMachineIP.setText(getResources().getText(R.string.IP_address));
        	
    		// enable all the components
    		for (int i = 0; i <mRadioMemoryType.getChildCount(); i++) 
    		{
    			mRadioMemoryType.getChildAt(i).setEnabled(true);      
    		}
    		/*for (int i = 0; i <mRadioRoute.getChildCount(); i++) 
    		{
    			mRadioRoute.getChildAt(i).setEnabled(true);      
    		}*/
    		mEditUserID.setEnabled(true);
    		mEditMachineIP.setEnabled(true);
    		
    		// Delete previous data from shared preferences
    		SharedPreferences setup = getSharedPreferences(EAGeneral.SETUP_FILE, 0);
		    SharedPreferences.Editor setupEditor = setup.edit(); 
		    setupEditor.putInt(EAGeneral.MEMORY_TYPE, -1);
		    setupEditor.putInt(EAGeneral.ROUTE, -1);
		    setupEditor.putString(EAGeneral.USER_ID, "");
		    setupEditor.putString(EAGeneral.MACHINE_IP, "");
		    setupEditor.putString(EAGeneral.LOG_FILE, "");
		    // Commit the edits!
		    setupEditor.commit();
		    
    	    SharedPreferences huntData = getSharedPreferences(EAGeneral.DATA_FILE, 0);
    	    SharedPreferences.Editor editor = huntData.edit();
    	    editor.putInt(EAGeneral.TREASURE_HUNT_MODE, 0);
    	    editor.putInt(EAGeneral.CURRENT_STEP, 0);
    	    editor.putInt(EAGeneral.CURRENT_QUESTION, 0);
    	    editor.putString(EAGeneral.PROCEED_STRING, "");
    	    editor.putBoolean(EAGeneral.PRIZE_Q_ANSWERED, false);
    	    editor.putBoolean(EAGeneral.MIGRATED, false);
    	    // Commit the edits!
		    editor.commit();
	 	}
	 	
	 	@Override
		public void onResume() {      
		   super.onResume();
		   // Restore preferences
	        SharedPreferences settings = getSharedPreferences(EAGeneral.SETUP_FILE, 0);
	        mMemoryType = settings.getInt(EAGeneral.MEMORY_TYPE, -1);
	        mRoute = settings.getInt(EAGeneral.ROUTE, -1);
	        mUserID = settings.getString(EAGeneral.USER_ID, "");
	        mMachineIP = settings.getString(EAGeneral.MACHINE_IP, (getResources().getText(R.string.IP_address)).toString());
	        mLogFileName = settings.getString(EAGeneral.LOG_FILE, "");
	        
	        //Log.d(getClass().getSimpleName(), "onResume " + mFeedbackType + " " + mRoute + " " + mUserID);
	        
	        // setup data exists
	        if (mMemoryType != -1) {
		        mRadioMemoryType.check(mMemoryType);
		        mRadioRoute.check(mRoute);
		        mEditUserID.setText(mUserID);
		        mEditMachineIP.setText(mMachineIP);
		        disableComponents();
	        }
	        
	        // check route 1 (for June 2015 experiment)
	        mRadioRoute.check(R.id.rbRoute1); 
	        for (int i = 0; i <mRadioRoute.getChildCount(); i++) 
    		{
    			mRadioRoute.getChildAt(i).setEnabled(false);      
    		}
		}
		
		@Override
		public void onPause() {
		   super.onPause();
		   
		   	// Saving the data in shared preferences
		    SharedPreferences settings = getSharedPreferences(EAGeneral.SETUP_FILE, 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putInt(EAGeneral.MEMORY_TYPE, mMemoryType);
		    editor.putInt(EAGeneral.ROUTE, mRoute);
		    editor.putString(EAGeneral.USER_ID, mUserID);
		    editor.putString(EAGeneral.MACHINE_IP, mMachineIP);
		    editor.putString(EAGeneral.LOG_FILE, mLogFileName);
		    //Log.d(getClass().getSimpleName(), "onPause " + mFeedbackType + " " + mRoute + " " + mUserID + " " + mMachineIP);

		    // Commit the edits!
		    editor.commit();
		}	
		 
}