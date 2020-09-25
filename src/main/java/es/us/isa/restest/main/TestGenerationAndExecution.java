package es.us.isa.restest.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.us.isa.restest.configuration.pojos.TestConfigurationObject;
import es.us.isa.restest.coverage.CoverageGatherer;
import es.us.isa.restest.coverage.CoverageMeter;
import es.us.isa.restest.generators.AbstractTestCaseGenerator;
import es.us.isa.restest.generators.ConstraintBasedTestCaseGenerator;
import es.us.isa.restest.generators.RandomTestCaseGenerator;
import es.us.isa.restest.reporting.AllureReportManager;
import es.us.isa.restest.reporting.StatsReportManager;
import es.us.isa.restest.runners.RESTestRunner;
import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.writers.IWriter;
import es.us.isa.restest.testcases.writers.RESTAssuredWriter;
import es.us.isa.restest.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static es.us.isa.restest.configuration.TestConfigurationIO.loadConfiguration;
import static es.us.isa.restest.util.FileManager.createDir;
import static es.us.isa.restest.util.FileManager.deleteDir;
import static es.us.isa.restest.util.PropertyManager.readProperty;
import static es.us.isa.restest.util.PropertyManager.readProperty;
import static es.us.isa.restest.util.Timer.TestStep.ALL;


/*
 * This class show the basic workflow of test case generation -> test case execution -> test reporting
 */
public class TestGenerationAndExecution {

    private static Integer numTestCases = 10;               // Number of test cases per operation
    private static String OAISpecPath;		                // Path to OAS specification file
    private static OpenAPISpecification spec;               // OAS specification
    private static String confPath;	                        // Path to test configuration file
    private static String targetDirJava;	                // Directory where tests will be generated.
    private static String packageName;						// Package name.
    private static String experimentName;					// Used as identifier for folders, etc.
    private static String testClassName;					// Name prefix of the class to be generated
    private static Boolean enableInputCoverage = true;      // Set to 'true' if you want the input coverage report.
    private static Boolean enableOutputCoverage = true;     // Set to 'true' if you want the input coverage report.
    private static Boolean enableCSVStats = true;           // Set to 'true' if you want statistics in a CSV file.
    private static Boolean ignoreDependencies = false;      // Set to 'true' if you don't want to use IDLReasoner.
    private static Float faultyRatio = 0.1f;                // Percentage of faulty test cases to generate. Defaults to 0.1
    private static Integer totalNumTestCases = -1;			// Total number of test cases to be generated (-1 for infinite loop)
    private static Integer timeDelay = -1;                  // Delay between requests in seconds (-1 for no delay)

    // For CBT only:
    private static Float faultyDependencyRatio = 0.5f;      // Percentage of faulty test cases due to dependencies to generate. Defaults to 0.05 (0.1*0.5)
    private static Integer reloadInputDataEvery = 100;      // Number of requests using the same randomly generated input data
    private static Integer inputDataMaxValues = 1000;       // Number of values used for each parameter when reloading input data

    private static final Logger logger = LogManager.getLogger(TestGenerationAndExecution.class.getName());

    
    public static void main(String[] args) {
        Timer.startCounting(ALL);
        
        // Read .properties file path. This file contains the configuration parameter for the generation
        if(args.length > 0)
            readParameterValues(args[0]);
        else
            readParameterValues(readProperty("evaluation.properties.dir") +  "/comments_betty.properties");

        // Create target directory if it does not exists
        createDir(targetDirJava);

        // RESTest runner
        AbstractTestCaseGenerator generator = createGenerator();            // Test case generator
        IWriter writer = createWriter();                                    // Test case writer
        AllureReportManager reportManager = createAllureReportManager();    // Allure test case reporter
        StatsReportManager statsReportManager = createStatsReportManager(); // Stats reporter
        RESTestRunner runner = new RESTestRunner(testClassName, targetDirJava, packageName, generator, writer, reportManager, statsReportManager);

        // Main loop
        int iteration = 1;
        while (totalNumTestCases == -1 || runner.getNumTestCases() < totalNumTestCases) {

            // Introduce optional delay
            if (iteration!=1 && timeDelay!=-1)
                delay(timeDelay);

            // Generate unique test class name to avoid the same class being loaded everytime
            String className = testClassName + "_" + IDGenerator.generateId();
            ((RESTAssuredWriter) writer).setClassName(className);
            runner.setTestClassName(className);

            // Test case generation + execution + test report generation
            runner.run();
 
            logger.info("Iteration {}. {} test cases generated.", iteration, runner.getNumTestCases());
            iteration++;
        }

        Timer.stopCounting(ALL);

        generateTimeReport();
    }

