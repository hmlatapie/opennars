package nars.term.transform;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Op;
import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.nal.nal4.Image;
import nars.term.*;

import java.util.*;
import java.util.function.ToIntFunction;


/* recurses a pair of compound term tree's subterms
across a hierarchy of sequential and permutative fanouts
where valid matches are discovered, backtracked,
and collected until a total solution is found.
the magnitude of a running integer depth metric ("power") serves
as a finite-time AIKR cutoff and its polarity as
returned indicates success value to the callee.  */
public class FindSubst extends Subst {

    public FindSubst(Op type, NAR nar) {
        this(type, nar.memory);
    }

    public FindSubst(Op type, Memory memory) {
        this(type, memory.random);
    }

    public FindSubst(Op type, Random random) {
        this(type, newDefaultMap(), newDefaultMap(), random);
    }

    private static final Map<Term,Term> newDefaultMap() {
        return Global.newHashMap(0);
    }

    public FindSubst(Op type, Map<Term, Term> xy, Map<Term, Term> yx, Random random) {
        super(random, type, xy, yx);
    }


    @Override
    public final Subst clone() {
        FindSubst x = new FindSubst(type,
                Global.newHashMap(xy),
                Global.newHashMap(yx),
                random);
        x.parent = parent;
        x.xyChanged = xyChanged; //necessary?
        x.yxChanged = yxChanged; //necessary?
        x.y = y;
        x.power = power;
        return x;
    }

    @Override
    public String toString() {
        return type + ":" + xy + ',' + yx;
    }



    private final void print(String prefix, Term a, Term b) {
        System.out.print(prefix);
        if (a != null)
            System.out.println(" " + a + " ||| " + b);
        else
            System.out.println();
        System.out.println("     " + this);
    }


    public abstract static class PatternOp extends PreCondition {
        abstract boolean run(Frame ff);

        @Override
        public final boolean test(RuleMatch ruleMatch) {
            return run(ruleMatch.subst);
        }
    }

    public abstract static class MatchOp extends PatternOp {

        /** if match not successful, does not cause the execution to
         * terminate but instead sets the frame's match flag */
        abstract public boolean match(Term f);

        @Override public final boolean run(Frame ff) {
//            if (ff.power < 0) {
//                return false;
//            }
            return match(ff.y);
        }

    }

    public static final class TermEquals extends MatchOp {
        public final Term a;

        public TermEquals(Term a) {
            this.a = a;
        }

        @Override
        public boolean match(Term t) {
            return a.equals(t);
        }

        @Override
        public String toString() {
            return "Term{" +  a +  '}';
        }
    }
    public static final class TermSizeEquals extends MatchOp {
        public final int size;

        public TermSizeEquals(int size) {
            this.size = size;
        }

        @Override
        public boolean match(Term t) {
            return t.size() == size;
        }

        @Override
        public String toString() {
            return "TermSizeEq{" + size + '}';
        }
    }
    public static final class TermVolumeMin extends MatchOp {
        public final int volume;

        public TermVolumeMin(int volume) {
            this.volume = volume;
        }

        @Override
        public boolean match(Term t) {
            return t.volume() >= volume;
        }

        @Override
        public String toString() {
            return "TermVolumeMin{" + volume + '}';
        }
    }

    public static final class TermStructure extends MatchOp {
        public final int bits;

        public TermStructure(Op matchingType, int bits) {
            this.bits = bits & (~matchingType.bit());
        }

        @Override
        public boolean match(Term t) {
            int s = t.structure();
            return (s | bits) == s;
        }

        @Override
        public String toString() {
            return "Structure{" + bits + '}';
        }
    }

    public static final class TermOpEquals extends MatchOp {
        public final Op type;

        public TermOpEquals(Op type) {
            this.type = type;
        }

        @Override
        public boolean match(Term t) {
            return t.op() == type;
        }

        @Override
        public String toString() {
            return "TermOpEq{" + type + '}';
        }
    }

