package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.INNER_CLASS_CONSTRUCTOR;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.jacoco.resolver.ConstructorIntegrationTests.ConstructorOfNonStaticInnerClassWithParentTypeParameter.InnerClass;

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

	public static class ConstructorWithGenericArgumentExtendingClass<T extends Predicate<T> & Serializable> {

		public ConstructorWithGenericArgumentExtendingClass(T arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithGenericArgumentExtendingClass() throws Exception {
		assertThat(resolve(ConstructorWithGenericArgumentExtendingClass.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static class ConstructorWithUpperBoundedWildcardsGeneric {

		public ConstructorWithUpperBoundedWildcardsGeneric(List<? extends Runnable> arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithUpperBoundedWildcardsGeneric() throws Exception {
		assertThat(resolve(ConstructorWithUpperBoundedWildcardsGeneric.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static class ConstructorWithLocalGenericParameter {

		public <T> ConstructorWithLocalGenericParameter(T value) {
			"".trim();;
		}
	}

	@Test
	public void testConstructorWithLocalGenericParameter() throws Exception {
		assertThat(resolve(ConstructorWithLocalGenericParameter.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public class ConstructorOfNonStaticInnerClass {

		ConstructorOfNonStaticInnerClass() {
			"".trim();
		}
	}

	@Test
	public void testConstructorOfNonStaticInnerClass() throws Exception {
		assertThat(resolve(ConstructorOfNonStaticInnerClass.class, INNER_CLASS_CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static class ConstructorWithArrayArgs {

		ConstructorWithArrayArgs(String[] arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithArrayArgs() throws Exception {
		assertThat(resolve(ConstructorWithArrayArgs.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static class ConstructorOfNonStaticInnerClassWithParentTypeParameter<T> implements Supplier<T> {

		@Override
		public T get() {
			new InnerClass(null);
			return null;
		}

		public class InnerClass {

			public InnerClass(T value) {
				"".trim();
			}
		}
	}

	@Test
	public void testConstructorOfNonStaticInnerClassWithParentTypeParameter() throws Exception {
		assertThat(resolve(ConstructorOfNonStaticInnerClassWithParentTypeParameter.class, Supplier.class, s -> s.get(),
				INNER_CLASS_CONSTRUCTOR)).isUnambiguouslyResolved();
	}

	public static class ConstructorVarargsParameter {

		public ConstructorVarargsParameter(String... args) {
			"".trim();
		}
	}

	@Test
	public void testConstructorVarargsParameter() throws Exception {
		assertThat(resolve(ConstructorVarargsParameter.class, CONSTRUCTOR)).isUnambiguouslyResolved();
	}
}
