package mt.runtime.primitives;

import mt.runtime.MTContext;
import mt.runtime.MTFloat;
import mt.runtime.MTInteger;
import mt.runtime.MTObject;

public final class MTObjectPrimitives {

    /**
     * Drapeau console : true si un 'print' a laisse
     * la ligne ouverte. Le REPL le consulte pour
     * rendre la main proprement avant son echo.
     */
    public static boolean lineOpen = false;

    private MTObjectPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    @Primitive("identity")
    public static MTObject identity(MTObject receiver, MTObject... arguments) {
        return receiver;
    }

    @Primitive("class")
    public static MTObject mtClass(MTObject receiver, MTObject... args) {
        return receiver.getMTClass();
    }

    @Primitive("isNil")
    public static MTObject isNil(MTObject receiver, MTObject... arguments) {
        return context(receiver).newBoolean(receiver.isNil());
    }

    @Primitive("notNil")
    public static MTObject notNil(MTObject receiver, MTObject... arguments) {
        return context(receiver).newBoolean(!receiver.isNil());
    }

    //
    // Comparaisons
    //

    /**
     * Egalite d'identite : la valeur par defaut de ==
     * pour TOUT objet du monde, heritee par toutes les
     * classes (le lookup remonte la chaine). Les classes
     * n'ont plus a le redefinir -- sauf divergence
     * assumee, il n'y en a aucune aujourd'hui.
     */
    @Primitive("==")
    public static MTObject sameAs(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(
            receiver.identityEquals(arguments[0]));
    }

    /**
     * Egalite par defaut : IDENTITE, comme en
     * Smalltalk-80 (Object>>= retourne ==). Les classes
     * a semantique de valeur (Integer, String, Array,
     * Dictionary, ...) SURCHARGENT avec leur egalite
     * de valeur ; les autres heritent de l'identite.
     */
    @Primitive("=")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(
            receiver.identityEquals(arguments[0]));
    }

    /**
     * Affiche le recepteur sans retour a la ligne.
     * Retourne le recepteur : 'x print print' est
     * legal (et penible). ;)
     */
    @Primitive("print")
    public static MTObject print(
            MTObject receiver,
            MTObject... arguments) {

        System.out.print(receiver.asString());
        lineOpen = true;
        return receiver;
    }

    /**
     * Affiche le recepteur puis passe a la ligne.
     */
    @Primitive("printLn")
    public static MTObject printLn(
            MTObject receiver,
            MTObject... arguments) {

        System.out.println(receiver.asString());
        return receiver;
    }
}
