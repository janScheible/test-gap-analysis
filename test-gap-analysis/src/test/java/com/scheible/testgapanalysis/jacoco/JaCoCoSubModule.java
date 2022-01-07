package com.scheible.testgapanalysis.jacoco;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.DomParser;
import com.scheible.testgapanalysis.ExternalFunctionalities.SaxParser;

/**
 *
 * @author sj
 */
@SubModule(includeSubPackages = false, uses = {DomParser.class, SaxParser.class})
public class JaCoCoSubModule {

}
