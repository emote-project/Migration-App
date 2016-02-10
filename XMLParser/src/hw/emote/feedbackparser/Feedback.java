package hw.emote.feedbackparser;

import java.util.ArrayList;

/**
 * @author Mei Yii Lim
 *
 * This Feedback class holds a list of FeedbackItems
 * 
 */

public class Feedback{
	ArrayList<FeedbackItem> feedbackItems;
	
	public Feedback(){
		feedbackItems = new ArrayList<FeedbackItem>();
	}
	
	public void add(FeedbackItem fbItem) {
		feedbackItems.add(fbItem);
	}
	
	public FeedbackItem getFeedbackItem(int i) {
		return feedbackItems.get(i);
	}
	
	public ArrayList<FeedbackItem> getFeedbackItems() {
		return feedbackItems;
	}
	
	public String toString() {
		String feedbackString = "";
		for (int i=0; i < feedbackItems.size(); i++) {
			feedbackString = feedbackString + feedbackItems.get(i).toString() + "; ";
		}
		return feedbackString;
	}
	
	/*JSONArray d;
	
	Feedback(){
		d = new JSONArray();
	}
	
	public void add(JSONObject k){
		d.put(k);
	}
	
	public Integer length() throws JSONException{
		return d.length();
	}
	
	public JSONObject get(Integer i) throws JSONException{
		return (JSONObject) d.get(i);
	}
	
	public String toString(){
		return d.toString();
	}*/
	
}
