package qengine.program;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import qengine.program.Dictionary;
import qengine.program.Indexation;
import qengine.program.logs.Log;
import qengine.program.parsers.DictionaryRDFHandler;
import qengine.program.parsers.IndexationRDFHandler;
import qengine.program.parsers.MainRDFHandler;
import qengine.program.utils.Utils;

import static java.lang.System.exit;
import static java.lang.System.setErr;

final class Main {
	static final String baseURI = null;
	static String queryFilename = "";
	static String dataFilename = "";

	/**
	 * Votre répertoire de travail où vont se trouver les fichiers à lire
	 */
	static String workingDir = "";

	/**
	 * Fichier contenant les requêtes sparql
	 */
	static String queryFile;

	/**
	 * Fichier contenant des données rdf
	 */
	static String dataFile;

	static final Dictionary dictionary = Dictionary.getInstance();

	static final Indexation indexation = Indexation.getInstance();
	// ========================================================================

	/**
	 * Méthode utilisée ici lors du parsing de requête sparql pour agir sur l'objet obtenu.
	 */
	public static void processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

		System.out.println("first pattern : " + patterns.get(0));

		System.out.println("object of the first pattern : " + patterns.get(0).getObjectVar().getValue());

		System.out.println("variables to project : ");

		// Utilisation d'une classe anonyme
		query.getTupleExpr().visit(new AbstractQueryModelVisitor<RuntimeException>() {

			public void meet(Projection projection) {
				System.out.println(projection.getProjectionElemList().getElements());
			}
		});
	}

	/**
	 * Entrée du programme
	 */
	public static void main(String[] args) throws Exception {
		handleArguments(args);

		System.out.println("# Parsing data ----------------------------------------");
		parseData();

		System.out.println("# Parsing queries ----------------------------------------");
		parseQueries();

		// Display on the console and save the logs
		Log.save();
	}

	// ========================================================================

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
				String optionValue = args[i+1];

				// Check the next value
				optionValue = checkOptionValue(optionName, optionValue);
				applyArgument(optionName, optionValue);

				i++;
			}
		}

		// Set the path to the files
		queryFile = workingDir + "/" + queryFilename;
		dataFile = workingDir + "/" + dataFilename;

		// Init log writers
		Log.initFileWriter();
	}

	public static void applyArgument(String option, String value) throws IOException {
		switch (option) {
			case "-workingDir":
				workingDir = value;
				break;
			case "-queries":
				queryFilename = value;
				break;
			case "-data":
				dataFilename = value;
				break;
			case "-output":
				Log.setFOLDER(value);
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
		System.out.println("\n[i] Usage example");
		System.out.println("\t java -jar qengine.jar -data ~/data/sample_data.nt -queries ~/data/sample_query.queryset");
		System.out.println("\t java -jar qengine.jar -workingDir ~/data -data sample_data.nt -queries sample_query.queryset");
	}

	/**
	 * Traite chaque requête lue dans {@link #queryFile} avec {@link #processAQuery(ParsedQuery)}.
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
		try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
			SPARQLParser sparqlParser = new SPARQLParser();
			Iterator<String> lineIterator = lineStream.iterator();
			StringBuilder queryString = new StringBuilder();

			while (lineIterator.hasNext())
			/*
			 * On stocke plusieurs lignes jusqu'à ce que l'une d'entre elles se termine par un '}'
			 * On considère alors que c'est la fin d'une requête
			 */
			{
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
	 * Traite chaque triple lu dans {@link #dataFile} avec {@link MainRDFHandler}.
	 */
	private static void parseData() throws FileNotFoundException, IOException {

		// Utiliser pour stocker le temps de départ et de fin d'évaluation
		long startTimer;
		long endTimer;

		// Mise en place du dictionnaire --------------------------------------------------
		try (Reader dataReader = new FileReader(dataFile)) {
			RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

			DictionaryRDFHandler dictionaryRDFHandler = new DictionaryRDFHandler();
			rdfParser.setRDFHandler(dictionaryRDFHandler);

			startTimer = Utils.getCurrentTime();
			rdfParser.parse(dataReader, baseURI);

			endTimer = System.currentTimeMillis() - startTimer;
			System.out.println("[+] Dictionary done! ("+endTimer+"ms)");
			Log.setExecTimeDictionary(endTimer);
		}
		// --------------------------------------------------------------------------------

		// Mise en place de l'indexation --------------------------------------------------
		try (Reader dataReader = new FileReader(dataFile)) {
			RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

			IndexationRDFHandler indexationRDFHandler = new IndexationRDFHandler();
			rdfParser.setRDFHandler(indexationRDFHandler);

			startTimer = Utils.getCurrentTime();
			rdfParser.parse(dataReader, baseURI);

			endTimer = System.currentTimeMillis() - startTimer;
			System.out.println("[+] Indexation done! ("+endTimer+"ms)");
			Log.setExecTimeIndexation(endTimer);
		}
		// --------------------------------------------------------------------------------
	}
}
