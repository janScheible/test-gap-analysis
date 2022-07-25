package com.scheible.testgapanalysis.analysis.testgap;

import java.util.Objects;
import java.util.Optional;

import com.scheible.testgapanalysis.common.ToStringBuilder;

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
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TestGapMethod) {
			final TestGapMethod other = (TestGapMethod) obj;
			return Objects.equals(topLevelTypeFqn, other.topLevelTypeFqn)
					&& Objects.equals(description, other.description) && sourceLine == other.sourceLine
					&& sourceColumn == other.sourceColumn && Objects.equals(coveredMethodName, other.coveredMethodName)
					&& Objects.equals(coveredMethodLine, other.coveredMethodLine);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(topLevelTypeFqn, description, sourceLine, sourceColumn, coveredMethodName,
				coveredMethodLine);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("topLevelTypeFqn", topLevelTypeFqn)
				.append("description", description).append("sourceLine", sourceLine)
				.append("sourceColumn", sourceColumn).append("coveredMethodName", coveredMethodName)
				.append("coveredMethodLine", coveredMethodLine).build();
	}
}
