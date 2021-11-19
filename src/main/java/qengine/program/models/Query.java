package qengine.program.models;

import qengine.program.Dictionary;
import qengine.program.Indexation;
import qengine.program.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

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

        for (Clause clause : where) {
            int predicateValue = dictionary.getWordByValue(clause.getPredicate());
            int objectValue = dictionary.getWordByValue(clause.getObject());

            // Check if we found one for both
            if (predicateValue == -1 || objectValue == -1) {
                System.err.println("[!] The query below cannot be fetch. The subject or/and predicate or/and object " +
                        "is not in the dictionary");
                System.err.println("[i] Query: \n" + toString());
                System.err.println("[i] Values: " +
                        "\n\t- Predicate: " + predicateValue +
                        "\n\t- Object: " + objectValue);
                System.err.println(Utils.HLINE);
                return;
            }

            System.out.println("[i] Values: " +
                    "\n\t- Predicate: " + predicateValue +
                    "\n\t- Object: " + objectValue);

            HashMap<Integer, HashMap<Integer, Integer>> pos = index.getPos();

            HashMap<Integer, Integer> subMap = pos.get(predicateValue);
            Integer subKey = subMap.get(objectValue);
            System.out.println(subKey);

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
