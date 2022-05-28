package com.scheible.testgapanalysis.git;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author sj
 */
public class RepositoryStatus {

	private final String oldCommitHash;
	private final Optional<String> newCommitHash;

	private final Set<String> addedFiles;
	private final Set<String> changedFiles;

	private final Map<String, String> oldContents;
	private final Map<String, String> newContents;

	RepositoryStatus(final String oldCommitHash, final Optional<String> newCommitHash, final Set<String> addedFiles,
			final Set<String> changedFiles, final Map<String, String> oldContents,
			final Map<String, String> newContents) {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;

		this.addedFiles = Collections.unmodifiableSet(addedFiles);
		this.changedFiles = Collections.unmodifiableSet(changedFiles);

		this.oldContents = Collections.unmodifiableMap(oldContents);
		this.newContents = Collections.unmodifiableMap(newContents);
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

	public Map<String, String> getOldContents() {
		return oldContents;
	}

	public Map<String, String> getNewContents() {
		return newContents;
	}
}
