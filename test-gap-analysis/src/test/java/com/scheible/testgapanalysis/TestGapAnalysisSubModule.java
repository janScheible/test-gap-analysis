package com.scheible.testgapanalysis;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.Slf4j;
import com.scheible.testgapanalysis.analysis.AnalysisSubModule;
import com.scheible.testgapanalysis.common.CommonSubModule;
import com.scheible.testgapanalysis.git.GitSubModule;
import com.scheible.testgapanalysis.jacoco.JaCoCoSubModule;

/**
 *
 * @author sj
 */
@SubModule(includeSubPackages = false, uses = {AnalysisSubModule.class, JaCoCoSubModule.class, Slf4j.class,
		GitSubModule.class, CommonSubModule.class})
public class TestGapAnalysisSubModule {

}
