package nars.logic.entity.tlink;

import nars.logic.entity.Term;

import java.util.Objects;


public interface TermLinkKey {
    public Term getTarget();

    public String getPrefix();

    default public int termLinkHashCode() {
        return Objects.hash(getPrefix(), getTarget());
    }

    default public boolean termLinkEquals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        TermLinkKey t = (TermLinkKey) obj;
        return getPrefix().equals(t.getPrefix()) && getTarget().equals(t.getTarget());
    }

}