package qengine.program.models;

import qengine.program.Dictionary;
import qengine.program.Indexation;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class Query {

    private String select;
    private ArrayList<Triplet> where;

    public Query() {
        this.select = "";
        this.where = new ArrayList<>();
    }

    public Query(String select, ArrayList<Triplet> where) {
        this.select = select;
        this.where = where;
    }

    public int getNbTriplet() {
        return where.size();
    }

    public void addTriplet(Triplet whereTriplet) {
        this.where.add(whereTriplet);
    }

    public TreeSet<Integer> fetch(Dictionary dictionary, Indexation index) {
        if (where.size() == 1) {
            return fetchTriplet(dictionary, index, where.get(0));
        } else {
            return mergeJoin(dictionary, index, where);
        }
    }

    public TreeSet<Integer> fetchTriplet(Dictionary dictionary, Indexation index, Triplet clause) {
        TreeSet<Integer> subjectsFound = new TreeSet<>();

        // Retrieve predicate and object id from the dictionary
        int predValue = dictionary.getWordReverseByKey(clause.getPredicate());
        int objValue = dictionary.getWordReverseByKey(clause.getObject());

        // Check if we found a value for both value, else no response available
        if (predValue == -1 || objValue == -1) {
            // DEBUG ONLY
            // System.err.println("[!] The predicate or the object doesn't exist in the dictionary");
            return null;
        }

        // Get the frequences
        int predFreq = index.getFrequence(predValue);
        int objFreq = index.getFrequence(objValue);

        // Check if we found a value for both frequency, else no response available
        if (predFreq == -1 || objFreq == -1) {
            // DEBUG ONLY
            // System.err.println("[!] No frequency found for the predicate or the object give");
            return null;
        }

        // if predicate frequency is lower or equal to the object we use the POS method
        // else use te OPS method
        if (predFreq <= objFreq) {
            TreeMap<Integer, TreeSet<Integer>> objects = index.getPos().get(predValue);
            if (objects != null) {
                subjectsFound = index.getPos().get(predValue).get(objValue);
            }
        } else {
            TreeMap<Integer, TreeSet<Integer>> predicates = index.getOps().get(objValue);
            if (predicates != null) {
                subjectsFound = index.getOps().get(objValue).get(predValue);
            }
        }

        return subjectsFound;
    }

    public TreeSet<Integer> mergeJoin(Dictionary dictionary, Indexation index, ArrayList<Triplet> clauses) {
        //ex: (([0, 1, 2] join [0, 2, 5]) join [2, 5, 7]) --> [2]

        TreeSet<Integer> response;

        // Init the response with a first join
        TreeSet<Integer> fetchA = fetchTriplet(dictionary, index, clauses.get(0));
        TreeSet<Integer> fetchB = fetchTriplet(dictionary, index, clauses.get(1));

        if (fetchA == null || fetchB == null) {
            return null;
        }

        response = join(fetchA, fetchB);
        if (response.isEmpty()) {
            return null;
        }

        for (int i = 2; i < clauses.size(); i++) {
            TreeSet<Integer> fetch = fetchTriplet(dictionary, index, clauses.get(i));
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

    // NAIVE VERSION OF FETCH
    public TreeSet<Integer> fetchNaive(Dictionary dictionary, Indexation index) {

        // For verbose only
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("\n[i] Fetching... \n").append(toString());

        boolean errFlag = false;

        TreeSet<Integer> keyResults = new TreeSet<>();

        for (Triplet clause : where) {
            int predicateValue = dictionary.getWordReverseByKey(clause.getPredicate());
            int objectValue = dictionary.getWordReverseByKey(clause.getObject());

            // Check if we found a value for both, else no response available
            if (predicateValue == -1 || objectValue == -1) {
                errFlag = true;
                break;
            }

            // Search by using pos method
            TreeMap<Integer, TreeSet<Integer>> subMap = index.getPos().get(predicateValue);
            TreeSet<Integer> subjects = subMap.get(objectValue);

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

        return keyResults;
    }

    @Override
    public String toString() {

        // Transform the where clause into a string
        StringBuilder whereBuilder = new StringBuilder();
        for (Triplet clause : where) whereBuilder.append(clause).append("\n");

        return "SELECT ?" + select + " WHERE {\n" + whereBuilder.toString() + " }";
    }

    public boolean isEqual(Query newQuery) {
        if (where.size() != newQuery.where.size()) {
            return false;
        }

        return newQuery.where.containsAll(where);

//        for (Triplet triplet : where) {
//            if (!newQuery.where.containsAll(where)) {
//                System.out.println("triplet not in: " + triplet);
//                System.out.println(newQuery.where.contains(triplet));
//                return false;
//            }
//        }
    }
}