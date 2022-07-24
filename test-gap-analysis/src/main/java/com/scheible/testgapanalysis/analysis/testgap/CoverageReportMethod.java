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

	public CoverageReportMethod(final String coveredMethodName, final int coveredMethodLine) {
		this.coveredMethodName = coveredMethodName;
		this.coveredMethodLine = coveredMethodLine;
	}

	public String getCoveredMethodName() {
		return coveredMethodName;
	}

	public int getCoveredMethodLine() {
		return coveredMethodLine;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof CoverageReportMethod) {
			final CoverageReportMethod other = (CoverageReportMethod) obj;
			return Objects.equals(coveredMethodName, other.coveredMethodName)
					&& coveredMethodLine == other.coveredMethodLine;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(coveredMethodName, coveredMethodLine);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("coveredMethodName", coveredMethodName)
				.append("coveredMethodLine", coveredMethodLine).build();
	}
}
