package hw.emote.eatreasurehunt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Mei Yii Lim
 *
 * This is the class that provides bearing with reference to magnetic north - Azimuth
 * 
 * It implements the SensorEventListener to get the accelerometer and magnetic sensors reading 
 * for calculation of the bearing
 */

public class EABearingService extends Service implements SensorEventListener {
    
	// device sensor manager
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
 
    public static final String BROADCAST_AZIMUTH = "hw.emote.eatreasurehunt.eabearingservice";
    public static final String AZIMUTH = "azimuth";
    
    /**
     * angle to magnetic north
     */
    private float mAzimuth = Float.NaN;
 
    /**
     * Intent to broadcast the bearing on sensor change
     */
    private Intent mIntent;
    
    @Override
    public void onCreate() {
    	
    	// intent to broadcast the Azimuth
    	mIntent = new Intent(BROADCAST_AZIMUTH);
    	
    	// initialize sensor capabilities
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	// the system's sensor listeners
    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
	    //Toast.makeText(this, "Bearing service destroyed", Toast.LENGTH_SHORT).show();
	    mSensorManager.unregisterListener(this, mAccelerometer);
        mSensorManager.unregisterListener(this, mMagnetometer);        
	}
    
 	@Override
	public void onSensorChanged(SensorEvent event) {	
		
		if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            float azimuthInRadians = mOrientation[0];
            // changing -180 to 180 for 0 to 360
            float azimuthInDegrees = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
            mAzimuth = azimuthInDegrees;
            //Log.d(getClass().getSimpleName(), "azimuth " + mAzimuth);
            
            mIntent.putExtra(AZIMUTH, mAzimuth);
            sendBroadcast(mIntent);
        }
	}
 	
 	/**
     * @return current bearing
     */
    public float getCurrentBearing()
    {
        return mAzimuth;
    }
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}
