package mt.runtime.primitives;

import mt.runtime.MTContext;
import mt.runtime.MTDictionary;
import mt.runtime.MTMethod;
import mt.runtime.MTObject;
import mt.runtime.MTSymbol;
import mt.runtime.exceptions.MTException;

public final class MTDictionaryPrimitives {

    private MTDictionaryPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    /** Cast durci (patron du 02/11), format uniforme */
    private static MTMethod asBlock(
            MTObject argument,
            String selector) {

        if (!(argument instanceof MTMethod method)) {
            throw new MTException(
                argument + " n'est pas un Block ("
                + selector + ")");
        }

        return method;
    }

    @Primitive("size")
    public static MTObject size(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newInteger(
            ((MTDictionary) receiver).size());
    }

    @Primitive("isEmpty")
    public static MTObject isEmpty(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(
            ((MTDictionary) receiver).size() == 0);
    }

    /**
     * Cle absente : MTException explicite. at:ifAbsent:
     * viendra plus tard pour la derogation propre.
     */
    @Primitive("at:")
    public static MTObject at(
            MTObject receiver,
            MTObject... arguments) {

        MTDictionary self = (MTDictionary) receiver;
        MTObject value = self.get(arguments[0]);

        if (value == null) {
            throw new MTException(
                "Cle absente : " + arguments[0]);
        }

        return value;
    }

    /** Retourne la valeur inseree (convention Smalltalk) */
    @Primitive("at:put:")
    public static MTObject atPut(
            MTObject receiver,
            MTObject... arguments) {

        MTDictionary self = (MTDictionary) receiver;
        self.put(arguments[0], arguments[1]);

        return arguments[1];
    }

    @Primitive("includesKey:")
    public static MTObject includesKey(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newBoolean(
            ((MTDictionary) receiver)
                .get(arguments[0]) != null);
    }

    /** Ordre d'insertion */
    @Primitive("keys")
    public static MTObject keys(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newArray(
            ((MTDictionary) receiver).keys());
    }

    /** Ordre d'insertion, en accord avec keys */
    @Primitive("values")
    public static MTObject values(
            MTObject receiver,
            MTObject... arguments) {

        return context(receiver).newArray(
            ((MTDictionary) receiver).values());
    }

    /**
     * do: parcourt les VALEURS (convention Smalltalk :
     * un dictionnaire est une collection de valeurs).
     * Retourne le recepteur.
     */
    @Primitive("do:")
    public static MTObject do_(
            MTObject receiver,
            MTObject... arguments) {

        MTDictionary self = (MTDictionary) receiver;
        MTMethod block = asBlock(arguments[0], "do:");

        for (MTObject value : self.values()) {
            block.invoke(null, value);
        }

        return self;
    }

    /**
     * removeKey: retourne la valeur retiree (convention
     * Smalltalk). Absente : MTException -- premier
     * consommateur reel de la reflexivite (05/11).
     */
    @Primitive("removeKey:")
    public static MTObject removeKey(
            MTObject receiver,
            MTObject... arguments) {

        MTDictionary self = (MTDictionary) receiver;
        MTObject value = self.removeKey(arguments[0]);

        if (value == null) {
            throw new MTException(
                "Cle absente : " + arguments[0]);
        }

        return value;
    }

        /**
     * Egalite de valeur : meme taille, puis chaque cle
     * et sa valeur comparees par la meme regle que les
     * cles de la table (valueEquals).
     */
    @Primitive("=")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        MTDictionary self = (MTDictionary) receiver;
        MTObject other = arguments[0];

        if (!(other instanceof MTDictionary that)
                || self.size() != that.size()) {
            return context(receiver).newBoolean(false);
        }

        for (MTObject key : self.keys()) {

            MTObject value = self.get(key);
            MTObject otherValue = that.get(key);

            if (otherValue == null
                    || !MTDictionary.valueEquals(
                        value, otherValue)) {
                return context(receiver).newBoolean(false);
            }
        }

        return context(receiver).newBoolean(true);
    }

    @Primitive("==")
    public static MTObject sameAs(
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
