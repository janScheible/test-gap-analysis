package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.INNER_CLASS_CONSTRUCTOR;

import java.util.Set;

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
		assertThat(resolve(SimpleConstructor.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static class ConstructorWithGenericParameter {

		public ConstructorWithGenericParameter(Set<String> values) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithGenericParameter() throws Exception {
		assertThat(resolve(ConstructorWithGenericParameter.class, CONSTRUCTOR)).isUnambiguouslyResolved();
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
		assertThat(resolve(ConstructorWithInitializer.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static class ConstructorWithGenericArgument<T> {

		public ConstructorWithGenericArgument(T arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithGenericArgument() throws Exception {
		assertThat(resolve(ConstructorWithGenericArgument.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	class ConstructorOfNonStaticInnerClass {

		ConstructorOfNonStaticInnerClass() {

		}
	}

	@Test
	public void testConstructorOfNonStaticInnerClass() throws Exception {
		assertThat(resolve(ConstructorOfNonStaticInnerClass.class, INNER_CLASS_CONSTRUCTOR)).isUnambiguouslyResolved();
	}
}
