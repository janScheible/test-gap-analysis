package com.scheible.testgapanalysis.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis._test.CoverageTestClass;
import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResolver;
import com.scheible.testgapanalysis.jacoco.resolver.CoverageResult;
import com.scheible.testgapanalysis.parser.JavaParserHelper;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class Analysis {

	static final Predicate<MethodCompareWrapper> NON_GETTER_OR_SETTER_METHOD = mcw -> !mcw.getParsedMethod().getName()
			.startsWith("get") && !mcw.getParsedMethod().getName().startsWith("set")
			&& !mcw.getParsedMethod().getTopLevelTypeFqn().equals(CoverageTestClass.class.getName());

	public static AnalysisResult perform(final RepositoryStatus status, final Predicate<String> fileFilter,
			final Set<MethodWithCoverageInfo> coverageInfo) {
		final Map<String, String> newOrChangedFilesWithContent = status.getNewContents(fileFilter);

		// all methods of new or changed files in the new state compared to the old state
		final Set<MethodCompareWrapper> newContentMethods = newOrChangedFilesWithContent.entrySet().stream()
				.flatMap(e -> JavaParserHelper.getMethods(e.getValue()).stream()).map(MethodCompareWrapper::new)
				.filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		// all methods of changed files in the new state that already existed in the old state
		final Set<MethodCompareWrapper> oldContentMethods = status.getOldContents(fileFilter).entrySet().stream()
				.flatMap(e -> JavaParserHelper.getMethods(e.getValue()).stream()).map(MethodCompareWrapper::new)
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

		final CoverageResolver coverageResolver = CoverageResolver.with(
				newContentMethods.stream().map(MethodCompareWrapper::getParsedMethod).collect(Collectors.toSet()),
				coverageInfo);

		final CoverageResult coverageResult = coverageResolver
				.resolve(MethodCompareWrapper.unwrap(newOrChangedMethods));

		final Set<ParsedMethod> uncoveredNewOrChangedMethods = coverageResult.getResolved().entrySet().stream()
				.filter(e -> e.getValue().getCoveredInstructionCount() == 0).map(Entry::getKey)
				.collect(Collectors.toSet());

		return new AnalysisResult(status.getOldCommitHash(), status.getNewCommitHash(),
				newOrChangedFilesWithContent.keySet(), MethodCompareWrapper.unwrap(newOrChangedMethods),
				coverageResult.getResolved(), uncoveredNewOrChangedMethods, coverageResult.getUnresolved());
	}
}
