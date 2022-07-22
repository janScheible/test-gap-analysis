package com.scheible.testgapanalysis.analysis.testgap;

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
	public String toString() {
		return String.format("'%s' at line %d", coveredMethodName, coveredMethodLine);
	}
}
