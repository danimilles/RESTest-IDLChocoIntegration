package es.us.isa.restest.testcases.writers;

import es.us.isa.restest.configuration.TestConfigurationIO;
import es.us.isa.restest.configuration.pojos.Operation;
import es.us.isa.restest.configuration.pojos.TestConfigurationObject;
import es.us.isa.restest.configuration.pojos.TestParameter;
import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.TestCase;
import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;

import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJava;

/** This class defines a test writer to use PITest. It creates a Java class with JUnit test cases
 * ready to be executed.
 * 
 * @author Jose Ramon Fernandez de la Rosa
 *
 */
public class PITestWriter implements IWriter {

	private boolean OAIValidation = true;
	private boolean bodiesAsString = false;
	private boolean logging = false;				// Log everything (ONLY IF THE TEST FAILS)

	private String resourceClassName;
	private String resourceClassPackage;
	private String bodyEntityName;
	private String bodyEntityPackage;

	private String specPath;						// Path to OAS specification file
	private String testConfPath;
	private String testFilePath;					// Path to test configuration file
	private String className;						// Test class name
	private String packageName;						// Package name
	private String baseURI;							// API base URI

	private String APIName;							// API name (necessary for folder name of exported data)

	public PITestWriter(String specPath, String testConfPath, String testFilePath, String className, String packageName) {
		this.specPath = specPath;
		this.testConfPath = testConfPath;
		this.testFilePath = testFilePath;
		this.className = className;
		this.packageName = packageName;
	}
	
	/* (non-Javadoc)
	 * @see es.us.isa.restest.testcases.writers.IWriter#write(java.util.Collection)
	 */
	@Override
	public void write(Collection<TestCase> testCases) {

		TestConfigurationObject conf = TestConfigurationIO.loadConfiguration(testConfPath);
		OpenAPISpecification spec = new OpenAPISpecification(specPath);
		
		// Initializing content
		String contentFile = "";
		
		// Generating imports
		contentFile += generateImports(packageName, resourceClassPackage, resourceClassName, bodyEntityPackage, bodyEntityName);
		
		// Generate className
		contentFile += generateClassName(className);

		// Generate attributes
		contentFile += generateAttributes(specPath);
		
		// Instantiate OpenAPISpecification
		contentFile += generateSetUp();

		// Generate tests
		int ntest=1;
		for(TestCase t: testCases)
			contentFile += generateTest(t,ntest++, conf, spec);
		
		// Close class

		contentFile += generateResponseValidationMethod(bodyEntityName);

		contentFile += "}\n";
		
		//Save to file
		saveToFile(testFilePath,className,contentFile);
		
		/* Test Compile
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(System.in, System.out, System.err, TEST_LOCATION + this.specification.getInfo().getTitle().replaceAll(" ", "") + "Test.java");
		*/
	}

	private String generateImports(String packageName, String resourceClassPackage, String resourceClassName, String bodyEntityPackage, String bodyEntityName) {
		String content = "";
		
		if (packageName!=null)
			content += "package " + packageName + ";\n\n";
				
		content += "import org.junit.*;\n"
				+  "import com.fasterxml.jackson.databind.JsonNode;\n"
				+  "import com.fasterxml.jackson.core.type.TypeReference;\n"
				+  "import com.fasterxml.jackson.databind.ObjectMapper;\n"
				+  "import com.fasterxml.jackson.core.JsonParseException;\n"
				+  "import com.fasterxml.jackson.databind.JsonMappingException;\n"
				+  "import java.io.IOException;\n"
				+  "import org.jboss.resteasy.spi.Failure;\n"
				+  "import javax.ws.rs.core.Response;\n"
				+  "import java.util.Collection;\n"
				+  "import java.util.Map;\n"
				+  "import org.junit.FixMethodOrder;\n"
				+  "import static org.junit.Assert.fail;\n"
				+  "import static org.junit.Assert.assertTrue;\n"
				+  "import org.junit.runners.MethodSorters;\n"
				+  "import io.swagger.parser.SwaggerParser;\n"
				+  "import io.swagger.models.Swagger;\n"
				+  "import io.swagger.models.HttpMethod;\n"
				+  "import io.swagger.models.Model;\n"
				+  "import io.swagger.models.properties.RefProperty;\n"
				+  "import io.swagger.models.properties.ArrayProperty;\n"
				+  "import " + resourceClassPackage + "." + resourceClassName + ";\n";
		if(bodyEntityName != null && bodyEntityPackage != null)
			content += "import " + bodyEntityPackage + "." + bodyEntityName + ";\n";
		
		content +="\n";
		
		return content;
	}
	
