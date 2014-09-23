/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.japanesedictionary;


import cz.muni.fi.japanesejmdictsaxparser.CronTaskProvider;
import cz.muni.fi.japanesejmdictsaxparser.util.CompressFolder;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Jaroslav
 */
public class Main {
    
    public static void main(String [] args){
        /*Map<String, String> map = new HashMap<>();
        map.put("OPENSHIFT_DATA_DIR", "output/");
        setEnv(map);*/
        try {
            File sourceFolder = new File(System.getenv("OPENSHIFT_DATA_DIR")+"sources");
            CompressFolder.deleteDirectory(sourceFolder);
            sourceFolder.mkdirs();
                    
            String outputFolder=System.getenv("OPENSHIFT_DATA_DIR")+"output";
            File output = new File(outputFolder);
            output.mkdirs();
            CronTaskProvider.downloadPreprocesTatoebaSentences();
            
            CronTaskProvider.downloadPreprocesJmdict();
            CronTaskProvider.prepareJmDict(output);
            
            CronTaskProvider.downloadPreprocesKanjidic2();
            CronTaskProvider.prepareKanjidic2(output);
            
            
            
            
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    
    }
    
    
    
    
    protected static void setEnv(Map<String, String> newenv)
    {
      try
        {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        }
        catch (NoSuchFieldException e)
        {
          try {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
          } catch (Exception e2) {
            e2.printStackTrace();
          }
        } catch (Exception e1) {
            e1.printStackTrace();
        } 
    }
}
