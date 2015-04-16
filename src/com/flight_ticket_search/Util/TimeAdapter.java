/**
 * 
 */
package com.flight_ticket_search.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.flight_ticket_search.Entity.Time;
import com.flight_ticket_search.Entity.Date;

/**
 * @author lxybi_000
 *
 */
public class TimeAdapter {
	
	/**
	 * Return the string format of date(stored in database format) plus one 
	 * dbDateForm has to be the format of "YYYY_MM_DD", which is used to query
	 * the database and the returned date which is one day bigger is in the same
	 * format
	 * 
	 * @param dbDateQueryForm: Query parameter used to query database
	 *  store in the format of:	2015_05_10
	 * @return String representation in same format(day added by one)
	 */
	public String addOneDay(String dbDateForm) {
		int day = (String.valueOf(dbDateForm.charAt(8)).equals("0"))? 
				Integer.parseInt(String.valueOf(dbDateForm.charAt(9))):
					Integer.parseInt(dbDateForm.substring(8,10));
		day++;
		return  dbDateForm.substring(0, 8) + (day<10? "0"+day:day);
	}
	
	
	/**
	 * Return the offset of a location/Airport with known latitude and longitude
	 * Append latitude and longitude as URL parameter to 
	 * http://www.earthtools.org/timezone/latitude/longitude
	 * and query for the offset of the airport using its
	 * longitude and latitude information
	 * 
	 * @param airport used to set the TimeZone
	 * @return the time zone offset used to convert GMT time to Local
	 */
	public int getTimezoneOffset(double lat, double longt) {
		
		try {
			
			URL url = new URL("http://www.earthtools.org/timezone/"+lat+"/"+longt);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			StringBuffer result = new StringBuffer();
			
			while ((line = bReader.readLine()) != null) {
				result.append(line);
			}
			String resultString = result.toString();
			bReader.close();
			System.out.println(resultString);
			
			/* Next step parse the XML String data */
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			Document document = null;
		
			// DOM parser instance
			DocumentBuilder builder = builderFactory.newDocumentBuilder();

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(resultString));
			document = builder.parse(is);
			
			 // Write the parsed document to an xml file
		   /* 
		    TransformerFactory transformerFactory = TransformerFactory.newInstance();
		    Transformer transformer = transformerFactory.newTransformer();
		    DOMSource source = new DOMSource(document);

		    StreamResult result2 =  new StreamResult(new File("./my-file.xml"));
		    transformer.transform(source, result2);
			*/
			
			// get root element, which is <timezone>
			Element rootElement = document.getDocumentElement();
			NodeList nodes = rootElement.getChildNodes();
			
			// To retrieve the <offset> element from parent <timezone>, the 3rd childNode
			Node node = nodes.item(5); // item number changed as it is not org.w3c.dom
			String offset = "";
			// process child element
			offset = node.getTextContent();
			
			//System.out.println(offset);
			return Integer.parseInt(offset);
			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		
		return 0;
	}
	
	
	/**
	 * Return the local time given the GMT time and its offset
	 * The returned time should be a new Time instance that should
	 * never change the original GMT time instance, which is used 
	 * among the multi-legs union 
	 * 
	 * @param gmtTime of the flight legs retrieved from the database
	 * @param offset of the given departure/arrival airport of the flight leg
	 * @return
	 */
	public Time getLocalTime(Time gmtTime, int offset) {
		
		Time time = new Time();
		
		if (gmtTime.getHour() + offset > 24) {
			
			time.setHour(gmtTime.getHour() + offset -24);
			time.setMinute(gmtTime.getMinute());
			time.setDate(new Date(2015, 5, gmtTime.getDate().getDay() + 1));
			
		} else if (gmtTime.getHour() + offset < 0) {
			
			time.setHour(gmtTime.getHour() + offset + 24);
			time.setMinute(gmtTime.getMinute());
			time.setDate(new Date(2015, 5, gmtTime.getDate().getDay() - 1));
			
		} else {
			
			time.setHour(gmtTime.getHour() + offset);
			time.setMinute(gmtTime.getMinute());
			time.setDate(new Date(2015, 5, gmtTime.getDate().getDay()));
		}
		
		return time;
	}
	
	
	public static void main(String[] args) {
		/* 
		Time time = new Time();
		time.localToGMT("2015-05-10 20:00:00", "America/Los_Angeles");
		time.localToGMT("2015-05-10 20:00:00", "America/New_York");
	
		time.GMTtoLocal("2015-05-10 20:00:00", "America/Los_Angeles");
		time.GMTtoLocal("2015-05-10 20:00:00", "America/New_York");
		*/
		TimeAdapter timer = new TimeAdapter();
		System.out.println(timer.addOneDay("2015_05_12"));
	}
	
}