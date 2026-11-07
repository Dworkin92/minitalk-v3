package mt.runtime.primitives;

import java.util.ArrayList;
import java.util.List;

import mt.runtime.MTClass;
import mt.runtime.MTContext;
import mt.runtime.MTObject;
import mt.runtime.MTSymbol;
import mt.runtime.MTSymbolObject;
import mt.runtime.exceptions.MTException;

/**
 * Reflexivite des classes (07/11), installee sur Class :
 * toute classe du monde herite de ces messages. Le
 * recepteur est TOUJOURS une classe.
 *
 * Regle de vocabulaire : le prefixe inst/class dit le
 * COTE, toujours explicite -- la contrepartie de la
 * classe-unique (pas de metaclasse par classe, donc pas
 * d'ambiguite par recepteur).
 */
public final class MTClassPrimitives {

    private MTClassPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    private static MTClass asClass(
            MTObject receiver, String selector) {

        if (receiver instanceof MTClass c) {
            return c;
        }

        throw new MTException(
            receiver + " n'est pas une Class (" + selector + ")");
    }

    private static MTSymbol asSymbol(
            MTObject value, String selector) {

        if (value instanceof MTSymbolObject s) {
            return s.symbol();
        }

        throw new MTException(
            value + " n'est pas un Symbol (" + selector + ")");
    }

    /** La classe mere ; nil a la racine. */
    @Primitive("superclass")
    public static MTObject superclass(
            MTObject receiver, MTObject... arguments) {

        MTClass self = asClass(receiver, "superclass");

        if (self.getSuperclass() == null) {
            return context(receiver).nil();
        }

        return self.getSuperclass();
    }

    /** Noms des methodes d'instance LOCALES, tries
     * (determinisme des tests). */
    @Primitive("instSelectors")
    public static MTObject instSelectors(
            MTObject receiver, MTObject... arguments) {

        return selectorsOf(receiver, "instSelectors",
            asClass(receiver, "instSelectors")
                .getInstSelectors());
    }

    /** Noms des methodes de classe LOCALES, tries. */
    @Primitive("classSelectors")
    public static MTObject classSelectors(
            MTObject receiver, MTObject... arguments) {

        return selectorsOf(receiver, "classSelectors",
            asClass(receiver, "classSelectors")
                .getClassSelectors());
    }

    private static MTObject selectorsOf(
            MTObject receiver, String selector,
            java.util.Set<MTSymbol> names) {

        List<MTSymbol> tries = new ArrayList<>(names);
        tries.sort((a, b) ->
            a.getValue().compareTo(b.getValue()));

        List<MTObject> result = new ArrayList<>();
        for (MTSymbol name : tries) {
            result.add(
                context(receiver).symbolObject(name));
        }

        return context(receiver).newArray(result);
    }

    /** Retire une methode d'instance LOCALE.
     * Integer removeInstMethod: #negated.
     * Rend la classe (convention Smalltalk). */
    @Primitive("removeInstMethod:")
    public static MTObject removeInstMethod(
            MTObject receiver, MTObject... arguments) {

        asClass(receiver, "removeInstMethod:")
            .removeInstanceMethod(
                asSymbol(arguments[0], "removeInstMethod:"));

        return receiver;
    }

    /** Retire une methode de classe LOCALE. */
    @Primitive("removeClassMethod:")
    public static MTObject removeClassMethod(
            MTObject receiver, MTObject... arguments) {

        asClass(receiver, "removeClassMethod:")
            .removeClassMethod(
                asSymbol(arguments[0], "removeClassMethod:"));

        return receiver;
    }
}
