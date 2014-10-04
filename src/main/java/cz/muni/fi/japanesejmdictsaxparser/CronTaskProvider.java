/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.japanesejmdictsaxparser;


import cz.muni.fi.japanesedictionary.tatoeba.CsvParser;
import cz.muni.fi.japanesejmdictsaxparser.download.Downloader;
import cz.muni.fi.japanesejmdictsaxparser.parser.SaxParser;
import cz.muni.fi.japanesejmdictsaxparser.util.CompressFolder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.SAXException;

/**
 *
 * @author Jarek
 */
public class CronTaskProvider {
    final static Logger log = LoggerFactory.getLogger(CronTaskProvider.class);   
    public static final String JMDICT = "http://ftp.monash.edu.au/pub/nihongo/JMdict.gz";
    public static final String KANJIDICT = "http://www.csse.monash.edu.au/~jwb/kanjidic2/kanjidic2.xml.gz";
    public static final String TATOEBA_JAPANESE_INDICES = "http://downloads.tatoeba.org/exports/jpn_indices.tar.bz2";
    public static final String TATOEBA_SENTENCES = "http://downloads.tatoeba.org/exports/sentences.tar.bz2";
    public static final String TATOEBA_LINKS = "http://downloads.tatoeba.org/exports/links.tar.bz2";
    public static void deleteSources(File folder){
        CompressFolder.deleteDirectory(folder);
        folder.mkdir();
    }
    
    
    public static void downloadPreprocesJmdict() throws MalformedURLException, IOException{
        URL jmdictUrl = new URL(JMDICT);
        File file = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/jmdict.gz");
        File jmdict = Downloader.downloadFile(jmdictUrl, file);
        FileInputStream inputStreamJMdict = new FileInputStream(jmdict);
        log.debug("JMdict - gzip");
        GZIPInputStream gzipInputStreamJMDict = new GZIPInputStream(inputStreamJMdict);
        File convertOutput = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/kanjidic-temp.xml");
        CompressFolder.deleteIfExist(convertOutput);
        SaxParser.preprocesXmlFile(gzipInputStreamJMDict, convertOutput);
    }


    
    public static void prepareJmDict(File outputFolder) throws 
            MalformedURLException, 
            IOException, 
            ParserConfigurationException, 
            SAXException{
        log.debug("JMdict - download");
        
        File outputFileAndroid = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/jmdict");
        outputFileAndroid.mkdirs();
        log.debug("JMdict - parse");
        File convertOutput = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/kanjidic-temp.xml");
        SaxParser.parseJmdict(new FileInputStream(convertOutput), outputFileAndroid);     
        log.debug("JMdict - zip");
        
        if(!outputFolder.exists()){
            outputFolder.mkdirs();
        }
        File outputJmdictAndroid = new File(outputFolder, "jmdict.zip");
        CompressFolder.deleteIfExist(outputJmdictAndroid);
        CompressFolder.zip(outputFileAndroid, outputJmdictAndroid);

    }

    public static void downloadPreprocesKanjidic2() throws MalformedURLException, IOException{
        URL kanjidic2Url = new URL(KANJIDICT);
        File file = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/kanjidic2.gz");
        File kanjidic2 = Downloader.downloadFile(kanjidic2Url, file);
        FileInputStream inputStream = new FileInputStream(kanjidic2);
        log.debug("Kanjdiic2 - gzip reader");
        GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
        File convertOutput = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/kanjidic-temp.xml");
        CompressFolder.deleteIfExist(convertOutput);
        SaxParser.preprocesXmlFile(gzipInputStream, convertOutput);
    }
    
    public static void prepareKanjidic2(File outputFolder) throws 
            MalformedURLException, 
            IOException, 
            ParserConfigurationException, 
            SAXException{
        File convertOutput = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/kanjidic-temp.xml");

        File outputFileKanjidic2Android = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/kanjidic2");
        outputFileKanjidic2Android.mkdir();
        log.debug("Kanjdiic2 - parse");
        SaxParser.parseKanjidic2(new FileInputStream(convertOutput), outputFileKanjidic2Android);
        
        if(!outputFolder.exists()){
            outputFolder.mkdirs();
        }
        log.debug("Kanjdiic2 - zip");
        File outputKanjidic = new File(outputFolder, "kanjidic2.zip");
        CompressFolder.deleteIfExist(outputKanjidic);
        CompressFolder.zip(outputFileKanjidic2Android, outputKanjidic);

    }
    
    
    public static void downloadPreprocesTatoebaSentences() throws MalformedURLException, IOException{
        URL tatoebaJapaneseIndicesUrl = new URL(TATOEBA_JAPANESE_INDICES);
        File japaneseIndices = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoeba-indices.tar.bz2");
        File tatoebaJapaneseIndices = Downloader.downloadFile(tatoebaJapaneseIndicesUrl, japaneseIndices);
        
        
        URL tatoebaSentencesUrl = new URL(TATOEBA_SENTENCES);
        File sentences = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoeba-sentences.tar.bz2");
        File tatoebaSentences = Downloader.downloadFile(tatoebaSentencesUrl, sentences);
        
        
        URL tatoebaLinksUrl = new URL(TATOEBA_LINKS);
        File fileLinks = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoeba-links.tar.bz2");
        File tatoebaLinks = Downloader.downloadFile(tatoebaLinksUrl, fileLinks);

        log.debug("tatoeba - gzip reader");
        
        CsvParser.prepareCsv(tatoebaJapaneseIndices, tatoebaSentences, tatoebaLinks);
    }
    
    
    
}
