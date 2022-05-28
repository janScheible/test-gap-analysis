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

	private static class ClassWithSource {

		private final Class<?> testClass;
		private final Class<?> topLevelClass;
		private final String source;

		private ClassWithSource(Class<?> testClass, Class<?> topLevelClass, String source) {
			this.testClass = testClass;
			this.topLevelClass = topLevelClass;
			this.source = source;
		}
	}

	/**
	 * Resolves the passed Java class to the test sources ('src/test/java') for Java source parsing. Returns only
	 * methods of the tpye passed as filter.
	 */
	public static Set<ParsedMethod> parseJavaTestSource(final Class<?> testClass, final MethodType... filterTypes)
			throws IOException {
		final Set<MethodType> filterTypesSet = new HashSet<>(Arrays.asList(filterTypes));

		final ClassWithSource classWithSource = readJavaTestSource(testClass);
		final JavaParser javaParser = new JavaParser();
		final Set<ParsedMethod> parsedMethods = javaParser.getMethods(classWithSource.source).stream()
				.filter(m -> (!m.getScope().isEmpty() && m.getScope().get(0).equals(testClass.getSimpleName()))
						|| classWithSource.testClass.equals(classWithSource.topLevelClass))
				.filter(m -> filterTypesSet.contains(m.getMethodType())).collect(Collectors.toSet());
		return parsedMethods;
	}

	private static ClassWithSource readJavaTestSource(final Class<?> testClass) throws IOException {
		Class<?> topLevelClass = testClass;
		while (topLevelClass.getEnclosingClass() != null) {
			topLevelClass = topLevelClass.getEnclosingClass();
		}

		final Path path = Paths.get(".", "src", "test", "java",
				topLevelClass.getName().replaceAll(quote("."), quoteReplacement(File.separator)) + ".java");

		return new ClassWithSource(testClass, topLevelClass,
				new String(Files.readAllBytes(path), StandardCharsets.UTF_8));
	}
}