	private String generateClassName(String className) {
		return "@FixMethodOrder(MethodSorters.NAME_ASCENDING)\n"
			 + "public class " + className + " {\n\n";
	}
	
	private String generateAttributes(String specPath) {
		String content = "";
		
//		if (OAIValidation)
		content += "\tprivate static final String OAI_JSON_URL = \"" + specPath + "\";\n"
				+  "\tprivate static ObjectMapper mapper = new ObjectMapper();\n"
				+  "\tprivate static Swagger spec;\n";
//				+  "\tprivate final ResponseValidationFilter validationFilter = new ResponseValidationFilter(OAI_JSON_URL);\n"
//				+  "\tprivate StatusCode5XXFilter statusCode5XXFilter = new StatusCode5XXFilter();\n";

		content += "\n";
		
		return content;
	}
	
	private String generateSetUp() {
		return 	"\t@BeforeClass\n "
			  + "\tpublic static void setUp() {\n"
			  + "\t\tspec = new SwaggerParser().read(OAI_JSON_URL);\n"
			  +	"\t}\n\n";
	}

	private String generateTest(TestCase t, int instance, TestConfigurationObject conf, OpenAPISpecification spec) {
		String content="";

		String bodyParam = spec.getSpecification().getPath(t.getPath()).getOperationMap().get(t.getMethod()).getParameters().stream()
				.filter(x -> x.getIn().equals("body"))
				.map(Parameter::getName)
				.findFirst()
				.orElse(null);
		
		// Generate test method header
		content += generateMethodHeader(t,instance);

		// Generate initialization of the API resource class and the booleans needed
		content += generateTestInitialization();

		// Generate the start of the try block
		content += generateTryBlockStart();

		// Generate all stuff needed before the API request
		content += generatePreRequest(t);
		
		// Generate parameters and the API call
		content += generateParametersAndAPICall(t, conf, bodyParam);

		// Generate the asserts
		content += generateAsserts(t);

		// Generate all stuff needed after the asserts
		content += generatePostAsserts(t, bodyParam);

		// Generate the end of the try block, including its corresponding catch
		content += generateTryBlockEnd(t);
		
		// Close test method
		content += "\t}\n\n";
		
		return content;
	}


	private String generateMethodHeader(TestCase t, int instance) {
		return "\t@Test\n" +
				"\tpublic void " + t.getId() + "() {\n";
	}

	private String generateTestInitialization() {
		return "\t\t" + resourceClassName + " resource = " + resourceClassName + ".getInstance();\n\n";
	}

	private String generateTryBlockStart() {
		return "\t\ttry {\n";
	}

	private String generatePreRequest(TestCase t) {
		String content = "";

		if (t.getBodyParameter() != null) {
			content += generateJSONtoObjectConversion(t);
		} else if(t.getMethod().equals(HttpMethod.POST) || t.getMethod().equals(HttpMethod.PUT) || t.getMethod().equals(HttpMethod.PATCH))
			content += "\t\t\t" + bodyEntityName + " jsonBody = null;\n\n";
		return content;
	}

	private String generateJSONtoObjectConversion(TestCase t) {
		String content = "";
		String bodyParameter = escapeJava(t.getBodyParameter());

		if(!bodiesAsString) {
			content += "\t\t\t" + bodyEntityName + " jsonBody = mapper.readValue(\""
					+  bodyParameter
					+  "\", " +  bodyEntityName + ".class);\n\n";
		} else {
			content += "\t\t\tString jsonBody = \"" + bodyParameter + "\";\n\n";
		}


		return content;
	}
	
