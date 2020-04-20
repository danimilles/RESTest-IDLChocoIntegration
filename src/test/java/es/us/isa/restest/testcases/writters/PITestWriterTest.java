package es.us.isa.restest.testcases.writters;

import es.us.isa.restest.specification.OpenAPISpecification;
import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.testcases.writers.PITestWriter;
import es.us.isa.restest.testcases.writers.RESTAssuredWriter;
import io.swagger.models.HttpMethod;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PITestWriterTest {
	
	@Test
	public void test() {
		
		// Load specification
		String OAISpecPath = "src/test/resources/Comments/swagger.yaml";
		OpenAPISpecification spec = new OpenAPISpecification(OAISpecPath);

		String testConfPath = "src/test/resources/Comments/testConf.yaml";
		
		// Create test case
		List<TestCase> testCases = new ArrayList<TestCase>();
		TestCase tc = new TestCase("getCommentsId", true, "getAll","/comments" ,HttpMethod.GET);
		tc.setOutputFormat("application/json");
		
		tc.addQueryParameter("type", "sdfhjlsdfghjdfhjkldfg");
		tc.addQueryParameter("order", "date");
		
		tc.setExpectedOutputs(spec.getSpecification().getPath("/comments").getGet().getResponses());
		
		testCases.add(tc);
		
		// Write test case
		PITestWriter writer = new PITestWriter(OAISpecPath, testConfPath, "src/generation/java/restassured/pitest", "CommentPitest", "restassured.pitest");

		Map<String, String> bodyEntities = new HashMap<>();
		bodyEntities.put("ALL", "Comment");
		Map<String, String> resourceClasses = new HashMap<>();
		bodyEntities.put("ALL", "CommentResource");

		writer.setBodyEntityName(bodyEntities);
		writer.setBodyEntityPackage("comments.model");
		writer.setResourceClassName(resourceClasses);
		writer.setResourceClassPackage("comments.resources");

		writer.write(testCases);
		
	}

}
