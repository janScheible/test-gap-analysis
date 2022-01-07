package com.scheible.testgapanalysis.maven;

import com.scheible.testgapanalysis.DebugCoverageResolution;
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
@Mojo(name = "debug-coverage-resolution", threadSafe = true, requiresProject = false)
public class DebugCoverageResolutionMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.basedir}")
	private File workingDir;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		DebugCoverageResolution.run(workingDir);
	}
}
