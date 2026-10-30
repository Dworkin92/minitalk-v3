package mt.runtime.primitives;

import java.util.ArrayList;
import java.util.List;

import mt.runtime.MTArray;
import mt.runtime.MTBoolean;
import mt.runtime.MTContext;
import mt.runtime.MTInteger;
import mt.runtime.MTMethod;
import mt.runtime.MTObject;
import mt.runtime.MTSymbol;
import mt.runtime.exceptions.MTException;

public final class MTArrayPrimitives {

    // Cache : le selecteur de comparaison, interne au runtime.
    private static final MTSymbol EQUALS =
        MTSymbol.intern("=");

    private MTArrayPrimitives() {
    }

    private static MTContext context(MTObject receiver) {
        return receiver.getMTClass().getContext();
    }

    private static MTMethod asBlock(MTObject argument) {
        if (!(argument instanceof MTMethod method)) {
            throw new MTException(
                "Un bloc est attendu en argument");
        }
        return method;
    }

    @Primitive("size")
    public static MTObject size(
            MTObject receiver,
            MTObject... arguments) {

        MTArray self = (MTArray) receiver;

        return context(self).newInteger(
            self.javaValue().size());
    }

    @Primitive("isEmpty")
    public static MTObject isEmpty(
            MTObject receiver,
            MTObject... arguments) {

        MTArray self = (MTArray) receiver;

        return context(self).newBoolean(
            self.javaValue().isEmpty());
    }

    /** Acces 1-based, comme String>>at: */
    @Primitive("at:")
    public static MTObject at(
            MTObject receiver,
            MTObject... arguments) {

        MTArray self = (MTArray) receiver;
        int index = ((MTInteger) arguments[0]).javaValue();

        if (index < 1 || index > self.javaValue().size()) {
            throw new MTException(
                "Index hors bornes : " + self
                + " at: " + index);
        }

        return self.javaValue().get(index - 1);
    }

    /**
     * Egalite de valeur : meme taille, puis chaque
     * element compare par son propre message '='.
     * Les tableaux imbriques sont donc couverts.
     */
    @Primitive("=")
    public static MTObject equals(
            MTObject receiver,
            MTObject... arguments) {

        MTArray self = (MTArray) receiver;

        if (!(arguments[0] instanceof MTArray other)
                || self.javaValue().size()
                    != other.javaValue().size()) {
            return context(receiver).newBoolean(false);
        }

        for (int i = 0; i < self.javaValue().size(); i++) {
            if (!elementEquals(
                    self.javaValue().get(i),
                    other.javaValue().get(i))) {
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

    /**
     * do: parcours le tableau, invoque le bloc avec
     * chaque element. Retourne le recepteur.
     */
    @Primitive("do:")
    public static MTObject do_(MTObject receiver, MTObject... arguments) {

        MTArray self = (MTArray) receiver;
        MTMethod block = asBlock(arguments[0]);

        for (MTObject element : self.javaValue()) {
            block.invoke(null, element);
        }

        return self;
    }

    /** collect: retourne le tableau des valeurs du bloc */
    @Primitive("collect:")
    public static MTObject collect(
            MTObject receiver,
            MTObject... arguments) {

        MTArray self = (MTArray) receiver;
        MTMethod block = asBlock(arguments[0]);

        List<MTObject> results = new ArrayList<>();

        for (MTObject element : self.javaValue()) {
            results.add(block.invoke(null, element));
        }

        return context(receiver).newArray(results);
    }

    /**
     * select: garde les elements pour lesquels le bloc
     * retourne vrai. Conditions strictement booleennes
     * (decision du 24/10) : un bloc qui retourne autre
     * chose qu'un booleen leve une MTException.
     */
    @Primitive("select:")
    public static MTObject select(
            MTObject receiver,
            MTObject... arguments) {

        MTArray self = (MTArray) receiver;
        MTMethod block = asBlock(arguments[0]);

        List<MTObject> kept = new ArrayList<>();

        for (MTObject element : self.javaValue()) {
            MTObject verdict = block.invoke(null, element);

            if (!(verdict instanceof MTBoolean b)) {
                throw new MTException(
                    "select: attend un bloc"
                    + " strictement booleen, pas "
                    + verdict);
            }

            if (b.javaValue()) {
                kept.add(element);
            }
        }

        return context(receiver).newArray(kept);
    }

    /**
     * includes: vrai si un element est egal a
     * l'argument, par son propre message '=' -- la
     * meme egalite profonde que pour l'egalite des
     * tableaux, reutilisee telle quelle.
     */
    @Primitive("includes:")
    public static MTObject includes(
            MTObject receiver,
            MTObject... arguments) {

        MTArray self = (MTArray) receiver;
        MTObject sought = arguments[0];

        for (MTObject element : self.javaValue()) {
            if (elementEquals(element, sought)) {
                return context(receiver).newBoolean(true);
            }
        }

        return context(receiver).newBoolean(false);
    }

    private static boolean elementEquals(
            MTObject left,
            MTObject right) {

        if (left.identityEquals(right)) {
            return true;
        }

        MTObject result = left.send(EQUALS, right);

        return result instanceof MTBoolean b
            && b.javaValue();
    }
}
