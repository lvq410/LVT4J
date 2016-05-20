package com.lvt4j.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAX extends DefaultHandler {
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		if ("wordbook"!=qName && "item"!=qName) {
			System.out.println(qName+str);
			
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub
		System.out.println(target + "|" + data);
		super.processingInstruction(target, data);
	}

	private String str;
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		str = new String(ch,start,length).trim();
//		super.characters(ch, start, length);
	}

	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, Exception {
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(new File("d:\\w.xml"), new SAX());
	}
}
