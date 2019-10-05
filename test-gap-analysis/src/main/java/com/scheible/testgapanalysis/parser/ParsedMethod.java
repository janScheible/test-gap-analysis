package com.scheible.testgapanalysis.parser;

import java.util.Objects;

/**
 *
 * @author sj
 */
public class ParsedMethod {

	private final String typeFullyQualifiedName;
	private final String methodName;
	private final String hash;

	public ParsedMethod(final String typeFullyQualifiedName, final String methodName, final String hash) {
		this.typeFullyQualifiedName = typeFullyQualifiedName;
		this.methodName = methodName;
		this.hash = hash;
	}

	public String getTypeFullyQualifiedName() {
		return typeFullyQualifiedName;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getHash() {
		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof ParsedMethod) {
			final ParsedMethod other = (ParsedMethod) obj;
			return Objects.equals(this.typeFullyQualifiedName, other.typeFullyQualifiedName)
					&& Objects.equals(this.methodName, other.methodName) && Objects.equals(this.hash, other.hash);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.typeFullyQualifiedName, this.methodName, this.hash);
	}

	@Override
	public String toString() {
		return typeFullyQualifiedName + "#" + methodName + "@" + hash;
	}
}
