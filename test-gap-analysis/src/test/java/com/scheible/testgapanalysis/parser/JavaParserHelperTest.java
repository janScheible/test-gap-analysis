package com.scheible.testgapanalysis.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;

import com.scheible.testgapanalysis.common.Files2;

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
		final Set<ParsedMethod> methods = JavaParserHelper
				.getMethods(Files2.readUtf8(JavaParserHelper.class, "CoverageTestClass.java"));

		assertThat(methods).extracting(ParsedMethod::getTopLevelTypeFqn)
				.containsOnly("com.scheible.testgapanalysis._test.CoverageTestClass");

		final List<ParsedMethod> constructors = filter(methods, ParsedMethod::isConstructor);
		assertThat(constructors.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("<init>", 36), new MethodWithLine("<init>", 43));

		final List<ParsedMethod> initializers = filter(methods, ParsedMethod::isInitializer);
		assertThat(initializers.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("<initbl>", 26));

		final List<ParsedMethod> lambdas = filter(methods, ParsedMethod::isLambdaMethod);
		assertThat(lambdas.stream().map(JavaParserHelperTest::toMethodWithLine)).containsOnly(
				new MethodWithLine("lambda", 30), new MethodWithLine("doItLambda", "lambda", 52),
				new MethodWithLine("doItRunanble.Runnable.run", "lambda", 70),
				new MethodWithLine("doItMultipleLamdaSingleLine", "lambda", 83),
				new MethodWithLine("doItMultipleLamdaSingleLine", "lambda", 83),
				new MethodWithLine("doItMultipleLamdaSingleLineMultiLine", "lambda", 88),
				new MethodWithLine("doItMultipleLamdaSingleLineMultiLine", "lambda", 89));

		final List<ParsedMethod> instanceMethods = filter(methods, ParsedMethod::isMethod);
		assertThat(instanceMethods.stream().map(JavaParserHelperTest::toMethodWithLine)).containsOnly(
				new MethodWithLine("doIt", 48), new MethodWithLine("doItLambda", 52), new MethodWithLine("execute", 58),
				new MethodWithLine("doItRunanble", 65), new MethodWithLine("doItRunanble.Runnable", "run", 70),
				new MethodWithLine("doItMultipleLamdaSingleLine", 83),
				new MethodWithLine("doItMultipleLamdaSingleLineMultiLine", 87),
				new MethodWithLine("methodWithDoLoop", 107), new MethodWithLine("methodWithTryCatch", 100));

		final List<ParsedMethod> staticInitializers = filter(methods, ParsedMethod::isStaticInitializer);
		assertThat(staticInitializers.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("<clinit>", 22));

		final List<ParsedMethod> staticMethods = filter(methods, ParsedMethod::isStaticMethod);
		assertThat(staticMethods.stream().map(JavaParserHelperTest::toMethodWithLine))
				.containsOnly(new MethodWithLine("staticMethod", 95));
	}

	@Test
	public void testMethodMasking() {
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

	private static List<ParsedMethod> filter(final Collection<ParsedMethod> units,
			final Predicate<ParsedMethod> predicate) {
		return units.stream().filter(predicate).collect(Collectors.toList());
	}

	private static MethodWithLine toMethodWithLine(final ParsedMethod method) {
		return new MethodWithLine(method.getScope().stream().collect(Collectors.joining(".")), method.getName(),
				method.getFirstCodeLine());
	}
}
