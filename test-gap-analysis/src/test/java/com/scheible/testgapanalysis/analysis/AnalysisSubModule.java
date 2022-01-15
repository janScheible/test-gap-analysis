package com.scheible.testgapanalysis.analysis;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.Slf4j;
import com.scheible.testgapanalysis._test.CoverageTestClassSubModule;
import com.scheible.testgapanalysis.git.GitSubModule;
import com.scheible.testgapanalysis.jacoco.JaCoCoSubModule;
import com.scheible.testgapanalysis.jacoco.resolver.JaCoCoResolverSubModule;
import com.scheible.testgapanalysis.parser.ParserSubModule;

/**
 *
 * @author sj
 */
@SubModule(uses = {JaCoCoSubModule.class, JaCoCoResolverSubModule.class, GitSubModule.class, ParserSubModule.class,
		CoverageTestClassSubModule.class, Slf4j.class})
public class AnalysisSubModule {

}
