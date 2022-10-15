package com.scheible.testgapanalysis.git;

import java.util.Objects;

/**
 * A particular state of a Git repository identified by either an object id (a SHA-1 hash), a ref name (e.g.
 * 'refs/tags/v1.0.0', 'v1.0.0', 'HEAD' or 'HEAD^') or the special 'WORKING_TREE' identifier. See
 * https://mirrors.edge.kernel.org/pub/software/scm/git/docs/gitrevisions.html for more details.
 * 
 * @author sj
 */
public class GitRepoState {

	public static final GitRepoState WORKING_TREE = new GitRepoState("WORKING_TREE");

	private final String value;

	public GitRepoState(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof GitRepoState) {
			GitRepoState other = (GitRepoState) obj;
			return Objects.equals(this.value, other.value);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.value);
	}
}