    public static final class MatchImageIndex extends MatchOp {
        public final int index;

        public MatchImageIndex(int index) {
            this.index = index;
        }
        @Override public boolean match(Term t) {
            return ((Image)t).relationIndex == index;
        }

        @Override
        public String toString() {
            return "MatchImageIndex{" + index + '}';
        }
    }

//    public static class MatchTerm implements PatternOp {
//        public final Term term;
//
//        public MatchTerm(Term term) {
//            this.term = term;
//        }
//    }

    public static class MatchTerm extends PatternOp {
        public final Term x;

        public MatchTerm(Term c) {
            this.x = c;
        }

        @Override
        public boolean run(Frame ff) {
            return ff.match(x, ff.y);
        }

        @Override
        public String toString() {
            return "MatchTerm{" +  x +  '}';
        }
    }

    public static class MatchXVar extends PatternOp {
        public final Variable x;

        public MatchXVar(Variable c) {
            this.x = c;
        }

        @Override
        public boolean run(Frame ff) {
            return ff.matchXvar(x, ff.y);
        }

        @Override
        public String toString() {
            return "MatchXVar{" +  x +  '}';
        }
    }

    @Deprecated public final static class MatchCompound extends PatternOp {
        public final Compound x;

        public MatchCompound(Compound c) {
            this.x = c;
        }

        @Override
        public boolean run(Frame ff) {
            return ff.matchCompound(x, ((Compound)ff.y));
        }

        @Override
        public String toString() {
            return "MatchCompound{" +  x +  '}';
        }
    }

    public final static class MatchPermute extends PatternOp {
        public final Compound x;

        public MatchPermute(Compound c) {
            this.x = c;
        }

        @Override
        public boolean run(Frame ff) {
            return ff.matchPermute(x, ((Compound)ff.y));
        }

        @Override
        public String toString() {
            return "MatchPermute{" +  x +  '}';
        }
    }

    /** push in to children */
    public static final PatternOp Subterms = new PatternOp() {

        @Override public String toString() {  return "Subterms";        }

        @Override public boolean run(Frame ff) {
            ff.parent = (Compound)ff.y;
            return true;
        }
    };

    /** pop out to parent */
    public static final PatternOp Superterm = new PatternOp() {

        @Override public String toString() {  return "Superterm"; }

        @Override public boolean run(Frame ff) {
            ff.y = ff.parent;
            ff.parent = null;
            return true;
        }
    };

    public static final class SelectSubterm extends PatternOp {
        public final int index;

        public SelectSubterm(int index) {
            this.index = index;
        }

        @Override
        public boolean run(Frame ff) {
            ff.y = ff.parent.term(index);
            return true;
        }

        @Override
        public String toString() {
            return "SelectSubterm{" + index + '}';
        }
    }


    public static final class MatchSubterm extends PatternOp {
        public final int index;
        private final Term x;

        public MatchSubterm(Term x, int index) {
            this.index = index;
            this.x = x;
        }

        @Override
        public boolean run(Frame ff) {
            Term y = ff.y = ff.parent.term(index);
            return ff.match(x, y);
        }


        @Override
        public String toString() {
            return "MatchSubterm{" + x + "," + index + '}';
        }
    }

    /** match 0th subterm (fast) */
    public static final class MatchTheSubterm extends PatternOp {

        private final Term x;

        public MatchTheSubterm(Term x) {
            this.x = x;
        }

        @Override
        public boolean run(Frame ff) {
            return ff.match(x, ff.y.term(0) );
        }

        @Override
        public String toString() {
            return "MatchTheSubterm{" + x + '}';
        }
    }

    /** represents the "program" that the matcher will execute */
    public static class TermPattern {

        public final PreCondition[] code;
        public final Term term;
        private Op type = null;

        public TermPattern(Op type, Term pattern) {

            this.term = pattern;
            this.type = type;

            List<PreCondition> code = Global.newArrayList();

            //compile the code
            compile(pattern, code);
            //code.add(End);
            code.add(new RuleMatch.Stage(RuleMatch.MatchStage.Post));

            this.code = code.toArray(new PreCondition[code.size()]);
        }

