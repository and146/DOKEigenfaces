/**
 * 
 */
package and146.projects.eigenfaces;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.opencv.highgui.Highgui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import and146.projects.eigenfaces.IFaceRecognizer.RecognizerTestScenario;
import and146.projects.eigenfaces.domain.TrainingSet;
import and146.projects.eigenfaces.nativelibs.NativeLibrariesUtils;
import and146.projects.eigenfaces.opencv.OpenCVHelper;
import and146.projects.eigenfaces.persistence.PersistenceManager;
import and146.projects.eigenfaces.persistence.dao.IGenericCRUDDao;
import and146.projects.eigenfaces.persistence.dao.TrainingSetDao;

/**
 * @author neonards
 *
 */
public class EigenfacesApplication {
	
	/* possible command line options */
	public static final Option CLI_OPT_HELP = new Option("h", "help", false, "show this help");
	public static final Option CLI_OPT_TRAINING_SET_XML = new Option("t", "training-set-xml", true, "path to the XML containing the training set");
	public static final Option CLI_OPT_RECOGNIZE_FACE = new Option("r", "recognize-face", true, "path to the image file required to recognize");
	public static final Option CLI_OPT_CLEANUP_DB = new Option("c", "cleanup-database", false, "clean up database containing the last training set");
	public static final Option CLI_OPT_DEBUG = new Option("d", "debug", false, "enables the debug mode (maximal verbosity)");
	public static final Option CLI_OPT_LOG_TO_FILE = new Option("l", "log", true, "enables the logging to a file");
	public static final Option CLI_OPT_TEST_RECOGNIZER_EXIST = new Option("te", "test-recognizer-existing-faces", false, "tests the recognizer's confidence by attempting to recognize each item in the training set");
	public static final Option CLI_OPT_TEST_RECOGNIZER_SKIP = new Option("ts", "test-recognizer-skipped-faces", false, "tests the recognizer's confidence by attempting to recognize one skipped face of each person in the training set");
	public static final Option CLI_OPT_PERSIST_TRAININGSET = new Option("p", "persist-training-set", false, "Persists the provided training set");
	public static final Option CLI_OPT_SAVE_AVERAGE_FACE = new Option("af", "save-average-face", true, "Saves the average face to a specified file.");
	//
	private static Logger log = LoggerFactory.getLogger(EigenfacesApplication.class);
	/* default settings for logger */
	private static String LOG_PATTERN = "%d [%p|%c|%C{1}] %m%n";
	private static Level LOG_LEVEL = Level.INFO;
	//
	private static CommandLine commandLine = null;
	private static Options options = null;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initLogger();
		
		log.info("Starting the application ...");
		log.info(String.format("System info: %s %s", NativeLibrariesUtils.getOperatingSystem(), NativeLibrariesUtils.getArchitecture()));
		
		// parse the command line arguments
		initCli(args);
		
		// update the logger appenders based on CLI settings
		updateLoggerAppenders();
		
		// load OpenCV's native libraries
		OpenCVHelper.loadNativeLibraries();
		
		// init persistence
		PersistenceManager.init();
		
		log.info("Initialization done.");
		System.out.println();
		
		if (getCommandLine().hasOption(CLI_OPT_HELP.getOpt())) {
			printHelp(true);
		}
		
		//
		checkNeccessaryArguments();
		
