package mt.runtime;

/**
 * Portee-vue des variables d'instance : lookup lit chez
 * l'instance (si DECLAREE par sa classe), assign y
 * ecrit. Le reste remonte la chaine lexicale normale.
 */
public final class MTInstanceScope
        extends MTScope {

    private final MTInstance receiver;

    public MTInstanceScope(
            MTScope parent,
            MTInstance receiver) {

        super(parent);
        this.receiver = receiver;
    }

    @Override
    public MTObject lookup(MTSymbol name) {

        if (receiver.getMTClass()
                .hasInstVar(name)) {

            MTObject value =
                receiver.getProperty(name);

            return (value != null)
                ? value
                : receiver.getMTClass()
                    .getContext().nil(); // ADAPTER : votre accesseur nil
        }

        return super.lookup(name);
    }

    @Override
    public boolean assign(
            MTSymbol name,
            MTObject value) {

        if (receiver.getMTClass()
                .hasInstVar(name)) {

            receiver.setProperty(name, value);
            return true;
        }

        return super.assign(name, value);
    }
}
