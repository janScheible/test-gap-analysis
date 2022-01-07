package com.scheible.testgapanalysis.jacoco.resolver;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.common.CommonSubModule;
import com.scheible.testgapanalysis.jacoco.JaCoCoSubModule;
import com.scheible.testgapanalysis.parser.ParserSubModule;

/**
 *
 * @author sj
 */
@SubModule(uses = {JaCoCoSubModule.class, ParserSubModule.class, CommonSubModule.class})
public class JaCoCoResolverSubModule {

}
