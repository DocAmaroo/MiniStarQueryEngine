package qengine.program.models;

public class FilePath {
    private String name;
    private String description;
    private String type;
    private String path = "";

    public FilePath(String name, String description) {
        this.name = name;
        this.description = description;
        this.type = "FILE";
    }

    public FilePath(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toCSV() {
        return type + "," + name + "," + path + "," + description;
    }

    @Override
    public String toString() {
        return "[i] "+ type + " | " + name + ": " + path;
    }
}
