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

	RepositoryStatus(final String oldCommitHash, final Optional<String> newCommitHash, final Set<String> addedFiles,
			final Set<String> changedFiles, final Map<String, String> oldContents,
			final Map<String, String> newContents) {
		this.oldCommitHash = oldCommitHash;
		this.newCommitHash = newCommitHash;

		this.addedFiles = Collections.unmodifiableSet(new HashSet<>(addedFiles));
		this.changedFiles = Collections.unmodifiableSet(new HashSet<>(changedFiles));

		this.oldContents = Collections.unmodifiableMap(new HashMap<>(oldContents));
		this.newContents = Collections.unmodifiableMap(new HashMap<>(newContents));
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

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof RepositoryStatus) {
			final RepositoryStatus other = (RepositoryStatus) obj;
			return Objects.equals(oldCommitHash, other.oldCommitHash)
					&& Objects.equals(newCommitHash, other.newCommitHash)
					&& Objects.equals(addedFiles, other.addedFiles) && Objects.equals(changedFiles, other.changedFiles)
					&& Objects.equals(oldContents, other.oldContents) && Objects.equals(newContents, other.newContents);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldCommitHash, newCommitHash, addedFiles, changedFiles, oldContents, newContents);
	}

	@Override
	public String toString() {
		final Function<Entry<String, String>, Entry<String, String>> shortenAndRemoveNewlines = entry //
		-> new SimpleImmutableEntry<>(entry.getKey(),
				ToStringBuilder.shorten(entry.getValue().replaceAll("\\R", ""), 30));
		final Function<Map<String, String>, Map<String, String>> shortener = map -> map.entrySet().stream()
				.map(shortenAndRemoveNewlines).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

		return new ToStringBuilder(getClass()).append("oldCommitHash", oldCommitHash)
				.append("newCommitHash", newCommitHash).append("addedFiles", addedFiles)
				.append("changedFiles", changedFiles).append("oldContents", shortener.apply(oldContents))
				.append("newContents", shortener.apply(newContents)).build();
	}
}
