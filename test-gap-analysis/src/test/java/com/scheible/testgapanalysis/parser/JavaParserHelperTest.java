package com.scheible.testgapanalysis.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public class JavaParserHelperTest {

	static class MethodWithLine {

		final String scope;
		final String name;
		final int firstCodeLine;

		public MethodWithLine(String scope, String name, int firstCodeLine) {
			this.scope = scope;
			this.name = name;
			this.firstCodeLine = firstCodeLine;
		}

		public MethodWithLine(String name, int firstCodeLine) {
			this("", name, firstCodeLine);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof MethodWithLine) {
				final MethodWithLine other = (MethodWithLine) obj;
				return Objects.equals(this.scope, other.scope) && Objects.equals(this.name, other.name)
						&& Objects.equals(this.firstCodeLine, other.firstCodeLine);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(scope, name, firstCodeLine);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[scope='" + scope + "', name='" + name + "', firstCodeLine="
					+ firstCodeLine + "]";
		}
	}

	@Test
	public void testCoverageTestClassMethodParsing() throws IOException {
		final String code = readCoverageTestClass();

		final List<ParsedMethod> methods = JavaParserHelper.getMethods(code).stream()
				.sorted(((Comparator<ParsedMethod>) (a, b) -> a.getMethodType().compareTo(b.getMethodType()))
						.thenComparing((a, b) -> Integer.compare(a.getFirstCodeLine(), b.getFirstCodeLine())))
				.collect(Collectors.toList());

		assertThat(methods).extracting(ParsedMethod::getTopLevelTypeFqn)
				.containsOnly("com.scheible.testgapanalysis._test.CoverageTestClass");

		final List<ParsedMethod> constructors = filter(methods, MethodType.CONSTRUCTOR);
		assertThat(constructors.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("<init>", 36), new MethodWithLine("<init>", 43));

		final List<ParsedMethod> initializers = filter(methods, MethodType.INITIALIZER);
		assertThat(initializers.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("<initbl>", 26));

		final List<ParsedMethod> lambdas = filter(methods, MethodType.LAMBDA_METHOD);
		assertThat(lambdas.stream().map(JavaParserHelperTest::toMethodWithLine)).containsOnly(
				new MethodWithLine("lambda", 30), new MethodWithLine("doItLambda", "lambda", 52),
				new MethodWithLine("doItRunanble.Runnable.run", "lambda", 70),
				new MethodWithLine("doItMultipleLamdaSingleLine", "lambda", 83),
				new MethodWithLine("doItMultipleLamdaSingleLine", "lambda", 83),
				new MethodWithLine("doItMultipleLamdaSingleLineMultiLine", "lambda", 88),
				new MethodWithLine("doItMultipleLamdaSingleLineMultiLine", "lambda", 89));

		final List<ParsedMethod> instanceMethods = filter(methods, MethodType.METHOD);
		assertThat(instanceMethods.stream().map(JavaParserHelperTest::toMethodWithLine)).containsOnly(
				new MethodWithLine("doIt", 48), new MethodWithLine("doItLambda", 52), new MethodWithLine("execute", 58),
				new MethodWithLine("doItRunanble", 65), new MethodWithLine("doItRunanble.Runnable", "run", 70),
				new MethodWithLine("doItMultipleLamdaSingleLine", 83),
				new MethodWithLine("doItMultipleLamdaSingleLineMultiLine", 87));

		final List<ParsedMethod> staticInitializers = filter(methods, MethodType.STATIC_INITIALIZER);
		assertThat(staticInitializers.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("<clinit>", 22));

		final List<ParsedMethod> staticMethods = filter(methods, MethodType.STATIC_METHOD);
		assertThat(staticMethods.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("staticMethod", 95));
	}

	@Test
	public void testMethodParsing() {
		final Set<ParsedMethod> methods = JavaParserHelper.getMethods("" //
				+ "package com.scheible.testgapanalysis.parser;\n" //
				+ "\n" //
				+ "import java.util.Optional;\n" //
				+ "\n" //
				+ "public class ParserTestClass { //#debug\n" //
				+ "\n" //
				+ "	public String doIt(String arg1, final boolean isDebugMode) {\n" //
				+ "		// trim the string or jsut be happy\n" //
				+ "		return Optional.ofNullable(arg1).map(a -> /* make it short */ a.trim()).orElse(\":-)\");\n" //
				+ "	}\n" //
				+ "}\n" //
				+ "");

		assertThat(methods).extracting(pm -> pm.getRelevantCode().trim()).containsOnly(
				"#######################################a -> ################### a.trim()################",
				"#public String doIt(String arg1, final boolean isDebugMode) {\n" //
						+ "###################################\n" //
						+ "return Optional.ofNullable(arg1).map(#################################).orElse(\":-)\");\n" //
						+ "}");
	}

	private static List<ParsedMethod> filter(final List<ParsedMethod> units, final MethodType methodType) {
		return units.stream().filter(u -> u.getMethodType() == methodType).collect(Collectors.toList());
	}

	private static MethodWithLine toMethodWithLine(final ParsedMethod method) {
		return new MethodWithLine(method.getScope().stream().collect(Collectors.joining(".")), method.getName(),
				method.getFirstCodeLine());
	}

	private static String readCoverageTestClass() throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(
				JavaParserHelper.class.getResourceAsStream("CoverageTestClass.java"), StandardCharsets.UTF_8))) {
			return buffer.lines().collect(Collectors.joining("\n"));
		}
	}
}
