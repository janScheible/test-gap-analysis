package com.scheible.testgapanalysis.git;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author sj
 */
public class RepositoryStatus {

	private final Optional<File> currentDir;

	private final String oldCommitHash;
	private final Optional<String> newCommitHash;

	private final Set<String> addedFiles;
	private final Set<String> changedFiles;

	public RepositoryStatus(final Optional<File> currentDir, final String oldCommitHash,
			final Optional<String> newCommitHash, final Set<String> addedFiles, final Set<String> changedFiles) {
		this.currentDir = currentDir;

		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;

		this.addedFiles = Collections.unmodifiableSet(addedFiles);
		this.changedFiles = Collections.unmodifiableSet(changedFiles);
	}

	public static RepositoryStatus ofWorkingCopyChanges(final Optional<File> currentDir) {
		final Set<String> addedFiles = new HashSet<>();
		final Set<String> changedFiles = new HashSet<>();

		final Ref head;

		try {
			try (Repository repository = GitHelper.open(currentDir)) {
				try (Git git = new Git(repository)) {
					head = repository.exactRef(Constants.HEAD);

					final Status status = git.status().call();

					addedFiles.addAll(status.getAdded());
					addedFiles.addAll(status.getUntracked());

					changedFiles.addAll(status.getModified());
					changedFiles.removeAll(status.getAdded());
				} catch (GitAPIException ex2) {
					throw new IllegalStateException(ex2);
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		return new RepositoryStatus(currentDir, head.getObjectId().getName(), Optional.empty(), addedFiles,
				changedFiles);
	}

	public static RepositoryStatus ofCommitComparedToHead(final Optional<File> currentDir, final String commitHash) {
		return ofCommitsCompared(currentDir, commitHash, Constants.HEAD);
	}

	public static RepositoryStatus ofCommitsCompared(final Optional<File> currentDir, final String oldObjectId,
			final String newObjectId) {
		final Set<String> addedFiles = new HashSet<>();
		final Set<String> changedFiles = new HashSet<>();

		String resolvedNewObjectId;

		try {
			try (Repository repository = GitHelper.open(currentDir)) {
				try (Git git = new Git(repository)) {
					resolvedNewObjectId = Constants.HEAD.equals(newObjectId)
							? repository.exactRef(Constants.HEAD).getObjectId().getName()
							: newObjectId;

					final List<DiffEntry> diff = git.diff()
							.setOldTree(GitHelper.prepareTreeParser(repository, oldObjectId))
							.setNewTree(GitHelper.prepareTreeParser(repository, resolvedNewObjectId)).call();

					for (final DiffEntry entry : diff) {
						if (entry.getChangeType() == DiffEntry.ChangeType.ADD
								|| entry.getChangeType() == DiffEntry.ChangeType.COPY) {
							addedFiles.add(entry.getNewPath());
						} else if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY
								|| entry.getChangeType() == DiffEntry.ChangeType.RENAME) {
							changedFiles.add(entry.getNewPath());
						}
					}
				} catch (GitAPIException ex2) {
					throw new IllegalStateException(ex2);
				}
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		return new RepositoryStatus(currentDir, oldObjectId, Optional.of(resolvedNewObjectId), addedFiles,
				changedFiles);
	}

	public Map<String, String> getOldContents(final Predicate<String> fileFilter) {
		return GitHelper.getCommitedContents(currentDir, oldCommitHash,
				changedFiles.stream().filter(fileFilter).collect(Collectors.toSet()));
	}

	public Map<String, String> getNewContents(final Predicate<String> fileFilter) {
		if (!newCommitHash.isPresent()) {
			return Collections
					.unmodifiableMap(Stream.concat(addedFiles.stream(), changedFiles.stream()).filter(fileFilter)
							.collect(Collectors.toMap(Function.identity(), GitHelper::readFromWorkingCopyUtf8)));
		} else {
			return GitHelper.getCommitedContents(currentDir, newCommitHash.get(),
					Collections.unmodifiableSet(Stream.concat(addedFiles.stream(), changedFiles.stream())
							.filter(fileFilter).collect(Collectors.toSet())));
		}
	}

	public Set<String> getAddedFiles() {
		return addedFiles;
	}

	public Set<String> getChangedFiles() {
		return changedFiles;
	}
}
