package com.scheible.testgapanalysis.common;

/**
 * Simplistic toString() builder backed by a {@code StringBuilder}.
 *
 * @author sj
 */
public class ToStringBuilder {

	private final StringBuilder stringBuilder = new StringBuilder();

	private boolean appended = false;

	public ToStringBuilder(final Class<?> clazz) {
		stringBuilder.append(clazz.getSimpleName()).append('[');

	}

	public ToStringBuilder append(final String fieldName, final Object value) {
		if (appended) {
			stringBuilder.append(", ");
		}
		appended = true;

		stringBuilder.append(fieldName).append('=');

		if (value instanceof String) {
			stringBuilder.append('\'');
		}
		stringBuilder.append(value);
		if (value instanceof String) {
			stringBuilder.append('\'');
		}

		return this;
	}

	public String build() {
		stringBuilder.append(']');
		return stringBuilder.toString();
	}

	public static String shorten(final String value, final int maxLenght) {
		if (value.length() <= maxLenght) {
			return value;
		} else {
			return value.substring(0, maxLenght) + "...";
		}
	}
}
