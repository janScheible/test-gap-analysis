package com.scheible.testgapanalysis.parser;

import java.util.Objects;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
class AssertableMethod {

	final MethodType type;
	final String name;
	final int firstCodeLine;

	AssertableMethod(final MethodType type, final String name, final int firstCodeLine) {
		this.type = type;
		this.name = name;
		this.firstCodeLine = firstCodeLine;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof AssertableMethod) {
			final AssertableMethod other = (AssertableMethod) obj;
			return Objects.equals(this.type, other.type) && Objects.equals(this.name, other.name)
					&& Objects.equals(this.firstCodeLine, other.firstCodeLine);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, firstCodeLine);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type='" + type + "', name='" + name + "', firstCodeLine=" + firstCodeLine
				+ "]";
	}
}
