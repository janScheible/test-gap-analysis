package com.scheible.testgapanalysis.git;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

/**
 * Pretty weak tests... just executes the git code on the git repository of the project.
 * 
 * @author sj
 */
public class GitHelperTest {

	@Test
	public void testRepositoryStatusAndContent() {
		final RepositoryStatus status = RepositoryStatus.ofWorkingCopyChanges(Optional.empty());
		assertThat(status).isNotNull();

		final Map<String, String> content = status.getOldContents((file) -> true);
		assertThat(content).isNotNull();
	}

	@Test
	public void testCompareToHead() {
		final List<String> commitHashes = GitHelper.getCommitHashes(Optional.empty(), 5);
		final String commitHash = commitHashes.get(commitHashes.size() - 1);

		final RepositoryStatus status = RepositoryStatus.ofCommitComparedToHead(Optional.empty(), commitHash);

		assertThat(status.getAddedFiles()).isNotNull();
		assertThat(status.getChangedFiles()).isNotNull();
	}

	@Test
	public void testCompareToCommits() {
		final List<String> commitHashes = GitHelper.getCommitHashes(Optional.empty(), 5);
		final String oldCommitHash = commitHashes.get(commitHashes.size() - 1);
		final String newCommitHash = commitHashes.get(0);

		final RepositoryStatus status = RepositoryStatus.ofCommitsCompared(Optional.empty(), oldCommitHash,
				newCommitHash);

		assertThat(status.getAddedFiles()).isNotNull();
		assertThat(status.getChangedFiles()).isNotNull();
	}
}
