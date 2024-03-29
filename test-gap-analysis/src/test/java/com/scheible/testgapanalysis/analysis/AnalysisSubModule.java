package com.scheible.testgapanalysis.analysis;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.Slf4j;
import com.scheible.testgapanalysis.common.CommonSubModule;
import com.scheible.testgapanalysis.git.GitSubModule;
import com.scheible.testgapanalysis.jacoco.JaCoCoSubModule;
import com.scheible.testgapanalysis.jacoco.resolver.JaCoCoResolverSubModule;
import com.scheible.testgapanalysis.parser.ParserSubModule;

/**
 *
 * @author sj
 */
@SubModule(includeSubPackages = false, uses = {JaCoCoSubModule.class, JaCoCoResolverSubModule.class, GitSubModule.class,
		ParserSubModule.class, Slf4j.class, CommonSubModule.class})
public class AnalysisSubModule {

}
