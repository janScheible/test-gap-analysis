package com.scheible.testgapanalysis.jacoco;

import static com.scheible.testgapanalysis.jacoco.JaCoCoHelper.getIsNotChildOfSubDirsPredicate;
import static com.scheible.testgapanalysis.jacoco.JaCoCoHelper.getMethodCoverage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;

import java.nio.file.Paths;

import org.junit.Test;

import com.scheible.testgapanalysis.common.Files2;

/**
 *
 * @author sj
 */
public class JaCoCoHelperTest {

	@Test
	public void testMethodCoverage() {
		assertThat(getMethodCoverage(Files2.readUtf8(JaCoCoHelperTest.class, "jacoco.xml"))).hasSize(20);
	}

	@Test
	public void testIsNotChildOfSubDirsPredicate() {
		assertThat(getIsNotChildOfSubDirsPredicate(newLinkedHashSet(Paths.get("test", "ignoreA")))
				.test(Paths.get("test", "ignoreA", "file.txt"))).isFalse();

		assertThat(getIsNotChildOfSubDirsPredicate(newLinkedHashSet(Paths.get("test", "ignoreA")))
				.test(Paths.get("test", "some", "file.txt"))).isTrue();
	}
}
