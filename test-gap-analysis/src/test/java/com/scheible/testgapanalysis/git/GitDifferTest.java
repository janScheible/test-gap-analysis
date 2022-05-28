package com.scheible.testgapanalysis.git;

import static com.scheible.testgapanalysis.common.FilesUtils.getWorkingDir;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Pretty weak tests... just executes the git code on the git repository of the project.
 *
 * @author sj
 */
public class GitDifferTest {

	private final GitDiffer gitDiffer = new GitDiffer();

	@Test
	public void testRepositoryStatusAndContent() {
		final RepositoryStatus status = gitDiffer.ofWorkingCopyChanges(getWorkingDir(), file -> true);
		assertThat(status).isNotNull();

		final Map<String, String> content = status.getOldContents();
		assertThat(content).isNotNull();
	}

	@Test
	public void testCompareToHead() {
		final List<String> commitHashes = GitUtils.getCommitHashes(getWorkingDir(), 5);
		final String commitHash = commitHashes.get(commitHashes.size() - 1);

		final RepositoryStatus status = gitDiffer.ofCommitComparedToHead(getWorkingDir(), commitHash, file -> true);

		assertThat(status.getAddedFiles()).isNotNull();
		assertThat(status.getChangedFiles()).isNotNull();
	}

	@Test
	public void testCompareToCommits() {
		final List<String> commitHashes = GitUtils.getCommitHashes(getWorkingDir(), 5);
		final String oldCommitHash = commitHashes.get(commitHashes.size() - 1);
		final String newCommitHash = commitHashes.get(0);

		final RepositoryStatus status = GitDiffer.ofCommitsCompared(getWorkingDir(), oldCommitHash, newCommitHash,
				file -> true);

		assertThat(status.getAddedFiles()).isNotNull();
		assertThat(status.getChangedFiles()).isNotNull();
	}
}
