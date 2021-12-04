package qengine.program;

import java.util.HashMap;
import java.util.Objects;

public class Dictionary {

    /**
     * All the words of the dictionary
     */
    private HashMap<Integer, String> words;
    private HashMap<String, Integer> wordsReverse;

    /**
     * Counter to get a unique id for each word|predicate we add
     */
    private int wordsIdCounter;

    // ========================================================================

    public Dictionary(){
        words = new HashMap<>();
        wordsReverse = new HashMap<>();
        wordsIdCounter = 0;
    }

    public HashMap<Integer, String> getWords(){
        return words;
    }

    public HashMap<String, Integer> getWordsReverse(){
        return wordsReverse;
    }

    public String getWordByKey(int key){
        return words.get(key);
    }

    public int getWordReverseByKey(String key){
        Integer value = wordsReverse.get(key);
        return Objects.requireNonNullElse(value, -1);
    }

    public void addWord(String word) {
        words.computeIfAbsent(wordsIdCounter, k -> {
            wordsReverse.put(word, wordsIdCounter);
            wordsIdCounter++;
            return word;
        });
    }
}