package hw.emote.feedbackparser;

/**
 * @author Mei Yii Lim
 *
 * This is the FeedbackTemplate class which holds both positive and negative Feedback for a particular feedback type.
 * 
 */

public class FeedbackTemplate {
	
	String name;
	Feedback positive, negative;
	
	public FeedbackTemplate(String name){
		this.name = name;
	}
	
	public void setPositiveFeedback(Feedback positive){
		this.positive = positive;
	}
	
	public void setNegativeFeedback(Feedback negative){
		this.negative = negative;
	}
	
	public String getName(){
		return name;
	}
	
	public Feedback getPositiveFeedback(){
		return positive;
	}

	public Feedback getNegativeFeedback(){
		return negative;
	}
	
	public String toString(){
		return (name + ", positive: " + positive.toString() + " negative: " + negative.toString());
	}
}
