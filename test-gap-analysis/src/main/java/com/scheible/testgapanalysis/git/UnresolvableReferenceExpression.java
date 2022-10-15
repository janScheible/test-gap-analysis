package com.scheible.testgapanalysis.git;

import java.io.IOException;

/**
 *
 * @author sj
 */
public class UnresolvableReferenceExpression extends IOException {

	private static final long serialVersionUID = 1L;

	public UnresolvableReferenceExpression(String referenceExpression) {
		super(String.format("Can't resolve the reference expression '%s'.", referenceExpression));
	}
}
