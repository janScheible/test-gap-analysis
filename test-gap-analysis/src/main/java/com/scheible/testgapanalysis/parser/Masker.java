package com.scheible.testgapanalysis.parser;

import java.util.Scanner;

import com.github.javaparser.Position;
import com.github.javaparser.Range;

/**
 *
 * @author sj
 */
public class Masker {

	/**
	 * Replaces all masked areas of the code in the passed range with a whitespace.
	 */
	static String apply(final String code, final Range range, final Iterable<Range> masks) {
		return apply(code, range, masks, false);
	}

	static String apply(final String code, final Range range, final Iterable<Range> masks, final boolean debug) {
		final StringBuilder result = new StringBuilder();

		try (Scanner scanner = new Scanner(code)) {
			int i = 1;
			while (scanner.hasNextLine()) {
				final StringBuilder line = new StringBuilder(scanner.nextLine());

				if (i >= range.begin.line && i <= range.end.line) {
					mask(line, i, range, debug, masks);

					final String maskedLine = (debug ? line.toString() : line.toString().replaceAll("\\s+", " "))
							.trim();
					if (!maskedLine.isEmpty()) {
						result.append(maskedLine).append('\n');
					}
				}

				i++;
			}
		}

		return result.toString();
	}

	private static void mask(final StringBuilder line, final int i, final Range range, final boolean debug,
			final Iterable<Range> masks) {
		for (int j = 1; j <= line.length(); j++) {
			final Position currentPos = new Position(i, j);

			if (!range.contains(currentPos)) {
				line.setCharAt(j - 1, debug ? '#' : ' ');
				continue;
			}

			for (final Range mask : masks) {
				if (mask.contains(currentPos)) {
					line.setCharAt(j - 1, debug ? '#' : ' ');
					break;
				}
			}
		}
	}
}
