package qengine.program.models;

import qengine.program.Dictionary;
import qengine.program.Indexation;
import qengine.program.logs.Log;
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

        // For verbose only
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("\n[i] Fetching... \n").append(toString());

        boolean errFlag = false;

        ArrayList<Integer> keyResults = new ArrayList<>();

        for (Clause clause : where) {
            int predicateValue = dictionary.getWordByValue(clause.getPredicate());
            int objectValue = dictionary.getWordByValue(clause.getObject());

            // Check if we found a value for both, else no response available
            if (predicateValue == -1 || objectValue == -1) {
                errFlag = true;
                break;
            }

            HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> pos = index.getPos();

            // Search by using pos method
            HashMap<Integer, ArrayList<Integer>> subMap = pos.get(predicateValue);
            ArrayList<Integer> subjects = subMap.get(objectValue);

            // No subjects found, mean no valid response
            if (subjects == null) {
                errFlag = true;
                break;
            }

            // First response receive with the first where condition
            if (keyResults.isEmpty()) {
                keyResults.addAll(subjects);
            }

            // Else, compare the two arrays and keep the common value
            else {
                keyResults.retainAll(subjects);

                if (keyResults.isEmpty()) {
                    errFlag = true;
                    break;
                }
            }
        }

        if (errFlag || keyResults.isEmpty()) {
            strBuilder.append("\n[i] Cannot found a response to this query");
        }
        else {
            strBuilder.append("\n[i] Query response:");
            for (int key : keyResults) {
                strBuilder.append("\n\t* ").append(dictionary.getWordByKey(key));
            }
        }

        strBuilder.append("\n").append(Utils.HLINE);
        if (Log.isVerbose) System.out.println(strBuilder.toString());
    }

    @Override
    public String toString() {

        // Transform the where clause into a string
        StringBuilder whereBuilder = new StringBuilder();
        for (Clause clause : where) whereBuilder.append(clause).append("\n");

        return "SELECT ?" + select + " WHERE {\n" + whereBuilder.toString() + " }";
    }
}
