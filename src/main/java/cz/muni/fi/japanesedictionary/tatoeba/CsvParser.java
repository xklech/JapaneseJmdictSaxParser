package cz.muni.fi.japanesedictionary.tatoeba;

import cz.muni.fi.japanesedictionary.enums.Lang;
import cz.muni.fi.japanesejmdictsaxparser.util.CompressFolder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jaroslav Klech
 */
public class CsvParser {
    
    final static Logger log = LoggerFactory.getLogger(CsvParser.class);  
    private static final String LOG_TAG = "CsvParser";	
    
    
    public static void prepareCsv(File japaneseIndices, File inputFileSentences, File inputFileLinks) throws IOException {
        if(japaneseIndices == null || !japaneseIndices.exists()){
            throw new IllegalArgumentException("japaneseIndices");
        }
        if(inputFileSentences == null || !inputFileSentences.exists()){
            throw new IllegalArgumentException("inputFileSentences");
        }
        if(inputFileLinks == null || !inputFileLinks.exists()){
            throw new IllegalArgumentException("inputFileLinks");
        }

        
        long startTime = System.currentTimeMillis();

        File tatoebaIndicesFolder = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoebaIndicesFolder");
        CompressFolder.deleteDirectory(tatoebaIndicesFolder);
        tatoebaIndicesFolder.mkdirs();

        Directory dirIndices = FSDirectory.open(tatoebaIndicesFolder);
        Analyzer  analyzerIndices = new CJKAnalyzer(Version.LUCENE_36);
        IndexWriterConfig configIndices = new IndexWriterConfig(Version.LUCENE_36, analyzerIndices);
         
        Set<Integer> japaneseIds = new HashSet<>(); 
        Map<Integer, Integer> mapOtherJapanese = new HashMap<>();
        InputStream inputIndices = new BZip2CompressorInputStream(new FileInputStream(japaneseIndices));

        try (BufferedReader inputBReader = new BufferedReader(new InputStreamReader(inputIndices, StandardCharsets.UTF_8)); IndexWriter mWriter = new IndexWriter(dirIndices, configIndices)) {
                        
            Document mDocument = new Document();
            Field japaneseSentenceId = new Field("japanese_sentence_id", "", Field.Store.YES, Field.Index.NO);
            Field japaneseTag = new Field("japanese_tag", "", Field.Store.YES, Field.Index.NOT_ANALYZED);
            mDocument.add(japaneseSentenceId);
            mDocument.add(japaneseTag);
            int count = 0;  
            int number = 1;            
            String line;
            while (null != (line = inputBReader.readLine())) {
                String[] split = line.split("\\t");
                if( split != null && split.length > 2 && split[2] != null )  {
                    try{
                        int idJapaneseSentence = Integer.parseInt(split[0]);
                        japaneseSentenceId.setValue(split[0]);
                        japaneseIds.add(idJapaneseSentence);
                        mapOtherJapanese.put(idJapaneseSentence, idJapaneseSentence);
                        String[] indices = split[2].split(" ");
                        for(String indice : indices){
                            String kanji;
                            count++;
                            if(indice.contains("(")){
                                kanji = indice.substring(0, indice.indexOf("("));
                                //log.debug("indice: " + indice);
                                String reading = indice.substring(indice.indexOf('(')+1, indice.indexOf(')'));
                                if(reading != null && reading.length() > 0){
                                    //log.debug("reading: " + reading);
                                    japaneseTag.setValue(reading);
                                    mWriter.addDocument(mDocument);
                                }
                            } else if(indice.contains("[")) {
                                kanji = indice.substring(0, indice.indexOf("["));
                            } else if (indice.contains("{")) {
                                kanji = indice.substring(0, indice.indexOf("{"));
                            } else {
                                kanji = indice;
                            }
                            
                            if(kanji != null && kanji.length() > 0) {
                                japaneseTag.setValue(kanji);
                                mWriter.addDocument(mDocument);
                            }
                        }
                        if(count > 100_000 * number){
                            number++;
                            log.debug("indices processed: " + count + ", duration: " + ((System.currentTimeMillis() - startTime) / 60000.0) + " minutes");
                            mWriter.commit();
                        }
                    }catch(NumberFormatException ignore){}
                }
            }
            log.debug("japanese index size: " + count + ", final duration: " + ((System.currentTimeMillis() - startTime) / 60000.0) + " minutes");
        }
        
        InputStream inputLinks = new BZip2CompressorInputStream(new FileInputStream(inputFileLinks));

        
         try (BufferedReader inputBReader = new BufferedReader(new InputStreamReader(inputLinks, StandardCharsets.UTF_8));) {
            String line;
            while (null != (line = inputBReader.readLine())) {
                
                    try{
                        String[] split = line.split("\\t");
                        int idSource = Integer.parseInt(split[0]);
                        int idTarget = Integer.parseInt(split[1]);
                        if(japaneseIds.contains(idSource)){

                            mapOtherJapanese.put(idTarget, idSource);
                        }
                    }catch(NumberFormatException ignore){}
                }
            }
         

         
         
         
         /**
          * 
          *  first Lucene index: japanese sentences, one field is number
          *  second lucene index: translate sentences only for japanese sentences, 
          *                       indexed by id of japanese sentence
          * 
          */
        File tatoebaTranslationFolder = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoebaTranslationFolder");
        CompressFolder.deleteDirectory(tatoebaTranslationFolder);
        tatoebaTranslationFolder.mkdirs();
        Directory dirLanguage = FSDirectory.open(tatoebaTranslationFolder);
        Analyzer  analyzerLanguage = new CJKAnalyzer(Version.LUCENE_36);
        IndexWriterConfig configLanguage = new IndexWriterConfig(Version.LUCENE_36,analyzerLanguage);
         
        InputStream inputSentenses = new BZip2CompressorInputStream(new FileInputStream(inputFileSentences));
        try (BufferedReader inputBReader = new BufferedReader(new InputStreamReader(inputSentenses, StandardCharsets.UTF_8)); IndexWriter mWriter = new IndexWriter(dirLanguage, configLanguage)) {
                        
            Document mDocument = new Document();
            Field sentence = new Field("sentence", "", Field.Store.YES, Field.Index.NO);
            Field language = new Field("language", "", Field.Store.YES, Field.Index.NO);
            Field idField = new Field("japanese_id", "", Field.Store.NO, Field.Index.NOT_ANALYZED);
            mDocument.add(sentence);
            mDocument.add(language);
            mDocument.add(idField);
            int count = 0;  
            int number = 0;
                        
            String line;
            
            Set<String> languages = Lang.getAll();
            
            while (null != (line = inputBReader.readLine())) {
                //log.debug(line);
                String[] split = line.split("\\t");
                if( split != null && split.length > 2 )  {
                    try{
                        int idSource = Integer.parseInt(split[0]);
                        if(mapOtherJapanese.containsKey(idSource) && languages.contains(split[1])){   
                            sentence.setValue(split[2]);
                            language.setValue(split[1]);
                            idField.setValue(String.valueOf(mapOtherJapanese.get(idSource)));                        
                            mWriter.addDocument(mDocument);
                            count++;
                            if(count > 100_000){
                                count = 0;
                                number++;
                                log.debug("translations processed: " + number * 100_000 + ", duration: " + ((System.currentTimeMillis() - startTime) / 60000.0) + " minutes");
                                mWriter.commit();
                            }
                        }
                    }catch(NumberFormatException ignore){}
                }
            }
        }
         
         
         
        long endTime = System.currentTimeMillis();  
        
        
        log.debug("duration: " + ((endTime - startTime) / 60000.0) + " minutes");
        File outputDir = new File(System.getenv("OPENSHIFT_DATA_DIR") + "output");
        outputDir.mkdirs();
        
        File outputTatoebaIndices = new File(outputDir, "tatoeba_indices.zip");
        CompressFolder.deleteIfExist(outputTatoebaIndices);
        CompressFolder.zip(tatoebaIndicesFolder, outputTatoebaIndices);
        
        File outputTatoebaTranslations = new File(outputDir, "tatoeba_senteces.zip");
        CompressFolder.deleteIfExist(outputTatoebaTranslations);
        CompressFolder.zip(tatoebaTranslationFolder, outputTatoebaTranslations);
    }
    
    
}