		proceed();
		
	}
	
	private static void initLogger() {
		BasicConfigurator.configure();
		
		org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
		rootLogger.getLoggerRepository().resetConfiguration();
		
		ConsoleAppender console = new ConsoleAppender(); //create appender
		//configure the appender
		String PATTERN = LOG_PATTERN;
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(LOG_LEVEL);
		console.activateOptions();
		//add appender to any Logger (here is root)
		rootLogger.addAppender(console);
		  
	}
	
	private static void updateLoggerAppenders() {
		org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
		
		// debug mode?
		if (getCommandLine().hasOption(CLI_OPT_DEBUG.getOpt())) {
			rootLogger.getLoggerRepository().resetConfiguration();
			LOG_LEVEL = Level.DEBUG;
			
			ConsoleAppender console = new ConsoleAppender(); //create appender
			//configure the appender
			console.setLayout(new PatternLayout(LOG_PATTERN)); 
			console.setThreshold(LOG_LEVEL);
			console.activateOptions();
			//add appender to any Logger (here is root)
			rootLogger.addAppender(console);
		}
		
		// log to file?
		if (getCommandLine().hasOption(CLI_OPT_LOG_TO_FILE.getOpt())) {
			String logFile = getCommandLine().getOptionValue(CLI_OPT_LOG_TO_FILE.getOpt());
			
			FileAppender fa = new FileAppender();
			fa.setName("FileLogger");
			fa.setFile(logFile);
			fa.setLayout(new PatternLayout(LOG_PATTERN));
			fa.setThreshold(LOG_LEVEL);
			fa.setAppend(true);
			fa.activateOptions();

			//add appender to any Logger (here is root)
			rootLogger.addAppender(fa);
		}
	}
	
	/**
	 * Initializes Apache CLI to work with command line arguments
	 * @param args
	 */
	private static void initCli(String[] args) {
		log.debug("Parsing command line arguments...");
		options = new Options();
		
		fillCommandLineOptions();
		
		CommandLineParser parser = new BasicParser();
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			log.error("Unable to parse command line arguments: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Fills the list of possible command line options
	 */
	private static void fillCommandLineOptions() {
		getOptions().addOption(CLI_OPT_HELP);
		getOptions().addOption(CLI_OPT_TRAINING_SET_XML);
		getOptions().addOption(CLI_OPT_RECOGNIZE_FACE);
		getOptions().addOption(CLI_OPT_CLEANUP_DB);
		getOptions().addOption(CLI_OPT_DEBUG);
		getOptions().addOption(CLI_OPT_LOG_TO_FILE);
		getOptions().addOption(CLI_OPT_TEST_RECOGNIZER_EXIST);
		getOptions().addOption(CLI_OPT_PERSIST_TRAININGSET);
		getOptions().addOption(CLI_OPT_TEST_RECOGNIZER_SKIP);
		getOptions().addOption(CLI_OPT_SAVE_AVERAGE_FACE);
	}
	
	/**
	 * Checks that the user put all neccessary arguments on command line
	 */
	private static void checkNeccessaryArguments() {
		boolean valid = true;
		
		if (
				// no training set
				!getCommandLine().hasOption(CLI_OPT_TRAINING_SET_XML.getOpt())
						
				// no recognition required
				&& !getCommandLine().hasOption(CLI_OPT_RECOGNIZE_FACE.getOpt())
				
				// no standalone cleanup required
				&& !getCommandLine().hasOption(CLI_OPT_CLEANUP_DB.getOpt())
				
				// no standalone recognizer test
				&& !getCommandLine().hasOption(CLI_OPT_TEST_RECOGNIZER_EXIST.getOpt())
				&& !getCommandLine().hasOption(CLI_OPT_TEST_RECOGNIZER_SKIP.getOpt())
				
				// no standalone average face save
				&& !getCommandLine().hasOption(CLI_OPT_SAVE_AVERAGE_FACE.getOpt())
				) {
			valid = false;
		}
		
		if (!valid) {
			System.out.println("Missing some arguments.");
			System.out.println();
			printHelp();
			System.exit(1);
		}
	}
	
	/**
	 * Proceeds based on given params
	 */
	private static void proceed() {
		// required cleanup?
		if (getCommandLine().hasOption(CLI_OPT_CLEANUP_DB.getOpt())) {
			log.debug("Initiating database cleanup...");
			PersistenceManager.cleanupDatabase();
		}
		
		IFaceRecognizer recognizer = new EigenfacesRecognizer();
		
		IGenericCRUDDao<TrainingSet> trainingSetDao = new TrainingSetDao();
		int trainingSets = trainingSetDao.getAll().size();
		
		// training set
		if (getCommandLine().hasOption(CLI_OPT_TRAINING_SET_XML.getOpt())) {
			
			switch(trainingSets) {
				/*
				 * Since we're working with only one training set, we will
				 * allow training process only when there is no training set 
				 * already persisted in the database.
				 */
				case 0:
					log.debug("Initiating training set processing...");
					recognizer.train(getCommandLine().getOptionValue(CLI_OPT_TRAINING_SET_XML.getOpt()));
					if (getCommandLine().hasOption(CLI_OPT_PERSIST_TRAININGSET.getOpt())) {
						log.info("Persisting training set...");
						trainingSetDao.add(recognizer.getTrainingSet());
					}
					break;
				case 1:
					log.error("There is already an existing training set in the database."
							+ " Please use -c to delete it, or remove the -t option "
							+ "from command line.");
					System.exit(1);
					break;
				default:
					throw new IllegalStateException("Too many training sets in the database."); // shouldn't happen
			}
			
		} else {
			log.info("No training set provided on the command line, attempting"
					+ "to load a persisted one.");
			switch (trainingSets) {
				case 0:
					log.error("There is no training set available (neither"
							+ "passed on the command line, nor persisted)");
					throw new IllegalStateException("No trainig set.");
				case 1:
					recognizer.loadTrainingSet(trainingSetDao.getAll().get(0));
					break;
				default:
					throw new IllegalStateException("Too many training sets in the database."); // shouldn't happen
			}
		}
		
		// save average face to a file
		if (getCommandLine().hasOption(CLI_OPT_SAVE_AVERAGE_FACE.getOpt())) {
			String filePath = getCommandLine().getOptionValue(CLI_OPT_SAVE_AVERAGE_FACE.getOpt());
			log.info(String.format("Savig average face to a file '%s' ...", filePath));
			Highgui.imwrite(filePath, recognizer.getTrainingSet().getAverageFace());
		}
		
		// test recognizer - all existing faces in the training set
		if (getCommandLine().hasOption(CLI_OPT_TEST_RECOGNIZER_EXIST.getOpt())) {
			if (recognizer.getTrainingSet() == null) {
				log.error("No training set available to test against.");
				throw new IllegalStateException("No training set available to test against.");
			}
			
			recognizer.testPrediction(RecognizerTestScenario.EXISTING_TRAINING_FACES);
		}
		
		// test recognizer - one skipped face per each person in the training set
				if (getCommandLine().hasOption(CLI_OPT_TEST_RECOGNIZER_SKIP.getOpt())) {
					if (recognizer.getTrainingSet() == null) {
						log.error("No training set available to test against.");
						throw new IllegalStateException("No training set available to test against.");
					}
					
					recognizer.testPrediction(RecognizerTestScenario.SKIPPED_TRAINING_FACES);
				}
		
		// recognize face
		if (getCommandLine().hasOption(CLI_OPT_RECOGNIZE_FACE.getOpt())) {
			log.debug("Initiating face recognition...");
			recognizer.predict(getCommandLine().getOptionValue(CLI_OPT_RECOGNIZE_FACE.getOpt()));
		}
		
		System.exit(0);
	}
	
	public static CommandLine getCommandLine() {
		return commandLine;
	}
	
	public static Options getOptions() {
		return options;
	}
	
	/**
	 * Prints help on command line
	 */
	public static void printHelp() {
		printHelp(false);
	}
	
	/**
	 * Prints help on command line
	 * @param isRequestedByUser Tells if the user requested it, or it's an error state (different formatting)
	 */
	public static void printHelp(boolean isRequestedByUser) {
		if (!isRequestedByUser) { // e.g. missing param
			System.out.println("----");
			System.out.println();
		}
		
		String jarName = EigenfacesApplication.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		jarName = jarName.substring(jarName.lastIndexOf(NativeLibrariesUtils.getOperatingSystem().getFilePathDelimiter()) + 1);
		if (jarName.length() == 0) // running from IDE
			jarName = EigenfacesApplication.class.getSimpleName();
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(jarName, getOptions());
		System.exit(isRequestedByUser ? 0 : 1);
	}
}
