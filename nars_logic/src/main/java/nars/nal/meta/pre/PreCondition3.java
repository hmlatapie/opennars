package nars.nal.meta.pre;

import nars.nal.RuleMatch;
import nars.nal.meta.PreCondition;
import nars.term.Term;

/**
 * Created by me on 8/15/15.
 */
abstract public class PreCondition3 extends PreCondition {
    public Term arg1=null, arg2=null, arg3=null;


    /** no arguments should be null */
    public PreCondition3(Term var1, Term var2, Term var3) {
        super();
        this.arg1 = var1;
        this.arg2 = var2;
        this.arg3 = var3;
    }

    @Override
    public boolean test(RuleMatch m) {
        //these should not resolve to null
        Term a = m.apply(arg1);
        if (a == null) return false;
        Term b = m.apply(arg2);
        if (b == null) return false;
        Term c = m.apply(arg3);
        if (c == null) return false;

        return test(m, a, b, c);
    }

    abstract public boolean test(RuleMatch m, Term a, Term b, Term c);

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + arg1 + ',' + arg2 + ',' + arg3 + ']';
    }
}
