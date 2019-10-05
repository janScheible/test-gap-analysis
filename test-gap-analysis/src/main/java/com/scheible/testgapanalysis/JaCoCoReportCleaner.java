package com.scheible.testgapanalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheible.testgapanalysis.jacoco.JaCoCoHelper;

/**
 *
 * @author sj
 */
public class JaCoCoReportCleaner {

	private static final Logger logger = LoggerFactory.getLogger(JaCoCoReportCleaner.class);

	public static void run() {
		JaCoCoHelper.findJaCoCoFiles().forEach(f -> {
			if (f.delete()) {
				logger.info("Deleted {}.", f);
			} else {
				logger.info("Failed to delete {}!", f);
			}
		});
	}
}
