package com.scheible.testgapanalysis.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.git.FileChange;
import com.scheible.testgapanalysis.git.GitChangeSet;
import com.scheible.testgapanalysis.jacoco.InstrumentedMethod;
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

	public Analysis(JavaParser javaParser) {
		this.javaParser = javaParser;
	}

	public AnalysisResult perform(GitChangeSet changeSet, Set<InstrumentedMethod> instrumentedMethods) {
		// all methods of new or changed files in the new state compared to the old state
		Set<MethodCompareWrapper> previousStateMethods = changeSet.getChanges().stream()
				.filter(change -> !change.isDeletion())
				.flatMap(change -> this.javaParser.getMethods(change.getCurrentContent().get()).stream())
				.map(MethodCompareWrapper::new).filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		// all methods of changed files in the new state that already existed in the old state
		Set<MethodCompareWrapper> currentStateMethods = changeSet.getChanges().stream().filter(FileChange::isChange)
				.flatMap(change -> this.javaParser.getMethods(change.getPreviousContent().get()).stream())
				.map(MethodCompareWrapper::new).filter(NON_GETTER_OR_SETTER_METHOD).collect(Collectors.toSet());

		// @formatter:off
		//  previous state methods
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
		//       current state methods
		// @formatter:on

		Set<MethodCompareWrapper> unchangedMethods = new HashSet<>(previousStateMethods);
		unchangedMethods.retainAll(currentStateMethods);

		Set<MethodCompareWrapper> newOrChangedMethods = new HashSet<>(previousStateMethods);
		newOrChangedMethods.removeAll(unchangedMethods);

		CoverageResolver coverageResolver = CoverageResolver.with(instrumentedMethods);

		CoverageResult coverageResult = coverageResolver.resolve(MethodCompareWrapper.unwrap(newOrChangedMethods));

		Map<ParsedMethod, InstrumentedMethod> uncoveredNewOrChangedMethods = coverageResult.getResolvedMethods()
				.entrySet().stream().filter(e -> e.getValue().getCoveredInstructionCount() == 0)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		Map<ParsedMethod, InstrumentedMethod> coveredNewOrChangedMethods = new HashMap<>(
				coverageResult.getResolvedMethods());
		uncoveredNewOrChangedMethods.forEach(coveredNewOrChangedMethods::remove);

		return new AnalysisResult(coveredNewOrChangedMethods, uncoveredNewOrChangedMethods,
				coverageResult.getEmptyMethods(), coverageResult.getUnresolvedMethods(),
				coverageResult.getAmbiguousCoverage());
	}
}
