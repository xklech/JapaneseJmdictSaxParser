/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.japanesedictionary.enums;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jaroslav
 */
public enum Lang {
    JAPANESE,
    ENGLISH,
    FRENCH,
    DUTCH,
    DEUTSCH ,
    RUSSIAN;
/**
 *                 public static final String JAPANESE = "jpn";
    public static final String ENGLISH = "eng";
    public static final String FRENCH = "fra";
    public static final String DUTCH = "nld";
    public static final String DEUTSCH = "deu";
    public static final String RUSSIAN = "rus";
 */

    private static final Map<Lang, String> mMapLanguages;
    static{
        mMapLanguages = new HashMap<>();
        mMapLanguages.put(JAPANESE, "jpn");
        mMapLanguages.put(ENGLISH, "eng");  
        mMapLanguages.put(FRENCH, "fra");
        mMapLanguages.put(DUTCH, "nld");
        mMapLanguages.put(DEUTSCH, "deu");
        mMapLanguages.put(RUSSIAN, "rus");
                
    }
    
    public static String getLanguage(Lang code){
        return mMapLanguages.get(code);
    }
    
    public static Set<String> getAll(){
        Set<String> languages = new HashSet<>();
        languages.add(getLanguage(JAPANESE));
        languages.add(getLanguage(ENGLISH));
        languages.add(getLanguage(FRENCH));
        languages.add(getLanguage(DUTCH));
        languages.add(getLanguage(DEUTSCH));
        languages.add(getLanguage(RUSSIAN));
        return languages;
    }
    
    
    
    public static Set<String> getWithoutJap(){
        Set<String> languages = getAll();
        languages.remove(getLanguage(JAPANESE));
        return languages;
    }
    
    
    
}