    // Read the parameter values from a .properties file
    private static void readParameterValues(String evalPropertiesFilePath) {

        numTestCases = readProperty(evalPropertiesFilePath, "numtestcases") != null?
                Integer.parseInt(readProperty(evalPropertiesFilePath, "numtestcases")) :
                numTestCases;

        OAISpecPath = readProperty(evalPropertiesFilePath, "oaispecpath");
        confPath = readProperty(evalPropertiesFilePath, "confpath");

        targetDirJava = readProperty(evalPropertiesFilePath, "targetdirjava") != null?
                readProperty(evalPropertiesFilePath, "targetdirjava") :
                generateDefaultTargetDir();

        packageName = readProperty(evalPropertiesFilePath, "packagename") != null?
                readProperty(evalPropertiesFilePath, "packagename") :
                getAPITitle(false);

        experimentName = readProperty(evalPropertiesFilePath, "experimentname") != null?
                readProperty(evalPropertiesFilePath, "experimentname") :
                getAPITitle(false);

        testClassName = readProperty(evalPropertiesFilePath, "testclassname") != null?
                readProperty(evalPropertiesFilePath, "testclassname") :
                getAPITitle(true);

        enableInputCoverage = readProperty(evalPropertiesFilePath, "enableinputcoverage") != null?
                Boolean.parseBoolean(readProperty(evalPropertiesFilePath, "enableinputcoverage")) :
                enableInputCoverage;

        enableOutputCoverage = readProperty(evalPropertiesFilePath, "enableoutputcoverage") != null?
                Boolean.parseBoolean(readProperty(evalPropertiesFilePath, "enableoutputcoverage")) :
                enableOutputCoverage;

        enableCSVStats = readProperty(evalPropertiesFilePath, "enablecsvstats") != null?
                Boolean.parseBoolean(readProperty(evalPropertiesFilePath, "enablecsvstats")) :
                enableCSVStats;

        ignoreDependencies = readProperty(evalPropertiesFilePath, "ignoredependencies") != null?
                Boolean.parseBoolean(readProperty(evalPropertiesFilePath, "ignoredependencies")) :
                ignoreDependencies;

        totalNumTestCases = readProperty(evalPropertiesFilePath, "numtotaltestcases") != null?
                Integer.parseInt(readProperty(evalPropertiesFilePath, "numtotaltestcases")) :
                totalNumTestCases;

        timeDelay = readProperty(evalPropertiesFilePath, "delay") != null?
                Integer.parseInt(readProperty(evalPropertiesFilePath, "delay")) :
                timeDelay;

        faultyRatio = readProperty(evalPropertiesFilePath, "faultyratio") != null?
                Float.parseFloat(readProperty(evalPropertiesFilePath, "faultyratio")) :
                faultyRatio;

        faultyDependencyRatio = readProperty(evalPropertiesFilePath, "faultydependencyratio") != null?
                Float.parseFloat(readProperty(evalPropertiesFilePath, "faultydependencyratio")) :
                faultyDependencyRatio;

        reloadInputDataEvery = readProperty(evalPropertiesFilePath, "reloadinputdataevery") != null?
                Integer.parseInt(readProperty(evalPropertiesFilePath, "reloadinputdataevery")) :
                reloadInputDataEvery;

        inputDataMaxValues = readProperty(evalPropertiesFilePath, "inputdatamaxvalues") != null?
                Integer.parseInt(readProperty(evalPropertiesFilePath, "inputdatamaxvalues")) :
                inputDataMaxValues;
    }

    
    
