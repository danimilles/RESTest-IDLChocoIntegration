package es.us.isa.restest.inputs;

import java.util.List;
import java.util.Random;

public interface ITestDataGenerator {
	Object nextValue();
	String nextValueAsString();

	/**
	 * Obtains the next generator to be used by a TestCaseGenerator. If the next case must be faulty, and there are faulty test
	 * data generators, this method could return a faulty generator. Otherwise, this method will return a nominal generator.
	 * @param nominalGens a list of nominal test data generators.
	 * @param faultyGens a list of faulty test data generators (can be empty).
	 * @param faulty this parameter determines if the test case must be faulty or not.
	 * @return the next generator.
	 */
	static ITestDataGenerator nextGenerator(List<ITestDataGenerator> nominalGens, List<ITestDataGenerator> faultyGens, Boolean faulty) {
		ITestDataGenerator generator;
		Random random = new Random();
		if(faulty && !faultyGens.isEmpty() && random.nextDouble() < 0.5f) {
			int index = random.nextInt(faultyGens.size());
			generator = faultyGens.get(index);
		} else {
			int index = random.nextInt(nominalGens.size());
			generator = nominalGens.get(index);
		}
		return generator;
	}
}
