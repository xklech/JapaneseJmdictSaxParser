package cz.muni.fi.japanesejmdictsaxparser.parser;

import cz.muni.fi.japanesejmdictsaxparser.saxholder.SaxDataHolder;
import cz.muni.fi.japanesejmdictsaxparser.saxholder.SaxKanjidic2Holder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  Parses Given JMDict dictionary and Kanjidict2 sdictionary for 
 *  JapaneseDictionary app on Android
 * 
 * @author Jaroslav klech
 */

public class SaxParser {
    
    /**
     * Parses JMDict file to given folder using Lucene 3.6
     * 
     * @param input inputstream from jmdict source file
     * @param output output File created Directory
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public static void parseJmdict(InputStream input, File output) 
            throws IOException, ParserConfigurationException, SAXException{

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();
        DefaultHandler handler = new SaxDataHolder(output);
        saxParser.parse(input,handler);
    }
    
    /**
     * Parses Kanjidic2 file to given folder using Lucene 3.6
     * 
     * @param input inputstream from kanjidic2 source file
     * @param output output File created Directory
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public static void parseKanjidic2(InputStream input, File output) 
            throws IOException, ParserConfigurationException, SAXException{ 
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();
        DefaultHandler handler = new SaxKanjidic2Holder(output);

        saxParser.parse(input,handler);
    }  
    
}
