package qengine.program;

import qengine.program.utils.Utils;

import java.util.*;

public class Indexation {

    private TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> ops;
    private TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> pos;
    private TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> spo;

    private final HashMap<Integer, Integer> frequencies; // ID de la valeur ; frequence d'apparition

    public Indexation() {
        ops = new TreeMap<>();
        pos = new TreeMap<>();
        spo = new TreeMap<>();

        frequencies = new HashMap<Integer, Integer>();
    }

    public TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> getPos() {
        return pos;
    }

    public TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> getOps() {
        return ops;
    }

    public TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> getSpo() {
        return spo;
    }

    public HashMap<Integer, Integer> getFrequencies() {
        return frequencies;
    }

    public Integer getFrequency(int key) {
        return frequencies.get(key);
    }

    /**
     * Add to all index, at the correct format needed
     */
    public void addToAllIndex(int subject, int predicate, int object) {
        addToIndex(ops, object, predicate, subject);
        addToIndex(pos, predicate, object, subject);
        addToIndex(spo, subject, predicate, object);

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
        if (frequencies.containsKey(n)) {
            frequencies.replace(n, frequencies.get(n) + 1);
        } else {
            frequencies.put(n, 1);
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