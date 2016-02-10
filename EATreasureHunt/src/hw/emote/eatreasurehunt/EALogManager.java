package hw.emote.eatreasurehunt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

/**
 * @author Mei Yii Lim
 *
 * This is the Log Manager for the EATreasureHunt App.
 * 
 * It creates two files. One for logging the different steps completion, questions and answers with time stamps and 
 * the other without time stamps to be used in teacher students discussions after they have completed the treasure hunt.
 */

public class EALogManager {
	static final String DATEFORMAT = "yyyy-MM-dd";
	static final String TIMEFORMAT = "HH:mm:ss";
	private File mSDCard;
	private File mLogFile;
	private File mQAFile;
	private File mGPSFile;
	private FileWriter mLogWriter;
	private FileWriter mQAWriter;
	private FileWriter mGPSWriter;
	
	public EALogManager() {
		mSDCard = null;
		mLogFile = null;
		mQAFile = null;
		mGPSFile = null;
		mLogWriter = null;
		mQAWriter = null;
		mGPSWriter = null;
	}
	
	public boolean checkStorageStatus() {
		String status = Environment.getExternalStorageState();
		  if (!status.equals(Environment.MEDIA_MOUNTED)) {
		   return false;
	    }
		return true;
	}
	
	public void createLogFile(String fileName) {
		mSDCard = Environment.getExternalStorageDirectory();
		mLogFile = new File(mSDCard.getAbsolutePath() + "/" + fileName);
		mQAFile = new File(mSDCard.getAbsolutePath() + "/qa_" + fileName);
		GregorianCalendar gcalendar = new GregorianCalendar();	
		String date = gcalendar.get(Calendar.DATE) + "/" + gcalendar.get(Calendar.MONTH) + "/" + gcalendar.get(Calendar.YEAR);
		String time = gcalendar.get(Calendar.HOUR) + ":" + gcalendar.get(Calendar.MINUTE) + ":" + gcalendar.get(Calendar.SECOND);
		try {			
			mLogWriter = new FileWriter(mLogFile, true);	
			mLogWriter.append(date + " " + time + " " + fileName + "\n");
			
			mQAWriter = new FileWriter(mQAFile, true);	
			//mGPSWriter = new FileWriter(mGPSFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void writeToLogFile(String data) {	
		GregorianCalendar gcalendar = new GregorianCalendar();	
		String time = gcalendar.get(Calendar.HOUR) + ":" + gcalendar.get(Calendar.MINUTE) + ":" + gcalendar.get(Calendar.SECOND);
		try {
			mLogWriter.append(time + " " + data);
			mQAWriter.append(data);
		} catch (IOException e) {
			e.printStackTrace();
		} 				
	}	
	
	public void closeLogFile() {
		try {
			mLogWriter.close();
			mQAWriter.close();
			//mGPSWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
	}	
	
	public void createGPSFile(String fileName) {
		mSDCard = Environment.getExternalStorageDirectory();
		mGPSFile = new File(mSDCard.getAbsolutePath() + "/gps_" + fileName);	
		try {			
			mGPSWriter = new FileWriter(mGPSFile, true);
			mGPSWriter.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			mGPSWriter.append("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\" creator=\"EATreasureHunt\">\n");
			mGPSWriter.append("<metadata>\n");
			mGPSWriter.append("\t<name>" + fileName + "</name>\n");
			mGPSWriter.append("</metadata>\n");
			mGPSWriter.append("\t<trk>\n");
			mGPSWriter.append("\t\t<trkseg>\n");
			mGPSWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void writeToGPSFile(String fileName, Location location) {	
		mSDCard = Environment.getExternalStorageDirectory();
		mGPSFile = new File(mSDCard.getAbsolutePath() + "/gps_" + fileName);
		//GregorianCalendar gCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdfDate = new SimpleDateFormat(DATEFORMAT);
		SimpleDateFormat sdfTime = new SimpleDateFormat(TIMEFORMAT);
	    sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
	    sdfTime.setTimeZone(TimeZone.getTimeZone("UTC"));
	    Date date = new Date();
	    String utcDate = sdfDate.format(date);
	    String utcTime = sdfTime.format(date);
		try {
			mGPSWriter = new FileWriter(mGPSFile, true);   
			mGPSWriter.append("\t\t\t<trkpt lon=\""+ location.getLongitude() + "\" lat=\"" + location.getLatitude() + "\">\n");
			mGPSWriter.append("\t\t\t\t<time>" + utcDate + "T" + utcTime + "Z</time>\n");
			mGPSWriter.append("\t\t\t</trkpt>\n");
			mGPSWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 				
	}	
	
	public void terminateGPSFile(String fileName) {
		mSDCard = Environment.getExternalStorageDirectory();
		mGPSFile = new File(mSDCard.getAbsolutePath() + "/gps_" + fileName);	
		try {
			mGPSWriter = new FileWriter(mGPSFile, true);     
			mGPSWriter.append("\t\t</trkseg>\n");
			mGPSWriter.append("\t</trk>\n");
			mGPSWriter.append("</gpx>");
			mGPSWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
