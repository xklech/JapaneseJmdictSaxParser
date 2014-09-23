package cz.muni.fi.japanesedictionary.tatoeba;

import cz.muni.fi.japanesedictionary.enums.Lang;
import cz.muni.fi.japanesejmdictsaxparser.util.CompressFolder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import org.apache.lucene.document.NumericField;
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
    
    
    public static void prepareCsv(File inputFileSentences, File inputFileLinks) throws IOException {
        if(inputFileSentences == null || !inputFileSentences.exists()){
            throw new IllegalArgumentException("inputFileSentences");
        }
        if(inputFileLinks == null || !inputFileLinks.exists()){
            throw new IllegalArgumentException("inputFileLinks");
        }

        
        long startTime = System.currentTimeMillis();

        InputStream inputSentenses = new BZip2CompressorInputStream(new FileInputStream(inputFileSentences));
        File convertOutput = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoeba-sentences-temp.csv");
        OutputStream output = new FileOutputStream(convertOutput);
        Set<Integer> japaneseIds = new HashSet<>();
        
        File tatoebaJapaneseFolder = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources/tatoebaJapaneseFolder");
        tatoebaJapaneseFolder.mkdirs();
        Directory dir = FSDirectory.open(tatoebaJapaneseFolder);
        Analyzer  analyzer = new CJKAnalyzer(Version.LUCENE_36);
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
        try (BufferedReader inputBReader = new BufferedReader(new InputStreamReader(inputSentenses, StandardCharsets.UTF_8)); BufferedWriter outputBWriter = new BufferedWriter(new OutputStreamWriter(output, StandardCharsets.UTF_8)); IndexWriter mWriter = new IndexWriter(dir, config)) {            

            Document mDocument = new Document();
            Field japaneseindex = new Field("japanese_index", "", Field.Store.NO, Field.Index.ANALYZED);
            Field japanese = new Field("japanese", "", Field.Store.YES, Field.Index.NO);
            NumericField idField = new NumericField("japanese_id", Field.Store.YES, false);
            mDocument.add(japaneseindex);
            mDocument.add(japanese);
            mDocument.add(idField);
            int count = 0;   
            int number = 0;
            int countJapanese = 0;
            
            Set<String> languages = Lang.getWithoutJap();
            String line;
            while (null != (line = inputBReader.readLine())) {
                String[] split = line.split("\\t");
                if(split == null || split.length < 3){
                    continue;
                }
                if( languages.contains(split[1]) )  {
                    outputBWriter.write(line + "\n");
                }
                if( Lang.getLanguage(Lang.JAPANESE).equals(split[1]) )  {
                    countJapanese++;
                    try{
                        int id = Integer.parseInt(split[0]);
                        japaneseIds.add(id);
                        
                        String indexString = split[2].replaceAll(".(?!$)", "$0 ");
                        japaneseindex.setValue(indexString);
                        japanese.setValue(split[2]);
                        idField.setIntValue(id);                        
                        mWriter.addDocument(mDocument);
                        count++;
                        if(count > 100_000){
                            count = 0;
                            number++;
                            log.debug("japanese processed: " + number * 100_000 + ", duration: " + ((System.currentTimeMillis() - startTime) / 60000.0) + " minutes");
                            mWriter.commit();
                        }
                    }catch(NumberFormatException ignore){}
                }

            }
            log.debug("japanese count: " + countJapanese);
        }
        
        
        InputStream inputLinks = new BZip2CompressorInputStream(new FileInputStream(inputFileLinks));
        Map<Integer, Integer> mapOtherJapanese = new HashMap<>();
        
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
        tatoebaTranslationFolder.mkdirs();

        Directory dirLanguage = FSDirectory.open(tatoebaTranslationFolder);
        Analyzer  analyzerLanguage = new CJKAnalyzer(Version.LUCENE_36);
        IndexWriterConfig configLanguage = new IndexWriterConfig(Version.LUCENE_36,analyzerLanguage);
         
         
        inputSentenses = new FileInputStream(convertOutput);
        try (BufferedReader inputBReader = new BufferedReader(new InputStreamReader(inputSentenses, StandardCharsets.UTF_8)); IndexWriter mWriter = new IndexWriter(dirLanguage, configLanguage)) {
                        
            Document mDocument = new Document();
            Field translation = new Field("translation", "", Field.Store.YES, Field.Index.NO);
            Field language = new Field("japanese", "", Field.Store.YES, Field.Index.NO);
            NumericField idField = new NumericField("japanese_id");
            mDocument.add(translation);
            mDocument.add(language);
            mDocument.add(idField);
            int count = 0;  
            int number = 0;
                        
            String line;
            while (null != (line = inputBReader.readLine())) {
                String[] split = line.split("\\t");
                if( split != null && split.length > 1 )  {
                    try{
                        int idSource = Integer.parseInt(split[0]);
                        if(mapOtherJapanese.containsKey(idSource)){
                            
                            translation.setValue(split[2]);
                            language.setValue(split[1]);
                            idField.setIntValue(mapOtherJapanese.get(idSource));                        
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
        File outputTatoebaJapanese = new File(outputDir, "tatoeba_japanese.zip");
        CompressFolder.deleteIfExist(outputTatoebaJapanese);
        CompressFolder.zip(tatoebaJapaneseFolder, outputTatoebaJapanese);
        
        File outputTatoebaTranslations = new File(outputDir, "tatoeba_translations.zip");
        CompressFolder.deleteIfExist(outputTatoebaTranslations);
        CompressFolder.zip(tatoebaTranslationFolder, outputTatoebaTranslations);
        
    }
    
    
}
