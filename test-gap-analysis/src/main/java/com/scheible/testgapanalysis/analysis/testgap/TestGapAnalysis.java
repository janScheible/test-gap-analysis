package com.scheible.testgapanalysis.analysis.testgap;

import java.io.File;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.analysis.AnalysisResult;
import com.scheible.testgapanalysis.analysis.testgap.NewOrChangedFile.State;
import com.scheible.testgapanalysis.common.FilesUtils;
import com.scheible.testgapanalysis.git.GitChangeSet;
import com.scheible.testgapanalysis.git.GitRepoChangeScanner;
import com.scheible.testgapanalysis.git.GitRepoChangeScanner.PreviousType;
import com.scheible.testgapanalysis.git.GitRepoState;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class TestGapAnalysis {

	private final Analysis analysis;
	private final JaCoCoReportParser jaCoCoReportParser;
	private final GitRepoChangeScanner gitRepoChangeScanner;

	public TestGapAnalysis(Analysis analysis, JaCoCoReportParser jaCoCoReportParser,
			GitRepoChangeScanner gitRepoChangeScanner) {
		this.analysis = analysis;
		this.jaCoCoReportParser = jaCoCoReportParser;
		this.gitRepoChangeScanner = gitRepoChangeScanner;
	}

	public TestGapReport run(File workDir, File sourceDir, Set<File> jaCoCoReportFiles,
			Optional<String> referenceCommitHash, Optional<String> previousBranchRegEx,
			Optional<String> previousTagRegEx) {
		Set<MethodWithCoverageInfo> coverageInfo = this.jaCoCoReportParser.getMethodCoverage(jaCoCoReportFiles);

		Path sourceDirAsPath = FilesUtils.toCanonical(sourceDir).toPath();

		if ((previousBranchRegEx.isPresent() ? 1 : 0) + (previousTagRegEx.isPresent() ? 1 : 0)
				+ (referenceCommitHash.isPresent() ? 1 : 0) > 1) {
			throw new IllegalArgumentException("Reference commit hash, previous branch RegEx and previous tag RegEx "
					+ "are mutually exclusive and therefore only a single one can be passed.");
		}

		GitChangeSet changeSet;
		if (previousBranchRegEx.isPresent()) {
			changeSet = this.gitRepoChangeScanner.compareHeadWithPrevious(sourceDirAsPath, PreviousType.BRANCH,
					previousBranchRegEx.get());
		}
		if (previousTagRegEx.isPresent()) {
			changeSet = this.gitRepoChangeScanner.compareHeadWithPrevious(sourceDirAsPath, PreviousType.TAG,
					previousTagRegEx.get());
		} else if (referenceCommitHash.isPresent()) {
			changeSet = this.gitRepoChangeScanner.compareHeadWithRepoState(sourceDirAsPath,
					new GitRepoState(referenceCommitHash.get()));
		} else {
			changeSet = this.gitRepoChangeScanner.compareWorkingTreeWithHead(sourceDirAsPath);
		}

		Set<NewOrChangedFile> newOrChangedFiles = changeSet.getChanges().stream().filter(change -> !change.isDeletion())
				.map(change -> new NewOrChangedFile(change.getRelativePath(),
						change.isCreation() ? State.NEW : State.CHANGED))
				.collect(Collectors.toSet());

		CoverageResult coverageResult = performTestGapAnalysis(changeSet, coverageInfo);

		return TestGapReport.builder().setWorkDir(sourceDir.getAbsolutePath())
				.setOldCommitHash(changeSet.getPreviousState().getValue())
				.setNewCommitHash(Optional.ofNullable(changeSet.getCurrentState().equals(GitRepoState.WORKING_TREE)
						? null
						: changeSet.getCurrentState().getValue()))
				.setJaCoCoReportFiles(FilesUtils.toRelative(workDir, jaCoCoReportFiles))
				.setJaCoCoCoverageCount(coverageInfo.size()).setNewOrChangedFiles(newOrChangedFiles)
				.setCoveredMethods(coverageResult.coveredMethods).setUncoveredMethods(coverageResult.uncoveredMethods)
				.setEmptyMethods(coverageResult.emptyMethods).setUnresolvableMethods(coverageResult.unresolvableMethods)
				.setAmbiguouslyResolvedCoverage(coverageResult.ambiguouslyResolvedCoverage).build();
	}

	private CoverageResult performTestGapAnalysis(GitChangeSet changeSet, Set<MethodWithCoverageInfo> coverageInfo) {
		AnalysisResult result = this.analysis.perform(changeSet, coverageInfo);

		return new CoverageResult(toTestGapMethods(result.getCoveredMethods()),
				toTestGapMethods(result.getUncoveredMethods()), toTestGapMethods(result.getEmptyMethods()),
				toTestGapMethods(result.getUnresolvableMethods()),
				toAmbigouslyResolvedTestGapMethod(result.getAmbiguouslyResolvedCoverage()));
	}

	private static Set<TestGapMethod> toTestGapMethods(Map<ParsedMethod, MethodWithCoverageInfo> methodsWithCoverage) {
		return methodsWithCoverage.entrySet().stream().map(mwc -> toTestGapMethod(mwc.getKey(), mwc.getValue()))
				.collect(Collectors.toSet());
	}

	private static Set<TestGapMethod> toTestGapMethods(Set<ParsedMethod> methodsOnly) {
		return methodsOnly.stream().map(m -> toTestGapMethod(m, null)).collect(Collectors.toSet());
	}

	private static TestGapMethod toTestGapMethod(ParsedMethod method, MethodWithCoverageInfo coverage) {
		return coverage == null
				? new TestGapMethod(method.getTopLevelTypeFqn(), method.getDescription(), method.getFirstCodeLine(),
						method.getCodeColumn())
				: new TestGapMethod(method.getTopLevelTypeFqn(), method.getDescription(), method.getFirstCodeLine(),
						method.getCodeColumn(), coverage.getName(), coverage.getLine());
	}

	private static Map<CoverageReportMethod, Set<TestGapMethod>> toAmbigouslyResolvedTestGapMethod(
			Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage) {
		return ambiguouslyResolvedCoverage.entrySet().stream()
				.map(e -> new SimpleImmutableEntry<>(
						new CoverageReportMethod(e.getKey().getName(), e.getKey().getLine()),
						toTestGapMethods(e.getValue())))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private static class CoverageResult {

		private final Set<TestGapMethod> coveredMethods;
		private final Set<TestGapMethod> uncoveredMethods;

		private final Set<TestGapMethod> emptyMethods;
		private final Set<TestGapMethod> unresolvableMethods;
		private final Map<CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage;

		private CoverageResult(Set<TestGapMethod> coveredMethods, Set<TestGapMethod> uncoveredMethods,
				Set<TestGapMethod> emptyMethods, Set<TestGapMethod> unresolvableMethods,
				Map<CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage) {
			this.coveredMethods = coveredMethods;
			this.uncoveredMethods = uncoveredMethods;

			this.emptyMethods = emptyMethods;
			this.unresolvableMethods = unresolvableMethods;
			this.ambiguouslyResolvedCoverage = ambiguouslyResolvedCoverage;
		}
	}
}
