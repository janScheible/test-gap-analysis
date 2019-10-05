package com.scheible.testgapanalysis.maven;

import com.scheible.testgapanalysis.JaCoCoReportCleaner;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 *
 * @author sj
 */
@Mojo(name = "clean-jacoco-reports", threadSafe = true, requiresProject = false)
public class JaCoCoReportCleanerMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		JaCoCoReportCleaner.run();
	}
}
