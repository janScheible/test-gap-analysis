package com.scheible.testgapanalysis.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.junit.jupiter.api.Test;

/**
 *
 * @author sj
 */
public class GitRepoChangeScannerTest {

	@Test
	public void testFilterNoPathFilter() {
		assertThat(GitRepoChangeScanner.buildPathFilter(Paths.get("temp"), Paths.get("temp")))
				.isEqualTo(TreeFilter.ALL);
	}

	@Test
	public void testFilterPathFilterSingleDir() {
		assertThat(GitRepoChangeScanner.buildPathFilter(Paths.get("temp"), Paths.get("temp", "foo")))
				.isInstanceOfSatisfying(PathFilter.class,
						pathFilter -> assertThat(pathFilter.getPath()).isEqualTo("foo"));
	}

	@Test
	public void testFilterPathFilterMultipleDir() {
		assertThat(GitRepoChangeScanner.buildPathFilter(Paths.get("temp"), Paths.get("temp", "foo", "bar")))
				.isInstanceOfSatisfying(PathFilter.class,
						pathFilter -> assertThat(pathFilter.getPath()).isEqualTo("foo/bar"));
	}
}
