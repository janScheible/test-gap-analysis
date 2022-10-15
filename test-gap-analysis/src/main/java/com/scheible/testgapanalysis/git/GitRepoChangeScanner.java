package com.scheible.testgapanalysis.git;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 *
 * @author sj
 */
public class GitRepoChangeScanner {

	public enum PreviousType {
		BRANCH, TAG
	}

	private final PreviousStateFinder previousStateFinder;
	private final TreeDiffer treeDiffer;
	private final boolean javaFilesOnly;

	GitRepoChangeScanner(boolean javaFilesOnly) {
		this.previousStateFinder = new PreviousStateFinder();
		this.treeDiffer = new TreeDiffer();
		this.javaFilesOnly = javaFilesOnly;
	}

	public GitRepoChangeScanner() {
		this(true);
	}

	public GitChangeSet compareWorkingTreeWithHead(Path gitRepositorySubDir) {
		return compare(gitRepositorySubDir, findRepository(gitRepositorySubDir), Optional.empty());
	}

	public GitChangeSet compareHeadWithRepoState(Path gitRepositorySubDir, GitRepoState previousState) {
		return compare(gitRepositorySubDir, findRepository(gitRepositorySubDir), Optional.of(previousState));
	}

	public GitChangeSet compareHeadWithPrevious(Path gitRepositorySubDir, PreviousType type, String regEx) {
		try (Repository repository = findRepository(gitRepositorySubDir)) {
			GitRepoState previousState = this.previousStateFinder.getPreviousState(repository, type, regEx);
			return compare(gitRepositorySubDir, repository, Optional.of(previousState));
		}
	}

	private GitChangeSet compare(Path gitRepositorySubDir, Repository repository, Optional<GitRepoState> repoState) {
		try {
			String previousReferenceExpression = repoState.map(GitRepoState::getValue).orElse(Constants.HEAD);
			ObjectId previousObjectId = repository.resolve(previousReferenceExpression);
			if (previousObjectId == null) {
				throw new UnresolvableReferenceExpression(previousReferenceExpression);
			}
			ObjectId currentObjectId = repoState.isPresent() ? repository.resolve(Constants.HEAD) : null;

			Set<FileChange> changes = this.treeDiffer.scan(repository, previousObjectId, currentObjectId,
					buildFilter(repository.getWorkTree().toPath(), gitRepositorySubDir, this.javaFilesOnly));

			return new GitChangeSet(new GitRepoState(previousObjectId.name()),
					currentObjectId != null ? new GitRepoState(currentObjectId.name()) : GitRepoState.WORKING_TREE,
					changes);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static TreeFilter buildFilter(Path workingTree, Path workingTreeSubDir, boolean javaFilesOnly) {
		return AndTreeFilter.create(buildPathFilter(workingTree, workingTreeSubDir),
				javaFilesOnly ? PathSuffixFilter.create(".java") : TreeFilter.ALL);
	}

	static TreeFilter buildPathFilter(Path workingTree, Path workingTreeSubDir) {
		Path relativePath = workingTree.relativize(workingTreeSubDir);
		boolean isSubDir = !workingTree.resolve(relativePath).equals(workingTree);

		return isSubDir ? PathFilter.create(relativePath.toString().replace('\\', '/')) : TreeFilter.ALL;
	}

	private static Repository findRepository(Path gitRepositorySubDir) {
		try {
			return new FileRepositoryBuilder().findGitDir(gitRepositorySubDir.toFile()).setMustExist(true).build();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}
}
