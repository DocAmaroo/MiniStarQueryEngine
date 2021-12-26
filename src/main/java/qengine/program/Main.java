package qengine.program;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import qengine.program.logs.Log;
import qengine.program.models.Query;
import qengine.program.models.Triplet;
import qengine.program.parsers.DictionaryRDFHandler;
import qengine.program.parsers.IndexationRDFHandler;
import qengine.program.parsers.MainRDFHandler;
import qengine.program.utils.Utils;

import static java.lang.System.exit;

final class Main {
    private static Boolean WARM_UP = true;
    private static final String baseURI = null;

    /**
     * Path to the working directory, the query file and the data file.
     */
    public static String workingDir = "";
    private static String queryPath;
    private static String dataPath;

    /**
     * Instance of the dictionary and the indexes
     */
    private static Dictionary dictionary;
    private static Indexation indexation;

    /**
     * Query attributs
     */
    private static long nbQueryWithResponse = 0;
    private static ArrayList<Query> queries = new ArrayList<>();

    /**
     * Store the number of queries split by the number of condition in them
     * ex: key=1 represent the number of queries with 1 conditions, i=1 with 2 conditions etc...
     */
    private static HashMap<Integer, Integer> nbQueriesByNTriplets = new HashMap<>();
    private static HashMap<Integer, ArrayList<Integer>> queriesIdByNTriplets = new HashMap<>();

    // ========================================================================

    /**
     * Entrée du programme
     */
    public static void main(String[] args) throws Exception {
        int nbDuplicates = 0;

        // Utiliser pour stocker le temps de départ et de fin d'évaluation
        long mainExecutionTime = System.currentTimeMillis();
        long startTimer;
        long endTimer;

        // For verbose only
        StringBuilder strBuilder = new StringBuilder();

        // Start by handling arguments give
        handleArguments(args);

        // Parse the file containing the data
        System.out.println("[i] Parsing data...");
        startTimer = System.currentTimeMillis();
        parseData();
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Parsing data done (" + endTimer + "ms)");
        System.out.println(Utils.HLINE);

        // Parse the file containing all the query
        System.out.println("[i] Parsing queries...");
        startTimer = System.currentTimeMillis();
        parseQueries(queryPath);
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Parsing queries done (" + endTimer + "ms)");
        System.out.println(Utils.HLINE);

        // Remove duplicates
        nbDuplicates = queries.size();
        queries = removeDuplicateQuery();
        nbDuplicates -= queries.size();


        // Warm up
        if (WARM_UP) {
            setQueriesByNTriplets();

            System.out.println("[i] Warm up...");
            startTimer = System.currentTimeMillis();
            warmup();
            endTimer = System.currentTimeMillis() - startTimer;
            System.out.println("[+] Warm up done (" + endTimer + "ms)");
            System.out.println(Utils.HLINE);
        }

        // Fetch all queries
        System.out.println("[i] Fetching ...");
        startTimer = System.currentTimeMillis();
        fetchQuery();
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Fetching done (" + endTimer + "ms)");


        // Logs only
        Log.setExecTimeQuery(endTimer);
        Log.setExecTimeMain(System.currentTimeMillis() - mainExecutionTime);
        Log.save();

        System.out.println("[i] More informations: ");
        System.out.println("\t* Total of query: " + queries.size());
        System.out.println("\t* Number of query with response: " + nbQueryWithResponse);
        System.out.println("\t* Number of query without response " + (queries.size() - nbQueryWithResponse));
        System.out.println("\t* Number of query duplicate " + nbDuplicates);
        if (WARM_UP) System.out.println("\t* Number of query by number of conditions {nbConditions=nbQuery} \n" + nbQueriesByNTriplets);
    }

    // ========================================================================


