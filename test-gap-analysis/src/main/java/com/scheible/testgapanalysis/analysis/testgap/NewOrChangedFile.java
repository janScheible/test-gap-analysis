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
		return this.repositoryPath;
	}

	public boolean isSkipped() {
		return this.skipped;
	}

	public State getState() {
		return this.state;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof NewOrChangedFile) {
			final NewOrChangedFile other = (NewOrChangedFile) obj;
			return Objects.equals(this.repositoryPath, other.repositoryPath) && this.skipped == other.skipped
					&& this.state == other.state;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.repositoryPath, this.skipped, this.state);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("repositoryPath", this.repositoryPath)
				.append("skipped", this.skipped).append("state", this.state).build();
	}
}
