package com.scheible.testgapanalysis.common;

/**
 *
 * @author sj
 */
public abstract class EqualsUtils {

	private EqualsUtils() {
	}

	public static boolean equals(double d1, double d2) {
		return Double.compare(d1, d2) == 0;
	}
}
