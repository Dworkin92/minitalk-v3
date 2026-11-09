package mt.runtime.primitives;

import java.util.ArrayList;
import java.util.List;

import mt.runtime.MTArray;
import mt.runtime.MTClass;
import mt.runtime.MTContext;
import mt.runtime.MTInstance;
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

    /**
     * Object subclass: #Person (08/11).
     * Cree la classe fille, l'enregistre, cable sa
     * classe (regle du 07/11). Rend la classe.
     * Les variables et les acces viennent APRES, par
     * le flux << -- la naissance ne fait que nommer.
     */
    @Primitive("subclass:")
    public static MTObject subclass(
            MTObject receiver, MTObject... arguments) {

        MTClass parent = asClass(receiver, "subclass:");
        MTContext world = context(receiver);

        MTSymbol name = asSymbol(arguments[0], "subclass:");

        if (world.lookupClass(name) != null) {
            throw new MTException(
                "Classe existante : " + name);
        }

        MTClass created = new MTClass(
            world, name.getValue(), parent);

        created.setMTClass(world.getClassClass());
        world.registerClass(created);

        return created;
    }

    /**
     * Person << instVars: #(#name #age) -- declare les
     * variables d'instance (symboles EXPLICITES,
     * coherence 26/10). Le flux est ordonne : on
     * declare AVANT de generer les acces.
     */
    @Primitive("instVars:")
    public static MTObject instVars(
            MTObject receiver, MTObject... arguments) {

        MTClass self = asClass(receiver, "instVars:");

        for (MTObject each : asSymbols(
                arguments[0], "instVars:").javaValue()) {

            self.addInstVar(asSymbol(each, "instVars:"));
        }

        return receiver;
    }

    @Primitive("classVars:")
    public static MTObject classVars(
            MTObject receiver, MTObject... arguments) {

        MTClass self = asClass(receiver, "classVars:");

        for (MTObject each : asSymbols(
                arguments[0], "classVars:").javaValue()) {

            MTSymbol var = asSymbol(each, "classVars:");

            if (!self.hasClassVar(var)) {
                self.putClassVar(var, context(receiver).nil());
            }
        }

        return receiver;
    }
        
    /**
     * accessors: -- getter ET setter par variable
     * declaree. Le DERNIER QUI CAUSE A RAISON : si une
     * methode existe deja, la generation l'ecrase
     * (addInstanceMethod, meme loi que le flux).
     */
    @Primitive("accessors:")
    public static MTObject accessors(
            MTObject receiver, MTObject... arguments) {

        MTClass self = asClass(receiver, "accessors:");

        for (MTObject each : asSymbols(
                arguments[0], "accessors:").javaValue()) {

            MTSymbol var = declareOrRefuse(
                self, each, "accessors:");

            installGetter(self, var);
            installSetter(self, var);
        }

        return receiver;
    }

    /** getters: -- le getter seul. */
    @Primitive("getters:")
    public static MTObject getters(
            MTObject receiver, MTObject... arguments) {

        MTClass self = asClass(receiver, "getters:");

        for (MTObject each : asSymbols(
                arguments[0], "getters:").javaValue()) {

            installGetter(self,
                declareOrRefuse(self, each, "getters:"));
        }

        return receiver;
    }

    /** setters: -- le setter seul. */
    @Primitive("setters:")
    public static MTObject setters(
            MTObject receiver, MTObject... arguments) {

        MTClass self = asClass(receiver, "setters:");

        for (MTObject each : asSymbols(
                arguments[0], "setters:").javaValue()) {

            installSetter(self,
                declareOrRefuse(self, each, "setters:"));
        }

        return receiver;
    }

    /**
     * Person new -- instance vide : les variables
     * declarees se lisent nil tant que rien n'a ecrit.
     */
    @Primitive("new")
    public static MTObject newInstance(
            MTObject receiver, MTObject... arguments) {

        return new MTInstance(asClass(receiver, "new"));
    }

    //
    // Aides de generation
    //

    /** La garde : on ne genere que le DECLARE. */
    private static MTSymbol declareOrRefuse(
            MTClass owner, MTObject value, String selector) {

        MTSymbol var = asSymbol(value, selector);

        if (!owner.hasInstVar(var)) {
            throw new MTException(
                "variable inconnue : "
                + owner.getName() + " >> " + var);
        }

        return var;
    }

    /** Le getter unaire : rend la valeur, nil si
     * jamais assignee. Rend le receiver (convention
     * du flux). */
    private static void installGetter(
            MTClass owner, MTSymbol varName) {

        owner.addInstanceMethod(varName,
            (receiver, arguments) -> {
                MTObject value =
                    ((MTInstance) receiver)
                        .getProperty(varName);

                return value == null
                    ? owner.getContext().nil() : value;
            });
    }

    /** Le setter : rend la VALEUR AFFECTEE
     * (valeur de l'affectation). */
    private static void installSetter(
            MTClass owner, MTSymbol varName) {

        owner.addInstanceMethod(
            MTSymbol.intern(varName.getValue() + ":"),
            (receiver, arguments) -> {
                ((MTInstance) receiver)
                    .setProperty(varName, arguments[0]);

                return arguments[0];
            });
    }

    /** L'argument des messages de declaration :
     * un tableau de symboles explicites. */
    private static MTArray asSymbols(
            MTObject value, String selector) {

        if (value instanceof MTArray array) {
            return array;
        }

        throw new MTException(
            value + " n'est pas un Array (" + selector + ")");
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
