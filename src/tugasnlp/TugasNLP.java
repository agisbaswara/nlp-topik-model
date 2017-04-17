/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugasnlp;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;


import IndonesianNLP.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Agis Baswara
 */
public class TugasNLP {
    
    public static ArrayList<String[]> dataSet = new ArrayList<>();
    public static ArrayList<String> daftarKata;
    public static ArrayList<String> daftarKataString;
    public static ArrayList<String> daftarKataSteammer;
    public static ArrayList<String> daftarKataStopword;
    
    public static Map<String, Integer> dataTerm;
    
    public static ArrayList<String> notSteamming;
    public static String[] stopwords;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        daftarKata = new ArrayList<>();
        daftarKataString = new ArrayList<>();
        daftarKataSteammer = new ArrayList<>();
        daftarKataStopword = new ArrayList<>();
        dataTerm = new HashMap<>();
        
        notSteamming = new ArrayList<>();
        notSteamming.add("tangan");
        notSteamming.add("telapak");
        
        JSONReadFromFile();
        for (String[] strings : dataSet) {
            String teks = strings[2];
            if (teks.isEmpty()){
                continue;
            }
            IndonesianPOSTagger ipostag = new IndonesianPOSTagger();
            ArrayList<String[]> str = ipostag.doPOSTag(teks);
            int n = str.size();
            for (int i = 0; i < n; i++) {
                if (str.get(i)[1].equals("NN") && !daftarKata.contains(str.get(i)[0])){
                  daftarKata.add(str.get(i)[0]);
                }
            }    
            
            //Only String
            for (String string : daftarKata) {
                String str_ = string.replaceAll("[^A-Za-z/-]", "").toLowerCase();
                if (str_.length() > 1 && !isInteger(str_)) {
                    daftarKataString.add(str_);
                }
            }

            //Stemmer
            for (String string : daftarKataString) {
                String str_;
                if (notSteamming.contains(string)){
                    str_ = string;
                } else {
                    IndonesianStemmer isteam = new IndonesianStemmer();
                    str_ = isteam.stemSentence(string);
                }
                
                if (str_.length() > 1 && !daftarKataSteammer.contains(str_)) {
                    daftarKataSteammer.add(str_);
                }
            }

            //Stopword
            stopwords = FileToString(new File("data/stopwords.txt")).split("\r");
            ArrayList<String> dsStopwords = new ArrayList<>();
            for (String stopword : stopwords) {
                dsStopwords.add(stopword.trim());
            }
            for (String string : daftarKataSteammer) {
                String str_ = string;
                if (str_.length() > 1 && !dsStopwords.contains(str_)) {
                    daftarKataStopword.add(str_);
                    dataTerm.put(str_, 0);
                }
            }

            IndonesianSentenceTokenizer tokenizer = new IndonesianSentenceTokenizer();
            ArrayList<String> strings_ = tokenizer.tokenizeSentence(teks);
            for (String string : strings_) {
                String str_ = string.replaceAll("[^A-Za-z/-]", "").toLowerCase();
                if (notSteamming.contains(string)){
                    str_ = string;
                } else {
                    IndonesianStemmer isteam = new IndonesianStemmer();
                    str_ = isteam.stemSentence(string);
                }
                if (str_.length() > 1 && !isInteger(str_) && !dsStopwords.contains(str_)) {
                    if (dataTerm.containsKey(str_)){
                        dataTerm.put(str_, dataTerm.get(str_) + 1);
                    }
                }
            }
            
            dataTerm = MapUtil.sortByValue(dataTerm);
            
            for (Map.Entry<String, Integer> entry : dataTerm.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                System.out.println(key + " / " + String.valueOf(value));
            }
        }        
    }
    
    /**
     * Read Data From JSON File
     */
    public static void JSONReadFromFile() {
        JSONParser parser = new JSONParser();
        try {
 
            JSONArray a = (JSONArray) parser.parse(new FileReader("data/corpus-single.txt"));

            for (Object o : a)
            {
                JSONObject person = (JSONObject) o;
                String[] str = new String[3];
                str[0]  = (String) person.get("title");
                str[1]  = (String) person.get("abstract");
                str[2]  = (String) person.get("content");
                dataSet.add(str);
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String FileToString(File file) throws IOException {
        int len;
        char[] chr = new char[4096];
        final StringBuffer buffer = new StringBuffer();
        final FileReader reader = new FileReader(file);
        try {
            while ((len = reader.read(chr)) > 0) {
                buffer.append(chr, 0, len);
            }
        } finally {
            reader.close();
        }
        return buffer.toString();
    }
    
    public static boolean isInteger(String str) {
    if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

}
