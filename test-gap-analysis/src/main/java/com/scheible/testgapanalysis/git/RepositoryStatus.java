package com.scheible.testgapanalysis.git;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.scheible.testgapanalysis.common.ToStringBuilder;

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

	RepositoryStatus(String oldCommitHash, Optional<String> newCommitHash, Set<String> addedFiles,
			Set<String> changedFiles, Map<String, String> oldContents, Map<String, String> newContents) {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;

		this.addedFiles = Collections.unmodifiableSet(new HashSet<>(addedFiles));
		this.changedFiles = Collections.unmodifiableSet(new HashSet<>(changedFiles));

		this.oldContents = Collections.unmodifiableMap(new HashMap<>(oldContents));
		this.newContents = Collections.unmodifiableMap(new HashMap<>(newContents));
	}

	public String getOldCommitHash() {
		return this.oldCommitHash;
	}

	public Optional<String> getNewCommitHash() {
		return this.newCommitHash;
	}

	public Set<String> getAddedFiles() {
		return this.addedFiles;
	}

	public Set<String> getChangedFiles() {
		return this.changedFiles;
	}

	public Map<String, String> getOldContents() {
		return this.oldContents;
	}

	public Map<String, String> getNewContents() {
		return this.newContents;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof RepositoryStatus) {
			RepositoryStatus other = (RepositoryStatus) obj;
			return Objects.equals(this.oldCommitHash, other.oldCommitHash)
					&& Objects.equals(this.newCommitHash, other.newCommitHash)
					&& Objects.equals(this.addedFiles, other.addedFiles)
					&& Objects.equals(this.changedFiles, other.changedFiles)
					&& Objects.equals(this.oldContents, other.oldContents)
					&& Objects.equals(this.newContents, other.newContents);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.oldCommitHash, this.newCommitHash, this.addedFiles, this.changedFiles,
				this.oldContents, this.newContents);
	}

	@Override
	public String toString() {
		Function<Entry<String, String>, Entry<String, String>> shortenAndRemoveNewlines = entry //
		-> new SimpleImmutableEntry<>(entry.getKey(),
				ToStringBuilder.shorten(entry.getValue().replaceAll("\\R", ""), 30));
		Function<Map<String, String>, Map<String, String>> shortener = map -> map.entrySet().stream()
				.map(shortenAndRemoveNewlines).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		return new ToStringBuilder(getClass()).append("oldCommitHash", this.oldCommitHash)
				.append("newCommitHash", this.newCommitHash).append("addedFiles", this.addedFiles)
				.append("changedFiles", this.changedFiles).append("oldContents", shortener.apply(this.oldContents))
				.append("newContents", shortener.apply(this.newContents)).build();
	}
}
