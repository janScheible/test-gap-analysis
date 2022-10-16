package com.scheible.testgapanalysis.jacoco.resolver;

import java.util.Objects;
import java.util.regex.Pattern;

import com.scheible.testgapanalysis.jacoco.InstrumentedMethod;
import com.scheible.testgapanalysis.parser.ParsedMethod;

/**
 *
 * @author sj
 */
class TopLevelType {

	private static final String SLASH_PATTERN = Pattern.quote("/");

	private final String fullQualifiedName;

	private TopLevelType(String fullQualifiedName) {
		this.fullQualifiedName = fullQualifiedName;
	}

	static TopLevelType of(ParsedMethod method) {
		return new TopLevelType(method.getTopLevelTypeFqn());
	}

	static TopLevelType of(InstrumentedMethod method) {
		// JaCoCo uses slashes instead of dots
		String fullQualifiedName = method.getClassName().replaceAll(SLASH_PATTERN, ".");
		// JaCoCo also uses dollar notation for nested classes
		int dollarIndex = fullQualifiedName.indexOf('$');

		return new TopLevelType(dollarIndex > 0 ? fullQualifiedName.substring(0, dollarIndex) : fullQualifiedName);
	}

	public String getFullQualifiedName() {
		return this.fullQualifiedName;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof TopLevelType) {
			TopLevelType other = (TopLevelType) obj;
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
