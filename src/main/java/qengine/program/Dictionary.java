package qengine.program;

import java.util.HashMap;

public class Dictionary {

    /**
     * Create a Singleton Instance of the Dictionary
     */
    private static Dictionary instance = new Dictionary();

    /**
     * All the words of the dictionary
     */
    private HashMap<Integer, String> words;

    /**
     * Counter to get a unique id for each word|predicate we add
     */
    private int wordsIdCounter;

    // ========================================================================

    private Dictionary(){
        words = new HashMap<>();
        wordsIdCounter = 0;
    }

    public static Dictionary getInstance() {
        return instance;
    }

    public HashMap<Integer, String> getWords(){
        return words;
    }

    public String getWordByKey(int key){
        return words.get(key);
    }

    public int getWordByValue(String value){
        for(int i = 0; i < words.size(); i++){
            if(words.get(i).equals(value)){
                return i;
            }
        }
        return -1;
    }

    public void addWord(String word) {
        if (!words.containsValue(word)) {
            words.put(wordsIdCounter, word);
            wordsIdCounter++;
        }
    }
}