    private static String generateDefaultTargetDir() {
        return "src/generation/java/" + getAPITitle(false);
    }
    
    
    // Return the title of the API
    private static String getAPITitle(boolean capitalize) {
    	
        if(spec == null) {
            spec = new OpenAPISpecification(OAISpecPath);
        }
        
        return spec.getTitle(capitalize);
    }


    // Create a test case generator
    private static AbstractTestCaseGenerator createGenerator() {
        // Load specification
        if(spec == null) {
            spec = new OpenAPISpecification(OAISpecPath);
        }

        // Load configuration
        TestConfigurationObject conf = loadConfiguration(confPath, spec);

        // Create generator
        AbstractTestCaseGenerator generator;
        if(ignoreDependencies)
            generator = new RandomTestCaseGenerator(spec, conf, numTestCases);
        else {
            generator = new ConstraintBasedTestCaseGenerator(spec, conf, numTestCases);
            ((ConstraintBasedTestCaseGenerator) generator).setFaultyDependencyRatio(faultyDependencyRatio);
            ((ConstraintBasedTestCaseGenerator) generator).setInputDataMaxValues(inputDataMaxValues);
            ((ConstraintBasedTestCaseGenerator) generator).setReloadInputDataEvery(reloadInputDataEvery);
        }
        generator.setFaultyRatio(faultyRatio);

        return generator;
    }

    // Create a writer for RESTAssured
    private static IWriter createWriter() {
        String basePath = spec.getSpecification().getServers().get(0).getUrl();
        RESTAssuredWriter writer = new RESTAssuredWriter(OAISpecPath, targetDirJava, testClassName, packageName, basePath);
        writer.setLogging(true);
        writer.setAllureReport(true);
        writer.setEnableStats(enableCSVStats);
        writer.setEnableOutputCoverage(enableOutputCoverage);
        writer.setAPIName(experimentName);
        return writer;
    }

    // Create an Allure report manager
    private static AllureReportManager createAllureReportManager() {
        String allureResultsDir = PropertyManager.readProperty("allure.results.dir") + "/" + experimentName;
        String allureReportDir = PropertyManager.readProperty("allure.report.dir") + "/" + experimentName;

        // Delete previous results (if any)
        deleteDir(allureResultsDir);
        deleteDir(allureReportDir);

        AllureReportManager arm = new AllureReportManager(allureResultsDir, allureReportDir);
        arm.setHistoryTrend(true);
        return arm;
    }

    // Create an statistics report manager
    private static StatsReportManager createStatsReportManager() {
        String testDataDir = PropertyManager.readProperty("data.tests.dir") + "/" + experimentName;
        String coverageDataDir = PropertyManager.readProperty("data.coverage.dir") + "/" + experimentName;

        // Delete previous results (if any)
        deleteDir(testDataDir);
        deleteDir(coverageDataDir);

        // Recreate directories
        createDir(testDataDir);
        createDir(coverageDataDir);

        return new StatsReportManager(testDataDir, coverageDataDir, enableCSVStats, enableInputCoverage, enableOutputCoverage, new CoverageMeter(new CoverageGatherer(spec)));
    }

    
    private static void generateTimeReport() {
        ObjectMapper mapper = new ObjectMapper();
        String timePath = readProperty("data.tests.dir") + "/" + experimentName + "/" + readProperty("data.tests.time");
        try {
            mapper.writeValue(new File(timePath), Timer.getCounters());
        } catch (IOException e) {
            logger.error("The time report cannot be generated. Stack trace:");
            logger.error(e.getMessage());
        }
        logger.info("Time report generated.");
    }

    
    /*
     * Stop the execution n seconds
     */
    private static void delay(Integer time) {
        try {
        	logger.info("Introducing delay of {} seconds", time);
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e) {
            logger.error("Error introducing delay", e);
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}