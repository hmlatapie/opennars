package nars.nar.experimental;

import nars.Global;
import nars.bag.Bag;
import nars.bag.impl.CurveBag;
import nars.budget.ItemAccumulator;
import nars.budget.ItemComparator;
import nars.concept.Concept;
import nars.cycle.DefaultCycle;
import nars.nar.NewDefault;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.task.filter.DerivationFilter;
import nars.task.filter.FilterBelowConfidence;
import nars.task.filter.FilterDuplicateExistingBelief;
import nars.task.filter.LimitDerivationPriority;
import nars.term.Term;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


/**
 * Created by me on 7/21/15.
 */
public class Equalized extends NewDefault {



    public Equalized(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(maxConcepts, conceptsFirePerCycle, termLinksPerCycle);
    }




    public static class EqualizedCycle extends DefaultCycle {

        /** stores sorted tasks temporarily */
        private final List<Task> temporary = Global.newArrayList();

        public EqualizedCycle(ItemAccumulator taskAccumulator, Bag<Term, Concept> concepts, AtomicInteger conceptsFiredPerCycle) {
            super(taskAccumulator, concepts, null, null, null, conceptsFiredPerCycle);
        }

        /**
         * An atomic working cycle of the system:
         *  0) optionally process inputs
         *  1) optionally process new task(s)
         *  2) optionally process novel task(s)
         *  2) optionally fire a concept
         **/
        @Override
        public void cycle() {

            int conceptsToFire = conceptsFiredPerCycle.get();

            concepts.forgetNext(
                    memory.param.conceptForgetDurations,
                    Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                    memory);

            //inputs
            if (memory.isInputting()) {

                //input all available percepts
                Task t;
                while ((t = percepts.get())!=null) {
                    if (t.isCommand())
                        memory.input(t);
                    else
                        newTasks.add(t);
                }
            }

            queueNewTasks();



            //new tasks
            float maxBusyness = conceptsFiredPerCycle.get(); //interpret concepts fired per cycle as business limit
            int newTasksToFire = newTasks.size();

            Iterator<Task> ii = newTasks.iterateHighestFirst(temporary);

            float priFactor = 1f / maxBusyness; //divide the procesesd priority by the expected busyness of this cycle to approximate 1.0 total

            float b = 0;
            for (int n = newTasksToFire;  ii.hasNext() && n > 0; n--) {
                Task next = ii.next();
                if (next == null) break;

                newTasks.items.removeKey(next); //remove from new items

                float nextPri = next.getPriority();

                TaskProcess tp = TaskProcess.get(memory, next, priFactor);
                if (tp!=null) {
                    tp.run();
                    b += next.getPriority();
                }

                if (b > maxBusyness)
                    break;
            }
            temporary.clear();



            //1 concept if (memory.newTasks.isEmpty())*/
            float conceptForgetDurations = memory.param.conceptForgetDurations.floatValue();
            ConceptProcess.forEachPremise(memory,
                    () -> nextConceptToProcess(conceptForgetDurations),
                    conceptsToFire, f -> f.run()
            );


            int added = commitNewTasks();

            //System.out.print("newTasks=" + newTasksToFire + " + " + added + "  ");

            //System.out.print("concepts=" + conceptsToFire + "  ");

            memory.runNextTasks();

            final int maxNewTasks = conceptsToFire * memory.duration();
            if (newTasks.size() > maxNewTasks) {
                int removed = newTasks.limit(maxNewTasks, new Consumer<Task>() {
                    @Override public void accept(Task task) {
                        memory.removed(task, "Ignored");
                    }
                }, temporary);

                //System.out.print("discarded=" + removed + "  ");
            }

            //System.out.println();


        }



    }

//    @Override
//    public BloomFilterNovelPremiseGenerator newPremiseGenerator() {
//        int novelCycles = duration.get();
//        return new BloomFilterNovelPremiseGenerator(termLinkMaxMatched, novelCycles /* cycle to clear after */,
//                novelCycles * conceptTaskTermProcessPerCycle.get(),
//                0.01f /* false positive probability */ );
//    }


    @Override
    protected DerivationFilter[] getDerivationFilters() {
        return new DerivationFilter[]{
                new FilterBelowConfidence(0.01),
                new FilterDuplicateExistingBelief(),
                new LimitDerivationPriority()
                //param.getDefaultDerivationFilters().add(new BeRational());
        };
    }

    @Override
    public CycleProcess newCycleProcess() {
        return new EqualizedCycle(
                new ItemAccumulator(new ItemComparator.Plus()),
                newConceptBag(),
                conceptsFiredPerCycle
        );
    }

    public Bag<Term, Concept> newConceptBag() {
        CurveBag<Term, Concept> b = new CurveBag(rng, getActiveConcepts());
        b.mergePlus();
        return b;
    }
}
