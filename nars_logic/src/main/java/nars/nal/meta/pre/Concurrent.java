package nars.nal.meta.pre;

import nars.Premise;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.nal7.Tense;
import nars.task.Task;

/**
 * Created by me on 8/15/15.
 */
public class Concurrent extends PreCondition {

    public Concurrent() {
        super();
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public final boolean test(RuleMatch m) {
        final Premise premise = m.premise;

        if (!premise.isTaskAndBeliefEvent())
            return false;

        final Task task = premise.getTask();
        final Task belief = premise.getBelief();

        //return task.concurrent(belief, m.premise.duration());
        return Tense.overlaps(task, belief);
    }

}