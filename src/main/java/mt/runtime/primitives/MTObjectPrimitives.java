package mt.runtime.primitives;

import mt.runtime.MTContext;
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
