package qengine.program.models;

import qengine.program.Dictionary;
import qengine.program.Indexation;
import qengine.program.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Query {

    private String select;
    ArrayList<Clause> where;

    private String sourceString;

    public Query() {
        this.select = "";
        this.where = new ArrayList<>();
    }

    public Query(String select, ArrayList<Clause> where) {
        this.select = select;
        this.where = where;
    }

    public void setSelect(String select) {
        this.select = select;
    }

    public void setWhere(ArrayList<Clause> where) {
        this.where = where;
    }

    public void addWhereClause(Clause whereClause) {
        this.where.add(whereClause);
    }

    public void setSourceString(String sourceString) {
        this.sourceString = sourceString;
    }

    public void fetch(Dictionary dictionary, Indexation index) {
        System.out.println("[i] Fetching : \n" + toString());

        ArrayList<Integer> keyResults = new ArrayList<>();

        for (Clause clause : where) {
            int predicateValue = dictionary.getWordByValue(clause.getPredicate());
            int objectValue = dictionary.getWordByValue(clause.getObject());

            // Check if we found a value for both, else no response available
            if (predicateValue == -1 || objectValue == -1) {
                System.out.println("[i] Cannot found a response to this query");
                return;
            }

            HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> pos = index.getPos();

            // Search by using pos method
            HashMap<Integer, ArrayList<Integer>> subMap = pos.get(predicateValue);
            ArrayList<Integer> subjects = subMap.get(objectValue);

            if (keyResults.isEmpty()) {
                keyResults.addAll(subjects);
            }
            else {
                keyResults = subjects.stream()
                        .filter(keyResults::contains)
                        .collect(Collectors.toCollection(ArrayList::new));

                if (keyResults.isEmpty()) {
                    System.out.println("[i] Cannot found a response to this query");
                    return;
                }
            }
        }

        System.out.println("[i] Query response:");
        for (int key : keyResults) {
            System.out.println("\t* " + dictionary.getWordByKey(key));
        }
        System.out.println(Utils.HLINE);
    }

    @Override
    public String toString() {

        // Transform the where clause into a string
        StringBuilder whereBuilder = new StringBuilder();
        for (Clause clause : where) whereBuilder.append(clause).append("\n");

        return "SELECT ?" + select + " WHERE {\n" + whereBuilder.toString() + " }";
    }
}
