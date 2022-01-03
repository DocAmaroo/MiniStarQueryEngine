package qengine.program;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.VCARD;
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

    /**
     * Path to the working directory, the query file and the data file.
     */
    public static String workingDir = "";
    private static String queryPath;
    private static String dataPath;

    private static Boolean EXEC_JENA = false;
    private static Boolean WARM_UP = true;
    private static Boolean REMOVE_DUPLICATES = false;
    private static String FOLDER_NO_DUPLICATES = "noDuplicates";
    private static String FILE_NO_DUPLICATES_NAME = "noDuplicates";
    private static final String baseURI = "http://www.w3.org/";

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
    private static HashMap<Integer, ArrayList<Query>> queriesSortByNbTriplet = new HashMap<>();

    /** Jena
     *
     */
    private static Model model;
    private static ArrayList<org.apache.jena.query.Query> jQueries = new ArrayList<>();

    // ========================================================================

    /**
     * Entrée du programme
     */
    public static void main(String[] args) throws Exception {
        int nbDuplicates = 0;

        // Utiliser pour stocker le temps de départ et de fin d'évaluation
        long mainExecutionTime = System.currentTimeMillis();
        long startStep, endStep;
        long startTimer, endTimer;

        // For verbose only
        StringBuilder strBuilder = new StringBuilder();

        // Start by handling arguments give
        handleArguments(args);


        // ======================================
        // JENA
        if (EXEC_JENA) {
            startStep = System.currentTimeMillis();
            executeJena();
            endStep = System.currentTimeMillis() - startStep;
            System.out.println("[i] Jena complete (" + endStep + "ms)");
            System.out.println(Utils.HLINE);
        }


        // ======================================
        // Our engine
        startStep = System.currentTimeMillis();

        // Handle data
        System.out.println("[i] Parsing data...");
        startTimer = System.currentTimeMillis();
        parseData();
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Parsing data done (" + endTimer + "ms)");
        System.out.println(Utils.HLINE);

        // Handle queries
        System.out.println("[i] Parsing queries...");
        startTimer = System.currentTimeMillis();
        parseQueries(queryPath, false);
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Parsing queries done (" + endTimer + "ms)");
        System.out.println(Utils.HLINE);

        // Remove duplicates
        if (REMOVE_DUPLICATES) {

            System.out.println("[i] Removing duplicates");

            int saveTotalQuery = getTotalQuery();
            startTimer = System.currentTimeMillis();
            removeDuplicateQuery();
            endTimer = System.currentTimeMillis() - startTimer;
            nbDuplicates = saveTotalQuery - queries.size();

            FileWriter fileWriter;
            BufferedWriter bufferedWriter;
            PrintWriter outputFile;

            try {
                if (!workingDir.isBlank()) FOLDER_NO_DUPLICATES = workingDir + "/" + FOLDER_NO_DUPLICATES;
                File file = new File(FOLDER_NO_DUPLICATES);

                // Create the output folder if it doesn't exist
                if (!file.exists() && !file.mkdirs()) {
                    System.err.println("[!] Cannot created output folder");
                } else {
                    String absoluteFilePath = FOLDER_NO_DUPLICATES + "/" + FILE_NO_DUPLICATES_NAME + ".queryset";
                    fileWriter = new FileWriter(absoluteFilePath);
                    bufferedWriter = new BufferedWriter(fileWriter);
                    outputFile = new PrintWriter(bufferedWriter);

                    System.out.println("[i] Writing the new file...");
                    for (Query query : queries) {
                        outputFile.write(query.toString());
                    }
                    System.out.println("[+] New file has been created at: " + absoluteFilePath);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("[+] Removing duplicates done (" + endTimer + "ms)");
            System.out.println(Utils.HLINE);
        }

        // Warm up
        if (WARM_UP) {
            System.out.println("[i] Warm up...");
            //setQueriesByNTriplets();

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
        System.out.println(Utils.HLINE);

        endStep = System.currentTimeMillis() - startStep;
        System.out.println("[i] Local engine complete (" + endStep + "ms)");
        System.out.println(Utils.HLINE);


        // Logs only
        Log.setExecTimeQuery(endTimer);
        Log.setExecTimeMain(System.currentTimeMillis() - mainExecutionTime);
        Log.save();

        System.out.println("[i] More informations: ");
        System.out.println("\t* Total of query: " + queries.size());
        System.out.println("\t* Number of query with response: " + nbQueryWithResponse);
        System.out.println("\t* Number of query without response " + (queries.size() - nbQueryWithResponse));
        if (REMOVE_DUPLICATES)
            System.out.println("\t* Number of query duplicate " + nbDuplicates);
        if (WARM_UP)
            System.out.println("\t* Number of query by number of conditions {nbConditions=nbQuery} \n" + nbQueriesByNTriplets);
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
            RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);

            // On utilise notre implémentation de handler
            rdfParser.setRDFHandler(abstractRDFHandler);

            // Parsing et traitement de chaque triple par le handler
            rdfParser.parse(dataReader, baseURI);
        }
    }

    /**
     * Traite chaque requête lue dans {@link #queryPath} avec {@link #processAQuery(ParsedQuery)}.
     */
    private static void parseQueries(String filePath, boolean flag) {
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
                    parseQueriesFile(queryPath + "/" + file, flag);
                }
            }
        } else {
            if (filePath.endsWith(".queryset")) {
                parseQueriesFile(filePath, flag);
            } else {
                System.err.println("[!] Le fichier de query spécifié est invalide (suffixe .queryset non reconnus)");
                exit(0);
            }
        }
    }

    private static void parseQueriesFile(String file, boolean flag) {
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
                    if (flag) {
                        // Using Jena
                        jQueries.add(QueryFactory.create(queryString.toString()));
                    } else {
                        // Process the query
                        ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);
                        processAQuery(query);
                    }

                    // Reset le buffer de la requête en chaine vide
                    queryString.setLength(0);
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

        if (!REMOVE_DUPLICATES) queries.add(q);

        int nbTriplet = q.getNbTriplet();
        if (!queriesSortByNbTriplet.containsKey(nbTriplet)) {
            queriesSortByNbTriplet.put(nbTriplet, new ArrayList<>(List.of(q)));
            nbQueriesByNTriplets.put(nbTriplet, 1);
        } else {
            queriesSortByNbTriplet.get(nbTriplet).add(q);
            nbQueriesByNTriplets.put(nbTriplet, nbQueriesByNTriplets.get(nbTriplet) + 1);
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

    private static void fetchQueryWithJena() {
        for (org.apache.jena.query.Query query : jQueries) {
            QueryExecution qef = QueryExecutionFactory.create(query, model);
            ResultSet res = qef.execSelect();
        }
    }

    private static void warmup() {
        // We want 5% of each type of patrons
        int nbQueryToFetch = (int) Math.ceil(queries.size() * 0.05);

        HashMap<Integer, ArrayList<Query>> sortByNTriplet = new HashMap<>();

        for (Query query : queries) {
            int nbTriplet = query.getNbTriplet();

            if (!sortByNTriplet.containsKey(nbTriplet)) {
                sortByNTriplet.put(nbTriplet, new ArrayList<>(List.of(query)));
            } else if (sortByNTriplet.get(nbTriplet).size() < nbQueryToFetch) {
                sortByNTriplet.get(nbTriplet).add(query);
            }
        }

        for (int i = 0; i < sortByNTriplet.size(); i++) {
            ArrayList<Query> queries = sortByNTriplet.get(i + 1);
            for (Query query : queries) {
                query.fetch(dictionary, indexation);
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

                if (optionName.equals("-verbose") || optionName.equals("-jena")) {
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
                break;
            case "-rmd":
                REMOVE_DUPLICATES = true;
                FILE_NO_DUPLICATES_NAME = value;
                break;
            case "-jena":
                EXEC_JENA = true;
                break;
        }
    }

    public static void removeDuplicateQuery() {
        for (int i=0; i < queriesSortByNbTriplet.size(); i++) {
            ArrayList<Query> queriesOfNbTriplet = queriesSortByNbTriplet.get(i+1);
            ArrayList<Query> distinctQuery = queriesOfNbTriplet.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
            nbQueriesByNTriplets.put(i+1, distinctQuery.size());
            queries.addAll(distinctQuery);
        }
    }

    public static int getTotalQuery() {
        int res = 0;
        for (int i=0; i < nbQueriesByNTriplets.size(); i++) {
            res += nbQueriesByNTriplets.get(i+1);
        }
        return res;
    }

    public static void executeJena() {
        long startTimer;
        long endTimer;

        // Handle data
        System.out.println("[i] Jena Parsing data...");
        startTimer = System.currentTimeMillis();
        initJenaModel();
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Parsing data done (" + endTimer + "ms)");
        System.out.println(Utils.HLINE);

        // Handle queries
        System.out.println("[i] Jena Parsing queries...");
        startTimer = System.currentTimeMillis();
        parseQueries(queryPath, true);
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Parsing queries  done (" + endTimer + "ms)");
        System.out.println(Utils.HLINE);

        // Fetch all queries
        System.out.println("[i] Fetching with Jena ...");
        startTimer = System.currentTimeMillis();
        fetchQueryWithJena();
        endTimer = System.currentTimeMillis() - startTimer;
        System.out.println("[+] Fetching done (" + endTimer + "ms)");
        System.out.println(Utils.HLINE);
    }

    public static void initJenaModel() {

        // Create Jena Model by reading data file
        model = ModelFactory.createDefaultModel();
        InputStream in = RDFDataMgr.open(dataPath);
        if (in == null) throw new IllegalArgumentException("File: " + dataPath + " not found");
        model.read(in, null);
    }
}