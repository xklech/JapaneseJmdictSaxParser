package cz.muni.fi.japanesejmdictsaxparser.parser;

import cz.muni.fi.japanesejmdictsaxparser.CronTaskProvider;
import cz.muni.fi.japanesejmdictsaxparser.saxholder.SaxDataHolder;
import cz.muni.fi.japanesejmdictsaxparser.saxholder.SaxKanjidic2Holder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  Parses Given JMDict dictionary and Kanjidict2 sdictionary for 
 *  JapaneseDictionary app on Android
 * 
 * @author Jaroslav klech
 */

public class SaxParser {
    
    final static Logger log = LoggerFactory.getLogger(CronTaskProvider.class);     
    /**
     * Parses JMDict file to given folder using Lucene 3.6
     * 
     * @param input inputstream from jmdict source file
     * @param androidOutput output File created Directory
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public static void parseJmdict(InputStream input, File androidOutput) 
            throws IOException, ParserConfigurationException, SAXException{

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        SAXParser saxParser = factory.newSAXParser();
        DefaultHandler handler = new SaxDataHolder(androidOutput);
        
        
        Reader reader = new InputStreamReader(input,"UTF-8");

        InputSource is = new InputSource(reader);
        is.setEncoding("UTF-8");
        saxParser.parse(is,handler);
        input.close();
    }
    
    /**
     * Parses Kanjidic2 file to given folder using Lucene 3.6
     * 
     * @param input inputstream from kanjidic2 source file
     * @param androidOutput output File created Directory
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException 
     */
    public static void parseKanjidic2(InputStream input, File androidOutput) 
            throws IOException, ParserConfigurationException, SAXException{ 
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);

        SAXParser saxParser = factory.newSAXParser();
        DefaultHandler handler = new SaxKanjidic2Holder(androidOutput);

        saxParser.parse(input,handler);
        input.close();
    }  
    
    public static File preprocesXmlFile(InputStream input, File output) throws FileNotFoundException, IOException{
        log.debug("Preprocess file");

        InputStreamReader inputStreamReader = new InputStreamReader(input,Charset.forName("utf-8"));    
        BufferedWriter writer;
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output),"utf-8"));
            String line;
            boolean comment = false;
            int start;
            int end;
            while((line = reader.readLine()) != null){
                line = line.replaceAll("&([^;]*);", "$1");
                if(!comment){
                    if((start = line.indexOf("<!--")) != -1){
                        if(start > 0){
                            writer.append(((String)line.subSequence(0, start)).trim());
                            //writer.newLine();
                        }
                        if((end = line.indexOf("-->")) != -1){
                            writer.append(line.subSequence(end+3, line.length()));
                            if((end+3) != (line.length()-1)){
                                writer.append("\n");
                            }
                            //writer.newLine();
                        }else{
                            comment = true;
                        }
                    }else{
                        writer.append(line);
                        writer.append("\n");
                        //writer.newLine();
                    }
                }else{
                    if((end = line.indexOf("-->")) != -1){
                        writer.append(line.subSequence(end+3, line.length()));
                        if((end+3) != (line.length()-1)){
                            writer.append("\n");
                        }
                        //writer.newLine();
                        comment = false;
                    }
                }
            }
        }
        writer.close();
        return output;
    }
    
    

    
}
