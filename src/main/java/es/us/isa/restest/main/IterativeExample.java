package es.us.isa.restest.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.us.isa.restest.coverage.CoverageMeter;
import es.us.isa.restest.generators.AbstractTestCaseGenerator;
import es.us.isa.restest.runners.RESTestRunner;
import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.writers.IWriter;
import es.us.isa.restest.testcases.writers.PITestWriter;
import es.us.isa.restest.testcases.writers.RESTAssuredWriter;
import es.us.isa.restest.util.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static es.us.isa.restest.util.FileManager.createDir;

public class IterativeExample {

    private static int numTestCases;			        // Number of test cases per operation
    private static String OAISpecPath;		                // Path to OAS specification file
    private static String confPath;	                        // Path to test configuration file
    private static String targetDirJava;	                // Directory where tests will be generated.
    //	private static String targetDirTestData = "target/test-data";						// Directory where tests will be exported to CSV.
//	private static String targetDirCoverageData = "target/coverage-data";				    // Directory where coverage will be exported to CSV.
    private static String packageName;						// Package name.
    private static String APIName;							// API name
    private static String testClassName;					// Name prefix of the class to be generated
    private static Boolean enableInputCoverage = true;      // Set to 'true' if you want the input coverage report.
    private static Boolean enableOutputCoverage = true;     // Set to 'true' if you want the input coverage report.
    private static Boolean enableCSVStats = true;           // Set to 'true' if you want statistics in a CSV file.
    private static Boolean ignoreDependencies = false;      // Set to 'true' if you don't want to use IDLReasoner.
    private static Float faultyRatio = 0.1f;                // Percentage of faulty test cases to generate. Defaults to 0.1
    private static Float faultyDependencyRatio = 0.5f;
    private static int totalNumTestCases = 50;				// Total number of test cases to be generated
    private static int timeDelay = -1;

    private static Boolean enablePitestWriter = false;
    private static Map<String, String> pitestBodyEntityName;
    private static String pitestBodyEntityPackage;
    private static Map<String, String> pitestResourceClassName;
    private static String pitestResourceClassPackage;
    private static Boolean pitestBodiesAsString;

    public static void main(String[] args) {

        if(args.length > 0)
            setParameters(args[0]);
        else
            setParameters("src/main/resources/APIProperties/youtube_search.properties");

        // Create target directory if it does not exists
        createDir(targetDirJava);
        if(enablePitestWriter)
            createDir(targetDirJava + "/pitest");

        OpenAPISpecification spec = new OpenAPISpecification(OAISpecPath);

        AbstractTestCaseGenerator generator = MainUtils.createGenerator(spec, confPath, numTestCases, ignoreDependencies);  // Test case generator
        generator.setFaultyRatio(faultyRatio);
        generator.setFaultyDependencyRatio(faultyDependencyRatio);
        IWriter writer = MainUtils.createWriter(spec, OAISpecPath, targetDirJava, testClassName, packageName, enableOutputCoverage, APIName);   // Test case writer

        AllureReportManager reportManager = MainUtils.createAllureReportManager(APIName);   // Allure test case reporter
        CSVReportManager csvReportManager = MainUtils.createCSVReportManager(APIName, enableCSVStats, enableInputCoverage);    // CSV test case reporter

        RESTestRunner runner;
        if(enableInputCoverage && enableOutputCoverage) {
            CoverageMeter covMeter = MainUtils.createCoverageMeter(spec);   //Coverage meter
            if(enablePitestWriter) {
                PITestWriter pitestWriter = MainUtils.createPITestWriter(OAISpecPath, confPath, targetDirJava, testClassName, packageName, pitestBodyEntityName, pitestBodyEntityPackage, pitestResourceClassName, pitestResourceClassPackage, pitestBodiesAsString);
                runner = new RESTestRunner(testClassName, targetDirJava, packageName, generator, writer, pitestWriter, reportManager, csvReportManager, covMeter);
            } else
                runner = new RESTestRunner(testClassName, targetDirJava, packageName, generator, writer, reportManager, csvReportManager, covMeter);
        } else {
            if(enablePitestWriter) {
                PITestWriter pitestWriter = MainUtils.createPITestWriter(OAISpecPath, confPath, targetDirJava, testClassName, packageName, pitestBodyEntityName, pitestBodyEntityPackage, pitestResourceClassName, pitestResourceClassPackage, pitestBodiesAsString);
                runner = new RESTestRunner(testClassName, targetDirJava, packageName, generator, writer, pitestWriter, reportManager, csvReportManager);
            } else
                runner = new RESTestRunner(testClassName, targetDirJava, packageName, generator, writer, reportManager, csvReportManager);
        }
        int iteration = 1;
        while (totalNumTestCases == -1 || runner.getNumTestCases() < totalNumTestCases) {

            // Introduce optional delay
            if (iteration!=1 && timeDelay!=-1)
                MainUtils.delay(timeDelay);

            // Generate unique test class name to avoid the same class being loaded everytime
            String className = testClassName + "_" + IDGenerator.generateId();
            ((RESTAssuredWriter) writer).setClassName(className);
            //If pitestWriter exists, its classname will be changed in setTestClassName method of RESTestRunner
            runner.setTestClassName(className);

            // Test case generation + execution + test report generation
            runner.run();

            System.out.println("Iteration "  + iteration + ". " +  runner.getNumTestCases() + " test cases generated.");
            iteration++;
        }

        if(enableCSVStats) {
            String csvNFPath = csvReportManager.getTestDataDir() + "/" + PropertyManager.readProperty("data.tests.testcases.nominalfaulty.file");
            generator.exportNominalFaultyToCSV(csvNFPath, "total");
        }
    }

