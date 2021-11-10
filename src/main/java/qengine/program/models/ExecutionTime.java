package qengine.program.models;

import qengine.program.logs.Log;

public class ExecutionTime {

    private String name;
    private long value;

    public ExecutionTime(String name) {
        this.name = name;
        this.value = -1;
    }

    public ExecutionTime(String name, long value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public long getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[i] EXECUTION TIME | " + name + ": " +
                (value != -1 ? value + "ms" : Log.UNAVAILABLE);
    }

    public String toCSV() {
        String type = "EXECUTION TIME";
        return type + "," + name + "," + value + "ms";
    }
}
