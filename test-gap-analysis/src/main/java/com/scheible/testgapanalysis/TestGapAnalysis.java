package com.scheible.testgapanalysis;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.git.RepositoryStatus;
import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;
import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;

/**
 *
 * @author sj
 */
public class TestGapAnalysis {

	private static final Logger logger = LoggerFactory.getLogger(TestGapAnalysis.class);

	public static void run(final Optional<String> referenceCommitHash) {
		logger.info("Comparing the {}", referenceCommitHash.map(h -> "repository head with commit " + h + ".")
				.orElseGet(() -> "working copy changes with the repository head."));

		final Set<MethodWithCoverageInfo> coverageInfo = JaCoCoHelper.findJaCoCoFiles().stream()
				.flatMap(f -> JaCoCoHelper.getMethodCoverage(f).stream()).collect(Collectors.toSet());
		if (coverageInfo.isEmpty()) {
			logger.info("No coverage info available!");
			return;
		}
		logger.info("Found coverage info about {} methods.", coverageInfo.size());

		final RepositoryStatus status = referenceCommitHash
				.map(h -> RepositoryStatus.ofCommitComparedToHead(Optional.empty(), h))
				.orElseGet(() -> RepositoryStatus.ofWorkingCopyChanges(Optional.empty()));

		Analysis.perform(status, coverageInfo);
	}
}
