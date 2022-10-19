package com.scheible.testgapanalysis.parser;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.JavaParser;
import com.scheible.testgapanalysis.ExternalFunctionalities.Slf4j;
import com.scheible.testgapanalysis.common.CommonSubModule;

/**
 *
 * @author sj
 */
@SubModule(uses = {JavaParser.class, CommonSubModule.class, Slf4j.class})
public class ParserSubModule {

}
