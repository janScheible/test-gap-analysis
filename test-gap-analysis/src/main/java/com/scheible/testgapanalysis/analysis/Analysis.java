package com.scheible.testgapanalysis.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.JavaParserHelper;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class Analysis {

	private static final Logger logger = LoggerFactory.getLogger(Analysis.class);

	private static final Predicate<String> NON_TEST_JAVA_FILE = f -> f.endsWith(".java")
			&& !f.startsWith("src/test/java/") && !f.contains("/src/test/java/");
	private static final Predicate<ParsedMethod> NON_GETTER_OR_SETTER_METHOD = pm -> !pm.getMethodName()
			.startsWith("get") && !pm.getMethodName().startsWith("set");

	public static Optional<AnalysisResult> perform(final RepositoryStatus status,
			final Set<MethodWithCoverageInfo> coverageInfo) {
		final Map<String, String> newOrChangedFilesWithContent = status.getNewContents(NON_TEST_JAVA_FILE);
		final Set<ParsedMethod> newOrChangedMethods = newOrChangedFilesWithContent.entrySet().stream()
				.flatMap(e -> JavaParserHelper.getMethods(e.getValue()).stream()).filter(NON_GETTER_OR_SETTER_METHOD)
				.collect(Collectors.toSet());

		if (newOrChangedFilesWithContent.isEmpty()) {
			logger.info("No new or changed files!");
			return Optional.empty();
		}

		logger.info("Found {} new or changed Java files.", newOrChangedFilesWithContent.size());

		final Set<ParsedMethod> changedFileLastCommitMethods = status.getOldContents(NON_TEST_JAVA_FILE).entrySet()
				.stream().flatMap(e -> JavaParserHelper.getMethods(e.getValue()).stream())
				.filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		final Set<ParsedMethod> unchangedMethods = new HashSet<>(newOrChangedMethods);
		unchangedMethods.retainAll(changedFileLastCommitMethods);

		newOrChangedMethods.removeAll(unchangedMethods);

		final Set<String> coveredMethods = coverageInfo.stream().filter(ci -> ci.getCoveredInstructionCount() > 0)
				.map(ci -> ci.getTypeFullyQualifiedName() + "#" + ci.getMethodName()).collect(Collectors.toSet());

		logger.info("New or changed methods (excluding setter and getter):");
		newOrChangedMethods.stream().sorted().map(ParsedMethod::toString).forEach(m -> logger.info(" - {}", m));

		logger.info("Covered methods:");
		coverageInfo.stream().sorted().map(MethodWithCoverageInfo::toString)
				.limit(logger.isDebugEnabled() ? Integer.MAX_VALUE : 10).forEach(m -> logger.info(" - {}", m));
		if (coverageInfo.size() > 10 && !logger.isDebugEnabled()) {
			logger.info(" - ...");
		}

		final Set<ParsedMethod> uncoveredNewOrChangedMethods = newOrChangedMethods.stream()
				.filter(m -> !coveredMethods.contains(m.getTypeFullyQualifiedName() + "#" + m.getMethodName()))
				.collect(Collectors.toSet());
		logger.info("Uncovered new or changed methods (excluding setter and getter):");
		uncoveredNewOrChangedMethods.stream().sorted()
				.map(pm -> pm.getTypeFullyQualifiedName() + "#" + pm.getMethodName())
				.forEach(m -> logger.info(" - {}", m));
		if (uncoveredNewOrChangedMethods.isEmpty()) {
			logger.info("none :-)");
		}

		return Optional.of(new AnalysisResult(newOrChangedMethods, coverageInfo, uncoveredNewOrChangedMethods));
	}
}
