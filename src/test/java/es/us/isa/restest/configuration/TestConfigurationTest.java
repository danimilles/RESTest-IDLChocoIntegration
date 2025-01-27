package es.us.isa.restest.configuration;

import es.us.isa.restest.specification.OpenAPISpecification;
import org.junit.Test;

import static es.us.isa.restest.util.FileManager.checkIfExists;
import static es.us.isa.restest.util.FileManager.deleteFile;
import static org.junit.Assert.*;

import es.us.isa.restest.configuration.TestConfigurationIO;
import es.us.isa.restest.configuration.pojos.TestConfigurationObject;

public class TestConfigurationTest {

	@Test
	public void testLoadConfiguration() {
		deleteFile("src/test/resources/Folder/testConf.yaml");
		String specPath = "src/test/resources/Folder/swagger.yaml";
		OpenAPISpecification spec = new OpenAPISpecification(specPath);
		String path = "src/test/resources/Folder/";
		TestConfigurationObject conf = TestConfigurationIO.loadConfiguration(path + "fullConf.yaml", spec);
		assertEquals("Wrong deseralization", 3, conf.getTestConfiguration().getOperations().get(0).getTestParameters().get(0).getGenerators().get(0).getGenParameters().size());
		//System.out.println(TestConfigurationIO.toString(conf)); // Print to String
		TestConfigurationIO.toFile(conf, path + "testConf.yaml");
		assertTrue(checkIfExists("src/test/resources/Folder/testConf.yaml"));
	}

}
