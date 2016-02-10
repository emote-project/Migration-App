package hw.emote.routeparser;

import java.util.ArrayList;

/**
 * @author Mei Yii Lim
 *
 * This is the Route class which contains a list of steps.
 */

public class Route {

	private String name;
	private ArrayList<Step> steps;
	
	public Route() {
		name = "";
		steps = new ArrayList<Step>();
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Step> getSteps() {
		return steps;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setSteps(ArrayList<Step> steps) {
		this.steps = steps;
	}
}
