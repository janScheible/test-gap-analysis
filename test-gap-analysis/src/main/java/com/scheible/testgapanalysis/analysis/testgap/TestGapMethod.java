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

	private TestGapMethod(String topLevelTypeFqn, String description, int sourceLine, int sourceColumn,
			String coveredMethodName, Integer coveredMethodLine) {
		this.topLevelTypeFqn = topLevelTypeFqn;
		this.description = description;
		this.sourceLine = sourceLine;
		this.sourceColumn = sourceColumn;
		this.coveredMethodName = Optional.ofNullable(coveredMethodName);
		this.coveredMethodLine = Optional.ofNullable(coveredMethodLine);
	}

	public TestGapMethod(String topLevelTypeFqn, String description, int sourceLine, int sourceColumn,
			String coveredMethodName, int coveredMethodLine) {
		this(topLevelTypeFqn, description, sourceLine, sourceColumn, coveredMethodName, (Integer) coveredMethodLine);
	}

	public TestGapMethod(String topLevelTypeFqn, String description, int sourceLine, int sourceColumn) {
		this(topLevelTypeFqn, description, sourceLine, sourceColumn, null, null);
	}

	public String getTopLevelTypeFqn() {
		return this.topLevelTypeFqn;
	}

	public String getDescription() {
		return this.description;
	}

	public int getSourceLine() {
		return this.sourceLine;
	}

	public int getSourceColumn() {
		return this.sourceColumn;
	}

	public Optional<String> getCoveredMethodName() {
		return this.coveredMethodName;
	}

	public Optional<Integer> getCoveredMethodLine() {
		return this.coveredMethodLine;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TestGapMethod) {
			TestGapMethod other = (TestGapMethod) obj;
			return Objects.equals(this.topLevelTypeFqn, other.topLevelTypeFqn)
					&& Objects.equals(this.description, other.description) && this.sourceLine == other.sourceLine
					&& this.sourceColumn == other.sourceColumn
					&& Objects.equals(this.coveredMethodName, other.coveredMethodName)
					&& Objects.equals(this.coveredMethodLine, other.coveredMethodLine);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.topLevelTypeFqn, this.description, this.sourceLine, this.sourceColumn,
				this.coveredMethodName, this.coveredMethodLine);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("topLevelTypeFqn", this.topLevelTypeFqn)
				.append("description", this.description).append("sourceLine", this.sourceLine)
				.append("sourceColumn", this.sourceColumn).append("coveredMethodName", this.coveredMethodName)
				.append("coveredMethodLine", this.coveredMethodLine).build();
	}
}