        private void compile(Term t, List<PreCondition> code) {


            boolean constant = false;

            if ((type == Op.VAR_PATTERN && (!Variable.hasPatternVariable(t)))) {
                constant = true;
            }
            if ((type != Op.VAR_PATTERN && !t.hasAny(type))) {
                constant = true;
            }

            if (!constant && (t instanceof Compound)) {
                compileCompound((Compound) t, code);
            }
            else {

                if (constant)
                    code.add(new TermEquals(t));
                else {
                    if (t.op() == type) {
                        code.add(new MatchXVar((Variable)t));
                    }
                    else {
                        //something else
                        code.add(new MatchTerm(t));
                    }
                }
            }


            //throw new RuntimeException("unknown compile behavior for term: " + t);

            //code.add(new MatchIt(v));
        }

        private void compileCompound(Compound t, List<PreCondition> code) {
            Compound<?> c = t;
            int s = c.size();

            code.add(new TermOpEquals(c.op())); //interference with (task,belief) pair term

            //TODO varargs with greaterEqualSize etc
            code.add(new TermSizeEquals(c.size()));

            boolean permute = c.isCommutative() && (s > 1);

            switch (s) {
                case 0:
                    //nothing to match
                    break;

                case 1:
                    code.add(new MatchTheSubterm(c.term(0)));
                    break;

                default:

                    if (c instanceof Image) {
                        code.add(new MatchImageIndex(((Image)c).relationIndex)); //TODO varargs with greaterEqualSize etc
                    }

                    code.add(new TermVolumeMin(c.volume()-1));

                    code.add(new TermStructure(type, c.structure()));

                    if (permute) {
                        code.add(new MatchPermute(c));
                    }
                    else {
                        compileNonCommutative(code, c);
                    }

                break;
            }
        }


        /** heuristic for ordering comparison of subterms; lower is first */
        private ToIntFunction<Term> subtermPrioritizer = (t) -> {

            if (t.op() == type) {
                return 0;
            }
            else if (t instanceof Compound) {
                if (!t.isCommutative()) {
                    return 1 + (1 * t.volume());
                } else {
                    return 1 + (2 * t.volume());
                }
            }
            else {
                return 1; //atomic
            }
        };

        private void compileNonCommutative(List<PreCondition> code, Compound<?> c) {

            final int s = c.size();
            TreeSet<SubtermPosition> ss = new TreeSet();

            for (int i = 0; i < s; i++) {
                Term x = c.term(i);
                ss.add(new SubtermPosition(x, i, subtermPrioritizer));
            }

            code.add( Subterms );

            ss.forEach(sp -> { //iterate sorted
                Term x = sp.term;
                int i = sp.position;

                compile2(x, code, i);
                //compile(type, x, code);
            });

            code.add( Superterm );
        }

        private void compile2(Term x, List<PreCondition> code, int i) {
            //TODO this is a halfway there.
            //in order for this to work, parent terms need to be stored in a stack or something to return to, otherwise they get a nulll and it crashes:

//            code.add(new SelectSubterm(i));
//            compile(x, code);
//
             if (x instanceof Compound) {
//                //compileCompound((Compound)x, code);
//            /*}
//            else {
                 code.add(new MatchSubterm(x, i));
             }
             else {
                 //HACK this should be able to handle atomic subterms without a stack
                 code.add(new SelectSubterm(i));
                 compile(x, code);
             }

        }

        final static class SubtermPosition implements Comparable<SubtermPosition> {

            public final int score;
            public final Term term; //the subterm
            public final int position; //where it is located

            public SubtermPosition(Term term, int pos, ToIntFunction<Term> scorer) {
                this.term = term;
                this.position = pos;
                this.score = scorer.applyAsInt(term);
            }

