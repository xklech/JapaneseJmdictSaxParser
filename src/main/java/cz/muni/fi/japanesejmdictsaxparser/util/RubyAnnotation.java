package cz.muni.fi.japanesejmdictsaxparser.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RubyAnnotation {

	public static String create(String kanjiString, String readingString) {
        if (kanjiString == null) {
            throw new NullPointerException("kanjiString");
        }
        if (readingString == null) {
            throw new NullPointerException("readingString");
        }
        if (kanjiString.matches("\\p{IsHan}+\\p{IsHiragana}\\p{IsHan}+")) { //correct erroneous data
        	String s = removeBlankEntries(kanjiString.split("\\p{IsHan}")).get(0);
        	if (!readingString.matches(".+" + s + ".+")) {
        		String [] array = kanjiString.split(s);
        		kanjiString = array[0] + array[1];
        	}
        }
        //only "kanji word"	
        if (kanjiString.matches("[\\p{IsHan}\\x{30F6}]+") || (kanjiString.matches("[\\p{IsHan}\\p{IsKatakana}]+") && readingString.matches("[\\p{IsHiragana}]+")) || !kanjiString.matches(".*\\p{IsHiragana}.*")) {
            return kanjiString + "," + readingString + ";";
        }
        if (kanjiString.matches("[\\p{IsHiragana}\\p{IsKatakana}]*")) {
            return kanjiString + ";";
        }
        final StringBuilder sb = new StringBuilder();
        if (kanjiString.matches(".*[\\p{IsHiragana}\\p{IsKatakana}].*")) {
        	final List<String> okuriganas = removeBlankEntries(kanjiString.split("[\\p{IsHan}\\p{IsLatin}\\p{IsKatakana}\\x{30FC}\\x{FF10}-\\x{FF19}]")); //remain only strings with hiragana
        	final Set<String> notDupliciteOkuriganas = new HashSet<>(okuriganas);
        	final boolean hasDupliciteOkurigana = okuriganas.size() != notDupliciteOkuriganas.size();
        	final List<String> kanjis = removeBlankEntries(kanjiString.split("\\p{IsHiragana}")); //remain only strings with kanji and katakana
        	List<String> furiganas = null;
        	final boolean startsWithKanji = kanjiString.matches("[\\p{IsHan}\\p{IsLatin}].*");
        	boolean isSameCount = false;
        	int runNum = 0;
        	while (!isSameCount && runNum < 3) {
        		String reading = readingString;
        		runNum++;
        		furiganas = new ArrayList<>();
	        	boolean isFirstRun = true;
	        	int pos = 0;
	        	while (!reading.isEmpty()) {
	        		if (pos < okuriganas.size()) {
	        			StringBuilder builder = new StringBuilder();
	        			final String okurigana = okuriganas.get(pos);
	            		while ((!hasDupliciteOkurigana && reading.matches(okurigana + ".+" + okurigana + ".*") && builder.length() < kanjis.get(furiganas.size()).length()) || (!reading.startsWith(okurigana) && !reading.isEmpty()) || (startsWithKanji && isFirstRun) || (runNum > 1 && reading.startsWith(okurigana + okurigana)) || (runNum > 2 && !hasDupliciteOkurigana && reading.matches(okurigana + ".+" + okurigana + ".*"))) {
	            			builder.append(reading.charAt(0));
	            			reading = reading.substring(1);
	            			isFirstRun = false;
	            		}
	            		if (reading.startsWith(okurigana)) {
	            			reading = reading.substring(okurigana.length(), reading.length());
	            		}
	            		if (builder.length() != 0) {
	            			furiganas.add(builder.toString());
	            		}
	            		isFirstRun = false;
	            		pos++;
	        		} else {
	        			furiganas.add(reading);
	        			reading = "";
	        		}
	            }
	        	isSameCount = kanjis.size() == furiganas.size();
        	} 
            String word = kanjiString;
            int i = 0; //kanji and furigana should have the same size
            while (!word.isEmpty()) {
            	if (word.matches("[\\p{IsHan}\\p{IsLatin}\\p{IsKatakana}\\x{30FC}\\x{FF10}-\\x{FF19}].*")) {
            		while (word.matches("[\\p{IsHan}\\p{IsLatin}\\p{IsKatakana}\\x{30FC}\\x{FF10}-\\x{FF19}].*")) {
            			sb.append(word.charAt(0));
            			word = word.substring(1);
            		}
            		sb.append(",").append(furiganas.get(i) + ";");
            		i++;
            	} else {
            		sb.append(word.substring(0, 1)).append(";");
            		word = word.substring(1);
            	}
            }	
        }
        return sb.toString();
    }

    private static List<String> removeBlankEntries(String[] array) {
        List<String> list = new ArrayList<>();
        for (String s : array) {
            if (!s.isEmpty()) {
                list.add(s);
            }
        }
        return list;
    }
}
