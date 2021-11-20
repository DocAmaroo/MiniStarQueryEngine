package qengine.program;

import qengine.program.utils.Utils;

import java.util.*;

public class Indexation {

    private static Indexation instance = new Indexation();

    // private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> ops;
    // private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> osp;
    private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> pos;
    // private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> pso;
    // private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> sop;
    private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> spo;

    private Indexation() {
        // ops = new HashMap<>();
        // osp = new HashMap<>();
        pos = new HashMap<>();
        // pso = new HashMap<>();
        // sop = new HashMap<>();
        spo = new HashMap<>();
    }

    public static Indexation getInstance() {
        return instance;
    }

    public HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> getPos() {
        return pos;
    }

    /**
     * Add to all index, at the correct format needed
     */
    public void addToAllIndex(int subject, int predicate, int object) {
        // addToIndex(ops, object, predicate, subject);
        // addToIndex(osp, object, subject, predicate);
        addToIndex(pos, predicate, object, subject);
        // addToIndex(pso, predicate, subject, object);
        //addToIndex(sop, subject, object, predicate);
         addToIndex(spo, subject, predicate, object);
    }

    public void addToIndex(HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> index, int key, int subkey, int value) {

        // The pattern we navigate throughout looks like: <key <subkey, [values...]>>
        // ex: sop = <subject <object, [predicates...]>>

        // Try to get the sub hash map
        HashMap<Integer, ArrayList<Integer>> subHashMap = index.get(key);

        // If null, means the key doesn't exist
        if (subHashMap == null) {
            index.put(key, new HashMap<Integer, ArrayList<Integer>>() {{
                put(subkey, new ArrayList<>(Arrays.asList(value)));
            }});
        } else {
            // Try to get the values
            ArrayList<Integer> values = subHashMap.get(subkey);

            // If null, means the sub key doesn't exist
            if (values == null) {
                subHashMap.put(subkey, new ArrayList<>(Arrays.asList(value)));
            } else if (!values.contains(value)){
                values.add(value);
            }
        }
    }

    /**
     * Choose one the name below to display the index you need:
     * <ul>
     *    <li>{@link #pos}</li>
     *    <li>{@link #spo}</li>
     * </ul>
     */
    public void displayIndexByName(String indexName) {
        System.out.println("\n[i] DISPLAY INDEX " + indexName.toUpperCase());
        switch (indexName) {
//            case "ops":
//                displayIndex(ops);
//                break;
//            case "osp":
//                displayIndex(osp);
//                break;
            case "pos":
                displayIndex(pos);
                break;
//            case "pso":
//                displayIndex(pso);
//                break;
//            case "sop":
//                displayIndex(sop);
//                break;
            case "spo":
                displayIndex(spo);
                break;
            default:
                System.out.println("[!] Cannot display this index | name give: " + indexName);
        }
        Utils.displayHLINE();
    }

    /**
     * Display all the values contains in an index give
     */
    public static void displayIndex(HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> index) {
        Iterator it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
