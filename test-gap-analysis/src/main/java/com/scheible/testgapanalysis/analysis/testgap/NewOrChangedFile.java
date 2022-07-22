package com.scheible.testgapanalysis.analysis.testgap;

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
	public String toString() {
		return String.format("[%s%s] %s", skipped ? "skipped, " : "", state == State.CHANGED ? "changed" : "new",
				repositoryPath);
	}
}
