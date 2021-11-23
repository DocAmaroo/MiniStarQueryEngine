package qengine.program.models;

import org.apache.jena.tdb.index.Index;
import qengine.program.Dictionary;
import qengine.program.Indexation;
import qengine.program.logs.Log;
import qengine.program.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

public class Query {

    private String select;
    private ArrayList<Clause> where;

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

    public ArrayList<Integer> fetch(Dictionary dictionary, Indexation index) {

        boolean errFlag = false;

        // The subjects to return
        ArrayList<Integer> subjects = new ArrayList<>();

        // We go through the clauses
        for (Clause clause : where) {

            // Retrieve predicate and object id from the dictionary
            int predValue = dictionary.getWordByValue(clause.getPredicate());
            int objValue = dictionary.getWordByValue(clause.getObject());

            // Check if we found a value for both value, else no response available
            if (predValue == -1 || objValue == -1) {
                System.err.println("[!] The predicate or the object doesn't exist in the dictionary");
                errFlag = true;
                break;
            }

            // Get the frequences
            int predFreq = Indexation.frequences.get(predValue);
            int objFreq = Indexation.frequences.get(objValue);

            // Check if we found a value for both frequency, else no response available
            if (predFreq == -1 || objFreq == -1) {
                System.err.println("[!] No frequency found for the predicate or the object give");
                errFlag = true;
                break;
            }


            TreeSet<Integer> subjectsFound;

            // if predicate frequency is lower or equal to the object we use the POS method
            // else use te OPS method
            if (predFreq <= objFreq) {
                subjectsFound = Indexation.pos.get(predValue).get(objValue);
            }
            else {
                subjectsFound = Indexation.ops.get(objValue).get(predValue);
            }

            // No more subjects to found, then we can leave
            if (subjectsFound == null) {
                break;
            } else {
                if (subjects.isEmpty()) {
                    subjects.addAll(subjectsFound);
                } else {
                    subjects.retainAll(subjectsFound);
                }
            }
        }

        if (errFlag) {
            return null;
        }
        else {
            return subjects;
        }
    }

    public void mergeJoin(Dictionary dictionary, Indexation indexation) {
        ArrayList<TreeSet<Integer>> responses = new ArrayList<>();
        boolean errFlag = false;

        // We go through the clauses
        for (Clause clause : where) {

            // Retrieve predicate and object id from the dictionary
            int predValue = dictionary.getWordByValue(clause.getPredicate());
            int objValue = dictionary.getWordByValue(clause.getObject());

            // Check if we found a value for both value, else no response available
            if (predValue == -1 || objValue == -1) {
                System.err.println("[!] The predicate or the object doesn't exist in the dictionary");
                errFlag = true;
                break;
            }

            // Get the frequences
            int predFreq = Indexation.frequences.get(predValue);
            int objFreq = Indexation.frequences.get(objValue);

            // Check if we found a value for both frequency, else no response available
            if (predFreq == -1 || objFreq == -1) {
                System.err.println("[!] No frequency found for the predicate or the object give");
                errFlag = true;
                break;
            }


            TreeSet<Integer> subjectsFound;

            // if predicate frequency is lower or equal to the object we use the POS method
            // else use te OPS method
            if (predFreq <= objFreq) {
                subjectsFound = Indexation.pos.get(predValue).get(objValue);
            }
            else {
                subjectsFound = Indexation.ops.get(objValue).get(predValue);
            }

            // No more subjects to found, then we can leave
            if (subjectsFound == null) {
                break;
            } else {
                responses.add(subjectsFound);
            }
        }


    }

    @Override
    public String toString() {

        // Transform the where clause into a string
        StringBuilder whereBuilder = new StringBuilder();
        for (Clause clause : where) whereBuilder.append(clause).append("\n");

        return "SELECT ?" + select + " WHERE {\n" + whereBuilder.toString() + " }";
    }
}