            @Override
            public int compareTo(SubtermPosition o) {
                if (this == o) return 0;
                int p = Integer.compare(o.score, score); //lower first
                if (p!=0) return p;
                return Integer.compare(position, o.position);
            }

            @Override
            public String toString() {
                return term + " x " + score + " (" + position + ')';
            }
        }


        @Override
        public String toString() {
            return "TermPattern{" + Arrays.toString(code) + '}';
        }
    }


    /** find substitutions, returning the success state. */
    @Override
    public final boolean next(final Term x, final Term y, int startPower) {
        this.power = startPower;

        boolean b = match(x, y);

        //System.out.println(startPower + "\t" + power);

        return b;
    }

    /** find substitutions using a pre-compiled term pattern */
    @Override
    @Deprecated public final boolean next(final TermPattern x, final Term y, int startPower) {

//        return next(x.term, y, startPower);

        this.power = startPower;

        PreCondition[] code = x.code;
        this.y = y;
        for (PreCondition o : code) {
            if (!(o instanceof PatternOp)) continue;
            if (!((PatternOp)o).run(this))
                return false;
        }
        return true;

    }

    /**
     * recurses into the next sublevel of the term
     * @return
     *      if success: a POSITIVE next power value, after having subtracted the cost (>0)
     *      if fail: the NEGATED next power value (<=0)
     **
     * this effectively uses the sign bit of the integer as a success flag while still preserving the magnitude of the decreased power for the next attempt
     */
    public final boolean match(final Term x, final Term y) {

        //if ((power = power - 1 /*costFunction(X, Y)*/) < 0)
          //  return power; //fail due to insufficient power

        //System.out.println("  m: " + x + " " + y + " " + power);


        if (x.equals(y)) {
            /*if (x!=y)
                System.err.println("NOT SHARED: " + x);
            else
                System.err.println("    SHARED: " + x);*/

            return true; //match
        }

        if ((--power) < 0)
            return false;


        final Op type1 = this.type;
        final Op xOp = x.op();
        if (xOp == type1) {
            return matchXvar((Variable)x, y);
        }

        final Op yOp = y.op();
        if (yOp == type1) {
            return matchYvar(x, y);
        }

        if (xOp.isVar()) {
            if (yOp.isVar()) {
                nextVarX((Variable) x, y);
                return true;
            }
        }
        else {
            if ((xOp == yOp) && (x instanceof Compound)) {
                return matchCompound((Compound) x, (Compound) y);
            }
        }

        return false;
    }

    private boolean matchYvar(Term x, Term y) {
        final Term ySubst = yx.get(y);

        if (ySubst != null) {
            return match(x, ySubst);
        }
        else {
            yxPut((Variable) y, x);
            if (y instanceof CommonVariable) {
                xyPut((Variable) y, x);
            }
            return true;
        }
    }

    public boolean matchXvar(Variable x, Term y) {
        final Term xSubst = xy.get(x);

        if (xSubst != null) {
            return match(xSubst, y);
        }
        else {
            nextVarX(x, y);
            return true;
        }
    }

    private static void printComparison(int power, Compound cx, Compound cy) {
        System.out.println(cx.structureString() + " " + cx.volume() + "\t" + cx);
        System.out.println(cy.structureString() + " " + cy.volume() + "\t" + cy);
        System.out.println(!cx.impossibleToMatch(cy) + "|" + !cy.impossibleToMatch(cx) + " ---> " + (power >= 0) + " " + power);
        System.out.println();
    }


    private final void nextVarX(final Variable xVar, final Term y) {
        final Op xOp = xVar.op();

        if (xOp == type) {
            putVarX(xVar, y);
        }
        else {
            final Op yOp = y.op();
            if (yOp == xOp) {
                 putCommon(xVar, (Variable) y);
            }
        }

    }


