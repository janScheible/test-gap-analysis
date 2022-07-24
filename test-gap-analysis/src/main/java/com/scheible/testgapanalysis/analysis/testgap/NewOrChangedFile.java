package com.scheible.testgapanalysis.analysis.testgap;

import java.util.Objects;

import com.scheible.testgapanalysis.common.ToStringBuilder;

/**
 *
 * @author sj
 */
public class NewOrChangedFile {

	public enum State {
		NEW, CHANGED
	}

	private final String repositoryPath;
	private final boolean skipped;
	private final State state;

	public NewOrChangedFile(final String name, final boolean skipped, final State state) {
		this.repositoryPath = name;
		this.skipped = skipped;
		this.state = state;
	}

	public String getName() {
		return repositoryPath;
	}

	public boolean isSkipped() {
		return skipped;
	}

	public State getState() {
		return state;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof NewOrChangedFile) {
			final NewOrChangedFile other = (NewOrChangedFile) obj;
			return Objects.equals(repositoryPath, other.repositoryPath) && skipped == other.skipped
					&& state == other.state;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(repositoryPath, skipped, state);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("repositoryPath", repositoryPath).append("skipped", skipped)
				.append("state", state).build();
	}
}
