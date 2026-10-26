package mt.runtime.primitives;

import mt.runtime.MTContext;
import mt.runtime.MTObject;

public final class MTSymbolPrimitives {

    private MTSymbolPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    /**
     * Les symboles sont internes (pool) et representes une
     * seule fois par contexte : l'egalite de valeur EST
     * l'identite. Pas de distinction = / == utile ici,
     * comme pour nil.
     */
    @Primitive("=")
    @Primitive("==")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(
            receiver.identityEquals(arguments[0]));
    }

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return receiver.asString();
    }
}
