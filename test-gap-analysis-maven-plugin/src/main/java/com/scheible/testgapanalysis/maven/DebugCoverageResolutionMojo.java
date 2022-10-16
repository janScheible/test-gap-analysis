package com.scheible.testgapanalysis.maven;

import com.scheible.testgapanalysis.debug.DebugCoverageResolution;
import com.scheible.testgapanalysis.debug.DebugCoverageResolutionReport;
import com.scheible.testgapanalysis.jacoco.JaCoCoReportParser;
import com.scheible.testgapanalysis.parser.JavaParser;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 *
 * @author sj
 */
@Mojo(name = "debug-coverage-resolution", threadSafe = true, requiresProject = false)
public class DebugCoverageResolutionMojo extends AbstractTestGapMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (this.buildDir.exists()) {
			DebugCoverageResolution debugCoverageResolution = new DebugCoverageResolution(new JavaParser(), 
					new JaCoCoReportParser());
			DebugCoverageResolutionReport report = debugCoverageResolution.run(this.baseDir, this.sourceDir,
					findRelevantJaCoCoReportFiles());

			logReport(report);
		} else {
			getLog().debug(String.format("Skipping debug coverage resolution because the '%s' directory does not "
					+ "exist and therefore no JaCoCo coverage reports are available.", this.buildDir));
		}
	}

	private void logReport(DebugCoverageResolutionReport report) {
		if (report.getCoverageInfoCount() == 0) {
			getLog().info("No coverage info available!");
		} else {
			getLog().info(String.format("Found coverage info about %d methods in %s.", report.getCoverageInfoCount(),
					report.getJaCoCoReportFiles()));
		}

		getLog().info(String.format("Found %d Java files with %d methods in '%s'.", report.getJavaFileCount(),
				report.getResolved().size() + report.getUnresolved().size(), this.sourceDir));

		if (!report.getResolved().isEmpty()) {
			getLog().info("Resolved methods:");
			report.getResolved().entrySet()
					.forEach(e -> getLog().info(String.format(" - %s -> %s", e.getKey(), e.getValue())));
		}
		
		if (!report.getEmpty().isEmpty()) {
			getLog().info("Empty methods (no coverage information available):");
			report.getEmpty()
					.forEach(u -> getLog().info(String.format(" - %s", u)));
		}

		if (!report.getAmbiguousCoverage().isEmpty()) {
			getLog().info("Ambiguously resolved methods (multiple methods were resolved to a single coverage information):");
			report.getAmbiguousCoverage().entrySet()
					.forEach(e -> getLog().info(String.format(" - %s -> %s", e.getKey(), e.getValue())));
		}

		if (!report.getUnresolved().isEmpty()) {
			getLog().info("Unresolvable methods (coverage information couldn't be found):");
			report.getUnresolved()
					.forEach(u -> getLog().info(String.format(" - %s", u)));
		}
	}
}
