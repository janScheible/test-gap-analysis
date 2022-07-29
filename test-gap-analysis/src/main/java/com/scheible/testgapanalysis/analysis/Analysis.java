package com.scheible.testgapanalysis.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResolver;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResult;
import com.scheible.testgapanalysis.parser.JavaParser;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class Analysis {

	static final Predicate<MethodCompareWrapper> NON_GETTER_OR_SETTER_METHOD = mcw -> !mcw.getParsedMethod().getName()
			.startsWith("get") && !mcw.getParsedMethod().getName().startsWith("set");

	private final JavaParser javaParser;

	public Analysis(final JavaParser javaParser) {
		this.javaParser = javaParser;
	}

	public AnalysisResult perform(final RepositoryStatus status, final Set<MethodWithCoverageInfo> coverageInfo) {
		final Map<String, String> newOrChangedFilesWithContent = status.getNewContents();

		// all methods of new or changed files in the new state compared to the old state
		final Set<MethodCompareWrapper> newContentMethods = newOrChangedFilesWithContent.entrySet().stream()
				.flatMap(e -> this.javaParser.getMethods(e.getValue()).stream()).map(MethodCompareWrapper::new)
				.filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		// all methods of changed files in the new state that already existed in the old state
		final Set<MethodCompareWrapper> oldContentMethods = status.getOldContents().entrySet().stream()
				.flatMap(e -> this.javaParser.getMethods(e.getValue()).stream()).map(MethodCompareWrapper::new)
				.filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		// @formatter:off
		//  previous methods
		// |----------------|
		// |                |
		//
		// +----------------+
		// | deleted        |
		// |    +----------------+
		// |    | unchanged |    |
		// +----|-----------+    |
		//      |    new/changed |
		//      +----------------+
		//
		//      |                |
		//      |----------------|
		//       current methods
		// @formatter:on

		final Set<MethodCompareWrapper> unchangedMethods = new HashSet<>(newContentMethods);
		unchangedMethods.retainAll(oldContentMethods);

		final Set<MethodCompareWrapper> newOrChangedMethods = new HashSet<>(newContentMethods);
		newOrChangedMethods.removeAll(unchangedMethods);

		final CoverageResolver coverageResolver = CoverageResolver.with(coverageInfo);

		final CoverageResult coverageResult = coverageResolver
				.resolve(MethodCompareWrapper.unwrap(newOrChangedMethods));

		final Map<ParsedMethod, MethodWithCoverageInfo> uncoveredNewOrChangedMethods = coverageResult
				.getResolvedMethods().entrySet().stream().filter(e -> e.getValue().getCoveredInstructionCount() == 0)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		final Map<ParsedMethod, MethodWithCoverageInfo> coveredNewOrChangedMethods = new HashMap<>(
				coverageResult.getResolvedMethods());
		uncoveredNewOrChangedMethods.forEach(coveredNewOrChangedMethods::remove);

		return new AnalysisResult(coveredNewOrChangedMethods, uncoveredNewOrChangedMethods,
				coverageResult.getEmptyMethods(), coverageResult.getUnresolvedMethods(),
				coverageResult.getAmbiguousCoverage());
	}
}
