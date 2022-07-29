package com.scheible.testgapanalysis.jacoco.resolver;

import java.util.Objects;
import java.util.regex.Pattern;

import com.scheible.testgapanalysis.jacoco.MethodWithCoverageInfo;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
class TopLevelType {

	private static final String SLASH_PATTERN = Pattern.quote("/");

	private final String fullQualifiedName;

	private TopLevelType(final String fullQualifiedName) {
		this.fullQualifiedName = fullQualifiedName;
	}

	static TopLevelType of(final ParsedMethod parsedMethod) {
		return new TopLevelType(parsedMethod.getTopLevelTypeFqn());
	}

	static TopLevelType of(final MethodWithCoverageInfo methodWithCoverageInfo) {
		// JaCoCo uses slashes instead of dots
		final String fullQualifiedName = methodWithCoverageInfo.getClassName().replaceAll(SLASH_PATTERN, ".");
		// JaCoCo also uses dollar notation for nested classes
		final int dollarIndex = fullQualifiedName.indexOf('$');

		return new TopLevelType(dollarIndex > 0 ? fullQualifiedName.substring(0, dollarIndex) : fullQualifiedName);
	}

	public String getFullQualifiedName() {
		return this.fullQualifiedName;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof TopLevelType) {
			final TopLevelType other = (TopLevelType) obj;
			return Objects.equals(this.fullQualifiedName, other.fullQualifiedName);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.fullQualifiedName);
	}

	@Override
	public String toString() {
		return this.fullQualifiedName;
	}
}
