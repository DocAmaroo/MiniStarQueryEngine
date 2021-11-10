package qengine.program;

import qengine.program.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class Indexation {

    private static Indexation instance = new Indexation();

    private ArrayList<ArrayList<Integer>> spo;
    private ArrayList<ArrayList<Integer>> sop;
    private ArrayList<ArrayList<Integer>> pso;
    private ArrayList<ArrayList<Integer>> ops;
    private ArrayList<ArrayList<Integer>> pos;
    private ArrayList<ArrayList<Integer>> osp;

    private Indexation() {
        spo = new ArrayList<>();
        sop = new ArrayList<>();
        pso = new ArrayList<>();
        ops = new ArrayList<>();
        pos = new ArrayList<>();
        osp = new ArrayList<>();
    }

    public static Indexation getInstance() {
        return instance;
    }


    /**
     * Add to all index, at the correct format needed
     */
    public void addToAllIndex(int subject, int predicate, int object) {
        sop.add(new ArrayList<>(Arrays.asList(subject, object, predicate)));
        pso.add(new ArrayList<>(Arrays.asList(predicate, subject, object)));
        ops.add(new ArrayList<>(Arrays.asList(object, predicate, subject)));
        spo.add(new ArrayList<>(Arrays.asList(subject, predicate, object)));
        pos.add(new ArrayList<>(Arrays.asList(predicate, object, subject)));
        osp.add(new ArrayList<>(Arrays.asList(object, subject, predicate)));
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
        switch (indexName) {
            case "sop":
                Utils.displayIndex(sop);
                break;
            case "pso":
                Utils.displayIndex(pso);
                break;
            case "ops":
                Utils.displayIndex(ops);
                break;
            case "spo":
                Utils.displayIndex(spo);
                break;
            case "pos":
                Utils.displayIndex(pos);
                break;
            case "osp":
                Utils.displayIndex(osp);
                break;
            default:
                System.out.println("[!] Cannot display this index | name give: " + indexName);
        }
    }
}
