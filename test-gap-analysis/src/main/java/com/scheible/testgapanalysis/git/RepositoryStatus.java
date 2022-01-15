package com.scheible.testgapanalysis.git;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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

import com.scheible.testgapanalysis.common.Files2;

/**
 *
 * @author sj
 */
public class RepositoryStatus {

	private final File workTreeDir;

	private final String oldCommitHash;
	private final Optional<String> newCommitHash;

	private final Set<String> addedFiles;
	private final Set<String> changedFiles;

	private RepositoryStatus(final File workTreeDir, final String oldCommitHash, final Optional<String> newCommitHash,
			final Set<String> addedFiles, final Set<String> changedFiles) {
		this.workTreeDir = workTreeDir;

		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;

		this.addedFiles = Collections.unmodifiableSet(addedFiles);
		this.changedFiles = Collections.unmodifiableSet(changedFiles);
	}

	public static RepositoryStatus ofWorkingCopyChanges(final File workingDir) {
		final Set<String> addedFiles = new HashSet<>();
		final Set<String> changedFiles = new HashSet<>();

		final Ref head;
		File workTreeDir;

		try {
			try (Repository repository = GitHelper.open(workingDir)) {
				workTreeDir = repository.getWorkTree();
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

		return filterFiles(workingDir, workTreeDir, head.getObjectId().getName(), Optional.empty(), addedFiles,
				changedFiles);
	}

	public static RepositoryStatus ofCommitComparedToHead(final File workingDir, final String commitHash) {
		return ofCommitsCompared(workingDir, commitHash, Constants.HEAD);
	}

	public static RepositoryStatus ofCommitsCompared(final File workingDir, final String oldObjectId,
			final String newObjectId) {
		final Set<String> addedFiles = new HashSet<>();
		final Set<String> changedFiles = new HashSet<>();

		String resolvedNewObjectId;
		File workTreeDir;

		try {
			try (Repository repository = GitHelper.open(workingDir)) {
				workTreeDir = repository.getWorkTree();

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

		return filterFiles(workingDir, workTreeDir, oldObjectId, Optional.of(resolvedNewObjectId), addedFiles,
				changedFiles);
	}

	/**
	 * Only include files that are in the working dir or in a sub-directory of working dir.
	 */
	private static RepositoryStatus filterFiles(final File workingDir, final File workTreeDir,
			final String oldCommitHash, final Optional<String> newCommitHash, final Set<String> addedFiles,
			final Set<String> changedFiles) {
		final String canonicalWorkingDir = Files2.toCanonical(workingDir).getAbsolutePath() + File.separator;
		final Predicate<String> isInWorkingDirSubDir = file -> Files2.toCanonical(appendChildFile(workTreeDir, file))
				.getAbsolutePath().startsWith(canonicalWorkingDir);

		return new RepositoryStatus(workTreeDir, oldCommitHash, newCommitHash,
				addedFiles.stream().filter(isInWorkingDirSubDir).collect(Collectors.toSet()),
				changedFiles.stream().filter(isInWorkingDirSubDir).collect(Collectors.toSet()));
	}

	private static File appendChildFile(final File parent, final String child) {
		return new File(parent, child);
	}

	public Map<String, String> getOldContents(final Predicate<String> fileFilter) {
		return GitHelper.getCommitedContents(workTreeDir, oldCommitHash,
				changedFiles.stream().filter(fileFilter).collect(Collectors.toSet()));
	}

	public Map<String, String> getNewContents(final Predicate<String> fileFilter) {
		if (!newCommitHash.isPresent()) {
			return Collections
					.unmodifiableMap(Stream.concat(addedFiles.stream(), changedFiles.stream()).filter(fileFilter)
							.map(file -> new SimpleImmutableEntry<>(file, appendChildFile(workTreeDir, file)))
							.collect(Collectors.toMap(Entry::getKey, entry -> Files2.readUtf8(entry.getValue()))));
		} else {
			return GitHelper.getCommitedContents(workTreeDir, newCommitHash.get(),
					Collections.unmodifiableSet(Stream.concat(addedFiles.stream(), changedFiles.stream())
							.filter(fileFilter).collect(Collectors.toSet())));
		}
	}

	public String getOldCommitHash() {
		return oldCommitHash;
	}

	public Optional<String> getNewCommitHash() {
		return newCommitHash;
	}

	public Set<String> getAddedFiles() {
		return addedFiles;
	}

	public Set<String> getChangedFiles() {
		return changedFiles;
	}
}
