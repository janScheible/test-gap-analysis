package com.scheible.testgapanalysis.git;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import com.scheible.testgapanalysis.common.FilesUtils;

/**
 *
 * @author sj
 */
public class GitDiffer {

	public RepositoryStatus ofWorkingCopyChanges(File workingDir, Predicate<String> fileFilter) {
		Set<String> addedFiles = new HashSet<>();
		Set<String> changedFiles = new HashSet<>();

		Ref head;
		File workTreeDir;

		try {
			try (Repository repository = GitUtils.open(workingDir)) {
				workTreeDir = repository.getWorkTree();
				try (Git git = new Git(repository)) {
					head = repository.exactRef(Constants.HEAD);

					Status status = git.status().call();

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
				changedFiles, fileFilter);
	}

	public RepositoryStatus ofCommitComparedToHead(File workingDir, String commitHash, Predicate<String> fileFilter) {
		return ofCommitsCompared(workingDir, commitHash, Constants.HEAD, fileFilter);
	}

	static RepositoryStatus ofCommitsCompared(File workingDir, String oldObjectId, String newObjectId,
			Predicate<String> fileFilter) {
		Set<String> addedFiles = new HashSet<>();
		Set<String> changedFiles = new HashSet<>();

		String resolvedNewObjectId;
		File workTreeDir;

		try {
			try (Repository repository = GitUtils.open(workingDir)) {
				workTreeDir = repository.getWorkTree();

				try (Git git = new Git(repository)) {
					resolvedNewObjectId = Constants.HEAD.equals(newObjectId)
							? repository.exactRef(Constants.HEAD).getObjectId().getName()
							: newObjectId;

					List<DiffEntry> diff = git.diff().setOldTree(GitUtils.prepareTreeParser(repository, oldObjectId))
							.setNewTree(GitUtils.prepareTreeParser(repository, resolvedNewObjectId)).call();

					for (DiffEntry entry : diff) {
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
				changedFiles, fileFilter);
	}

	/**
	 * Only include files that are in the working dir or in a sub-directory of working dir.
	 */
	private static RepositoryStatus filterFiles(File workingDir, File workTreeDir, String oldCommitHash,
			Optional<String> newCommitHash, Set<String> addedFiles, Set<String> changedFiles,
			Predicate<String> fileFilter) {
		String canonicalWorkingDir = FilesUtils.toCanonical(workingDir).getAbsolutePath() + File.separator;
		Predicate<String> isInWorkingDirSubDir = file -> FilesUtils.toCanonical(newFile(workTreeDir, file))
				.getAbsolutePath().startsWith(canonicalWorkingDir);

		Set<String> filteredAddedFiles = addedFiles.stream().filter(isInWorkingDirSubDir.and(fileFilter))
				.collect(Collectors.toSet());
		Set<String> filteredChangedFiles = changedFiles.stream().filter(isInWorkingDirSubDir.and(fileFilter))
				.collect(Collectors.toSet());

		return new RepositoryStatus(oldCommitHash, newCommitHash, filteredAddedFiles, filteredChangedFiles,
				GitUtils.getCommitedContents(workTreeDir, oldCommitHash, filteredChangedFiles),
				getNewContents(workTreeDir, newCommitHash, filteredAddedFiles, filteredChangedFiles));
	}

	private static Map<String, String> getNewContents(File workTreeDir, Optional<String> newCommitHash,
			Set<String> addedFiles, Set<String> changedFiles) {
		if (!newCommitHash.isPresent()) {
			return Collections.unmodifiableMap(Stream.concat(addedFiles.stream(), changedFiles.stream())
					.map(file -> new AbstractMap.SimpleImmutableEntry<>(file, newFile(workTreeDir, file)))
					.collect(Collectors.toMap(Map.Entry::getKey, entry -> FilesUtils.readUtf8(entry.getValue()))));
		} else {
			return GitUtils.getCommitedContents(workTreeDir, newCommitHash.get(), Collections.unmodifiableSet(
					Stream.concat(addedFiles.stream(), changedFiles.stream()).collect(Collectors.toSet())));
		}
	}

	// Extra method to allow a SpotBugs exclusion of PATH_TRAVERSAL_IN.
	private static File newFile(File parent, String child) {
		return new File(parent, child);
	}
}
