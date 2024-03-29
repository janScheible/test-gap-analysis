package com.scheible.testgapanalysis.maven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scheible.testgapanalysis.analysis.testgap.TestGapAnalysis;
import com.scheible.testgapanalysis.analysis.testgap.TestGapReport;
import com.scheible.testgapanalysis.analysis.Analysis;
import com.scheible.testgapanalysis.analysis.testgap.CoverageReportMethod;
import com.scheible.testgapanalysis.analysis.testgap.NewOrChangedFile;
import com.scheible.testgapanalysis.analysis.testgap.TestGapMethod;
import com.scheible.testgapanalysis.git.GitRepoChangeScanner;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.parser.JavaParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author sj
 */
@Mojo(name = "perform", threadSafe = true, requiresProject = false)
public class TestGapAnalysisMojo extends AbstractTestGapMojo {

	@Parameter(property = "test-gap-analysis.reference-commit-hash")
	private String referenceCommitHash;
	
	@Parameter(property = "test-gap-analysis.previous-branch-regex")
	private String previousBranchRegEx;

	@Parameter(property = "test-gap-analysis.previous-tag-regex")
	private String previousTagRegEx;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.buildDir.exists()) {
			TestGapAnalysis testGapAnalysis = new TestGapAnalysis(new Analysis(new JavaParser()),
					new JaCoCoReportParser(), new GitRepoChangeScanner());
			TestGapReport report = testGapAnalysis.run(this.baseDir, this.sourceDir,
					findRelevantJaCoCoReportFiles(), Optional.ofNullable(this.referenceCommitHash),
					Optional.ofNullable(previousBranchRegEx), Optional.ofNullable(previousTagRegEx));

			logReport(report);
			writeJsonReport(report);
		} else {
			getLog().debug(String.format("Skipping test gap analysis because the '%s' directory does not exist and "
					+ "therefore no JaCoCo coverage reports are available.", this.buildDir));
		}
	}

	private void logReport(TestGapReport report) {
		getLog().info(String.format("Performing test gap analysis in '%s'.", report.getWorkDir()));

		if (report.getJaCoCoCoverageCount() == 0) {
			getLog().info("No coverage info available!");
		} else {
			getLog().info(String.format("Found coverage info about %d methods in %s.", report.getJaCoCoCoverageCount(),
					report.getJaCoCoReportFiles()));
		}

		String oldCommitHash = report.getPreviousState().substring(0, 7);
		getLog().info(String.format("Comparing the %s", report.getCurrentState()
				.map(newCommitHash -> "repository head (" + newCommitHash.substring(0, 7)
				+ ") with reference commit " + oldCommitHash + ".")
				.orElseGet(() -> "working copy changes with the repository head (" + oldCommitHash + ").")));

		if (report.getNewOrChangedFiles().isEmpty()) {
			getLog().info("No new or changed files!");
		} else {
			getLog().info(String.format("Found %d new or changed Java files:",
					report.getNewOrChangedFiles().size()));
			report.getNewOrChangedFiles().stream().sorted(Comparator.comparing(NewOrChangedFile::getName))
					.map(Object::toString).forEach(newOrChangedFile -> getLog().info(" - " + newOrChangedFile));
		}

		getLog().info("Method blacklist (excluded from coverage check): all getter and setter");

		getLog().info(String.format("Test gap: %d%%", (int)(report.getTestGap() * 100)));
		
		if (!report.getCoveredMethods().isEmpty()) {
			getLog().info("Covered methods:");
			report.getCoveredMethods().stream().sorted(getTestGapMethodComparator())
					.map(Object::toString).forEach(coveredMethod -> getLog().info(" - " + coveredMethod));
		}

		if (!report.getUncoveredMethods().isEmpty()) {
			getLog().info("Uncovered methods:");
			report.getUncoveredMethods().stream().sorted(getTestGapMethodComparator())
					.map(Object::toString).forEach(uncoveredMethod -> getLog().info(" - " + uncoveredMethod));
		}

		if (!report.getEmptyMethods().isEmpty()) {
			getLog().info("Empty methods (no coverage information available):");
			report.getEmptyMethods().stream().sorted(getTestGapMethodComparator())
					.map(Object::toString).forEach(emptyMethod -> getLog().info(" - " + emptyMethod));
		}

		if (!report.getAmbiguouslyResolvedCoverage().isEmpty()) {
			getLog().info("Ambiguously resolved methods (multiple methods were resolved to a single coverage information):");
			report.getAmbiguouslyResolvedCoverage().entrySet().stream()
					.sorted(Comparator.comparing(TestGapAnalysisMojo::getCoverageReportMethodCoveredClassName)
							.thenComparing(TestGapAnalysisMojo::getCoverageReportMethodMethodLine))
					.forEach(e -> getLog().info(String.format(" - %s -> %s", e.getKey(), e.getValue())));
		}

		if (!report.getUnresolvableMethods().isEmpty()) {
			getLog().info("Unresolvable methods (coverage information couldn't be found):");
			report.getUnresolvableMethods().stream().sorted(getTestGapMethodComparator())
					.map(Object::toString).forEach(unresolvableMethod -> getLog().info(" - " + unresolvableMethod));
		}
	}

	private static Comparator<TestGapMethod> getTestGapMethodComparator() {
		return Comparator.comparing(TestGapMethod::getTopLevelTypeFqn)
				.thenComparing(Comparator.comparing(TestGapMethod::getSourceLine));
	}
	
	private static String getCoverageReportMethodCoveredClassName(Entry<CoverageReportMethod, Set<TestGapMethod>> entry) {
		return entry.getKey().getCoveredClassName();
	}

	private static int getCoverageReportMethodMethodLine(Entry<CoverageReportMethod, Set<TestGapMethod>> entry) {
		return entry.getKey().getCoveredMethodLine();
	}	

	private void writeJsonReport(TestGapReport report) throws MojoExecutionException {
		File reportFile = new File(this.buildDir, "test-gap-report.json");
		Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting()
				.registerTypeAdapter(Optional.class, new OptionalTypeAdapter()).create();

		try {
			Files.write(reportFile.toPath(), gson.toJson(report).getBytes());
		} catch (IOException ex) {
			throw new MojoExecutionException("Can't write test gap report to '" + reportFile + "'!", ex);
		}
	}
}

