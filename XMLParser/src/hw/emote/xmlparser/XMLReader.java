package hw.emote.xmlparser;

import java.io.InputStream;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

/**
 * @author Mei Yii Lim
 *
 * This is the main XML Reader class 
 * 
 */

public class XMLReader {

	public Document parse(URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(url);
        return document;
	}
	
	public Document parse(InputStream is) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(is);
        return document;
	}
}
