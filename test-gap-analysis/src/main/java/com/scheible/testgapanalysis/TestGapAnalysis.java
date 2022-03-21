package com.scheible.testgapanalysis;

import static java.util.Collections.emptySet;

import java.io.File;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.scheible.testgapanalysis.TestGapReport.CoverageReportMethod;
import com.scheible.testgapanalysis.TestGapReport.NewOrChangedFile;
import com.scheible.testgapanalysis.TestGapReport.NewOrChangedFile.State;
import com.scheible.testgapanalysis.TestGapReport.TestGapMethod;
import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.analysis.AnalysisResult;
import com.scheible.testgapanalysis.common.Files2;
import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class TestGapAnalysis {

	private static class RepositoryResult {

		private final RepositoryStatus repositoryStatus;
		private final Set<NewOrChangedFile> newOrChangedFiles;

		private RepositoryResult(final RepositoryStatus repositoryStatus,
				final Set<NewOrChangedFile> newOrChangedFiles) {
			this.repositoryStatus = repositoryStatus;
			this.newOrChangedFiles = newOrChangedFiles;
		}
	}

	private static class CoverageResult {

		private final Set<TestGapMethod> coveredMethods;
		private final Set<TestGapMethod> uncoveredMethods;

		private final Set<TestGapMethod> unresolvableMethods;
		private final Map<TestGapReport.CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage;

		private CoverageResult(final Set<TestGapMethod> coveredMethods, final Set<TestGapMethod> uncoveredMethods,
				final Set<TestGapMethod> unresolvableMethods,
				final Map<TestGapReport.CoverageReportMethod, Set<TestGapMethod>> ambiguouslyResolvedCoverage) {
			this.coveredMethods = coveredMethods;
			this.uncoveredMethods = uncoveredMethods;

			this.unresolvableMethods = unresolvableMethods;
			this.ambiguouslyResolvedCoverage = ambiguouslyResolvedCoverage;
		}
	}

	public static final Predicate<String> NON_TEST_JAVA_FILE = f -> f.endsWith(".java")
			&& !f.startsWith("src/test/java/") && !f.contains("/src/test/java/");

	public static TestGapReport run(final File workDir, final Set<File> jaCoCoReportFiles,
			final Optional<String> referenceCommitHash) {
		final Set<MethodWithCoverageInfo> coverageInfo = JaCoCoHelper.getMethodCoverage(jaCoCoReportFiles);
		final RepositoryResult repositoryResult = identifyFileChanges(referenceCommitHash, workDir);
		final CoverageResult coverageResult = performTestGapAnalysis(repositoryResult.repositoryStatus, coverageInfo);

		return new TestGapReport(workDir.getAbsolutePath(), repositoryResult.repositoryStatus.getOldCommitHash(),
				repositoryResult.repositoryStatus.getNewCommitHash(), Files2.toRelative(workDir, jaCoCoReportFiles),
				coverageInfo.size(), repositoryResult.newOrChangedFiles, coverageResult.coveredMethods,
				coverageResult.uncoveredMethods, coverageResult.unresolvableMethods,
				coverageResult.ambiguouslyResolvedCoverage);
	}

	private static RepositoryResult identifyFileChanges(final Optional<String> referenceCommitHash,
			final File workDir) {
		Set<NewOrChangedFile> newOrChangedFiles = emptySet();

		final RepositoryStatus status = referenceCommitHash
				.map(h -> RepositoryStatus.ofCommitComparedToHead(workDir, h))
				.orElseGet(() -> RepositoryStatus.ofWorkingCopyChanges(workDir));

		if (!(status.getAddedFiles().isEmpty() && status.getChangedFiles().isEmpty())) {
			newOrChangedFiles = Stream.concat(status.getChangedFiles().stream(), status.getAddedFiles().stream())
					.sorted()
					.map(newOrChangedFile -> new NewOrChangedFile(newOrChangedFile,
							!NON_TEST_JAVA_FILE.test(newOrChangedFile),
							status.getChangedFiles().contains(newOrChangedFile) ? State.CHANGED : State.NEW))
					.collect(Collectors.toSet());
		}

		return new RepositoryResult(status, newOrChangedFiles);
	}

	private static CoverageResult performTestGapAnalysis(final RepositoryStatus status,
			final Set<MethodWithCoverageInfo> coverageInfo) {
		final AnalysisResult result = Analysis.perform(status, NON_TEST_JAVA_FILE, coverageInfo);

		return new CoverageResult(toTestGapMethods(result.getCoveredMethods()),
				toTestGapMethods(result.getUncoveredMethods()), toTestGapMethods(result.getUnresolvableMethods()),
				toAmbigouslyResolvedTestGapMethod(result.getAmbiguouslyResolvedCoverage()));
	}

	private static Set<TestGapMethod> toTestGapMethods(
			final Map<ParsedMethod, MethodWithCoverageInfo> methodsWithCoverage) {
		return methodsWithCoverage.entrySet().stream().map(mwc -> toTestGapMethod(mwc.getKey(), mwc.getValue()))
				.collect(Collectors.toSet());
	}

	private static Set<TestGapMethod> toTestGapMethods(final Set<ParsedMethod> methodsOnly) {
		return methodsOnly.stream().map(m -> toTestGapMethod(m, null)).collect(Collectors.toSet());
	}

	private static TestGapMethod toTestGapMethod(final ParsedMethod method, final MethodWithCoverageInfo coverage) {
		return coverage == null
				? new TestGapReport.TestGapMethod(method.getTopLevelTypeFqn(), method.getDescription(),
						method.getCodeLine(), method.getCodeColumn())
				: new TestGapReport.TestGapMethod(method.getTopLevelTypeFqn(), method.getDescription(),
						method.getCodeLine(), method.getCodeColumn(), coverage.getName(), coverage.getLine());
	}

	private static Map<CoverageReportMethod, Set<TestGapMethod>> toAmbigouslyResolvedTestGapMethod(
			final Map<MethodWithCoverageInfo, Set<ParsedMethod>> ambiguouslyResolvedCoverage) {
		return ambiguouslyResolvedCoverage.entrySet().stream()
				.map(e -> new AbstractMap.SimpleImmutableEntry<>(
						new CoverageReportMethod(e.getKey().getName(), e.getKey().getLine()),
						toTestGapMethods(e.getValue())))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
}