	private String generateParametersAndAPICall(TestCase t, TestConfigurationObject conf, String bodyParam) {
		String content = "";

		//Obtains every parameter in the request
		Map<String, String> allParamsInRequest = new HashMap<>();
		allParamsInRequest.putAll(t.getHeaderParameters());
		allParamsInRequest.putAll(t.getPathParameters());
		allParamsInRequest.putAll(t.getQueryParameters());
		allParamsInRequest.putAll(t.getFormParameters());

		//Searches for the operation in the test configuration object
		Operation op = conf.getTestConfiguration().getTestPaths().stream()
				.flatMap(x -> x.getOperations().stream())
				.filter(x -> x.getOperationId().equalsIgnoreCase(t.getOperationId()))
				.findFirst()
				.get();

		//With the Operation object, the Writer can get the names of all the possible parameters of the operation
		List<String> allParamNames = op.getTestParameters().stream().map(TestParameter::getName).collect(Collectors.toCollection(ArrayList::new));
		//Removes the name of the body parameter of the list (if exists)
		if(bodyParam != null) allParamNames.remove(bodyParam);

		//Instantiates every possible parameter of the operation. If there's no value, it is instantiated as null.
		for(TestParameter tp : op.getTestParameters()) {
			if(!tp.getName().equals(bodyParam) && allParamsInRequest.get(tp.getName()) != null)
				content += "\t\t\tString " + tp.getName() + " = " + "\"" + allParamsInRequest.get(tp.getName()) + "\";\n";
			else if(!tp.getName().equals(bodyParam))
				content += "\t\t\tString " + tp.getName() + " = " + "null;\n";
		}

		//Instantiates the response of the API.
		content += "\n\t\t\tResponse response = resource." + op.getOperationId() + "(";

		//Adds the body parameter to the method (if exists)
		if(bodyParam != null)
			content += "jsonBody";

		//Adds the parameters to the method
		for(TestParameter tp : op.getTestParameters()) {
			if(!tp.getName().equals(bodyParam)) {
				if (!(allParamNames.indexOf(tp.getName()) == 0 && t.getBodyParameter() == null))
					content += ", ";
				content += tp.getName();
			}
		}

		content += ");\n\n";

		return content;
	}

	private String generateAsserts(TestCase t) {
		String content = "";

		//Status 500 validation
		content += "\t\t\tassertTrue(\"There was a Server Error.\", response.getStatus() < 500);\n";
		//Nominal or faulty validation
		if(t.getFaulty())
			content += "\t\t\tassertTrue(\"This faulty test case was expecting a 4XX status code but received other. Conformance error found\", response.getStatus() >= 400);\n";
		else if(!t.getFaulty() && t.getFulfillsDependencies())
			content += "\t\t\tassertTrue(\"This test case's input was correct, but received a 400 (Bad Request) status code. Conformance error found.\", response.getStatus() != 400);\n";
		//Response body validation
		content += "\t\t\tif(response.getEntity() instanceof Collection) {\n"
				+  "\t\t\t\tfor(" + bodyEntityName + " e : (Collection<"+ bodyEntityName +">)response.getEntity())\n"
				+  "\t\t\t\t\tresponseBodyValidation(e, \"" + t.getPath() + "\", HttpMethod." + t.getMethod().name() + ", String.valueOf(response.getStatus()), true);\n"
				+  "\t\t\t} else \n"
				+  "\t\t\t\tresponseBodyValidation(((" + bodyEntityName + ")response.getEntity()), \"" + t.getPath() + "\", HttpMethod." + t.getMethod().name() + ", String.valueOf(response.getStatus()), false);\n\n";

		return content;
	}

	private String generatePostAsserts(TestCase t, String bodyParam) {
		
		String content = "\t\t\tSystem.out.println(\"Test passed.\");\n";

		if (t.getBodyParameter() != null) {
			content += "\t\t} catch (JsonParseException|JsonMappingException e) {\n"
					+  "\t\t\tassertTrue(\"This test case's input was correct, but the input body was unable to deserialize. Conformance error found.\", " + t.getFaulty() + ");\n"
			        +  "\t\t\tSystem.out.println(\"Test passed.\");\n";

			content += "\t\t} catch (IOException e) {\n"
					+  "\t\t\te.printStackTrace();\n";
		}
		if(bodyParam != null) {
			content += "\t\t} catch (NullPointerException e) {\n"
					+  "\t\t\tassertTrue(\"This test case's input was correct, but the input body was null. Conformance error found.\", " + t.getFaulty() + ");\n"
					+  "\t\t\tSystem.out.println(\"Test passed.\");\n";
		}

		return content;
	}

