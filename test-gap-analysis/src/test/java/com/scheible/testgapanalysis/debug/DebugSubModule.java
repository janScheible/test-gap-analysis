package com.scheible.testgapanalysis.debug;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.common.CommonSubModule;
import com.scheible.testgapanalysis.jacoco.JaCoCoSubModule;
import com.scheible.testgapanalysis.jacoco.resolver.JaCoCoResolverSubModule;
import com.scheible.testgapanalysis.parser.ParserSubModule;

/**
 *
 * @author sj
 */
@SubModule(uses = {JaCoCoSubModule.class, ParserSubModule.class, JaCoCoResolverSubModule.class, CommonSubModule.class})
public class DebugSubModule {

}
