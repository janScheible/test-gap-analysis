package com.scheible.testgapanalysis.parser;

import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.ENUM_CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.INNER_CLASS_CONSTRUCTOR;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.LAMBDA_METHOD;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;
import static com.scheible.testgapanalysis.parser.TestClassSourceJavaParser.parseJavaTestSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.util.Maps.newHashMap;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public class JavaParserTest {

	public static class MethodParsing {

		public void doIt() {
			"".trim();
		}
	}

	@Test
	public void testMethodParsing() throws IOException {
		assertThat(parseMethods(MethodParsing.class, METHOD)).containsOnly(new AssertableMethod(METHOD, "doIt"));
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
				.containsOnly(new AssertableMethod(CONSTRUCTOR, "<init>"));
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
				.containsOnly(new AssertableMethod(ENUM_CONSTRUCTOR, "<init>"));
	}

	public static class ConstructorWithGenericTypeParamter<T> {

		public ConstructorWithGenericTypeParamter(T arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithGenericTypeParamter() throws IOException {
		assertThat(parseMethods(ConstructorWithGenericTypeParamter.class, CONSTRUCTOR))
				.containsOnly(new AssertableMethod(CONSTRUCTOR, "<init>")) //
				.first().matches(am -> am.getParsedMethod().getTypeParameters().equals(newHashMap("T", "Object")),
						"has parent type parameters");
	}

	public static class ConstructorWithGenericTypeParameterExtendingType<T extends Runnable> {

		public ConstructorWithGenericTypeParameterExtendingType(T arg) {
			"".trim();
		}
	}

	@Test
	public void testConstructorWithGenericTypeParameterExtendingType() throws IOException {
		assertThat(parseMethods(ConstructorWithGenericTypeParameterExtendingType.class, CONSTRUCTOR))
				.containsOnly(new AssertableMethod(CONSTRUCTOR, "<init>")) //
				.first().matches(am -> am.getParsedMethod().getTypeParameters().equals(newHashMap("T", "Runnable")),
						"has parent type parameters");
	}

	public class InnerClassConstructor {

		public InnerClassConstructor(Object arg) {
			"".trim();
		}
	}

	@Test
	public void testInnerClassConstructor() throws IOException {
		assertThat(parseMethods(InnerClassConstructor.class, INNER_CLASS_CONSTRUCTOR))
				.containsOnly(new AssertableMethod(INNER_CLASS_CONSTRUCTOR, "<init>")) //
				.first()
				.matches(am -> am.getParsedMethod().getOuterDeclaringType().equals(Optional.of("JavaParserTest")),
						"has outer declaring type");
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
				.containsOnly(new AssertableMethod(LAMBDA_METHOD, "lambda"));
	}

	@Test
	public void testRecordParsing() {
		// records require Java 16, test-gap source code is Java 8 --> parse from string
		assertThat(new JavaParser().getMethods("class Foo<J> {\n" + //
				"	record TestRecord<T>(String value) { \n" + //
				"		TestRecord(T val) {\n" + //
				"			this(null);\n" + //
				"		}\n" + //
				"	}\n" + //
				"}")).first().satisfies(pm -> {
					assertThat(pm.getScope()).containsOnly("TestRecord");
					assertThat(pm.getTypeParameters()).contains(entry("T", "Object"));
				});
	}

	public static class MethodMasking { // #debug

		public String doIt(String arg1, boolean isDebugMode) {
			// trim the string or jsut be happy
			return Optional.ofNullable(arg1).map(a -> /* make it short */ a.trim()).orElse(":-)");
		}
	}

	@Test
	public void testMethodMasking() throws IOException {
		Set<ParsedMethod> methods = parseJavaTestSource(MethodMasking.class, ParsedMethod.MethodType.METHOD);

		assertThat(methods).extracting(pm -> pm.getRelevantCode().trim())
				.containsOnly("##public String doIt(String arg1, boolean isDebugMode) {\n"
						+ "###################################\n"
						+ "return Optional.ofNullable(arg1).map(#################################).orElse(\":-)\");\n"
						+ "}");
	}

	private Stream<AssertableMethod> parseMethods(Class<?> clazz, MethodType... filterTypes) throws IOException {
		return parseJavaTestSource(clazz, filterTypes).stream().map(JavaParserTest::toAssertableMethod);
	}

	private static AssertableMethod toAssertableMethod(ParsedMethod parsedMethod) {
		return new AssertableMethod(parsedMethod, parsedMethod.getMethodType(), parsedMethod.getName());
	}
}
