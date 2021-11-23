package qengine.program.models;

import com.github.andrewoma.dexx.collection.internal.redblack.Tree;
import org.apache.jena.tdb.index.Index;
import org.eclipse.rdf4j.query.algebra.In;
import qengine.program.Dictionary;
import qengine.program.Indexation;
import qengine.program.logs.Log;
import qengine.program.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
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

    public TreeSet<Integer> fetch(Dictionary dictionary, Indexation index) {
        if (where.size() == 1) {
            return fetchClause(dictionary, where.get(0));
        } else {
            return mergeJoin(dictionary, where);
        }
    }

    public TreeSet<Integer> fetchClause(Dictionary dictionary, Clause clause) {
        TreeSet<Integer> subjectsFound = new TreeSet<>();

        // Retrieve predicate and object id from the dictionary
        int predValue = dictionary.getWordByValue(clause.getPredicate());
        int objValue = dictionary.getWordByValue(clause.getObject());

        // Check if we found a value for both value, else no response available
        if (predValue == -1 || objValue == -1) {
            System.err.println("[!] The predicate or the object doesn't exist in the dictionary");
            return null;
        }

        // Get the frequences
        int predFreq = Indexation.frequences.get(predValue);
        int objFreq = Indexation.frequences.get(objValue);

        // Check if we found a value for both frequency, else no response available
        if (predFreq == -1 || objFreq == -1) {
            System.err.println("[!] No frequency found for the predicate or the object give");
            return null;
        }

        // if predicate frequency is lower or equal to the object we use the POS method
        // else use te OPS method
        if (predFreq <= objFreq) {
            TreeMap<Integer, TreeSet<Integer>> objects = Indexation.pos.get(predValue);
            if (objects != null) {
                subjectsFound = Indexation.pos.get(predValue).get(objValue);
            }
        } else {
            TreeMap<Integer, TreeSet<Integer>> predicates = Indexation.ops.get(objValue);
            if (predicates != null) {
                subjectsFound = Indexation.ops.get(objValue).get(predValue);
            }
        }

        return subjectsFound;
    }

    public TreeSet<Integer> mergeJoin(Dictionary dictionary, ArrayList<Clause> clauses) {
        //ex: (([0, 1, 2] join [0, 2, 5]) join [2, 5, 7]) --> [2]

        TreeSet<Integer> response;

        // Init the response with a first join
        TreeSet<Integer> fetchA = fetchClause(dictionary, clauses.get(0));
        TreeSet<Integer> fetchB = fetchClause(dictionary, clauses.get(1));

        if (fetchA == null || fetchB == null) {
            return null;
        }

        response = join(fetchA, fetchB);
        if (response.isEmpty()) {
            return null;
        }

        for (int i = 2; i < clauses.size(); i++) {
            TreeSet<Integer> fetch = fetchClause(dictionary, clauses.get(i));
            response = join(response, fetch);

            if (response.isEmpty()) {
                return null;
            }
        }

        return response;
    }

    public TreeSet<Integer> join(TreeSet<Integer> treeA, TreeSet<Integer> treeB) {
        //ex: treeA: [0, 1, 2] && treeB: [0, 2, 5] --> [0, 2]

        // Transform TreeSet to ArrayList to ease of use on loop
        ArrayList<Integer> arrA = new ArrayList<>(treeA);
        ArrayList<Integer> arrB = new ArrayList<>(treeB);

        // The index for each array
        int indexA = 0;
        int indexB = 0;

        // The result of all value joined
        TreeSet<Integer> result = new TreeSet<>();

        while(indexA < arrA.size() && indexB < arrB.size()) {
            int valueA = arrA.get(indexA);
            int valueB = arrB.get(indexB);

            if (valueA == valueB) {
                result.add(arrA.get(indexA));
                indexA++;
                indexB++;
            } else if (valueA < valueB) {
                indexA++;
            } else {
                indexB++;
            }
        }

        return result;
    }

    @Override
    public String toString() {

        // Transform the where clause into a string
        StringBuilder whereBuilder = new StringBuilder();
        for (Clause clause : where) whereBuilder.append(clause).append("\n");

        return "SELECT ?" + select + " WHERE {\n" + whereBuilder.toString() + " }";
    }
}
