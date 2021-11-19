package qengine.program;

import qengine.program.utils.Utils;

import java.util.*;

public class Indexation {

    private static Indexation instance = new Indexation();

    private HashMap<Integer, HashMap<Integer, Integer>> ops;
    private HashMap<Integer, HashMap<Integer, Integer>> osp;
    private HashMap<Integer, HashMap<Integer, Integer>> pos;
    private HashMap<Integer, HashMap<Integer, Integer>> pso;
    private HashMap<Integer, HashMap<Integer, Integer>> sop;
    private HashMap<Integer, HashMap<Integer, Integer>> spo;

    private Indexation() {
        ops = new HashMap<>();
        osp = new HashMap<>();
        pos = new HashMap<>();
        pso = new HashMap<>();
        sop = new HashMap<>();
        spo = new HashMap<>();
    }

    public static Indexation getInstance() {
        return instance;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getPos() {
        return pos;
    }

    /**
     * Add to all index, at the correct format needed
     */
    public void addToAllIndex(int subject, int predicate, int object) {
        addToIndex(ops, object, predicate, subject);
        addToIndex(osp, object, subject, predicate);
        addToIndex(pos, predicate, object, subject);
        addToIndex(pso, predicate, subject, object);
        addToIndex(sop, subject, object, predicate);
        addToIndex(spo, subject, predicate, object);
    }

    public void addToIndex(HashMap<Integer, HashMap<Integer, Integer>> index, int key, int subkey, int value) {

        // The pattern we navigate throughout looks like: <key <subkey, value>>
        // ex: sop = <subject <object, predicate>>

        // Try to get the sub hash map
        HashMap<Integer, Integer> subHashMap = index.get(key);

        // If null, means the key doesn't exist
        if (subHashMap == null) {
            index.put(key, new HashMap<Integer, Integer>() {{ put(subkey, value); }});
        } else {
            // If null, means the sub key doesn't exist
            subHashMap.putIfAbsent(subkey, value);
        }
    }

    /**
     * Choose one the name below to display the index you need:
     * <ul>
     *    <li>{@link #sop}</li>
     *    <li>{@link #pso}</li>
     *    <li>{@link #ops}</li>
     *    <li>{@link #spo}</li>
     *    <li>{@link #pos}</li>
     *    <li>{@link #osp}</li>
     * </ul>
     */
    public void displayIndexByName(String indexName) {
        System.out.println("\n[i] DISPLAY INDEX " + indexName.toUpperCase());
        switch (indexName) {
            case "sop":
                displayIndex(sop);
                break;
            case "pso":
                displayIndex(pso);
                break;
            case "ops":
                displayIndex(ops);
                break;
            case "spo":
                displayIndex(spo);
                break;
            case "pos":
                displayIndex(pos);
                break;
            case "osp":
                displayIndex(osp);
                break;
            default:
                System.out.println("[!] Cannot display this index | name give: " + indexName);
        }
        Utils.displayHLINE();
    }

    /**
     * Display all the values contains in an index give
     */
    public static void displayIndex(HashMap<Integer, HashMap<Integer, Integer>> index) {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
