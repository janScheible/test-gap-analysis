package com.scheible.testgapanalysis.parser;

import static com.scheible.testgapanalysis.parser.JavaParserHelper.getMethods;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 *
 * @author sj
 */
public class JavaParserHelperTest {

	@Test
	public void testMethodParsing() {
		assertThat(getMethods("package bla.blub; public class Test { private void doIt() {}}")).hasSize(1).element(0)
				.satisfies(pm -> assertThat(pm.getMethodName()).isEqualTo("doIt"));
	}
}
