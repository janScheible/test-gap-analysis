package com.scheible.testgapanalysis.jacoco.resolver;

import static com.scheible.testgapanalysis.jacoco.resolver.AbstractIntegrationTest.CoverageResolutionAssert.assertThat;
import static com.scheible.testgapanalysis.parser.ParsedMethod.MethodType.METHOD;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

/**
 *
 * @author sj
 */
public class CornerCasesIntegrationTest extends AbstractIntegrationTest {

	public static class NestedClassWithMethod extends ArrayList<Object> {

		private static final long serialVersionUID = 1L;

		@Override
		public Iterator<Object> iterator() {
			return new Iterator<Object>() {
				@Override
				public Object next() {
					return new Object();
				}

				@Override
				public boolean hasNext() {
					return false;
				}
			};
		}
	}

	@Test
	public void testNestedClassWithMethod() throws Exception {
		assertThat(resolve(NestedClassWithMethod.class, ArrayList.class, al -> {
			al.iterator(); // execution of that code will trigger loading and instrumentation of the anonymous class
		}, METHOD)).isUnambiguouslyResolved();
	}
}
