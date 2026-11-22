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

    /**
     * (22/11) Forme SOURCE re-evaluable, avec le # :
     * #alpha asString. "alpha", mais #alpha printString
     * -> '#alpha'. Doctrine : asString rend le contenu
     * nu, printString rend la forme qui se re-parse
     * (precedent : l'affichage re-evaluable des
     * dictionnaires, 08/11).
     */
    @Primitive("printString")
    public static MTObject printString(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newString(
            receiver.toString());
    }

    @Primitive("asString")
    public static MTObject asString(
            MTObject receiver,
            MTObject... arguments) {

        return receiver.asString();
    }
}
