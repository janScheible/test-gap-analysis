package com.scheible.testgapanalysis.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 *
 * @author sj
 */
public class ToStringBuilderTest {

	@Test
	public void testSimpleCase() {
		assertThat(new ToStringBuilder(this.getClass()).append("test", 42).build())
				.isEqualTo("ToStringBuilderTest[test=42]");
	}

	@Test
	public void testMultipleValuesComma() {
		assertThat(new ToStringBuilder(this.getClass()).append("test", 42).append("foo", false).build())
				.isEqualTo("ToStringBuilderTest[test=42, foo=false]");
	}

	@Test
	public void testStringValueQuoting() {
		assertThat(new ToStringBuilder(this.getClass()).append("test", ":-)").build())
				.isEqualTo("ToStringBuilderTest[test=':-)']");
	}

	@Test
	public void testStringValueShortening() {
		assertThat(ToStringBuilder.shorten("abcdefg", 3)).isEqualTo("abc...");
		assertThat(ToStringBuilder.shorten("abcdefg", 7)).isEqualTo("abcdefg");
		assertThat(ToStringBuilder.shorten("abcdefg", 70)).isEqualTo("abcdefg");
	}
}