    /** returns the size of both compounds if matching and valid, or -1 if invalid match */
    @Deprecated private static int matchable(final Compound X, final Compound Y) {
        /** must have same # subterms */
        int xsize = X.size();
        if (xsize != Y.size()) {
            return -1;
        }

        //TODO see if there is a volume or structural constraint that can terminate early here

        /** if they are images, they must have same relationIndex */
        //TODO simplify comparison with Image base class
        if (X instanceof Image) {
            if (((Image) X).relationIndex != ((Image) Y).relationIndex)
                return -1;
        }

        return xsize;
    }

    /**
     * X and Y are of the same operator type and length (arity)
     * X's permutations matched against constant Y
     */
    public final boolean matchCompound(final Compound X, final Compound Y) {

        switch (matchable(X, Y)) {
            case -1:
                return false;
            case 0:
                return true;  //match
            case 1:
                return match(X.term(0), Y.term(0));
            default:  /*if (xLen >= 1) {*/
                if (X.isCommutative()) {
                    //commutative, try permutations
                    return matchPermute(X, Y);
                } else {
                    //non-commutative (must all match), or no permutation necessary (0 or 1 arity)
                    return matchSequence(X.subterms(), Y.subterms());
                }
        }
    }

    /**
     * @param x the compound which is permuted/shuffled
     * @param y what is being compared against
     *
     */
    public final boolean matchPermute(Compound x, Compound y) {
        //DequePool<ShuffledPermutations> pp = this.permutationPool;

        //final int len = x.size();

        //final int minAttempts = len; //heuristic assumption


        final ShuffleTermVector perm = new ShuffleTermVector(random, x);

        final Map<Term, Term> xy = this.xy; //local copy on stack
        final Map<Term, Term> yx = this.yx; //local copy on stack


        //push/save:
        final Map<Term, Term> savedXY = Global.newHashMap(xy);
        final Map<Term, Term> savedYX = Global.newHashMap(yx);
        xyChanged = yxChanged = false;

        boolean matched = false;

        while (perm.hasNext()) {

            perm.next();

            matched = matchSequence(perm, y);

            if (power < 0) {
                return false;
            }

            if (matched /*|| power <= 0*/)
                break;


            //try again; invert negated power back to a positive value for next attempt

            //pop/restore
            if (yxChanged) {
                yxChanged = false;
                restore(savedYX, yx);
            }

            if (xyChanged) {
                xyChanged = false;
                restore(savedXY, xy);
            }

            //ready to continue on next permutation

        }


        //finished
        return matched;

    }


    /**
     * elimination
     */
    private final void putVarX(final Variable x, final Term y) {
        xyPut(x, y);
        if (x instanceof CommonVariable) {
            yxPut(x, y);
        }
    }


    private void putCommon(final Variable x, final Variable y) {
        final Variable commonVar = CommonVariable.make(x, y);
        xyPut(x, commonVar);
        yxPut(y, commonVar);
        //return true;
    }

    private final void yxPut(Variable y, Term x) {
        yxChanged|= (yx.put(y, x)!=x);
    }

    private final void xyPut(Variable x, Term y) {
        xyChanged|= (xy.put(x, y)!=y);
    }


    private static void restore(Map<Term, Term> savedCopy, Map<Term, Term> originToRevert) {
        originToRevert.clear();
        originToRevert.putAll(savedCopy);
    }

    private boolean matchFork(final PatternOp[] code, int ip, Object calleeFrame, final Term Y) {
        return false;
    }

    /**
     * a branch for comparing a particular permutation, called from the main next()
     */


    /**
     * a branch for comparing a particular permutation, called from the main next()
     */
    public boolean matchSequence(final TermContainer X, final TermContainer Y) {

        final int yLen = Y.size();

        boolean phase = false;

        int processed = 0;

        //process non-commutative subterms in phase 1, then phase 2
        do {

            for (int i = 0; i < yLen; i++) {

                Term xSub = X.term(i);

                if (xSub.isCommutative() == phase) {
                    if (!match(xSub, Y.term(i)))
                        return false;
                    processed++;
                }
            }

            phase = !phase;

        } while(processed < yLen);

        //success
        return true;
    }

