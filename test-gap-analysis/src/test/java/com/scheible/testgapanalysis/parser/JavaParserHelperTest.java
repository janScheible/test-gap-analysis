package com.scheible.testgapanalysis.parser;

import static com.scheible.testgapanalysis.parser.JavaParserHelper.getClassBeginLine;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.LAMBDA_METHOD;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;
import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.getTestClassBeginLine;
import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.parseJavaTestSource;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
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
		return parseJavaTestSource(clazz, filterTypes).stream()
				.map(m -> new AssertableMethod(m.getMethodType(), m.getName(), m.getFirstCodeLine() - classBeginLine));
	}
}
