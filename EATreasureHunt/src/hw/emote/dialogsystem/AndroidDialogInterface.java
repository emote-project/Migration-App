package hw.emote.dialogsystem;

import hw.emote.dialogsystem.DialogInterface;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

import uk.ac.hw.lirec.emys3d.Emys3DActivity;
import uk.ac.hw.lirec.emys3d.EmysModel;
import uk.ac.hw.lirec.emys3d.EmysModel.Emotion;
import android.app.Activity;
import android.util.Log;

/**
 * @author iw24
 * For now this is just a test class, hence the hacky approach of passing it the activity, so
 * it can call methods on it.
 * 
 * Note the above comment, and realise that this hacky approach is what we went with, as 
 * it worked well enough for our experiment.
 */
public class AndroidDialogInterface extends DialogInterface {

	private static final long MOMENTARY_EXPRESSION_TIME = 1000; //ms to show an expression 
	private static final int MIGRATE_PORT = 5228;
	private static final String COMMAND_INVITE = "INVITE";
	private static final String COMMAND_MIGRATEIN = "MIGRATEIN";
	
	private Activity mActivity;
	private AndroidDialogProvider mProvider;
	private boolean mInterrupted = false;
	private boolean mWaiting = false;
	
	public AndroidDialogInterface(Activity mainActivity, AndroidDialogProvider provider ) {
		mActivity = mainActivity;
		mProvider = provider;
	}
	
