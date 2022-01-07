package com.scheible.testgapanalysis.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResolver;
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
	private static final Predicate<MethodCompareWrapper> NON_GETTER_OR_SETTER_METHOD = mcw -> !mcw.getParsedMethod()
			.getName().startsWith("get") && !mcw.getParsedMethod().getName().startsWith("set");

	public static Optional<AnalysisResult> perform(final RepositoryStatus status,
			final Set<MethodWithCoverageInfo> coverageInfo) {
		final Map<String, String> newOrChangedFilesWithContent = status.getNewContents(NON_TEST_JAVA_FILE);

		if (newOrChangedFilesWithContent.isEmpty()) {
			logger.info("No new or changed files!");
			return Optional.empty();
		}

		logger.info("Found {} new or changed Java files.", newOrChangedFilesWithContent.size());

		// all methods of new or changed files in the new state compared to the old state
		final Set<MethodCompareWrapper> newContentMethods = newOrChangedFilesWithContent.entrySet().stream()
				.flatMap(e -> JavaParserHelper.getMethods(e.getValue()).stream()).map(MethodCompareWrapper::new)
				.filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		// all methods of changed files in the new state that already existed in the old state
		final Set<MethodCompareWrapper> oldContentMethods = status.getOldContents(NON_TEST_JAVA_FILE).entrySet()
				.stream().flatMap(e -> JavaParserHelper.getMethods(e.getValue()).stream())
				.map(MethodCompareWrapper::new).filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		final Set<MethodCompareWrapper> unchangedMethods = new HashSet<>(newContentMethods);
		unchangedMethods.retainAll(oldContentMethods);

		final Set<MethodCompareWrapper> newOrChangedMethods = new HashSet<>(newContentMethods);
		newOrChangedMethods.removeAll(unchangedMethods);

		logger.info("New or changed methods (excluding setter and getter):");
		newOrChangedMethods.stream().sorted().map(MethodCompareWrapper::toString).forEach(m -> logger.info(" - {}", m));

		logger.info("Covered methods:");
		coverageInfo.stream().map(MethodWithCoverageInfo::toString)
				.limit(logger.isDebugEnabled() ? Integer.MAX_VALUE : 10).forEach(m -> logger.info(" - {}", m));
		if (coverageInfo.size() > 10 && !logger.isDebugEnabled()) {
			logger.info(" - ...");
		}

		final CoverageResolver coverageResolver = CoverageResolver.with(
				newContentMethods.stream().map(MethodCompareWrapper::getParsedMethod).collect(Collectors.toSet()),
				coverageInfo);

		final Set<ParsedMethod> uncoveredNewOrChangedMethods = coverageResolver
				.resolve(MethodCompareWrapper.unwrap(newOrChangedMethods)).getResolved().entrySet().stream()
				.filter(e -> e.getValue().getCoveredInstructionCount() == 0).map(Entry::getKey)
				.collect(Collectors.toSet());
		logger.info("Uncovered new or changed methods (excluding setter and getter):");
		uncoveredNewOrChangedMethods.stream().map(pm -> pm.getTopLevelTypeFqn() + "#" + pm.getName())
				.forEach(m -> logger.info(" - {}", m));
		if (uncoveredNewOrChangedMethods.isEmpty()) {
			logger.info("none :-)");
		}

		return Optional.of(new AnalysisResult(MethodCompareWrapper.unwrap(newContentMethods), coverageInfo,
				uncoveredNewOrChangedMethods));
	}
}
