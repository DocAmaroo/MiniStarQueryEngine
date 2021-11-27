package qengine.program;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
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
import qengine.program.models.Clause;
import qengine.program.parsers.DictionaryRDFHandler;
import qengine.program.parsers.IndexationRDFHandler;
import qengine.program.parsers.MainRDFHandler;
import qengine.program.utils.Utils;

import static java.lang.System.exit;

final class Main {
	private static final String baseURI = null;

	/**
	 * Votre répertoire de travail où vont se trouver les fichiers à lire
	 */
	public static String workingDir = "";

	/**
	 * Nom du fichier contenant les requêtes
	 */
	public static String queryFilename = "";

	/**
	 * Chemin du fichier contenant les requêtes sparql
	 */
	private static String queryFilePath;

	/**
	 * Fichier contenant des données rdf
	 */
	public static String dataFilename = "";

	/**
	 * Chemin du fichier contenant les données
	 */
	private static String dataFilePath;


	/**
	 * Instance of the dictionary and the indexes
	 */
	private static final Dictionary dictionary = Dictionary.getInstance();
	private static final Indexation indexation = Indexation.getInstance();
	// ========================================================================

	/**
	 * Entrée du programme
	 */
	public static void main(String[] args) throws Exception {
		long mainExecutionTime = System.currentTimeMillis();

		// For verbose only
		StringBuilder strBuilder = new StringBuilder();

		// Utiliser pour stocker le temps de départ et de fin d'évaluation
		long startTimer;
		long endTimer;

		handleArguments(args);

		System.out.println("# Parsing data " + Utils.HLINE);
		parseData();

		System.out.println("# Parsing queries " + Utils.HLINE);

		startTimer = System.currentTimeMillis();
		parseQueries();
		endTimer = System.currentTimeMillis() - startTimer;
		strBuilder.append("[+] Queries done! (").append(endTimer).append("ms)");
		Log.setExecTimeQuery(endTimer);


		// Logs only
		mainExecutionTime = System.currentTimeMillis() - mainExecutionTime;
		Log.setExecTimeMain(mainExecutionTime);

		// Display on the console and save the logs
		Log.save();

	}

	// ========================================================================


	/**
	 * Traite chaque triple lu dans {@link #dataFilePath} avec {@link MainRDFHandler}.
	 */
	private static void parseData() throws FileNotFoundException, IOException {

		// For verbose only
		StringBuilder strBuilder = new StringBuilder();

		// Utiliser pour stocker le temps de départ et de fin d'évaluation
		long startTimer;
		long endTimer;

		// Mise en place du dictionnaire --------------------------------------------------
		startTimer = System.currentTimeMillis();
		parse(new DictionaryRDFHandler());
		endTimer = System.currentTimeMillis() - startTimer;
		strBuilder.append("[+] Dictionary done! (").append(endTimer).append("ms)");
		Log.setExecTimeDictionary(endTimer);
		// --------------------------------------------------------------------------------

		// Mise en place de l'indexation --------------------------------------------------
		startTimer = System.currentTimeMillis();
		parse(new IndexationRDFHandler());
		endTimer = System.currentTimeMillis() - startTimer;
		strBuilder.append("[+] Indexation done! (").append(endTimer).append("ms)");
		Log.setExecTimeIndexation(endTimer);
		// --------------------------------------------------------------------------------
	}

	private static void parse(AbstractRDFHandler abstractRDFHandler) throws FileNotFoundException, IOException {
		try (Reader dataReader = new FileReader(dataFilePath)) {
			// On va parser des données au format ntriples
			RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

			// On utilise notre implémentation de handler
			rdfParser.setRDFHandler(abstractRDFHandler);

			// Parsing et traitement de chaque triple par le handler
			rdfParser.parse(dataReader, baseURI);
		}
	}

	/**
	 * Traite chaque requête lue dans {@link #queryFilePath} avec {@link #processAQuery(ParsedQuery)}.
	 */
	private static void parseQueries() throws FileNotFoundException, IOException {
		/**
		 * Try-with-resources
		 * 
		 * @see <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">Try-with-resources</a>
		 */
		/*
		 * On utilise un stream pour lire les lignes une par une, sans avoir à toutes les stocker
		 * entièrement dans une collection.
		 */
		try (Stream<String> lineStream = Files.lines(Paths.get(queryFilePath))) {
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
		}
	}

	/**
	 * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
	 */
	public static void processAQuery(ParsedQuery query) {
		Query q = new Query();

		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

		for (StatementPattern pattern : patterns) {
			String subject = pattern.getSubjectVar().getName().toString();
			String predicate = pattern.getPredicateVar().getValue().stringValue();
			String object = pattern.getObjectVar().getValue().stringValue();
			Clause whereClause = new Clause(subject, predicate, object);

			q.addWhereClause(whereClause);
		}

		// For verbose only
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("\n[i] Fetching... \n").append(q.toString());

		TreeSet<Integer> response = q.fetch(dictionary);

		if (response == null || response.isEmpty()) {
			strBuilder.append("\n[i] Cannot found a response to this query");
		}
		else {
			strBuilder.append("\n[i] Query response:");
			for (int key : response) {
				strBuilder.append("\n\t* ").append(dictionary.getWordByKey(key));
			}
		}

		strBuilder.append("\n").append(Utils.HLINE);
		if (Log.isVerbose) System.out.println(strBuilder.toString());


//		System.out.println("variables to project : ");

		// Utilisation d'une classe anonyme
//		query.getTupleExpr().visit(new AbstractQueryModelVisitor<RuntimeException>() {
//
//			public void meet(Projection projection) {
//				List<ProjectionElem> elements = projection.getProjectionElemList().getElements();
//
//				System.out.println("[i] Element " + Utils.HLINE);
//				System.out.println(elements);
//				System.out.println(Utils.HLINE);
//			}
//		});
	}

	public static void handleArguments(String[] args) throws IOException {

		// Use a List for ease of use
		List<String> argsToList = Arrays.asList(args);
		int index = -1;

		if (argsToList.contains("-help")) {
			help();
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

					String optionValue = args[i+1];

					// Check the next value
					optionValue = checkOptionValue(optionName, optionValue);
					applyArgument(optionName, optionValue);
					i++;
				}
			}
		}

		// Set the path to the files
		queryFilePath = workingDir + "/" + queryFilename;
		dataFilePath = workingDir + "/" + dataFilename;

		// Init log writers
		Log.initFileWriter();
	}

	public static void applyArgument(String option, String value) throws IOException {
		switch (option) {
			case "-workingDir":
				workingDir = value;
				Log.setWorkingDirectory(workingDir);
				break;
			case "-queries":
				queryFilename = value;
				Log.setQueryFileName(queryFilename);
				break;
			case "-data":
				dataFilename = value;
				Log.setDataFileName(dataFilename);
				break;
			case "-output":
				Log.setFOLDER(value);
				break;
			case "-verbose":
				Log.setIsVerbose(true);
				break;
		}
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
		System.out.println("\t -verbose --> print all information during execution process on the console. (tips: doesn't affect logs output)");
		System.out.println("\n[i] Usage example");
		System.out.println("\t java -jar qengine.jar -data ~/data/sample_data.nt -queries ~/data/sample_query.queryset");
		System.out.println("\t java -jar qengine.jar -workingDir ~/data -data sample_data.nt -queries sample_query.queryset");
	}

}
