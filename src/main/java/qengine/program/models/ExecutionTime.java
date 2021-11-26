package qengine.program.models;

import qengine.program.logs.Log;

public class ExecutionTime {

    private String name;
    private long value;
    private String description;

    public ExecutionTime(String name) {
        this.name = name;
        this.value = -1;
    }

    public ExecutionTime(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long toSecond() { return this.value * 1000; }

    @Override
    public String toString() {
        return "[i] EXECUTION TIME | " + name + ": " +
                (value != -1 ? value + "ms" : Log.UNAVAILABLE);
    }

    public String toCSV() {
        String type = "EXECUTION TIME";
        return type + "," + name + "," + value + "ms," + description;
    }
}
