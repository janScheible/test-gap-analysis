package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.CONSTRUCTOR;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class ConstructorIntegrationTests extends AbstractIntegrationTest {

	public static class SimpleConstructor {

		public SimpleConstructor() {
			"".trim();
		}
	}

	@Test
	public void testSimpleConstructor() throws Exception {
		assertThat(resolve(SimpleConstructor.class, CONSTRUCTOR).getUnresolved()).isEmpty();
	}

	public static class ConstructorWithInitializer {

		{
			"".trim();
		}

		public ConstructorWithInitializer() {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithInitializer() throws Exception {
		assertThat(resolve(ConstructorWithInitializer.class, CONSTRUCTOR).getUnresolved()).isEmpty();
	}
}
