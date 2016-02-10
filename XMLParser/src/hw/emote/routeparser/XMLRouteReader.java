package hw.emote.routeparser;

import hw.emote.xmlparser.XMLReader;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Mei Yii Lim
 *
 * This class reads the route.xml files and return a list of Route objects.
 * 
 */

public class XMLRouteReader extends XMLReader{
	//private final static String STEP_FILE = "steps.xml"; 
	private ArrayList<Route> routes;
	
	public XMLRouteReader(InputStream isRoute) throws JSONException, MalformedURLException{
		try {
			Document stepDoc = parse(isRoute);
			routes = getRoutes(stepDoc);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<Route> getRoutes(){
		return routes;
	}
	
	private ArrayList<Route> getRoutes(Document scriptDoc) throws DocumentException, JSONException {
		ArrayList<Route> routes = new ArrayList<Route>();
		Element root = scriptDoc.getRootElement();
		Integer in = 0;
		for (Iterator i = root.elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            if (element.getName().equals("route")){      
            	Route route = new Route();
                ArrayList<Step> steps = new ArrayList<Step>();
            	for (Iterator j = element.elementIterator(); j.hasNext();){            		
            		Element temp = (Element) j.next();
            		if (temp.getName().equals("name")){
            			route.setName(temp.getText());
            		} else if (temp.getName().equals("steps")){
            			for (Iterator k = temp.elementIterator(); k.hasNext();){
                    		Element e = (Element) k.next();
                    		Step step = getStepInfo(in, e);
                    		in++;
                    		steps.add(step);
                    		//System.out.println(step.toString());
            			}
            			route.setSteps(steps);
            		}      		            		
            	}
            	routes.add(route);
            }
           
        }
		return routes;
	}
	
	private Step getStepInfo(Integer index, Element e) {
		Step step =  new Step();
		ArrayList<Question> questions = new ArrayList<Question>();
		for (Iterator<Element> i = e.elementIterator(); i.hasNext(); ) {
            Element temp = (Element) i.next();
            step.setStepNo(index);
            if (temp.getName().equals("name")){
            	//System.out.println("Name: " + temp.getText());
            	step.setName(temp.getText());
            } else if (temp.getName().equals("text")){
            	//System.out.println("Text: " + temp.getText());
            	step.setTask(temp.getText());
            } else if (temp.getName().equals("gesture")){
            	//System.out.println("Gesture: " + temp.getText());
            	step.setGesture(temp.getText());
            } else if (temp.getName().equals("question")){    
            	Question question = new Question();
            	for (Iterator<Element> j = temp.elementIterator(); j.hasNext();){            		
            		Element e2 = (Element) j.next();
            		if (e2.getName().equals("answer")){
            			for ( Iterator<Attribute> l = e2.attributeIterator(); l.hasNext(); ) {
            				Attribute attribute = (Attribute) l.next();
                            //System.out.println("Attribute:" + attribute.getName());
                            if (attribute.getName().equals("isImage")){
                            	//System.out.println("Is image?:" + attribute.getText());
                            	question.setIsImage(Boolean.parseBoolean(attribute.getText()));
                            }
            			}
                		ArrayList<String> answers = new ArrayList<String>();
                		for (Iterator<Element> j2 = e2.elementIterator(); j2.hasNext();){
                    		Element e3 = (Element) j2.next();
                    		//System.out.println("Answer:" + e3.getText());
                    		answers.add(e3.getText());
                    		for ( Iterator<Attribute> k = e3.attributeIterator(); k.hasNext(); ) {
                                Attribute attribute = (Attribute) k.next();
                                //System.out.println("Attribute:" + attribute.getName());
                                if (attribute.getName().equals("correct")){
                                	//System.out.println("Correct answer:" + e3.getText());
                                	question.setCorrectAnswer(e3.getText());
                                }
                    		}
                		}
                		question.setAnswers(answers);
            		} else {
            			if (e2.getName().equals("text")){
            				//System.out.println("Question:" + e2.getText());
            				question.setQuestion(e2.getText());
            			} 
            			/* name & gesture
            			else {
            				q.put(e2.getName(), e2.getText());
            			}*/
            		}                   		
            	}     
            	questions.add(question);
                //System.out.println("Questions:" + questions.toString());
            }
            step.setQuestion(questions);            
        }
		return step;
	}
}
