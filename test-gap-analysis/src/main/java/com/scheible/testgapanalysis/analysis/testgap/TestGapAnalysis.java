package com.scheible.testgapanalysis.analysis.testgap;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.analysis.AnalysisResult;
import com.scheible.testgapanalysis.analysis.testgap.NewOrChangedFile.State;
import com.scheible.testgapanalysis.common.FilesUtils;
import com.scheible.testgapanalysis.git.GitDiffer;
import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
public class TestGapAnalysis {

	public static final Predicate<String> NON_TEST_JAVA_FILE = f -> f.endsWith(".java")
			&& !f.startsWith("src/test/java/") && !f.contains("/src/test/java/");

	private final Analysis analysis;
	private final JaCoCoReportParser jaCoCoReportParser;
	private final GitDiffer gitDiffer;

	public TestGapAnalysis(Analysis analysis, JaCoCoReportParser jaCoCoReportParser, GitDiffer gitDiffer) {
		this.analysis = analysis;
		this.jaCoCoReportParser = jaCoCoReportParser;
		this.gitDiffer = gitDiffer;
	}

	public TestGapReport run(File workDir, Set<File> jaCoCoReportFiles, Optional<String> referenceCommitHash) {
		Set<MethodWithCoverageInfo> coverageInfo = this.jaCoCoReportParser.getMethodCoverage(jaCoCoReportFiles);
		RepositoryResult repositoryResult = identifyFileChanges(referenceCommitHash, workDir);
		CoverageResult coverageResult = performTestGapAnalysis(repositoryResult.repositoryStatus, coverageInfo);

		return TestGapReport.builder().setWorkDir(workDir.getAbsolutePath())
				.setOldCommitHash(repositoryResult.repositoryStatus.getOldCommitHash())
				.setNewCommitHash(repositoryResult.repositoryStatus.getNewCommitHash())
				.setJaCoCoReportFiles(FilesUtils.toRelative(workDir, jaCoCoReportFiles))
				.setJaCoCoCoverageCount(coverageInfo.size()).setNewOrChangedFiles(repositoryResult.newOrChangedFiles)
				.setCoveredMethods(coverageResult.coveredMethods).setUncoveredMethods(coverageResult.uncoveredMethods)
				.setEmptyMethods(coverageResult.emptyMethods).setUnresolvableMethods(coverageResult.unresolvableMethods)
				.setAmbiguouslyResolvedCoverage(coverageResult.ambiguouslyResolvedCoverage).build();
	}

	private RepositoryResult identifyFileChanges(Optional<String> referenceCommitHash, File workDir) {
		Set<NewOrChangedFile> newOrChangedFiles = Collections.emptySet();

		RepositoryStatus status = referenceCommitHash
				.map(h -> this.gitDiffer.ofCommitComparedToHead(workDir, h, NON_TEST_JAVA_FILE))
				.orElseGet(() -> this.gitDiffer.ofWorkingCopyChanges(workDir, NON_TEST_JAVA_FILE));

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

	private CoverageResult performTestGapAnalysis(RepositoryStatus status, Set<MethodWithCoverageInfo> coverageInfo) {
		AnalysisResult result = this.analysis.perform(status, coverageInfo);

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
				.map(e -> new AbstractMap.SimpleImmutableEntry<>(
						new CoverageReportMethod(e.getKey().getName(), e.getKey().getLine()),
						toTestGapMethods(e.getValue())))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private static class RepositoryResult {

		private final RepositoryStatus repositoryStatus;
		private final Set<NewOrChangedFile> newOrChangedFiles;

		private RepositoryResult(RepositoryStatus repositoryStatus, Set<NewOrChangedFile> newOrChangedFiles) {
			this.repositoryStatus = repositoryStatus;
			this.newOrChangedFiles = newOrChangedFiles;
		}
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
