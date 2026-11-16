package mt.runtime;

/**
 * Portee-vue des variables de classe : lookup lit
 * dans classVars (si DECLAREE), assign y ecrit.
 */
public final class MTClassVarScope
        extends MTScope {

    private final MTClass target;

    public MTClassVarScope(
            MTScope parent,
            MTClass target) {

        super(parent);
        this.target = target;
    }

    @Override
    public MTObject lookup(MTSymbol name) {
        if (target.hasClassVar(name)) {
            MTObject value =
                target.classVarAt(name);

            return (value != null)
                ? value
                : target.getContext().nil();
        }

        return super.lookup(name);
    }

    @Override
    public boolean assign(
            MTSymbol name,
            MTObject value) {

        if (target.hasClassVar(name)) {
            target.putClassVar(name, value);
            return true;
        }

        return super.assign(name, value);
    }
}
