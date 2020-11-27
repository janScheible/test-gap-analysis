package com.scheible.testgapanalysis.maven;

import com.scheible.testgapanalysis.JaCoCoReportCleaner;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author sj
 */
@Mojo(name = "clean-jacoco-reports", threadSafe = true, requiresProject = false)
public class JaCoCoReportCleanerMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.basedir}")
	private File outputDirectory;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		JaCoCoReportCleaner.run(outputDirectory);
	}
}
