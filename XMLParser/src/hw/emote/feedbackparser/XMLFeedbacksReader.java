package hw.emote.feedbackparser;

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
import org.json.JSONException;
import org.json.JSONObject;



import com.example.xmlparser.R;

/**
 * @author Mei Yii Lim
 *
 * This class reads the feedback.xml files and return a list of FeedbackTemplate objects.
 * 
 */

public class XMLFeedbacksReader extends XMLReader{
	//private final static String FEEDBACK_FILE = "res/raw/feedback.xml"; 
	private ArrayList<FeedbackTemplate> fb;
	
	public XMLFeedbacksReader(InputStream isFeedback) {
		try {
			Document feedbackDoc = parse(isFeedback);			
			fb = getFeedback(feedbackDoc);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<FeedbackTemplate> getFeedback(){
		return fb;
	}
	
	private ArrayList<FeedbackTemplate> getFeedback(Document scriptDoc) throws DocumentException {
		ArrayList<FeedbackTemplate> feedback = new ArrayList<FeedbackTemplate>();
		Element root = scriptDoc.getRootElement();
		for (Iterator<Element> i = root.elementIterator(); i.hasNext(); ) {
            Element element = (Element) i.next();
            if (element.getName().equals("feedback")){
            	for (Iterator<Element> j = element.elementIterator(); j.hasNext();){
            		Element e = (Element) j.next();
            		FeedbackTemplate t;
            		String templateName = null;
            		for (Iterator<Attribute> k = e.attributeIterator(); k.hasNext(); ) {
                        Attribute attribute = (Attribute) k.next();
                        templateName = attribute.getValue();
            		}
            		t = new FeedbackTemplate(templateName);
            		for (Iterator<Element> l = e.elementIterator(); l.hasNext(); ) {
            			Feedback f = new Feedback();
            			Element temp = (Element) l.next();
            			f = getFeedbackInfo(temp);
                		if (temp.getName().equals("positive")){
            				t.setPositiveFeedback(f);
            			} 
            			else if (temp.getName().equals("negative")){
            				t.setNegativeFeedback(f);
            			}
                	}
            		//System.out.println(t.toString());
                	feedback.add(t);
            		
            	}
            }
        }
		return feedback;
	}
	
	
	private Feedback getFeedbackInfo(Element e){
		Feedback fb = new Feedback();
		for (Iterator<Element> j = e.elementIterator(); j.hasNext(); ) {
			Element temp2 = (Element) j.next();
			FeedbackItem fbItem = new FeedbackItem();
			for (Iterator<Element> k = temp2.elementIterator(); k.hasNext(); ) {
				Element temp3 = (Element) k.next();
				// set the text and gesture for the feedback item
				if (temp3.getName().equals("text")){
					fbItem.setText(temp3.getText());
				} else if (temp3.getName().equals("gesture")){
					fbItem.setGesture(temp3.getText());
				}
			}
			fb.add(fbItem);
		}
		return fb;
	}
}
