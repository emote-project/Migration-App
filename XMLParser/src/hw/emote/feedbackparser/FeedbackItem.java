package hw.emote.feedbackparser;

/**
 * @author Mei Yii Lim
 *
 * This class holds a FeedbackItem.
 * 
 */

public class FeedbackItem {

	private String text;
	private String gesture;
	
	public FeedbackItem() {
		text = "";
		gesture = "";
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setGesture(String gesture) {
		this.gesture = gesture;
	}
	
	public String getText() {
		return text;
	}
	
	public String getGesture() {
		return gesture;
	}
	
	public String toString() {
		return "text - " + text + ", gesture - " + gesture;
	}
}
