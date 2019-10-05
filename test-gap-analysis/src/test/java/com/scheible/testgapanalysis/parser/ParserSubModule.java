package com.scheible.testgapanalysis.parser;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.JavaParser;
import com.scheible.testgapanalysis.common.CommonSubModule;

/**
 *
 * @author sj
 */
@SubModule(uses = {JavaParser.class, CommonSubModule.class})
public class ParserSubModule {

}
