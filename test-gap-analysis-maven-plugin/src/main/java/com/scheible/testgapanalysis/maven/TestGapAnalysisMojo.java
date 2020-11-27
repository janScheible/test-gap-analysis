package com.scheible.testgapanalysis.maven;

import com.scheible.testgapanalysis.TestGapAnalysis;
import java.io.File;
import java.util.Optional;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author sj
 */
@Mojo(name = "perform", threadSafe = true, requiresProject = false)
public class TestGapAnalysisMojo extends AbstractMojo {

	@Parameter(property = "test-gap-analysis.reference-commit-hash")
	private String referenceCommitHash;
	
	@Parameter(defaultValue = "${project.basedir}")
	private File outputDirectory;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		TestGapAnalysis.run(outputDirectory, Optional.ofNullable(referenceCommitHash));
	}
}
