package com.scheible.testgapanalysis.git;

import static com.scheible.testgapanalysis.git.GitRepoChangeScanner.PreviousType.BRANCH;
import static com.scheible.testgapanalysis.git.GitRepoChangeScanner.PreviousType.TAG;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.zeroturnaround.zip.ZipUtil;

/**
 *
 * @author sj
 */
public class GitRepoChangeScannerIntegrationTest {

	private enum RepoState {

		INITIAL("initial commit", new GitRepoState("1b3c0408c2c6fc6f9f942e0c6627557eb2483573")), RELEASE_1_0_0(
				"first release",
				new GitRepoState("124556c581083cb3295e3a9b5f11d24f68326293")), RELEASE_1_1_0("second release",
						new GitRepoState("5e01fe715754217056bdab2167310360937c4c44")), HEAD("just a change",
								new GitRepoState("d7a129b64258d7cde488660cec26025533f3c2db")), WORKING_TREE(
										"working copy change", GitRepoState.WORKING_TREE);

		private final String content;
		private final GitRepoState gitRepoState;

		private RepoState(String content, GitRepoState gitRepoState) {
			this.content = content;
			this.gitRepoState = gitRepoState;
		}

		public boolean matchesContent(String content) {
			for (int i = 0; i < RepoState.values().length; i++) {
				boolean containsContent = content.contains(RepoState.values()[i].content);

				if ((i <= ordinal() && !containsContent) || (i > ordinal() && containsContent)) {
					return false;
				}
			}

			return true;
		}
	}

	@TempDir
	public static File temporaryFolder;

	// path to the unpacked git repo (that also contains remote refs for testing the branches and tags)
	private static Path GIT_REPO;

	private final GitRepoChangeScanner gitDiffer = new GitRepoChangeScanner(false);

	@BeforeAll
	public static void beforeClass() {
		InputStream testRepoZipInput = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(GitRepoChangeScannerIntegrationTest.class.getPackage().getName().replace('.', '/')
						+ "/change-test-repo.zip");
		ZipUtil.unpack(testRepoZipInput, temporaryFolder);
		GIT_REPO = temporaryFolder.toPath();
	}

	@Test
	public void testCompareWorkingTreeHeadWith() {
		GitChangeSet changeSet = gitDiffer.compareWorkingTreeWithHead(GIT_REPO);
		assertThat(changeSet).has(state().previous(RepoState.HEAD).current(RepoState.WORKING_TREE));
	}

	@Test
	public void testCompareHeadWithCommit() {
		GitChangeSet changeSet = gitDiffer.compareHeadWithRepoState(GIT_REPO, RepoState.RELEASE_1_0_0.gitRepoState);
		assertThat(changeSet).has(state().previous(RepoState.RELEASE_1_0_0).current(RepoState.HEAD));
	}

	@Test
	public void testCompareHeadWithPreviousCommit() {
		GitChangeSet changeSet = gitDiffer.compareHeadWithRepoState(GIT_REPO, new GitRepoState("HEAD^"));
		assertThat(changeSet).has(state().previous(RepoState.RELEASE_1_1_0).current(RepoState.HEAD));
	}

	@Test
	public void testCompareHeadWithPreviousBranch() {
		GitChangeSet changeSet = gitDiffer.compareHeadWithPrevious(GIT_REPO, BRANCH, "release-1\\.1\\.0");
		assertThat(changeSet).has(state().previous(RepoState.RELEASE_1_1_0).current(RepoState.HEAD));
	}

	@Test
	public void testCompareHeadWithPreviousTag() {
		GitChangeSet changeSet = gitDiffer.compareHeadWithPrevious(GIT_REPO, TAG, "v1\\.1\\.0");
		assertThat(changeSet).has(state().previous(RepoState.RELEASE_1_1_0).current(RepoState.HEAD));
	}

	private static PreviousStateBuilder state() {
		return previous -> current -> new Condition<GitChangeSet>() {
			@Override
			public boolean matches(GitChangeSet changeSet) {
				FileChange fileChange = changeSet.getChanges().isEmpty()
						? null
						: changeSet.getChanges().iterator().next();

				return changeSet.getPreviousState().equals(previous.gitRepoState)
						&& changeSet.getCurrentState().equals(current.gitRepoState)
						&& changeSet.getChanges().size() == 1
						&& previous.matchesContent(fileChange.getPreviousContent().get())
						&& current.matchesContent(fileChange.getCurrentContent().get());
			}
		};
	}

	private interface PreviousStateBuilder {

		CurrentStateBuilder previous(RepoState previous);
	}

	private interface CurrentStateBuilder {

		Condition<GitChangeSet> current(RepoState current);
	}
}
