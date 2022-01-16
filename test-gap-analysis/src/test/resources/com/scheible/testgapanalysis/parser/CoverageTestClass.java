package com.scheible.testgapanalysis._test;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Class for testing the parser and the mapping to the JaCoCo coverage report (this source file and the
 * generated coverage report have to be kept in sync with their copies in the test resources!).
 * 
 * @author sj
 */
@SuppressWarnings("PMD")
public abstract class CoverageTestClass {

	static class InnerStatic {

	}

	static {
		"".trim();
	}

	{
		"".trim();
		value2 = 47;
	}

	private static final Function<String, Integer> FUNCTION = value -> value.length();

	private final int value;
	private final int value2;

	private CoverageTestClass() {
		super();

		"".trim();
		this.value = 42;
	}

	public CoverageTestClass(String test, CoverageTestClass.InnerStatic inner) {
		"".trim();
		this.value = 43;
	}

	public void doIt() {
		"".trim();
	}

	public void doItLambda() {
		Predicate<String> ss = value -> value.isEmpty();
		ss.test("");
	}

	// comment
	private void execute(Supplier<String> supplier, Consumer<String> consumer) {
		consumer.accept(supplier.get());
	}

	/**
	 * doItRunanble comment
	 */
	public void doItRunanble() {
		"".trim();
		// comment
		new Thread(new Runnable() {
			@Override
			public void run() {
				Function<String, String> ss = value -> value;
				ss.apply(""); // comment at end of line
				/*
				 * comment block
				 */
				"".trim();
			}
		}).start();
		"".trim();
	}

	@Deprecated
	public void doItMultipleLamdaSingleLine() {
		execute(() -> ":-)", /* inline comment */ value -> value.trim());
	}

	public void doItMultipleLamdaSingleLineMultiLine() {
		execute(() -> {
			return ":-)";
		}, /* inline comment */ value -> value.trim());
	}

	public abstract int makeItSo();

	public static String staticMethod() {
		return ":-)";
	}

	public void methodWithTryCatch() {
		try {
			throw new IllegalStateException();
		} catch (IllegalStateException ex) {
		}
	}

	public void methodWithDoLoop() {
		do {
			"".trim();
		} while ("a".equals("b"));
	}
}
