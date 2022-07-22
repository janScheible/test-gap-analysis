package com.scheible.testgapanalysis.analysis.testgap;

import java.util.Optional;

/**
 *
 * @author sj
 */
public class TestGapMethod {

	private final String topLevelTypeFqn;
	private final String description;
	private final int sourceLine;
	private final int sourceColumn;
	private final Optional<String> coveredMethodName;
	private final Optional<Integer> coveredMethodLine;

	private TestGapMethod(final String topLevelTypeFqn, final String description, final int sourceLine,
			final int sourceColumn, final String coveredMethodName, final Integer coveredMethodLine) {
		this.topLevelTypeFqn = topLevelTypeFqn;
		this.description = description;
		this.sourceLine = sourceLine;
		this.sourceColumn = sourceColumn;
		this.coveredMethodName = Optional.ofNullable(coveredMethodName);
		this.coveredMethodLine = Optional.ofNullable(coveredMethodLine);
	}

	public TestGapMethod(final String topLevelTypeFqn, final String description, final int sourceLine,
			final int sourceColumn, final String coveredMethodName, final int coveredMethodLine) {
		this(topLevelTypeFqn, description, sourceLine, sourceColumn, coveredMethodName, (Integer) coveredMethodLine);
	}

	public TestGapMethod(final String topLevelTypeFqn, final String description, final int sourceLine,
			final int sourceColumn) {
		this(topLevelTypeFqn, description, sourceLine, sourceColumn, null, null);
	}

	public String getTopLevelTypeFqn() {
		return topLevelTypeFqn;
	}

	public String getDescription() {
		return description;
	}

	public int getSourceLine() {
		return sourceLine;
	}

	public int getSourceColumn() {
		return sourceColumn;
	}

	public Optional<String> getCoveredMethodName() {
		return coveredMethodName;
	}

	public Optional<Integer> getCoveredMethodLine() {
		return coveredMethodLine;
	}

	@Override
	public String toString() {
		final String coverageInfo = coveredMethodName.isPresent() && coveredMethodLine.isPresent()
				? String.format(" resolved to '%s' with line %d", coveredMethodName.get(), coveredMethodLine.get())
				: "";
		return String.format("%s%s at %d:%d%s", topLevelTypeFqn, description, sourceLine, sourceColumn, coverageInfo);
	}
}
