package com.scheible.testgapanalysis.jacoco;

import static com.scheible.testgapanalysis.jacoco.JaCoCoHelper.getMethodCoverage;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.scheible.testgapanalysis.common.Files2;

/**
 *
 * @author sj
 */
public class JaCoCoHelperTest {

	@Test
	public void testMethodCoverage() {
		assertThat(getMethodCoverage(Files2.readUtf8(JaCoCoHelperTest.class, "jacoco.xml")).size()).isEqualTo(83);
	}
}
