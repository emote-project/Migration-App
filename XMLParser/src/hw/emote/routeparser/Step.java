package hw.emote.routeparser;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Mei Yii Lim
 *
 * This class holds the steps for a Route.
 */

public class Step {
	
	private Integer stepNo;
	private String name, task;
	private String gestureName;
	private ArrayList<Question> questions;
	
	public Step(){
		questions = new ArrayList<Question>();
	}
	
	public void setStepNo(Integer i){
		this.stepNo = i;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setTask(String task){
		this.task = task;
	}
	
	public void setGesture(String gesture){
		this.gestureName = gesture;
	}
	
	public void setQuestion(ArrayList<Question> questions){
		this.questions = questions;
	}
	
	public ArrayList<Question> getQuestions(){
		return questions;
	}
	
	public Integer getStepNo(){
		return this.stepNo;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getTask(){
		return this.task;
	}
	
	public String getGesture(){
		return this.gestureName;
	}
		
	public String toString(){
		return ("Step:" + this.stepNo + "," + this.task + "\n" + "Question:" + this.questions.toString());
	}
}
