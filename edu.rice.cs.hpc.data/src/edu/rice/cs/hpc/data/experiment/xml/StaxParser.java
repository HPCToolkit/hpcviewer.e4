package edu.rice.cs.hpc.data.experiment.xml;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.Location;

/**************************************
 * 
 * XML parser via StAX
 *
 **************************************/
public class StaxParser implements IParser
{
	private XMLInputFactory inputFactory;
	private final Builder   builder;
	private Location        location;
	
	public StaxParser(Builder builder) {
		this.builder = builder;
	}
	
	
	@Override
	public void parse(String filename) throws Exception {
		
		if (inputFactory == null) {
			inputFactory = XMLInputFactory.newInstance();
		}
		InputStream in = new FileInputStream(filename);
		XMLStreamReader streamReader =  inputFactory.createXMLStreamReader(in);
		
		builder.begin();
		
		try {
			while(streamReader.hasNext()) {
				int type = streamReader.next();

				location = streamReader.getLocation();

				if (streamReader.isStartElement()) {
					String name = streamReader.getLocalName();
					int count = streamReader.getAttributeCount();
					String []attributeNames  = new String[count];
					String []attributeValues = new String[count];
					
					for(int i=0; i<count; i++) {
						attributeNames[i]  = streamReader.getAttributeLocalName(i);
						attributeValues[i] = streamReader.getAttributeValue(i);
					}
					builder.beginElement(name, attributeNames, attributeValues);
				
				} else if (streamReader.isEndElement()) {
					String name = streamReader.getLocalName();
					builder.endElement(name);
				}
			}
		} catch (XMLStreamException e) {
			if (location != null)
				builder.error(location.getLineNumber());
			else
				builder.error();
			throw e;
		}
		builder.end();
		location = null;
	}
	
	@Override
	public int getLineNumber() {

		if (location == null)
			return 0;
		
		return location.getLineNumber();
	}
}
