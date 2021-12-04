package qengine.program.logs;

import qengine.program.models.ExecutionTime;
import qengine.program.models.FilePath;
import qengine.program.utils.Utils;

import java.io.*;

public class Log {

    public static boolean isVerbose = false;

    /**
     * Output configuration
     */
    public static String FOLDER = "output";
    private static final String FILENAME = "qengine_logs";
    private static final String FILE_EXTENSION = "csv";


    /**
     * Return the relative path to the log file
     */
    public static String getOutputPath() {
        return FOLDER + "/" + FILENAME + "." + FILE_EXTENSION;
    }

    /**
     * Init file|buffer|print writer
     */
    static FileWriter FILE_WRITER;
    static BufferedWriter BUFFERED_WRITER;
    static PrintWriter OUTPUT_FILE;


    /**
     * FILE && DIRECTORY
     */
    public static FilePath FILE_DATA = new FilePath("Data", "The data file given");
    public static FilePath FILE_QUERY = new FilePath("Query", "The query file given");
    public static FilePath FOLDER_WORKING_DIR =  new FilePath("Working Directory", "The working directory containing data and query files", "FOLDER");

    /**
     * EXECUTION TIMER
     */
    public static ExecutionTime EXEC_TIME_DICTIONARY = new ExecutionTime("Dictionary", "Execution time to instantiate the dictionary");
    public static ExecutionTime EXEC_TIME_INDEXATION = new ExecutionTime("Indexation", "Execution time to instantiate indexes");
    public static ExecutionTime EXEC_TIME_QUERY = new ExecutionTime("Query", "Execution time to evaluate queries");
    public static ExecutionTime EXEC_TIME_MAIN = new ExecutionTime("Main", "Execution time of the main program");


    /**
     * Default value if the data value is not available
     */
    public static String UNAVAILABLE = "NOT_AVAILABLE";

    public static void initFileWriter() throws IOException {
        try {
            File file = new File(FOLDER);

            // Create the output folder if it doesn't exist
            if (!file.exists() && !file.mkdirs()) {
                System.err.println("[!] Cannot created output folder");
            } else {
                FILE_WRITER = new FileWriter(getOutputPath());
                BUFFERED_WRITER = new BufferedWriter(FILE_WRITER);
                OUTPUT_FILE = new PrintWriter(BUFFERED_WRITER);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setIsVerbose(boolean isVerbose) {
        Log.isVerbose = isVerbose;
    }

    public static void setFOLDER(String FOLDER) throws IOException {
        Log.FOLDER = FOLDER;
    }

    public static void setFileData(String path) {
        Log.FILE_DATA.setPath(path);
    }

    public static void setFileQuery(String path) {
        Log.FILE_QUERY.setPath(path);
    }

    public static void setFolderWorkingDir(String path) {
        Log.FOLDER_WORKING_DIR.setPath(path);
    }

    public static void setExecTimeDictionary(long execTimeDictionary) {
        EXEC_TIME_DICTIONARY.setValue(execTimeDictionary);
    }

    public static void setExecTimeIndexation(long execTimeIndexation) {
        EXEC_TIME_INDEXATION.setValue(execTimeIndexation);
    }

    public static void setExecTimeQuery(long execTimeQuery) {
        EXEC_TIME_QUERY.setValue(execTimeQuery);
    }

    public static void setExecTimeMain(long execTimeMain) {
        EXEC_TIME_MAIN.setValue(execTimeMain);
    }

    public static void write(String text) {
        OUTPUT_FILE.println(text);
    }
    // ----------------------------------------------------------------------

    /**
     * Save all the logs to the output file
     */
    public static void save() throws IOException {
        displayAllLogs();

        write(csvHeader());
        if (!FOLDER_WORKING_DIR.getPath().isEmpty()) {
            write(FOLDER_WORKING_DIR.toCSV());
        }
        write(FILE_DATA.toCSV());
        write(FILE_QUERY.toCSV());
        write(EXEC_TIME_DICTIONARY.toCSV());
        write(EXEC_TIME_INDEXATION.toCSV());
        write(EXEC_TIME_QUERY.toCSV());
        write(EXEC_TIME_MAIN.toCSV());

        System.out.println("\n[+] Logs have been successfully saved on: " + getOutputPath());
        Utils.displayHLINE();
        closeFileBuffer();
    }

    public static String csvHeader() {
        return "Type,Key,Value,Description";
    }

    /**
     * Display all the logs on the console
     */
    public static void displayAllLogs() {
        System.out.println("\n\n# LOGS " + Utils.HLINE);
        if (!FOLDER_WORKING_DIR.getPath().isEmpty()) {
            System.out.println(FOLDER_WORKING_DIR);
        }
        System.out.println(FILE_DATA);
        System.out.println(FILE_QUERY);
        System.out.println(EXEC_TIME_DICTIONARY);
        System.out.println(EXEC_TIME_INDEXATION);
        System.out.println(EXEC_TIME_QUERY);
        System.out.println(EXEC_TIME_MAIN);
    }

    /**
     * Close properly all file reader
     */
    public static void closeFileBuffer() throws IOException {
        OUTPUT_FILE.close();
        BUFFERED_WRITER.close();
        FILE_WRITER.close();
    }
}