    private static void setParameters(String APIPropertyFilePath) {
        numTestCases = Integer.parseInt(PropertyManager.readProperty(APIPropertyFilePath, "api.numtestcases"));
        OAISpecPath = PropertyManager.readProperty(APIPropertyFilePath, "api.oaispecpath");
        confPath = PropertyManager.readProperty(APIPropertyFilePath, "api.confpath");
        targetDirJava = PropertyManager.readProperty(APIPropertyFilePath, "api.targetdirjava");
        packageName = PropertyManager.readProperty(APIPropertyFilePath, "api.packagename");
        APIName = PropertyManager.readProperty(APIPropertyFilePath, "api.apiname");
        testClassName = PropertyManager.readProperty(APIPropertyFilePath, "api.testclassname");
        enableInputCoverage = Boolean.parseBoolean(PropertyManager.readProperty(APIPropertyFilePath, "api.enableinputcoverage"));
        enableOutputCoverage = Boolean.parseBoolean(PropertyManager.readProperty(APIPropertyFilePath, "api.enableoutputcoverage"));
        enableCSVStats = Boolean.parseBoolean(PropertyManager.readProperty(APIPropertyFilePath, "api.enablecsvstats"));
        ignoreDependencies = Boolean.parseBoolean(PropertyManager.readProperty(APIPropertyFilePath, "api.ignoredependencies"));
        totalNumTestCases = Integer.parseInt(PropertyManager.readProperty(APIPropertyFilePath, "api.numtotaltestcases"));
        timeDelay = Integer.parseInt(PropertyManager.readProperty(APIPropertyFilePath, "api.delay"));

        String faultyRatioString = PropertyManager.readProperty(APIPropertyFilePath, "api.faultyratio");
        if (faultyRatioString != null)
            faultyRatio = Float.parseFloat(faultyRatioString);

        String faultyDependencyRatioString = PropertyManager.readProperty(APIPropertyFilePath, "api.faultydependencyratio");
        if (faultyDependencyRatioString != null)
            faultyDependencyRatio = Float.parseFloat(faultyDependencyRatioString);

        Boolean pitest = Boolean.parseBoolean(PropertyManager.readProperty(APIPropertyFilePath, "api.pitest"));
        if(pitest) {
            enablePitestWriter = true;

            try {
                String bodyEntityName = PropertyManager.readProperty(APIPropertyFilePath, "api.pitest.bodyentityname");
                if(bodyEntityName.charAt(0) == '{') {
                    ObjectMapper mapper = new ObjectMapper();

                    pitestBodyEntityName = mapper.readValue(bodyEntityName, new TypeReference<Map<String, String>>(){});
                } else {
                    pitestBodyEntityName = new HashMap<>();
                    pitestBodyEntityName.put("ALL", bodyEntityName);
                }

                String resourceClassName = PropertyManager.readProperty(APIPropertyFilePath, "api.pitest.resourceclassname");
                if(resourceClassName.charAt(0) == '{') {
                    ObjectMapper mapper = new ObjectMapper();
                    pitestResourceClassName = mapper.readValue(resourceClassName, new TypeReference<Map<String, String>>(){});
                } else {
                    pitestResourceClassName = new HashMap<>();
                    pitestResourceClassName.put("ALL", resourceClassName);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            pitestBodyEntityPackage = PropertyManager.readProperty(APIPropertyFilePath, "api.pitest.bodyentitypackage");
            pitestResourceClassPackage = PropertyManager.readProperty(APIPropertyFilePath, "api.pitest.resourcepackage");
            pitestBodiesAsString = Boolean.parseBoolean(PropertyManager.readProperty(APIPropertyFilePath, "api.pitest.bodiesasstring"));
        }
    }
}
