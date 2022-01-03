package qengine.program.utils;

import static java.lang.System.exit;

public class Utils {

    public static String HLINE = "-----------------------------------------------------------";

    public static void displayHLINE() {
        System.out.println(HLINE + "\n");
    }

    public static String checkOptionValue(String option, String value) {
        if (value.startsWith("-")) {
            System.err.println("[!] The value of the option " + option + " is incorrect.");
            System.err.println("\t Value received: " + value);
            exit(0);
        }

        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

    public static void help() {
        System.out.println("java -jar <path/to/qengine.jar> [OPTIONS]");
        System.out.println("\n[i] See available options below:");
        System.out.println("\t -help --> show this message");
        System.out.println("\t -workingDir <path/to/dir> --> path to the directory containing queries or/and data. This value is optional");
        System.out.println("\t -queries <path/to/file> --> absolute path to the queries file, or the relative from a working directory specified");
        System.out.println("\t -data <path/to/file> --> absolute path to the data file, or the relative from a working directory specified");
        System.out.println("\t -output <path/to/dir> --> set the log output directory. By default is <path/to/qengine.jar>/output");
        System.out.println("\t -rmd filename --> save on the path give the queries without duplicates. By default save on workingDir/noDuplicates/filename");
        System.out.println("\t -verbose --> print all information during execution process on the console. (tips: doesn't affect logs output)");
        System.out.println("\t -jena --> execute Jena on the data and queries given.");
        System.out.println("\t -nowarmup --> allow to desactivate the warmup");
        System.out.println("\n[i] Usage example");
        System.out.println("\t java -jar qengine.jar -data ~/data/sample_data.nt -queries ~/data/sample_query.queryset -verbose");
        System.out.println("\t java -jar qengine.jar -workingDir ~/data -data sample_data.nt -queries sample_query.queryset -nowarmup -jena");
    }
}
