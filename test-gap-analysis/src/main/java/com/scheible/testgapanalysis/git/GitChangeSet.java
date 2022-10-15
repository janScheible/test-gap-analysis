package com.scheible.testgapanalysis.git;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author sj
 */
public class GitChangeSet {

	private final GitRepoState previousState;
	private final GitRepoState currentState;

	private final Set<FileChange> changes;

	public GitChangeSet(GitRepoState previousState, GitRepoState currentState, Set<FileChange> changes) {
		this.previousState = previousState;
		this.currentState = currentState;
		this.changes = Collections.unmodifiableSet(new HashSet<>(changes));
	}

	public GitRepoState getPreviousState() {
		return this.previousState;
	}

	public GitRepoState getCurrentState() {
		return this.currentState;
	}

	public Set<FileChange> getChanges() {
		return this.changes;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof GitChangeSet) {
			GitChangeSet other = (GitChangeSet) obj;
			return super.equals(obj) && Objects.equals(this.previousState, other.previousState)
					&& Objects.equals(this.currentState, other.currentState)
					&& Objects.equals(this.changes, other.changes);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.previousState, this.currentState, this.changes);
	}
}
