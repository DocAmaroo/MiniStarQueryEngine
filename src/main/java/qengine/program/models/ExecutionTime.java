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

    public void setValue(long value) {
        this.value = value;
    }

    public long toSecond() { return this.value * 1000; }

    @Override
    public String toString() {
        return "[i] EXECUTION TIME | " + name + ": " +
                (value != -1 ? toSecond() + "s" : Log.UNAVAILABLE);
    }

    public String toCSV() {
        String type = "EXECUTION TIME";
        return type + "," + name + "," + toSecond() + "s";
    }
}