    /**
     * Traite chaque triple lu dans {@link #dataPath} avec {@link MainRDFHandler}.
     */
    private static void parseData() throws IOException {

        // For verbose only
        StringBuilder strBuilder = new StringBuilder();

        // Utiliser pour stocker le temps de départ et de fin d'évaluation
        long startTimer;
        long endTimer;

        // Mise en place du dictionnaire --------------------------------------------------
        DictionaryRDFHandler dicoRDF = new DictionaryRDFHandler();
        startTimer = System.currentTimeMillis();
        parse(dicoRDF);
        endTimer = System.currentTimeMillis() - startTimer;
        strBuilder.append("[+] Dictionary done! (").append(endTimer).append("ms)");
        Log.setExecTimeDictionary(endTimer);

        dictionary = dicoRDF.getDico();
        // --------------------------------------------------------------------------------


        // Mise en place de l'indexation --------------------------------------------------
        IndexationRDFHandler indexRDF = new IndexationRDFHandler(dictionary);
        startTimer = System.currentTimeMillis();
        parse(indexRDF);
        endTimer = System.currentTimeMillis() - startTimer;
        strBuilder.append("[+] Indexation done! (").append(endTimer).append("ms)");
        Log.setExecTimeIndexation(endTimer);

        indexation = indexRDF.getIndex();
        // --------------------------------------------------------------------------------
    }

    private static void parse(AbstractRDFHandler abstractRDFHandler) throws IOException {
        File rep = new File(dataPath);

        if (rep.isDirectory()) {
            String[] fileList;

            if ((fileList = rep.list()) != null) {
                for (String file : fileList) {
                    parseDataFile(abstractRDFHandler, file);
                }
            }
        } else {
            parseDataFile(abstractRDFHandler, dataPath);
        }
    }

    public static void parseDataFile(AbstractRDFHandler abstractRDFHandler, String filename) throws IOException {
        try (Reader dataReader = new FileReader(filename)) {
            // On va parser des données au format ntriples
            RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

            // On utilise notre implémentation de handler
            rdfParser.setRDFHandler(abstractRDFHandler);

            // Parsing et traitement de chaque triple par le handler
            rdfParser.parse(dataReader, baseURI);
        }
    }

    /**
     * Traite chaque requête lue dans {@link #queryPath} avec {@link #processAQuery(ParsedQuery)}.
     */
    private static void parseQueries(String filePath) {
        File rep = new File(filePath);

        if (rep.isDirectory()) {
            Log.FILE_QUERY.setType("FOLDER");
            List<String> files;

            if (rep.list() != null) {
                files = Arrays.stream(rep.list())
                        .filter(file -> file.endsWith(".queryset"))
                        .collect(Collectors.toList());

                if (files.isEmpty()) {
                    System.err.println("[!] Aucun fichier de query (.queryset) est présent dans le dossier spécifié");
                    exit(0);
                }
                for (String file : files) {
                    parseQueriesFile(queryPath + "/" + file);
                }
            }
        } else {
            if (filePath.endsWith(".queryset")) {
                parseQueriesFile(filePath);
            } else {
                System.err.println("[!] Le fichier de query spécifié est invalide (suffixe .queryset non reconnus)");
                exit(0);
            }
        }
    }

