package com.scheible.testgapanalysis.jacoco;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.DomParser;
import com.scheible.testgapanalysis.ExternalFunctionalities.SaxParser;
import com.scheible.testgapanalysis.common.CommonSubModule;

/**
 *
 * @author sj
 */
@SubModule(includeSubPackages = false, uses = {DomParser.class, SaxParser.class, CommonSubModule.class})
public class JaCoCoSubModule {

}
