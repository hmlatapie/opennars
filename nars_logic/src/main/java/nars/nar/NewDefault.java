package nars.nar;

import nars.NAR;
import nars.nal.Deriver;
import nars.nal.PremiseProcessor;
import nars.nal.LogicStage;
import nars.nal.nal8.OpReaction;
import nars.op.app.STMEventInference;
import nars.op.mental.Abbreviation;
import nars.op.mental.Counting;
import nars.op.mental.FullInternalExperience;
import nars.op.mental.InternalExperience;
import nars.process.concept.ConceptFireTaskTerm;
import nars.process.concept.FilterEqualSubtermsAndSetPremiseBelief;

import static nars.op.mental.InternalExperience.InternalExperienceMode.Full;
import static nars.op.mental.InternalExperience.InternalExperienceMode.Minimal;

/**
 * Temporary class which uses the new rule engine for ruletables
 */
public class NewDefault extends Default {


    final Deriver der = Deriver.defaults;

    @Override
    public PremiseProcessor getPremiseProcessor() {
        return nalex(der);
    }

    public PremiseProcessor nalex(ConceptFireTaskTerm ruletable) {

        return new PremiseProcessor(

                new LogicStage /* <ConceptProcess> */ [] {
                        new FilterEqualSubtermsAndSetPremiseBelief(),
                        //new QueryVariableExhaustiveResults(),
                        ruletable
                        //---------------------------------------------
                } ,

                getDerivationFilters()

        );
    }
    public NewDefault() {
        super();
    }

    public NewDefault(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(maxConcepts, conceptsFirePerCycle, termLinksPerCycle);
    }

    /** initialization after NAR is constructed */
    @Override public void init(NAR n) {

        n.the(Deriver.class, der);

        n.setCyclesPerFrame(cyclesPerFrame);

        if (maxNALLevel >= 7) {
            //n.on(PerceptionAccel.class);
            n.on(STMEventInference.class);


            if (maxNALLevel >= 8) {

                for (OpReaction o : defaultOperators)
                    n.on(o);
                for (OpReaction o : exampleOperators)
                    n.on(o);

                //n.on(Anticipate.class);      // expect an event

                if (internalExperience == Minimal) {
                    new InternalExperience(n);
                    new Abbreviation(n);
                } else if (internalExperience == Full) {
                    new FullInternalExperience(n);
                    n.on(new Counting());
                }
            }
        }

        //n.on(new RuntimeNARSettings());

    }
}
