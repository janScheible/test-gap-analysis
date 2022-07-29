package com.scheible.testgapanalysis.analysis.testgap;

import java.util.Objects;

import com.scheible.testgapanalysis.common.ToStringBuilder;

/**
 *
 * @author sj
 */
public class CoverageReportMethod {

	private final String coveredMethodName;
	private final int coveredMethodLine;

	public CoverageReportMethod(String coveredMethodName, int coveredMethodLine) {
		this.coveredMethodName = coveredMethodName;
		this.coveredMethodLine = coveredMethodLine;
	}

	public String getCoveredMethodName() {
		return this.coveredMethodName;
	}

	public int getCoveredMethodLine() {
		return this.coveredMethodLine;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof CoverageReportMethod) {
			CoverageReportMethod other = (CoverageReportMethod) obj;
			return Objects.equals(this.coveredMethodName, other.coveredMethodName)
					&& this.coveredMethodLine == other.coveredMethodLine;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.coveredMethodName, this.coveredMethodLine);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("coveredMethodName", this.coveredMethodName)
				.append("coveredMethodLine", this.coveredMethodLine).build();
	}
}
