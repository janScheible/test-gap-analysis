package com.scheible.testgapanalysis.git;

import com.scheible.pocketsaw.api.SubModule;
import com.scheible.testgapanalysis.ExternalFunctionalities.JGit;
import com.scheible.testgapanalysis.ExternalFunctionalities.Slf4j;
import com.scheible.testgapanalysis.common.CommonSubModule;

/**
 *
 * @author sj
 */
@SubModule(uses = {JGit.class, Slf4j.class, CommonSubModule.class})
public class GitSubModule {

}
