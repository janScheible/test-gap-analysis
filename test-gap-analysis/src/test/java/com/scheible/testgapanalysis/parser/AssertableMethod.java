package com.scheible.testgapanalysis.parser;

import java.util.Objects;

import com.scheible.testgapanalysis.common.ToStringBuilder;
import com.scheible.testgapanalysis.parser.ParsedMethod.MethodType;

/**
 *
 * @author sj
 */
class AssertableMethod {

	private final transient ParsedMethod parsedMethod;

	private final MethodType type;
	private final String name;

	AssertableMethod(MethodType type, String name) {
		this(null, type, name);
	}

	AssertableMethod(ParsedMethod parsedMethod, MethodType type, String name) {
		this.parsedMethod = parsedMethod;

		this.type = type;
		this.name = name;
	}

	public ParsedMethod getParsedMethod() {
		return this.parsedMethod;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof AssertableMethod) {
			AssertableMethod other = (AssertableMethod) obj;
			return Objects.equals(this.type, other.type) && Objects.equals(this.name, other.name);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.name);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(getClass()).append("type", this.type).append("name", this.name).build();
	}
}
