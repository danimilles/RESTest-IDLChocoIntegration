package es.us.isa.restest.configuration;

import es.us.isa.restest.configuration.pojos.Generator;
import org.junit.Test;
import static org.junit.Assert.*;

import es.us.isa.restest.configuration.TestConfigurationIO;
import es.us.isa.restest.configuration.pojos.TestConfigurationObject;

public class TestConfigurationTest {

	@Test
	public void testLoadConfiguration() {
		String path = "src/main/resources/TestConfigurationMetamodel/configuration-model";
		TestConfigurationObject conf = TestConfigurationIO.loadConfiguration(path +".yaml");
		assertEquals("Wrong deserialization", 2, conf.getTestConfiguration().getTestPaths().get(0).getOperations().get(0).getTestParameters().get(0).getGenerators().get(0).getGenParameters().size());
		//System.out.println(TestConfigurationIO.toString(conf)); // Print to String
		TestConfigurationIO.toFile(conf, path + "-output.yaml");
	}

	@Test
	public void testLoadConfigurationMultipleGenerators() {
		String path = "src/main/resources/TestConfigurationMetamodel/configuration-model-multiple-generators";
		TestConfigurationObject conf = TestConfigurationIO.loadConfiguration(path + ".yaml");
		assertEquals("Wrong deserialization", 2, conf.getTestConfiguration().getTestPaths().get(0).getOperations().get(0).getTestParameters().get(0).getGenerators().size());
		for(Generator g : conf.getTestConfiguration().getTestPaths().get(0).getOperations().get(0).getTestParameters().get(0).getGenerators()) {
			assertEquals("Wrong deserialization", 2, g.getGenParameters().size());
		}
		TestConfigurationIO.toFile(conf, path + "-output.yaml");
	}

}
