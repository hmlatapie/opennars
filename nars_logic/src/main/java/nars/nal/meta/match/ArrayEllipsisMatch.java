package nars.nal.meta.match;

import nars.term.Term;
import nars.term.compound.Compound;
import nars.term.transform.Subst;

import java.util.Collection;
import java.util.Collections;

/**
 * implementation which stores its series of subterms as a Term[]
 */
public class ArrayEllipsisMatch<T extends Term> extends EllipsisMatch<T> {

    public final Term[] term;

    public ArrayEllipsisMatch(Compound y, int from, int to) {
        this(Subst.collect(y, from, to));
    }

    public ArrayEllipsisMatch(Term[] term) {
        this.term = term;
    }

    @Override
    public T build(Term[] subterms, Compound superterm) {
        return (T) superterm.clone(subterms);
    }

    @Override
    public boolean resolve(Subst substitution, Collection<Term> target) {
        Collections.addAll(target, term);
        return true;
    }

    @Override
    public int size() {
        return term.length;
    }
}