    @Override
    public final void putXY(Term x, Term y) {
        xy.put(x, y);
    }

    @Override
    public final Term resolve(Term t, Substitution s) {
        //TODO make a half resolve that only does xy?

        Term ret = t.substituted(s, xy);
        if(ret != null) {
            ret = ret.substituted(s, yx);
        }
        return ret;

    }

    @Override public final Map<Term, Term> xy() { return xy; }
    @Override public final Map<Term, Term> yx() { return yx; }

    //    private static class ShuffledPermutationsDequePool extends DequePool<ShuffledPermutations> {
//        public ShuffledPermutationsDequePool() {
//            super(1);
//        }
//
//        @Override public final ShuffledPermutations create() {
//            return new ShuffledPermutations();
//        }
//    }
//
//    private static class MapDequePool extends DequePool<Map<Term,Term>> {
//        public MapDequePool() {
//            super(1);
//        }
//
//        @Override public final Map<Term,Term> create() {
//            return Global.newHashMap();
//        }
//
//        @Override
//        public final void put(Map<Term, Term> i) {
//            i.clear();
//            super.put(i);
//        }
//    }

}


//    private int permute3(final Compound X, final Compound Y, int power) {
//
//        final Term[] ySubterms = Y.term;
//        final Term a = ySubterms[0];
//        final Term b = ySubterms[1];
//        final Term c = ySubterms[2];
//
//        int tries = 6;
//        Term d, e, f;
//
//        int order = random.nextInt(6); //random starting permutation
//
//        do {
//            switch (order) {
//                case 0: d = a; e = b; f = c;     break;
//                case 1: d = a; e = c; f = b;     break;
//                case 2: d = b; e = a; f = c;     break;
//                case 3: d = b; e = c; f = a;     break;
//                case 4: d = c; e = a; f = b;     break;
//                case 5: d = c; e = b; f = a;     break;
//                default:
//                    throw new RuntimeException("invalid permutation");
//            }
//
//            if ((power = matchAll(power, X.term,
//                    new Term[]{ d, e, f}) )  >= 0)
//                return power; //success
//            else {
//                power = -power; //try again; reverse negated power back to a positive value for next attempt
//                order = (order + 1) % 6;
//                tries--;
//            }
//        } while (tries > 0);
//
//        return fail(power); //fail
//    }

//
//    private int permute2(final Compound X, final Compound Y, int power) {
//
//        final Term[] xSubterms = X.term;
//        Term x0 = xSubterms[0];
//        Term x1 = xSubterms[1];
//
//        //50% probabilty of an initial swap
//        if (random.nextBoolean()) {
//            Term t = x0;
//            x0 = x1;
//            x1 = t;
//        }
//
//        //SAVE
//        HashMap<Variable, Term> tmpXY = Maps.newHashMap(xy);
//        HashMap<Variable, Term> tmpYX = Maps.newHashMap(yx);
//
//        final Term[] ySubterms = Y.term;
//
//        //allocate half of the power for the first attempt.
//        //2nd attempt will have at least this much (what remains from the first).
//        int subPower = power/2;
//
//        int remainingSubPower =
//               matchAll2(subPower, x0, x1, ySubterms);
//
//        //subtract expense and add the surplus
//        power -= (subPower - Math.abs(remainingSubPower));
//        if (remainingSubPower >= 0) //success
//            return power;
//
//        //RESTORE
//        xy.clear(); xy.putAll(tmpXY);
//        yx.clear(); yx.putAll(tmpYX);
//
//        power = matchAll2(power, x1, x0, ySubterms);
//        if (power < 0) {
//            //RESTORE
//            xy.clear(); xy.putAll(tmpXY);
//            yx.clear(); yx.putAll(tmpYX);
//        }
//
//        return power;
//    }


//    final protected int matchAll2(int power, final Term x0, final Term x1, final Term[] ySubterms) {
//        if ((power = find(x0, ySubterms[0], power)) < 0)
//            return power;
//        return       find(x1, ySubterms[1], power);
//    }
