package hw.emote.eatreasurehunt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Mei Yii Lim
 *
 * This is the class that provides location information through GPS
 * 
 * It implements the LocationListener to get GPS reading 
 */

public class EALocationService extends Service implements LocationListener {
 	
    private LocationManager mLocationManager;
    private Location mLocation;
    private long mPreviousTime;
 	
    public static final String BROADCAST_DECLINATION = "hw.emote.eatreasurehunt.ealocationservice";
    public static final String DECLINATION = "declination";
    public static final String ELAPSED_TIME = "elapsedTime";
    public static final String GPS_LOG_FILE = "gpsFile";
 
    // External Storage Log 
 	private EALogManager mEALogManager;
 	private String mLogFileName;
 	private boolean mLogFileCreated = false;
 	
    /**
     * Intent to broadcast the declination when location changes
     */
    private Intent mIntent;
    
    @Override
    public void onCreate() {
    
    	// intent to broadcast the declination
    	mIntent = new Intent(BROADCAST_DECLINATION);        
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mPreviousTime = SystemClock.elapsedRealtime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {        
        for (final String provider : mLocationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.PASSIVE_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                if (mLocation == null) {
                    mLocation = mLocationManager.getLastKnownLocation(provider);
                }
                // update location only every 10s and only when the distance has changed by 10 metres
                //mLocationManager.requestLocationUpdates(provider, 10000, 10.0f, this);
                mLocationManager.requestLocationUpdates(provider, 5000, 5.0f, this);
            }
        }
 	 	
        // Log Manager
 	 	mEALogManager = new EALogManager();
        mLogFileName = intent.getStringExtra(EAGeneral.LOG_FILE) + ".gpx";
        if (mEALogManager.checkStorageStatus()) {
 	 		mEALogManager.createGPSFile(mLogFileName);
 	 	}
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
	public IBinder onBind(Intent intent) {
		return null;
	}
    
	@Override
	public void onDestroy() {
	    //Toast.makeText(this, "Location service destroyed", Toast.LENGTH_SHORT).show();    
        mLocationManager.removeUpdates(this);
        mEALogManager.terminateGPSFile(mLogFileName);
	}
	
 	/**
     * @return current location
     */
    public Location getCurrentLocation() {
        return mLocation;
    }
    
    /**
     * @return user bearing - horizontal movement of the device
     */
    public float getUserBearing() {
    	return mLocation.getBearing();
    }
    
    /**
     * @return bearing from the current location to destination
     */
    public float getBearingToDestination(Location destination) {
    	return mLocation.bearingTo(destination);
    }
    
    /**
     * @return speed of user movement
     */
    public float getUserSpeed() {
    	return mLocation.getSpeed();
    }
		
	public void onLocationChanged(Location location) {
        mLocation = location;
        //****mEALogManager.writeToGPSFile(location);
        writeToGPSLog(location);
        //Log.d(getClass().getSimpleName(), "location " + mLocation);
        
        float declination = new GeomagneticField((float) mLocation.getLatitude(),
	            (float) mLocation.getLongitude(), (float) mLocation.getAltitude(),
	            mLocation.getTime()).getDeclination();
        long elapsedTime = SystemClock.elapsedRealtime() - mPreviousTime;
        mIntent.putExtra(DECLINATION, declination);
        mIntent.putExtra(ELAPSED_TIME, elapsedTime);
        sendBroadcast(mIntent);
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Logging the GPS data
	 * @param location
	 */
	private void writeToGPSLog(Location location) {
		if (mEALogManager.checkStorageStatus()) {
			mEALogManager.writeToGPSFile(mLogFileName, location);
		}
	}
}
