package com.scheible.testgapanalysis.parser;

import static com.scheible.testgapanalysis.parser.JavaParserHelper.getClassBeginLine;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.ENUM_CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.LAMBDA_METHOD;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;
import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.getTestClassBeginLine;
import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.parseJavaTestSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Test;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public class JavaParserHelperTest {

	@Test
	public void testClassBeginLine() {
		assertThat(getClassBeginLine("package test;\n" + "\n" + "public class Foo {\n" + "}", "Foo")).isEqualTo(3);
	}

	public static class MethodParsing {

		public void doIt() {
			"".trim();
		}
	}

	@Test
	public void testMethodParsing() throws IOException {
		assertThat(parseMethods(MethodParsing.class, METHOD)).containsOnly(new AssertableMethod(METHOD, "doIt", 3));
	}

	public static class ConstructorParsing {

		private final int value = 42;

		public ConstructorParsing() {
			"".trim();
		}
	}

	@Test
	public void testConstructorParsing() throws IOException {
		assertThat(parseMethods(ConstructorParsing.class, CONSTRUCTOR))
				.containsOnly(new AssertableMethod(CONSTRUCTOR, "<init>", 5));
	}

	public static enum EnumConstructorParsing {

		TEST(42);

		private final int value;

		EnumConstructorParsing(int value) {
			this.value = value;
		}
	}

	@Test
	public void testEnumConstructorParsing() throws IOException {
		assertThat(parseMethods(EnumConstructorParsing.class, ENUM_CONSTRUCTOR))
				.containsOnly(new AssertableMethod(ENUM_CONSTRUCTOR, "<init>", 7));
	}

	public static class ConstructorWithGenericArgument<T> {

		public ConstructorWithGenericArgument(T arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithGenericArgument() throws IOException {
		assertThat(parseMethods(ConstructorWithGenericArgument.class, CONSTRUCTOR))
				.containsOnly(new AssertableMethod(CONSTRUCTOR, "<init>", 3)) //
				.first()
				.matches(am -> am.getParsedMethod().getArgumentTypes().equals(Optional.of(Arrays.asList("Object"))));
	}

	public static class ConstructorWithGenericArgumentExtendingType<T extends Runnable> {

		public ConstructorWithGenericArgumentExtendingType(T arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithGenericArgumentExtendingType() throws IOException {
		assertThat(parseMethods(ConstructorWithGenericArgumentExtendingType.class, CONSTRUCTOR))
				.containsOnly(new AssertableMethod(CONSTRUCTOR, "<init>", 3)) //
				.first()
				.matches(am -> am.getParsedMethod().getArgumentTypes().equals(Optional.of(Arrays.asList("Object"))));
	}

	public static class LambdaParsing {

		public void doItLambda() {
			Predicate<String> ss = value -> value.isEmpty();
			ss.test("");
		}
	}

	@Test
	public void testLambdaParsing() throws IOException {
		assertThat(parseMethods(LambdaParsing.class, LAMBDA_METHOD))
				.containsOnly(new AssertableMethod(LAMBDA_METHOD, "lambda", 3));
	}

	public static class LambdaParsingMultiLineParametersParsing {

		public void doItLambda() {
			BiFunction<String, String, Integer> ss = (first, //
					second) -> 42;
			ss.apply("a", "b");
		}
	}

	@Test
	public void testLambdaParsingMultiLineParametersParsing() throws IOException {
		assertThat(parseMethods(LambdaParsingMultiLineParametersParsing.class, LAMBDA_METHOD))
				.containsOnly(new AssertableMethod(LAMBDA_METHOD, "lambda", 4));
	}

	public static class MethodMasking { // #debug

		public String doIt(String arg1, final boolean isDebugMode) {
			// trim the string or jsut be happy
			return Optional.ofNullable(arg1).map(a -> /* make it short */ a.trim()).orElse(":-)");
		}
	}

	@Test
	public void testMethodMasking() throws IOException {
		final Set<ParsedMethod> methods = parseJavaTestSource(MethodMasking.class, ParsedMethod.MethodType.METHOD);

		assertThat(methods).extracting(pm -> pm.getRelevantCode().trim())
				.containsOnly("##public String doIt(String arg1, final boolean isDebugMode) {\n"
						+ "###################################\n"
						+ "return Optional.ofNullable(arg1).map(#################################).orElse(\":-)\");\n"
						+ "}");
	}

	private Stream<AssertableMethod> parseMethods(final Class<?> clazz, final MethodType... filterTypes)
			throws IOException {
		final int classBeginLine = getTestClassBeginLine(clazz);
		return parseJavaTestSource(clazz, filterTypes).stream().map(
				m -> new AssertableMethod(m, m.getMethodType(), m.getName(), m.getFirstCodeLine() - classBeginLine));
	}
}