	public void startNav(final String from, final String to, final String callback) {
		if (mInterrupted)
			return;
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.navigateFromTo(from, to,callback);	
			}
		});

	}
	
	@Override
	public void speakText(final String text) {
		if (mInterrupted)
			return;
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.speakText(text);	
			}
		});
		waitForCallback();
	}
	
	@Override
	public void getResponse(final String infoText ) {
		if (mInterrupted)
			return;
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.confirmDialog(infoText);	
			}
		});

		waitForCallback();
		if (mInterrupted) {
			stopWaiting();
			mProvider.dismissConfirmDialog();
		}
	}
	
	@Override
	public synchronized void interruptDialog() {
		mInterrupted = true;
		this.notify();
	}
	
	public void stopWaiting() { mWaiting = false;}
	
	public synchronized void stopWaitingAndNotify() {
		stopWaiting();
		this.notify();
	}
	
	@Override
	public void resetDi() {

		mWaiting = false;
		mInterrupted = false;
	}
	
	public boolean isWaiting() { return mWaiting;}
	
	
	private String multiChoiceAnswer = new String();
	
	public void setMultiChoiceAnswer(String ans) { multiChoiceAnswer = ans;}
	
	@Override
	public String multipleChoiceQuestion(final Integer numChoices, final String[] options) {
		if (mInterrupted)
			return "";
		
		multiChoiceAnswer = "";
		
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.askMultiChoice(numChoices,options);	
			}
		});

		waitForCallback();
		if (mInterrupted) {
			stopWaiting();
			mProvider.dismissMultiDialog();
		}
		return multiChoiceAnswer;
	}
	
	private EmysModel.Emotion mood2emotion( Moods mood) {		
		switch (mood)  {
			case ANGER : return Emotion.ANGER;
			case NEUTRAL : return Emotion.NEUTRAL;
			case JOY : return Emotion.JOY;
			case SADNESS : return Emotion.SADNESS;
			case SLEEP : return Emotion.SLEEP;
			default : 	System.out.println("WARNING unknown mapping from mood: "+mood+" to emotion!");
						return Emotion.NEUTRAL;
		}	
	}
	
	private EmysModel.Emotion expression2emotion(Expression exp) {
		switch (exp)  {
		case ANGER : return Emotion.ANGER;
		case SURPRISE : return Emotion.SURPRISE;
		case JOY : return Emotion.JOY;
		case SADNESS : return Emotion.SADNESS;
		default : 	System.out.println("WARNING unknown mapping from expression: "+exp+" to emotion!");
					return Emotion.NEUTRAL;
		}
	}
	
	@Override
	public void setMood(Moods mood) {
		if (mInterrupted)
			return;
		
		mProvider.setExpression(mood2emotion(mood));
	}
	@Override
	public void showExpression(Expression expression) {
		if (mInterrupted)
			return;
		
		EmysModel.Emotion current = mProvider.getEmysEmotion();
		mProvider.setExpression(expression2emotion(expression));
		try {
			Thread.sleep(MOMENTARY_EXPRESSION_TIME); //try 600 milli
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		if (mInterrupted)
			return; //check here too incase this was interrupted. 
		mProvider.setExpression(current);
	}

	
	private synchronized void waitForCallback() {
		mWaiting = true;
		while (mWaiting && !mInterrupted) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}
	@Override
	public boolean migrateDataOut(String migrateTo, HashMap<String, String> dataToMigrate) {
		if (mInterrupted)
			return false;
		
		Log.v("migrateDataOut", dataToMigrate.toString());
		if (migrateTo.equalsIgnoreCase("none")) {
			mProvider.putMigrationData(dataToMigrate);
			return true;
		}
		boolean done = false;
		try {
			Log.v("migrateDataOut", "Connecting to: "+migrateTo +":"+MIGRATE_PORT);
			
            Socket migrateServer = new Socket(migrateTo,MIGRATE_PORT);
            Log.v("migrateDataOut", "Socket open");
            
            ObjectOutputStream out = new ObjectOutputStream(migrateServer.getOutputStream());
		
            out.writeObject(COMMAND_MIGRATEIN);
            out.flush();
            
            Log.v("migrateDataOut", "Sending data..");
            
            out.writeObject(dataToMigrate);
            out.flush();
            done = true;
            migrateServer.shutdownOutput();
          //  migrateServer.close();
            Log.v("migrateDataOut", "Done");
            return true;
            
		} catch (Exception e) {
        	//e.printStackTrace();
        	 Log.v("migrateDataOut", "ERROR excepption connecting to "+migrateTo+":"+MIGRATE_PORT);
        	 return done;
        }
		
	}
	
	public void blankScreen() {
		((Emys3DActivity)mActivity).blankScreen();
	}
	public void unBlankScreen() {
		((Emys3DActivity)mActivity).unBlankScreen();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean inviteMigrate(String migrateFrom) {
		if (mInterrupted)
			return false;
		
		// this needs to connect to a migration server, try and get the data
		//and return true if successful, and queue up the start of a new migrate-in/evaluation
		try {
			
			Log.v("inviteMigrate", "Connecting to: "+migrateFrom+ ":"+MIGRATE_PORT);
			
            Socket migrateServer = new Socket(migrateFrom,MIGRATE_PORT);
            Log.v("inviteMigrate", "socket opened");
            
            ObjectOutputStream out = new ObjectOutputStream(migrateServer.getOutputStream());
		
            Log.v("inviteMigrate", "sending invite");
            out.writeObject(COMMAND_INVITE);
            out.flush();
            Log.v("inviteMigrate", "reading object");
            
            ObjectInputStream in = new ObjectInputStream(migrateServer.getInputStream());
            final HashMap<String, String> migDataIn = (HashMap<String, String>) in.readObject();
            mActivity.runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				try {
						Thread.sleep(3000); // delay for 3 seconds
						mProvider.migrateDataIn(migDataIn);	
					} catch (InterruptedException e) {
						e.printStackTrace();
					}     				
    			}
    		});
            migrateServer.close();
            Log.v("inviteMigrate", "Done");
            blankScreen();
            return true;
            
		} catch (Exception e) {
        	e.printStackTrace();
        	 Log.v("inviteMigrate", "ERROR connecting to "+migrateFrom+":"+MIGRATE_PORT);
        	 return false;
        }
		
	}
	
	private String freetextAnswer = new String();
	
	public void setFreetextAnswer(String ans) { freetextAnswer = ans;}
	
	
	public String getFreetext() {
		
		if (mInterrupted)
			return null;
		
		freetextAnswer = "";
		
		mActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProvider.getFreeTextDialog();	
			}
		});

		waitForCallback();
		if (mInterrupted) {
			stopWaiting();
			mProvider.dismissConfirmDialog();
		}
		return freetextAnswer;

	}
	
	public boolean isInterrupted() {
		return mInterrupted;
	}
	
	public void migrateOutSound() {
		if (mInterrupted)
			return;
		
		mProvider.playMigrateOutSound();
		
		waitForCallback();
		if (mInterrupted) {
			stopWaiting();
		}
	}
	public void migrateInSound() {
		if (mInterrupted)
			return;
		
		mProvider.playMigrateInSound();
		waitForCallback();
		if (mInterrupted) {
			stopWaiting();
		}
	}
}