	private String generateTryBlockEnd(TestCase t) {
		String content = "";

		content +=  "\t\t} catch (Failure ex) {\n"
				+  "\t\t\tint errorCode = ex.getErrorCode();\n"
				+  "\t\t\tassertTrue(\"There was a Server Error.\", errorCode < 500);\n";
		if(t.getFaulty())
			content += "\t\t\tassertTrue(\"This faulty test case was expecting a 4XX status code but received other. Conformance error found\", errorCode >= 400);\n";
		else if(!t.getFaulty() && t.getFulfillsDependencies())
			content += "\t\t\tassertTrue(\"This test case's input was correct, but received a 400 (Bad Request) status code. Conformance error found.\", errorCode != 400);\n";

		content += "\t\t\tSystem.out.println(\"Test passed.\");\n"
				+  "\t\t}\n";

		return content;

	}

	private String generateResponseValidationMethod(String bodyEntityName) {
		return "\tprivate static void responseBodyValidation(" + bodyEntityName + " bodyResponse, String path, HttpMethod method, String responseStatus, Boolean arrayResponse) {\n"
			+  "\t\tModel swaggerResponse;\n"
			+  "\t\tif(!arrayResponse) {\n"
			+  "\t\t\tswaggerResponse = spec.getDefinitions().keySet().stream()\n"
			+  "\t\t\t\t.filter(x -> x.equals(((RefProperty)spec.getPath(path).getOperationMap().get(method).getResponses().get(responseStatus).getSchema()).getSimpleRef()))\n"
			+  "\t\t\t\t.map(x -> spec.getDefinitions().get(x))\n"
			+  "\t\t\t\t.findFirst()\n"
			+  "\t\t\t\t.orElse(null);\n"
			+  "\t\t} else {\n"
			+  "\t\t\tswaggerResponse = spec.getDefinitions().keySet().stream()\n"
			+  "\t\t\t\t.filter(x -> x.equals(((RefProperty)((ArrayProperty)spec.getPath(path).getOperationMap().get(method).getResponses().get(responseStatus).getSchema()).getItems()).getSimpleRef()))\n"
			+  "\t\t\t\t.map(x -> spec.getDefinitions().get(x))\n"
			+  "\t\t\t\t.findFirst()\n"
			+  "\t\t\t\t.orElse(null);\n"
			+  "\t\t}\n"
			+  "\t\ttry {\n"
			+  "\t\t\tMap<String, Object> mapObject = mapper.readValue(mapper.writeValueAsString(bodyResponse), new TypeReference<Map<String, Object>>(){});\n"
			+  "\t\t\tfor(String s : mapObject.keySet())\n"
			+  "\t\t\t\tassertTrue(\"The response body does not match with the Swagger specification. Unknown parameter: \" + s, swaggerResponse.getProperties().containsKey(s));\n"
			+  "\t\t} catch (IOException e) {\n"
			+  "\t\t\tfail(e.getMessage());\n"
			+  "\t\t}\n"
			+  "\t}\n\n";
	}

	private void saveToFile(String path, String className, String contentFile) {
		FileWriter testClass = null;
		try {
			testClass = new FileWriter(path + "/" + className + ".java");
			testClass.write(contentFile);
			testClass.flush();
			testClass.close();
		} catch(Exception ex) {
			System.err.println("Error writing test file: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	public boolean OAIValidation() {
		return OAIValidation;
	}

	public void setOAIValidation(boolean oAIValidation) {
		OAIValidation = oAIValidation;
	}

	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}


	public String getSpecPath() {
		return specPath;
	}

	public void setSpecPath(String specPath) {
		this.specPath = specPath;
	}

	public String getTestFilePath() {
		return testFilePath;
	}

	public void setTestFilePath(String testFilePath) {
		this.testFilePath = testFilePath;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public String getAPIName() {
		return APIName;
	}

	public void setAPIName(String APIName) {
		this.APIName = APIName;
	}

	public void setResourceClassName(String resourceClassName) {
		this.resourceClassName = resourceClassName;
	}

	public void setBodyEntityName(String bodyEntityName) {
		this.bodyEntityName = bodyEntityName;
	}

	public void setResourceClassPackage(String resourceClassPackage) {
		this.resourceClassPackage = resourceClassPackage;
	}

	public void setBodyEntityPackage(String bodyEntityPackage) {
		this.bodyEntityPackage = bodyEntityPackage;
	}

	public void setBodiesAsString(boolean bodiesAsString) {
		this.bodiesAsString = bodiesAsString;
	}
}
