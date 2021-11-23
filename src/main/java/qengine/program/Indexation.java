package qengine.program;

import qengine.program.utils.Utils;

import java.util.*;

public class Indexation {

    private static Indexation instance = new Indexation();

    public static TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> ops;
    public static TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pos;
    public static TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> spo;
    // private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> osp;
    // private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> pso;
    // private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> sop;

    public static HashMap<Integer, Integer> frequences; // ID de la valeur ; frequence d'apparition

    private Indexation() {
        ops = new TreeMap<>();
        pos = new TreeMap<>();
        spo = new TreeMap<>();
        // osp = new HashMap<>();
        // pso = new HashMap<>();
        // sop = new HashMap<>();
        frequences = new HashMap<Integer, Integer>();
    }

    public static Indexation getInstance() {
        return instance;
    }

    public TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> getPos() {
        return pos;
    }

    public TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> getOps() {
        return ops;
    }

    /**
     * Add to all index, at the correct format needed
     */
    public void addToAllIndex(int subject, int predicate, int object) {
        addToIndex(ops, object, predicate, subject);
        addToIndex(pos, predicate, object, subject);
        addToIndex(spo, subject, predicate, object);
        // addToIndex(osp, object, subject, predicate);
        // addToIndex(pso, predicate, subject, object);
        //addToIndex(sop, subject, object, predicate);

        updFrequency(subject);
        updFrequency(predicate);
        updFrequency(object);
    }

    public void addToIndex(TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> index, int key, int subKey, int value) {

        // The pattern we navigate throughout looks like: <key <subkey, [values...]>>
        // ex: sop = <subject <object, [predicates...]>>

        // Try to get the sub hash map
        TreeMap<Integer, TreeSet<Integer>> subHashMap = index.get(key);

        // If null, means the key doesn't exist
        if (subHashMap == null) {
            index.put(key, new TreeMap<Integer, TreeSet<Integer>>() {{
                put(subKey, new TreeSet<>(Arrays.asList(value)));
            }});
        } else {
            // Try to get the values
            TreeSet<Integer> values = subHashMap.get(subKey);

            // If null, means the sub key doesn't exist
            if (values == null) {
                subHashMap.put(subKey, new TreeSet<>(Arrays.asList(value)));
            } else if (!values.contains(value)){
                values.add(value);
            }
        }
    }

    private void updFrequency(int n) {
        if (frequences.containsKey(n)) {
            frequences.replace(n, frequences.get(n) + 1);
        } else {
            frequences.put(n, 1);
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
            case "ops":
                displayIndex(ops);
                break;
            case "pos":
                displayIndex(pos);
                break;
            case "spo":
                displayIndex(spo);
                break;
//            case "osp":
//                displayIndex(osp);
//                break;
//            case "pso":
//                displayIndex(pso);
//                break;
//            case "sop":
//                displayIndex(sop);
//                break;
            default:
                System.out.println("[!] Cannot display this index | name give: " + indexName);
        }
        Utils.displayHLINE();
    }

    /**
     * Display all the values contains in an index give
     */
    public static void displayIndex(TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> index) {
        Iterator<Map.Entry<Integer, TreeMap<Integer, TreeSet<Integer>>>> it = index.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
}
