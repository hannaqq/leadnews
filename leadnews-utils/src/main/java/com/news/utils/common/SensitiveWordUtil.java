package com.news.utils.common;


import java.util.*;

public class SensitiveWordUtil {

    public static Map<String, Object> dictionaryMap = new HashMap<>();


    /**
     * Initialize sensitive word dictionary using Trie structure
     * Builds a tree structure where each node contains the next character and an end flag
     *
     * @param words Collection of sensitive words to build dictionary
     */
    public static void initMap(Collection<String> words) {
        if (words == null) {
            System.out.println("Sensitive word list cannot be empty");
            return ;
        }

        // Initialize map with words.size() capacity
        // Note: actual entry count may be less due to words sharing the same first character
        Map<String, Object> map = new HashMap<>(words.size());
        // Current level data during traversal
        Map<String, Object> curMap = null;
        Iterator<String> iterator = words.iterator();

        while (iterator.hasNext()) {
            String word = iterator.next();
            curMap = map;
            int len = word.length();
            for (int i =0; i < len; i++) {
                // Traverse each character in the word
                String key = String.valueOf(word.charAt(i));
                // Check if current character exists in current level
                // If not, create new node and point current level to next node
                Map<String, Object> wordMap = (Map<String, Object>) curMap.get(key);
                if (wordMap == null) {
                    // Each node contains: next node map and isEnd flag
                    wordMap = new HashMap<>(2);
                    wordMap.put("isEnd", "0");
                    curMap.put(key, wordMap);
                }
                curMap = wordMap;
                // If current character is the last one, set isEnd flag to "1"
                if (i == len -1) {
                    curMap.put("isEnd", "1");
                }
            }
        }

        dictionaryMap = map;
    }

    /**
     * Check if text starting from beginIndex matches any sensitive word
     *
     * @param text Text to check
     * @param beginIndex Starting index in text
     * @return Length of matched word, 0 if no match
     */
    private static int checkWord(String text, int beginIndex) {
        if (dictionaryMap == null) {
            throw new RuntimeException("Dictionary cannot be empty");
        }
        boolean isEnd = false;
        int wordLength = 0;
        Map<String, Object> curMap = dictionaryMap;
        int len = text.length();
        // Match from beginIndex in text
        for (int i = beginIndex; i < len; i++) {
            String key = String.valueOf(text.charAt(i));
            // Get next node for current key
            curMap = (Map<String, Object>) curMap.get(key);
            if (curMap == null) {
                break;
            } else {
                wordLength ++;
                if ("1".equals(curMap.get("isEnd"))) {
                    isEnd = true;
                }
            }
        }
        if (!isEnd) {
            wordLength = 0;
        }
        return wordLength;
    }

    /**
     * Find all matched sensitive words and their occurrence count in text
     *
     * @param text Text to search
     * @return Map of matched words and their occurrence count
     */
    public static Map<String, Integer> matchWords(String text) {
        Map<String, Integer> wordMap = new HashMap<>();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            int wordLength = checkWord(text, i);
            if (wordLength > 0) {
                String word = text.substring(i, i + wordLength);
                // Increment match count for keyword
                if (wordMap.containsKey(word)) {
                    wordMap.put(word, wordMap.get(word) + 1);
                } else {
                    wordMap.put(word, 1);
                }

                i += wordLength - 1;
            }
        }
        return wordMap;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("私人侦探");
        list.add("信用卡提现");
        list.add("广告代理");

        // Initialize sensitive word dictionary
        initMap(list);


        String content="江户川柯南私人侦探，可以帮你解决：商务调查，要账清债，企业打假，寻人找人，财产调查，私人调查，电话：12345678901";
        // Search for sensitive words in text
        Map<String, Integer> map = matchWords(content);
        if(map.size() > 0){
            System.out.println(map);
        }else {
            System.out.println("No sensitive words found");
        }

    }
}
