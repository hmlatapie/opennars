package nars.nar;

import nars.nal.LogicPolicy;
import nars.nal.LogicRule;
import nars.nal.NALExecuter;
import nars.process.concept.*;
import nars.task.filter.DerivationFilter;
import nars.task.filter.FilterBelowConfidence;
import nars.task.filter.FilterDuplicateExistingBelief;

/**
 * Temporary class which uses the new rule engine for ruletables
 */
public class NewDefault extends Default {

    @Override
    public LogicPolicy getLogicPolicy() {
        return nalex(NALExecuter.defaults);
    }

    public static LogicPolicy nalex(ConceptFireTaskTerm ruletable) {

        return new LogicPolicy(

                new LogicRule /* <ConceptProcess> */ [] {
                        new FilterEqualSubtermsInRespectToImageAndProduct(),
                        ruletable
                        //---------------------------------------------
                } ,

                new DerivationFilter[] {
                        new FilterBelowConfidence(),
                        new FilterDuplicateExistingBelief(),
                }

        );
    }
}
