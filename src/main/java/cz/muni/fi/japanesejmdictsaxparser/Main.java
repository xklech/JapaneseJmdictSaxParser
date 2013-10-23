/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.japanesejmdictsaxparser;

import cz.muni.fi.japanesejmdictsaxparser.parser.SaxParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Jarek
 */
public class Main {
    
    //JMDICT: ftp://ftp.monash.edu.au/pub/nihongo/JMdict.gz
    // Kanjidic2: http://www.csse.monash.edu.au/~jwb/kanjidic2/kanjidic2.xml.gz
    
    public static void main(String[] args ) throws IOException, ParserConfigurationException, SAXException{
        
        System.err.println("JMDict!!");
        File jmdict = new File("sources/JMdict.gz");
        FileInputStream inputStreamJMdict = new FileInputStream(jmdict);
        GZIPInputStream gzipInputStreamJMDict = new GZIPInputStream(inputStreamJMdict);
        File outputFileJmdict = new File("sources/jmdict");
        outputFileJmdict.mkdir();
        SaxParser.parseJmdict(gzipInputStreamJMDict, outputFileJmdict);     
        
        System.err.println("Kanjidic!!");
        File kanjidic2 = new File("sources/kanjidic2.xml.gz");
        FileInputStream inputStream = new FileInputStream(kanjidic2);

        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        File outputFile = new File("sources/kanjidic2dic");
        outputFile.mkdir();
        SaxParser.parseKanjidic2(gzipInputStream, outputFile);
        
    }
    
}
