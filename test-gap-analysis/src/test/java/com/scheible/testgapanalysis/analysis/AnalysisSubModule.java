package com.scheible.testgapanalysis.analysis;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.Slf4j;
import com.scheible.testgapanalysis.git.GitSubModule;
import com.scheible.testgapanalysis.jacoco.JaCoCoSubModule;
import com.scheible.testgapanalysis.parser.ParserSubModule;

/**
 *
 * @author sj
 */
@SubModule(uses = {JaCoCoSubModule.class, GitSubModule.class, ParserSubModule.class, Slf4j.class})
public class AnalysisSubModule {

}
