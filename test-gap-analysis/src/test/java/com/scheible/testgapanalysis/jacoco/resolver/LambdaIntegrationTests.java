package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.LAMBDA_METHOD;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 *
 * @author sj
 */
public class LambdaIntegrationTests extends AbstractIntegrationTest {

	public static class SimpleLambda {

		public void doItLambda() {
			Predicate<String> ss = value -> value.isEmpty();
			ss.test("");
		}
	}

	@Test
	public void testSimpleLambda() throws Exception {
		assertThat(resolve(SimpleLambda.class, LAMBDA_METHOD)).isUnambiguouslyResolved();
	}

	public static class MultipleLambdaSingleLine {

		public void doItMultipleLambdaSingleLine() {
			execute(() -> ":-)", /* inline comment */ value -> value.trim());
		}

		private void execute(Supplier<String> supplier, Consumer<String> consumer) {
			consumer.accept(supplier.get());
		}
	}

	@Test
	public void testMultipleLambdaSingleLine() throws Exception {
		assertThat(resolve(MultipleLambdaSingleLine.class, LAMBDA_METHOD)).isUnambiguouslyResolved();
	}

	public static class MultipleLambdaSingleLineMultiLine {

		public void doItMultipleLambdaSingleLineMultiLine() {
			execute(() -> {
				return ":-)";
			}, /* inline comment */ value -> value.trim());
		}

		private void execute(Supplier<String> supplier, Consumer<String> consumer) {
			consumer.accept(supplier.get());
		}
	}

	@Test
	public void testMultipleLambdaSingleLineMultiLine() throws Exception {
		assertThat(resolve(MultipleLambdaSingleLineMultiLine.class, LAMBDA_METHOD)).isUnambiguouslyResolved();
	}

	public static class MultipleMethodsWithLambdas {

		public void doItLambda() {
			Predicate<String> ss = value -> value.isEmpty();
			ss.test("");
		}

		public void performLambda() {
			Predicate<String> bb = container -> container.isEmpty();
			bb.test("");
		}
	}

	@Test
	public void testMultipleMethodsWithLambdas() throws Exception {
		assertThat(resolve(MultipleMethodsWithLambdas.class, LAMBDA_METHOD)).isUnambiguouslyResolved();
	}
}
