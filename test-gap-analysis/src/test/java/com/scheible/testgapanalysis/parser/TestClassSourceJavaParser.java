package com.scheible.testgapanalysis.parser;

import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.quote;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
public class TestClassSourceJavaParser {

	/**
	 * Resolves the passed Java class to the test sources ('src/test/java') for Java source parsing. Returns only
	 * methods of the tpye passed as filter.
	 */
	public static Set<ParsedMethod> parseJavaTestSource(final Class<?> testClass, final MethodType... filterTypes)
			throws IOException {
		final Set<MethodType> filterTypesSet = new HashSet<>(Arrays.asList(filterTypes));

		final Set<ParsedMethod> parsedMethods = JavaParserHelper.getMethods(readJavaTestSource(testClass)).stream()
				.filter(m -> !m.getScope().isEmpty() && m.getScope().get(0).equals(testClass.getSimpleName()))
				.filter(m -> filterTypesSet.contains(m.getMethodType())).collect(Collectors.toSet());
		return parsedMethods;
	}

	private static String readJavaTestSource(final Class<?> testClass) throws IOException {
		Class<?> topLevelClass = testClass;
		while (topLevelClass.getEnclosingClass() != null) {
			topLevelClass = topLevelClass.getEnclosingClass();
		}

		final Path path = Paths.get(".", "src", "test", "java",
				topLevelClass.getName().replaceAll(quote("."), quoteReplacement(File.separator)) + ".java");

		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	public static int getTestClassBeginLine(final Class<?> testClass) throws IOException {
		return JavaParserHelper.getClassBeginLine(readJavaTestSource(testClass), testClass.getSimpleName());
	}
}
