package com.scheible.testgapanalysis.parser;

import java.util.Objects;

import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
class AssertableMethod {

	final transient ParsedMethod parsedMethod;

	final MethodType type;
	final String name;

	AssertableMethod(final MethodType type, final String name) {
		this(null, type, name);
	}

	AssertableMethod(final ParsedMethod parsedMethod, final MethodType type, final String name) {
		this.parsedMethod = parsedMethod;

		this.type = type;
		this.name = name;
	}

	public ParsedMethod getParsedMethod() {
		return parsedMethod;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof AssertableMethod) {
			final AssertableMethod other = (AssertableMethod) obj;
			return Objects.equals(this.type, other.type) && Objects.equals(this.name, other.name);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type='" + type + "', name='" + name + "']";
	}
}
