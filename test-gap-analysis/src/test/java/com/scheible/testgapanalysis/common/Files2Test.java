package com.scheible.testgapanalysis.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

import com.google.common.collect.Sets;

/**
 *
 * @author sj
 */
public class Files2Test {

	@Test
	public void testToRelative() {
		assertThat(Files2.toRelative(Paths.get("test", "it").toFile(),
				Sets.newHashSet(Paths.get("test", "it", "foo.txt").toFile()))).containsOnly("foo.txt");
	}
}