    private static void parseQueriesFile(String file) {
        try (Stream<String> lineStream = Files.lines(Paths.get(file))) {
            SPARQLParser sparqlParser = new SPARQLParser();
            Iterator<String> lineIterator = lineStream.iterator();
            StringBuilder queryString = new StringBuilder();

            /*
             * On stocke plusieurs lignes jusqu'à ce que l'une d'entre elles se termine par un '}'
             * On considère alors que c'est la fin d'une requête
             */
            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                queryString.append(line);

                if (line.trim().endsWith("}")) {
                    ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);

                    processAQuery(query); // Traitement de la requête, à adapter/réécrire pour votre programme

                    queryString.setLength(0); // Reset le buffer de la requête en chaine vide
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
     */
    public static void processAQuery(ParsedQuery query) {
        Query q = new Query();

        List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

        for (StatementPattern pattern : patterns) {
            String subject = pattern.getSubjectVar().getName();
            String predicate = pattern.getPredicateVar().getValue().stringValue();
            String object = pattern.getObjectVar().getValue().stringValue();
            Triplet triplet = new Triplet(subject, predicate, object);

            q.addTriplet(triplet);
        }

        queries.add(q);
    }

    private static void setQueriesByNTriplets() {
        for (Query query : queries) {
            int nbTriplet = query.getNbTriplet();
            if (!nbQueriesByNTriplets.containsKey(nbTriplet)) {
                nbQueriesByNTriplets.put(nbTriplet, 1);
                queriesIdByNTriplets.put(nbTriplet, new ArrayList<>(List.of(queries.size() - 1)));
            } else {
                nbQueriesByNTriplets.put(nbTriplet, nbQueriesByNTriplets.get(nbTriplet) + 1);
                queriesIdByNTriplets.get(nbTriplet).add(queries.size() - 1);
            }
        }
    }

    private static void fetchQuery() {
        for (Query query : queries) {
            if (Log.isVerbose) {
                System.out.println("\n[i] Fetching... \n" + query.toString());
            }

            TreeSet<Integer> response = query.fetch(dictionary, indexation);

            if (response == null || response.isEmpty()) {
                if (Log.isVerbose) System.out.println("\n[i] Cannot found a response to this query");
            } else {
                nbQueryWithResponse++;

                if (Log.isVerbose) {
                    System.out.println("\n[i] Query response:");
                    for (int key : response) {
                        System.out.println("\n\t* " + dictionary.getWordByKey(key));
                    }
                }
            }
            if (Log.isVerbose) System.out.println(Utils.HLINE);
        }
    }

    private static void warmup() {
        int nbQueryToFetch = (int) Math.ceil(queries.size() * 0.05);

        for (int i = 0; i < queriesIdByNTriplets.size(); i++) {
            ArrayList<Integer> queriesToFetch = queriesIdByNTriplets.get(i+1);

            int j=0;
            while(j < nbQueryToFetch && j < queriesToFetch.size()) {
                int queryIndex = queriesToFetch.get(j);
                queries.get(queryIndex).fetch(dictionary, indexation);
                j++;
            }
        }
    }

    public static void handleArguments(String[] args) throws IOException {

        // Use a List for ease of use
        List<String> argsToList = Arrays.asList(args);
        int index = -1;

        if (argsToList.contains("-help")) {
            Utils.help();
            exit(0);
        }

        ArrayList<String> mandatoryOptions = new ArrayList<>(Arrays.asList("-queries", "-data"));
        if (!argsToList.containsAll(mandatoryOptions)) {
            System.err.println("[!] No query or/and data file give\n[i] Use -help for more information");
            exit(0);
        }

        for (int i = 0; i < args.length; i++) {

            // If an option is found
            if (args[i].startsWith("-")) {
                String optionName = args[i];

                if (optionName.equals("-verbose")) {
                    applyArgument(optionName, "");
                } else {

                    String optionValue = args[i + 1];

                    // Check the next value
                    optionValue = Utils.checkOptionValue(optionName, optionValue);
                    applyArgument(optionName, optionValue);
                    i++;
                }
            }
        }

        // Set the path to the files
        dataPath = workingDir + "/" + dataPath;
        queryPath = workingDir + "/" + queryPath;

        // Init log writers
        Log.initFileWriter();
    }

    public static void applyArgument(String option, String value) throws IOException {
        switch (option) {
            case "-workingDir":
                workingDir = value;
                Log.setFolderWorkingDir(workingDir);
                break;
            case "-queries":
                queryPath = value;
                Log.setFileQuery(queryPath);
                break;
            case "-data":
                dataPath = value;
                Log.setFileData(dataPath);
                break;
            case "-output":
                Log.setFOLDER(value);
                break;
            case "-verbose":
                Log.setIsVerbose(true);
                break;
            case "-warmup":
                WARM_UP = !value.equals("f");
        }
    }

    public static ArrayList<Query> removeDuplicateQuery() {
        ArrayList<Query> noDuplicate = new ArrayList<>();
        ArrayList<Integer> toExclude = new ArrayList<>();

        for (int i = 0; i < queries.size(); i++) {
            if (!toExclude.contains(i)) {
                Query query = queries.get(i);
                noDuplicate.add(query);

                for (int j = i + 1; j < queries.size(); j++) {
                    if (query.isEqual(queries.get(j))) {
                        toExclude.add(j);
                    }
                }
            }
        }

        return noDuplicate;
    }
}