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
	private final State state;

	public NewOrChangedFile(String name, State state) {
		this.repositoryPath = name;
		this.state = state;
	}

	public String getName() {
		return this.repositoryPath;
	}

	public State getState() {
		return this.state;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof NewOrChangedFile) {
			NewOrChangedFile other = (NewOrChangedFile) obj;
			return Objects.equals(this.repositoryPath, other.repositoryPath) && this.state == other.state;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.repositoryPath, this.state);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("repositoryPath", this.repositoryPath).append("state", this.state)
				.build();
	}
}
