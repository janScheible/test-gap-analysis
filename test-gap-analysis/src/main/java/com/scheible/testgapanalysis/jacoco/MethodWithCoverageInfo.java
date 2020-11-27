package com.scheible.testgapanalysis.jacoco;

import java.util.Objects;

/**
 *
 * @author sj
 */
public class MethodWithCoverageInfo implements Comparable<MethodWithCoverageInfo> {

	private final String typeFullyQualifiedName;
	private final String methodName;
	private final int coveredInstructionCount;

	public MethodWithCoverageInfo(final String typeFullyQualifiedName, final String methodName,
			final int coveredInstructionCount) {
		this.typeFullyQualifiedName = typeFullyQualifiedName;
		this.methodName = methodName;
		this.coveredInstructionCount = coveredInstructionCount;
	}

	public String getTypeFullyQualifiedName() {
		return typeFullyQualifiedName;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getCoveredInstructionCount() {
		return coveredInstructionCount;
	}

	@Override
	public int compareTo(final MethodWithCoverageInfo other) {
		if (typeFullyQualifiedName.equals(other.typeFullyQualifiedName)) {
			return methodName.compareTo(other.methodName);
		} else {
			return typeFullyQualifiedName.compareTo(other.typeFullyQualifiedName);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof MethodWithCoverageInfo) {
			final MethodWithCoverageInfo other = (MethodWithCoverageInfo) obj;
			return Objects.equals(this.typeFullyQualifiedName, other.typeFullyQualifiedName)
					&& Objects.equals(this.methodName, other.methodName)
					&& Objects.equals(this.coveredInstructionCount, other.coveredInstructionCount);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.typeFullyQualifiedName, this.methodName, this.coveredInstructionCount);
	}

	@Override
	public String toString() {
		return typeFullyQualifiedName + "#" + methodName + "@" + coveredInstructionCount;
	}
}
